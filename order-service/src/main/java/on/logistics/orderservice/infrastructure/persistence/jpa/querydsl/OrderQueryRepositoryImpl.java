package on.logistics.orderservice.infrastructure.persistence.jpa.querydsl;

import static on.logistics.orderservice.domain.entity.QOrder.order;
import static on.logistics.orderservice.domain.entity.QVendorOrder.vendorOrder;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import on.logistics.orderservice.domain.entity.Order;
import on.logistics.orderservice.domain.repository.dtos.SearchOrderPageDto;
import on.logistics.orderservice.global.enums.AuthRole;
import on.logistics.orderservice.global.enums.PageSortBy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class OrderQueryRepositoryImpl implements OrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Order> searchOrderPage(SearchOrderPageDto searchOrderPageDto) {
        BooleanBuilder builder = getSearchOrderQuery(searchOrderPageDto);
        List<Order> companyList = searchOrderList(builder, searchOrderPageDto.pageable());
        Long total = totalCount(builder);
        return new PageImpl<>(companyList, searchOrderPageDto.pageable(), total);
    }

    @Override
    public Optional<Order> findOrderByVendorOrderId(UUID vendorOrderId) {
        BooleanBuilder builder = getVendorOrderQuery(vendorOrderId);
        return findOrder(builder);
    }

    private BooleanBuilder getVendorOrderQuery(UUID vendorOrderId) {
        BooleanBuilder builder = new BooleanBuilder();
        if (vendorOrderId != null) {
            builder.and(order.vendorOrders.any().id.eq(vendorOrderId));
        }
        return builder;
    }

    private Optional<Order> findOrder(BooleanBuilder builder) {
        return Optional.ofNullable(queryFactory
            .selectFrom(order)
            .leftJoin(order.orderer).fetchJoin()
            .leftJoin(order.vendorOrders, vendorOrder).fetchJoin()
            .leftJoin(vendorOrder.vendor).fetchJoin()
            .where(builder)
            .fetchOne());
    }

    private BooleanBuilder getSearchOrderQuery(SearchOrderPageDto searchOrderPageDto) {
        BooleanBuilder builder = new BooleanBuilder();
        if (!AuthRole.isAllowedSearchingOtherUserOrders(searchOrderPageDto.userRole())) {
            log.info("{} 권한의 사용자는 본인의 주문만 조회할 수 있습니다.", searchOrderPageDto.userRole());
            builder.and(order.orderer.userId.eq(searchOrderPageDto.userId()));
            return builder;
        }
        if (searchOrderPageDto.ordererUserId() != null) {
            builder.and(order.orderer.userId.eq(searchOrderPageDto.ordererUserId()));
        }
        if (searchOrderPageDto.ordererUserNickname() != null) {
            builder.and(
                order.orderer.userNickname.value.contains(
                    searchOrderPageDto.ordererUserNickname()));
        }
        if (searchOrderPageDto.ordererCompanyId() != null) {
            builder.and(order.orderer.companyId.eq(searchOrderPageDto.ordererCompanyId()));
        }
        if (searchOrderPageDto.ordererCompanyName() != null) {
            builder.and(
                order.orderer.companyName.value.contains(searchOrderPageDto.ordererCompanyName()));
        }
        if (searchOrderPageDto.vendorCompanyId() != null) {
            builder.and(
                order.vendorOrders.any().vendor.companyId.eq(searchOrderPageDto.vendorCompanyId()));
        }
        if (searchOrderPageDto.vendorCompanyName() != null) {
            builder.and(order.vendorOrders.any().vendor.name.value.contains(
                searchOrderPageDto.vendorCompanyName()));
        }
        return builder;
    }

    private List<Order> searchOrderList(BooleanBuilder builder, Pageable pageable) {
        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiers(pageable);

        List<Order> result = queryFactory
            .selectFrom(order)
            .leftJoin(order.orderer).fetchJoin()
            .leftJoin(order.vendorOrders, vendorOrder).fetchJoin()
            .leftJoin(vendorOrder.vendor).fetchJoin()
            .where(builder)
            .distinct()
            .orderBy(orderSpecifiers)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (!result.isEmpty()) {
            List<UUID> orderIds = result.stream()
                .map(Order::getId)
                .toList();

            queryFactory
                .selectFrom(vendorOrder)
                .join(vendorOrder.orderProducts).fetchJoin()
                .where(vendorOrder.order.id.in(orderIds))
                .fetch();
        }

        return result;
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        Sort sort = pageable.getSort();

        sort.forEach(orderBy -> {
            String sortBy = orderBy.getProperty();
            com.querydsl.core.types.Order direction =
                orderBy.getDirection() == Sort.Direction.ASC
                    ? com.querydsl.core.types.Order.ASC
                    : com.querydsl.core.types.Order.DESC;

            switch (PageSortBy.valueOf(sortBy.toUpperCase())) {
                case CREATED_AT ->
                    orderSpecifiers.add(new OrderSpecifier<>(direction, order.createdAt));
                case UPDATED_AT ->
                    orderSpecifiers.add(new OrderSpecifier<>(direction, order.updatedAt));
                case ID -> orderSpecifiers.add(new OrderSpecifier<>(direction, order.id));
            }
        });

        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }

    private Long totalCount(BooleanBuilder builder) {
        return queryFactory.select(order.count()).from(order).where(builder).fetchOne();
    }

}
