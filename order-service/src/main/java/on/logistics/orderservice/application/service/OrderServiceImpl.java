package on.logistics.orderservice.application.service;

import static on.logistics.orderservice.exception.OrderException.OutOfStockProductOrderException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import on.logistics.orderservice.application.service.dtos.cancel.CancelOrderRequestDto;
import on.logistics.orderservice.application.service.dtos.cancel.CancelOrderResponseDto;
import on.logistics.orderservice.application.service.dtos.create.CreateOrderRequestDto;
import on.logistics.orderservice.application.service.dtos.create.CreateOrderRequestDto.OrdersByVendor;
import on.logistics.orderservice.application.service.dtos.create.CreateOrderRequestDto.OrdersByVendor.OrderedProduct;
import on.logistics.orderservice.application.service.dtos.create.CreateOrderResponseDto;
import on.logistics.orderservice.application.service.dtos.get.all.SearchOrderPageRequestDto;
import on.logistics.orderservice.application.service.dtos.get.all.SearchOrderPageResponseDto;
import on.logistics.orderservice.application.service.dtos.get.detail.GetOrderDetailRequestDto;
import on.logistics.orderservice.application.service.dtos.get.detail.GetOrderDetailResponseDto;
import on.logistics.orderservice.application.service.dtos.returns.accept.ReturnOrderRequestDto;
import on.logistics.orderservice.application.service.dtos.returns.accept.ReturnOrderResponseDto;
import on.logistics.orderservice.application.service.dtos.returns.denied.ReturnRequestDeniedRequestDto;
import on.logistics.orderservice.application.service.dtos.returns.denied.ReturnRequestDeniedResponseDto;
import on.logistics.orderservice.application.service.dtos.returns.request.ReturnRequestRequestDto;
import on.logistics.orderservice.application.service.dtos.returns.request.ReturnRequestResponseDto;
import on.logistics.orderservice.application.service.dtos.update.UpdateOrderRequestDto;
import on.logistics.orderservice.application.service.dtos.update.UpdateOrderResponseDto;
import on.logistics.orderservice.domain.entity.Order;
import on.logistics.orderservice.domain.entity.OrderProduct;
import on.logistics.orderservice.domain.entity.Orderer;
import on.logistics.orderservice.domain.entity.Vendor;
import on.logistics.orderservice.domain.entity.VendorOrder;
import on.logistics.orderservice.domain.entity.dtos.CreateOrderDto;
import on.logistics.orderservice.domain.entity.dtos.CreateOrderProductDto;
import on.logistics.orderservice.domain.entity.dtos.CreateOrdererDto;
import on.logistics.orderservice.domain.entity.dtos.CreateVendorDto;
import on.logistics.orderservice.domain.entity.dtos.CreateVendorOrderDto;
import on.logistics.orderservice.domain.enums.OrderStatus;
import on.logistics.orderservice.domain.repository.OrderRepository;
import on.logistics.orderservice.domain.repository.dtos.SearchOrderPageDto;
import on.logistics.orderservice.exception.OrderException.OrderAccessDeniedException;
import on.logistics.orderservice.exception.OrderException.OrderNotFoundException;
import on.logistics.orderservice.exception.OrderException.OrderProductNotFoundException;
import on.logistics.orderservice.exception.OrderException.VendorOrderNotFoundException;
import on.logistics.orderservice.global.application.dtos.PageDto;
import on.logistics.orderservice.global.enums.AuthRole;
import on.logistics.orderservice.infrastructure.clients.ai.dtos.GenerateShippingDeadlineRequestDto;
import on.logistics.orderservice.infrastructure.clients.ai.feign.dtos.GenerateShippingDeadlineResponse;
import on.logistics.orderservice.infrastructure.clients.company.dtos.GetCompanyResponseDto;
import on.logistics.orderservice.infrastructure.clients.delivery.dtos.DeliveryRequestDto;
import on.logistics.orderservice.infrastructure.clients.exception.ExternalApiException;
import on.logistics.orderservice.infrastructure.clients.exception.ExternalApiException.ExternalApiBadRequestException;
import on.logistics.orderservice.infrastructure.clients.hub.dtos.GetHubByIdResponseDto;
import on.logistics.orderservice.infrastructure.clients.hub.dtos.GetHubManagerIdResponse;
import on.logistics.orderservice.infrastructure.clients.hub.dtos.ValidateHubManagerResponseDto;
import on.logistics.orderservice.infrastructure.clients.product.dtos.DecreaseProductStockRequestDto;
import on.logistics.orderservice.infrastructure.clients.product.dtos.RollbackDecreaseProductStockRequestDto;
import on.logistics.orderservice.infrastructure.clients.slack.dtos.SendMessageRequestDto;
import on.logistics.orderservice.infrastructure.clients.user.dtos.FindUserSlackEmailByUserIdResponse;
import on.logistics.orderservice.presentation.dtos.delete.DeleteOrderRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final ProductService productService;
    private final DeliveryService deliveryService;
    private final AIService aiService;
    private final HubService hubService;
    private final CompanyService companyService;
    private final SlackService slackService;
    private final UserService userService;

    @Transactional
    @Override
    public CreateOrderResponseDto createOrder(final CreateOrderRequestDto requestDto) {
        log.info("주문 생성 요청: {}", requestDto);

        var createOrderDto = CreateOrderDto.from(requestDto);
        Order createdOrder = Order.create(createOrderDto);

        Orderer orderer = createOrderer(createdOrder, requestDto);
        createdOrder.addOrdererDependency(orderer);

        List<VendorOrder> vendorOrders = createVendorOrders(
            createdOrder, requestDto.ordersByVendor());
        createdOrder.addVendorOrdersDependencies(vendorOrders);

        log.info("생성된 주문 저장: {}", createdOrder);
        Order savedOrder = orderRepository.save(createdOrder);

        vendorOrders.forEach(this::tryRequestDelivery);
        vendorOrders.forEach(this::trySendMessageToHubManager);

        return CreateOrderResponseDto.from(savedOrder);
    }

    private Orderer createOrderer(
        final Order createdOrder,
        final CreateOrderRequestDto requestDto
    ) {
        log.info("주문자 엔티티 생성");

        GetHubByIdResponseDto hubByCompanyId = getGetHubByIdResponseDto(requestDto.OrdererId());

        CreateOrdererDto createOrdererDto = CreateOrdererDto.of(
            requestDto, createdOrder, hubByCompanyId);
        return Orderer.create(createOrdererDto);
    }

    private GetHubByIdResponseDto getGetHubByIdResponseDto(final UUID companyId) {
        GetCompanyResponseDto company = companyService.getCompanyById(companyId);
        return hubService.getHubById(company.managedHubId());
    }

    private List<VendorOrder> createVendorOrders(
        final Order createdOrder,
        final List<OrdersByVendor> ordersByVendors
    ) {
        log.info("판매자 주문 목록 생성");

        List<VendorOrder> vendorOrders = new ArrayList<>();
        for (OrdersByVendor ordersByVendor : ordersByVendors) {
            VendorOrder vendorOrder = createVendorOrder(createdOrder, ordersByVendor);
            vendorOrders.add(vendorOrder);
        }

        return vendorOrders;
    }

    private VendorOrder createVendorOrder(
        final Order createdOrder,
        final OrdersByVendor ordersByVendor
    ) {
        log.info("판매자별 주문 엔티티 생성");

        var createVendorOrderDto = CreateVendorOrderDto.of(createdOrder, ordersByVendor);
        VendorOrder createdVendorOrder = VendorOrder.create(createVendorOrderDto);

        Vendor vendor = createVendor(createdVendorOrder, ordersByVendor);
        List<OrderProduct> orderProducts = createOrderProducts(createdVendorOrder, ordersByVendor);
        createdVendorOrder.addDependencies(vendor, orderProducts);

        var generateShippingDeadlineResponse = tryGenerateShippingDeadline(createdVendorOrder);
        createdVendorOrder.updateShippingDeadline(
            generateShippingDeadlineResponse.shippingDeadline());

        return createdVendorOrder;
    }

    private Vendor createVendor(
        final VendorOrder createdVendorOrder,
        final OrdersByVendor ordersByVendor
    ) {
        log.info("판매자 엔티티 생성");

        log.warn("공급 업체마다 2번의 API 호출이 발생합니다. 이는 N+1 문제를 발생시킬 수 있습니다.");
        GetHubByIdResponseDto hubByCompanyId = getGetHubByIdResponseDto(ordersByVendor.vendorId());

        var createVendorDto = CreateVendorDto.of(
            createdVendorOrder, ordersByVendor, hubByCompanyId);
        return Vendor.create(createVendorDto);
    }

    private List<OrderProduct> createOrderProducts(
        final VendorOrder createdVendorOrder,
        final OrdersByVendor ordersByVendor
    ) {
        log.info("주문 상품 목록 생성");

        List<OrderProduct> orderProducts = new ArrayList<>();
        List<OrderProduct> reducedProductStocks = new ArrayList<>();
        for (OrderedProduct orderedProduct : ordersByVendor.orderItems()) {
            OrderProduct orderProduct = createOrderProduct(
                createdVendorOrder, orderedProduct, reducedProductStocks);
            orderProducts.add(orderProduct);
        }
        return orderProducts;
    }

    private OrderProduct createOrderProduct(
        final VendorOrder vendorOrder,
        final OrderedProduct orderedProduct,
        final List<OrderProduct> reducedProductStocks
    ) {
        log.info("주문 상품 엔티티 생성");

        var createOrderProductDto = CreateOrderProductDto.of(vendorOrder, orderedProduct);
        OrderProduct createdOrderProduct = OrderProduct.create(createOrderProductDto);

        tryDecreaseProductStock(createdOrderProduct, reducedProductStocks);

        return createdOrderProduct;
    }

    private GenerateShippingDeadlineResponse tryGenerateShippingDeadline(
        final VendorOrder createdVendorOrder
    ) {
        log.info("배송 예상일 생성 요청");
        var requestDto = GenerateShippingDeadlineRequestDto.from(createdVendorOrder);
        try {
            return aiService.generateShippingDeadline(requestDto);
        } catch (ExternalApiException e) {
            log.warn("배송 예상일 생성 중 오류 발생: {}", e.getMessage());
            throw e;
        }
    }

    private void tryDecreaseProductStock(
        final OrderProduct orderProduct,
        final List<OrderProduct> reducedProductStocks
    ) {
        try {
            decreaseProductStock(orderProduct);
            reducedProductStocks.add(orderProduct);
        } catch (ExternalApiBadRequestException e) {
            log.warn("주문 상품 재고 감소 요청 데이터 오류: {}", e.getMessage());
            throw new OutOfStockProductOrderException();
        } catch (ExternalApiException e) {
            log.warn("주문 상품 재고 감소 중 오류 발생: {}", e.getMessage());
            reducedProductStocks.forEach(this::rollbackDecreaseProductStock);
            throw e;
        }
    }

    private void decreaseProductStock(final OrderProduct orderProduct) {
        log.info("주문할 상품 재고 감소");
        var requestDto = DecreaseProductStockRequestDto.from(orderProduct);
        productService.decreaseProductStock(requestDto);
    }

    private void tryRequestDelivery(final VendorOrder vendorOrder) {
        try {
            requestDelivery(vendorOrder);
        } catch (ExternalApiException e) {
            log.warn("배송 요청 중 오류 발생: {}", e.getMessage());
            rollbackDeliveryRequests(vendorOrder);
            throw e;
        }
    }

    private void rollbackDecreaseProductStock(final OrderProduct orderProduct) {
        log.info("주문할 상품 재고 감소 롤백");
        var requestDto = RollbackDecreaseProductStockRequestDto.from(orderProduct);
        productService.rollbackDecreaseProductStock(requestDto);
    }

    private void requestDelivery(final VendorOrder vendorOrder) {
        log.info("배송 요청");
        vendorOrder.ship();
        var requestDto = DeliveryRequestDto.from(vendorOrder);
        deliveryService.deliveryRequest(requestDto);
    }

    private void trySendMessageToHubManager(final VendorOrder vendorOrder) {
        try {
            sendMessageToHubManager(vendorOrder);
        } catch (ExternalApiException e) {
            log.warn("허브 관리자에게 메시지 전송 중 오류 발생: {}", e.getMessage());
            rollbackDeliveryRequests(vendorOrder);
            throw e;
        }
    }

    private void rollbackDeliveryRequests(VendorOrder vendorOrder) {
        log.info("배송 요청 롤백");
        log.warn("모든 상품에 대해서 재고 감소 롤백 요청이 발생하여 N+1 문제가 발생할 수 있습니다.");
        vendorOrder.getOrderProducts().forEach(this::rollbackDecreaseProductStock);
    }

    private void sendMessageToHubManager(final VendorOrder vendorOrder) {
        log.info("허브 관리자에게 메시지 전송");
        GetHubManagerIdResponse hubManagerIdResponse = hubService.getHubManagerId(
            vendorOrder.getOrder().getOrderer().getOrdererHubId());
        FindUserSlackEmailByUserIdResponse hubManager =
            userService.findUserSlackEmailByUserId(hubManagerIdResponse.hubManagerId());
        FindUserSlackEmailByUserIdResponse user =
            userService.findUserSlackEmailByUserId(vendorOrder.getOrder().getOrderer().getUserId());
        String message = getMessage(vendorOrder);
        SendMessageRequestDto sendMessageRequestDto = SendMessageRequestDto.of(
            vendorOrder.getOrder().getOrderer().getUserId(),
            user.slackEmail(),
            hubManager.slackEmail(), hubManagerIdResponse.hubManagerId(), message);
        slackService.sendMessageTo(sendMessageRequestDto);
    }

    private String getMessage(final VendorOrder vendorOrder) {
        Order order = vendorOrder.getOrder();
        Orderer orderer = order.getOrderer();
        Vendor vendor = vendorOrder.getVendor();
        StringBuilder sb = new StringBuilder();
        vendorOrder.getOrderProducts()
            .forEach(product -> {
                sb
                    .append("\n\t<<")
                    .append(product.getName().getValue())
                    .append(">> 상품이 ")
                    .append(product.getQuantity().getValue())
                    .append("개 주문되었습니다.");
            });
        String productString = sb.toString();
        return "주문 번호 : " + vendorOrder.getId() + "\n"
            + "주문자 정보 : " + orderer.getUserNickname().getValue()
            + " / " + orderer.getCompanyName().getValue() + "\n"
            + "상품 정보 : " + productString + "\n"
            + "배송 기한 : " + vendorOrder.getArrivalDeadline() + "\n"
            + "발송지 : " + vendor.getVendorHubName().getValue() + "\n"
            + "도착지 : " + order.getDestination() + "\n"
            + "\n"
            + "위 내용을 기반으로 도출된 최종 발송 시한은 "
            + vendorOrder.getShippingDeadline().format(
            DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분 ss초"))
            + " 입니다.";
    }

    @Override
    public PageDto<SearchOrderPageResponseDto> searchOrderPage(
        final SearchOrderPageRequestDto requestDto
    ) {
        log.info("주문자별 주문 목록 조회 요청: {}", requestDto);

        SearchOrderPageDto searchOrderPageDto = SearchOrderPageDto.from(requestDto);
        Page<Order> allByOrdererUserId = orderRepository.searchOrderPage(searchOrderPageDto);
        Page<SearchOrderPageResponseDto> getOrderPageByOrdererUserIdResponseDtoPage =
            allByOrdererUserId.map(SearchOrderPageResponseDto::from);
        return PageDto.from(getOrderPageByOrdererUserIdResponseDtoPage);
    }

    @Override
    public GetOrderDetailResponseDto getOrderDetail(final GetOrderDetailRequestDto requestDto) {
        log.info("주문 상세 조회 요청: {}", requestDto);

        Order order = orderRepository.findOrderById(requestDto.orderId())
            .orElseThrow(OrderNotFoundException::new);

        validateGetOrderDetailAccess(requestDto, order);

        return GetOrderDetailResponseDto.from(order);
    }

    private void validateGetOrderDetailAccess(
        final GetOrderDetailRequestDto requestDto,
        final Order order
    ) {
        log.warn("공급 업체는 본인 업체에 들어온 주문에 대해서 볼 수 있도록 개선되어야 합니다.");
        validateOrderHubManagerAccess(requestDto.role(), requestDto.userId(), order);
        validateOrdererAccess(requestDto.role(), requestDto.userId(), order);
    }

    private void validateOrderHubManagerAccess(
        final AuthRole role,
        final UUID userId,
        final Order order
    ) {
        if (role.isHubManager()) {
            ValidateHubManagerResponseDto responseDto = hubService.validateHubManager(
                userId, order.getOrderer().getOrdererHubId());
            isAccess(!responseDto.isExist());
        }
    }

    private void validateOrdererAccess(
        final AuthRole role,
        final UUID userId,
        final Order order
    ) {
        if (role.isCompanyManager() || role.isDeliveryManager()) {
            GetCompanyResponseDto company = companyService.getCompanyById(userId);
            isAccess(!company.companyId().equals(order.getOrderer().getCompanyId()));
        }
    }

    @Transactional
    @Override
    public UpdateOrderResponseDto updateOrder(final UpdateOrderRequestDto requestDto) {
        log.info("주문 수정 요청: {}", requestDto);

        Order order = orderRepository.findOrderById(requestDto.orderId())
            .orElseThrow(OrderNotFoundException::new);

        validateUpdateOrderAccess(requestDto, order);

        updateVendorOrders(order, requestDto.ordersByVendor());

        return UpdateOrderResponseDto.from(order);
    }

    private void validateUpdateOrderAccess(
        final UpdateOrderRequestDto requestDto,
        final Order order
    ) {
        validateOrderHubManagerAccess(requestDto.role(), requestDto.userId(), order);
        validateOrderOwnerDeniedAccess(requestDto.role());
    }

    private static void validateOrderOwnerDeniedAccess(
        final AuthRole role
    ) {
        isAccess(role.isCompanyManager() || role.isDeliveryManager());
    }

    private static void isAccess(boolean isAccess) {
        if (isAccess) {
            throw new OrderAccessDeniedException();
        }
    }

    private void updateVendorOrders(
        final Order order,
        final List<UpdateOrderRequestDto.OrdersByVendor> ordersByVendor
    ) {
        for (UpdateOrderRequestDto.OrdersByVendor orderByVendor : ordersByVendor) {
            VendorOrder vendorOrder = getIsBeforeShippedVendorOrder(
                order, orderByVendor.orderIdByVendor());

            vendorOrder.updateArrivalDeadline(orderByVendor.arrivalDeadline());

            updateOrderProducts(vendorOrder, orderByVendor.orderedProducts());
        }
    }

    private void updateOrderProducts(
        final VendorOrder vendorOrder,
        final List<UpdateOrderRequestDto.OrdersByVendor.OrderedProduct> orderedProducts
    ) {
        for (var product : orderedProducts) {
            OrderProduct orderProduct = vendorOrder.getOrderProducts().stream()
                .filter(op -> op.getProductId().equals(product.productId()))
                .findFirst()
                .orElseThrow(OrderProductNotFoundException::new);

            orderProduct.updateQuantity(product.quantity());
        }
    }

    @Transactional
    @Override
    public CancelOrderResponseDto cancelVendorOrder(final CancelOrderRequestDto requestDto) {
        log.info("주문 취소 요청: {}", requestDto);

        Order order = orderRepository.findOrderById(requestDto.orderId())
            .orElseThrow(OrderNotFoundException::new);

        VendorOrder vendorOrder = getIsBeforeShippedVendorOrder(
            order, requestDto.vendorOrderId());

        validateCancelVendorOrderAccess(requestDto, order);

        vendorOrder.cancel();
        return CancelOrderResponseDto.from(vendorOrder);
    }

    private void validateCancelVendorOrderAccess(
        final CancelOrderRequestDto requestDto,
        final Order order
    ) {
        validateOrderHubManagerAccess(requestDto.role(), requestDto.userId(), order);
        validateOrdererAccess(requestDto.role(), requestDto.userId(), order);
    }

    private static VendorOrder getIsBeforeShippedVendorOrder(
        final Order order,
        final UUID requestDto
    ) {
        return order.getVendorOrders().stream()
            .filter(vo -> vo.getId().equals(requestDto))
            .filter(vo -> OrderStatus.isBeforeShipped(vo.getStatus()))
            .findFirst()
            .orElseThrow(VendorOrderNotFoundException::new);
    }

    @Transactional
    @Override
    public void deleteVendorOrder(final DeleteOrderRequestDto requestDto) {
        log.info("주문 삭제 요청: {}", requestDto);

        Order order = orderRepository.findOrderById(requestDto.orderId())
            .orElseThrow(OrderNotFoundException::new);

        validateDeleteVendorOrderAccess(requestDto, order);

        VendorOrder vendorOrder = getIsAfterDeliveredVendorOrder(order, requestDto.vendorOrderId());

        order.removeVendorOrder(vendorOrder);
    }

    private void validateDeleteVendorOrderAccess(
        final DeleteOrderRequestDto requestDto,
        final Order order
    ) {
        validateOrderHubManagerAccess(requestDto.role(), requestDto.userId(), order);
        validateOrderOwnerDeniedAccess(requestDto.role());
    }

    @Transactional
    @Override
    public ReturnRequestResponseDto requestReturn(final ReturnRequestRequestDto requestDto) {
        log.info("반품 요청: {}", requestDto);

        Order order = orderRepository.findOrderById(requestDto.orderId())
            .orElseThrow(OrderNotFoundException::new);

        VendorOrder vendorOrder = getIsAfterDeliveredVendorOrder(
            order, requestDto.vendorOrderId());

        validateRequestReturnAccess(requestDto, order);

        vendorOrder.requestReturn();

        return ReturnRequestResponseDto.from(vendorOrder);
    }

    private void validateRequestReturnAccess(
        final ReturnRequestRequestDto requestDto,
        final Order order
    ) {
        validateOrdererAccess(requestDto.role(), requestDto.userId(), order);
    }

    private static VendorOrder getIsAfterDeliveredVendorOrder(
        final Order order,
        final UUID vendorOrderId
    ) {
        return order.getVendorOrders().stream()
            .filter(vo -> vo.getId().equals(vendorOrderId))
            .filter(vo -> OrderStatus.isAfterDelivered(vo.getStatus()))
            .findFirst()
            .orElseThrow(VendorOrderNotFoundException::new);
    }

    @Transactional
    @Override
    public ReturnRequestDeniedResponseDto denyReturnRequest(
        final ReturnRequestDeniedRequestDto requestDto
    ) {
        log.info("반품 거부 요청: {}", requestDto);

        Order order = orderRepository.findOrderById(requestDto.orderId())
            .orElseThrow(OrderNotFoundException::new);

        VendorOrder vendorOrder = getIsReturnRequestedVendorOrder(
            requestDto.vendorOrderId(), order);

        validateDenyReturnRequestAccess(requestDto, order);

        vendorOrder.denyReturn();

        return ReturnRequestDeniedResponseDto.from(vendorOrder);
    }

    private void validateDenyReturnRequestAccess(
        final ReturnRequestDeniedRequestDto requestDto,
        final Order order
    ) {
        validateOrderHubManagerAccess(requestDto.role(), requestDto.userId(), order);
        validateOrderOwnerDeniedAccess(requestDto.role());
    }

    @Transactional
    @Override
    public ReturnOrderResponseDto returnOrder(final ReturnOrderRequestDto requestDto) {
        log.info("반품 완료 요청: {}", requestDto);

        Order order = orderRepository.findOrderById(requestDto.orderId())
            .orElseThrow(OrderNotFoundException::new);

        VendorOrder vendorOrder = getIsReturnRequestedVendorOrder(
            requestDto.vendorOrderId(), order);

        validateReturnOrderAccess(requestDto, order);

        vendorOrder.returnOrder();

        Order returnedOrder = Order.create(order.getOrderer(), vendorOrder);
        VendorOrder returnedVendorOrder = returnedOrder.getVendorOrders().get(0);

        tryRequestDelivery(returnedVendorOrder);
        returnedVendorOrder.ship();

        Order savedReturnOrder = orderRepository.save(returnedOrder);
        return ReturnOrderResponseDto.from(savedReturnOrder);
    }

    private void validateReturnOrderAccess(
        final ReturnOrderRequestDto requestDto,
        final Order order
    ) {
        validateOrderHubManagerAccess(requestDto.role(), requestDto.userId(), order);
        validateOrderOwnerDeniedAccess(requestDto.role());
    }

    private static VendorOrder getIsReturnRequestedVendorOrder(
        final UUID vendorOrderId,
        final Order order
    ) {
        return order.getVendorOrders().stream()
            .filter(vo -> vo.getId().equals(vendorOrderId))
            .filter(vo -> OrderStatus.isReturnRequested(vo.getStatus()))
            .findFirst()
            .orElseThrow(VendorOrderNotFoundException::new);
    }
}
