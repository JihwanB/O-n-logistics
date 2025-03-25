package on.logistics.productservice.application.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import on.logistics.productservice.application.dto.CreateProductRequestDto;
import on.logistics.productservice.application.dto.SearchProductRequestDto;
import on.logistics.productservice.application.dto.UpdateIncreaseProductQuantityRequestDto;
import on.logistics.productservice.application.dto.UpdateProductRequestDto;
import on.logistics.productservice.application.dto.UpdateReduceProductQuantityRequestDto;
import on.logistics.productservice.domain.Product;
import on.logistics.productservice.domain.dto.CreateProductDto;
import on.logistics.productservice.domain.dto.UpdateProductDto;
import on.logistics.productservice.domain.repository.ProductRepository;
import on.logistics.productservice.exception.ProductException;
import on.logistics.productservice.exception.ProductExceptionCode;
import on.logistics.productservice.global.application.dtos.PageDto;
import on.logistics.productservice.global.domain.Passport;
import on.logistics.productservice.global.enums.AuthRole;
import on.logistics.productservice.global.utils.PassportUtil;
import on.logistics.productservice.infrastructure.clients.company.CompanyServiceClient;
import on.logistics.productservice.infrastructure.clients.company.feign.dtos.CompanyStatus;
import on.logistics.productservice.infrastructure.clients.company.feign.dtos.GetCompanyInfo;
import on.logistics.productservice.infrastructure.clients.hub.HubServiceClient;
import on.logistics.productservice.infrastructure.clients.hub.feign.dtos.GetHubInfo;
import on.logistics.productservice.infrastructure.clients.hub.feign.dtos.GetHubManagerBooleanResponse;
import on.logistics.productservice.infrastructure.clients.hub.feign.dtos.HubManagerBooleanRequest;
import on.logistics.productservice.presentation.dtos.response.CreateProductResponse;
import on.logistics.productservice.presentation.dtos.response.GetProductResponse;
import on.logistics.productservice.presentation.dtos.response.SearchProductResponse;
import on.logistics.productservice.presentation.dtos.response.UpdateIncreaseProductQuantityResponse;
import on.logistics.productservice.presentation.dtos.response.UpdateProductResponse;
import on.logistics.productservice.presentation.dtos.response.UpdateReduceProductQuantityResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CompanyServiceClient companyServiceClient;
    private final HubServiceClient hubServiceClient;
    private final PassportUtil passportUtil;

    @Override
    @Transactional
    public CreateProductResponse createProduct(CreateProductRequestDto requestDto) {
        Passport passport = getPassport(requestDto.httpServletRequest());
        validDeliveryManager(passport);

        // todo : 유저의 아이디 정보를 받아와서 권한 체크 필요
        GetCompanyInfo companyInfo = companyServiceClient.getCompanyInfo(requestDto.companyId());

        if (companyInfo == null || companyInfo.companyStatus() != CompanyStatus.APPROVED) {
            throw new ProductException(ProductExceptionCode.PRODUCT_COMPANY_IS_NOT_FOUND);
        }

        GetHubInfo hubInfo = hubServiceClient.getHubInfo(companyInfo.managedHubId());
        if (hubInfo == null) {
            throw new ProductException(ProductExceptionCode.PRODUCT_HUB_IS_NOT_FOUND);
        }

        CreateProductDto createProductDto = CreateProductDto.from(requestDto, companyInfo);
        Product product = Product.create(createProductDto);
        validHubManagerHubAndCompanyManager(passport, companyInfo, product);
        Product saved = productRepository.save(product);
        return CreateProductResponse.of(saved.getId());
    }

    @Override
    public PageDto<SearchProductResponse> searchProduct(SearchProductRequestDto requestDto) {
        Page<Product> products = productRepository.searchProduct(requestDto);
        Page<SearchProductResponse> responsePage = products.map(SearchProductResponse::from);
        return PageDto.from(responsePage);
    }

    @Override
    public GetProductResponse getProduct(UUID id) {
        Product product = getOrElseThrow(id);
        return GetProductResponse.from(product);
    }

    @Override
    @Transactional
    public UpdateProductResponse updateProduct(UpdateProductRequestDto requestDto) {
        Passport passport = getPassport(requestDto.httpServletRequest());
        log.info("Updating product {}", requestDto.productId());
        validDeliveryManager(passport);

        UpdateProductDto updateProductDto = UpdateProductDto.from(requestDto);
        Product product = getOrElseThrow(updateProductDto.productId());
        GetCompanyInfo companyInfo = companyServiceClient.getCompanyInfo(product.getCompanyId());
        validHubManagerHubAndCompanyManager(passport, companyInfo, product);

        product.update(updateProductDto);
        return UpdateProductResponse.of(product.getId());
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id, HttpServletRequest httpServletRequest) {
        Passport passport = getPassport(httpServletRequest);
        if (passport.getRole().equals(AuthRole.COMPANY_MANAGER.name()) || passport.getRole()
            .equals(AuthRole.DELIVERY_MANAGER.name())) {
            throw new ProductException(ProductExceptionCode.PRODUCT_ACCESS_DENIED);
        }
        Product product = getOrElseThrow(id);
        validHubManagerHub(passport, product);
        product.deleteSoftly();
    }

    @Override
    @Transactional
    public UpdateReduceProductQuantityResponse updateReduceProductQuantity(
        UpdateReduceProductQuantityRequestDto requestDto) {
        Passport passport = getPassport(requestDto.httpServletRequest());
        validDeliveryManager(passport);
        Product product = getOrElseThrow(requestDto.productId());
        GetCompanyInfo companyInfo = companyServiceClient.getCompanyInfo(product.getCompanyId());
        validHubManagerHubAndCompanyManager(passport, companyInfo, product);

        if (product.getQuantity().getValue() == 0) {
            throw new ProductException(ProductExceptionCode.PRODUCT_QUANTITY_LIMIT);
        }

        product.updateReduceQuantity(requestDto.productQuantity());
        return UpdateReduceProductQuantityResponse.of(product.getId());
    }

    // 임시 재고 분리 킹 갓 <<윤한나>> 테크 리더님
    @Override
    @Transactional
    public UpdateReduceProductQuantityResponse updateApiReduceProductQuantity(
        UpdateReduceProductQuantityRequestDto requestDto) {
        Passport passport = getPassport(requestDto.httpServletRequest());
        validDeliveryManager(passport);
        Product product = getOrElseThrow(requestDto.productId());
        GetCompanyInfo companyInfo = companyServiceClient.getCompanyInfo(product.getCompanyId());

        if (product.getQuantity().getValue() == 0) {
            throw new ProductException(ProductExceptionCode.PRODUCT_QUANTITY_LIMIT);
        }

        product.updateReduceQuantity(requestDto.productQuantity());
        return UpdateReduceProductQuantityResponse.of(product.getId());
    }


    @Override
    @Transactional
    public UpdateIncreaseProductQuantityResponse updateIncreaseProductQuantity(
        UpdateIncreaseProductQuantityRequestDto requestDto) {
        Passport passport = getPassport(requestDto.httpServletRequest());
        validDeliveryManager(passport);
        Product product = getOrElseThrow(requestDto.productId());
        GetCompanyInfo companyInfo = companyServiceClient.getCompanyInfo(product.getCompanyId());
        validHubManagerHubAndCompanyManager(passport, companyInfo, product);

        product.updateIncreaseQuantity(requestDto.productQuantity());
        return UpdateIncreaseProductQuantityResponse.of(product.getId());
    }

    private Product getOrElseThrow(UUID id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ProductException(ProductExceptionCode.PRODUCT_IS_NOT_FOUND));
    }

    private Passport getPassport(HttpServletRequest passportRequest) {
        return passportUtil.getPassportByHttpServletRequest(passportRequest);
    }

    private void validDeliveryManager(Passport passport) {
        if (passport.getRole().equals(AuthRole.DELIVERY_MANAGER.name())) {
            throw new ProductException(ProductExceptionCode.PRODUCT_ACCESS_DENIED);
        }
    }

    private void validHubManagerHubAndCompanyManager(Passport passport,
        GetCompanyInfo company, Product product) {
        validHubManagerHub(passport, product);
        validCompanyManager(passport, company);
    }

    private void validHubManagerHub(Passport passport, Product product) {
        if (passport.getRole().equals(AuthRole.HUB_MANAGER.name())) {
            GetHubManagerBooleanResponse hubManagerBoolean = getGetHubManagerBooleanResponse(
                passport,
                product.getManagedHubId());
            if (Boolean.FALSE.equals(hubManagerBoolean.isExist())) {
                throw new ProductException(ProductExceptionCode.PRODUCT_ACCESS_DENIED);
            }
        }
    }

    private void validCompanyManager(Passport passport, GetCompanyInfo company) {
        if (passport.getRole().equals(AuthRole.COMPANY_MANAGER.name()) && !company.userId()
            .equals(passport.getUserId())) {
            throw new ProductException(ProductExceptionCode.PRODUCT_ACCESS_DENIED);
        }
    }

    private GetHubManagerBooleanResponse getGetHubManagerBooleanResponse(Passport passport,
        UUID hubId) {
        HubManagerBooleanRequest hubManagerBooleanRequest = HubManagerBooleanRequest.of(
            passport.getUserId(), hubId);
        log.info(hubId.toString());
        log.info(passport.getUserId().toString());
        return hubServiceClient.getHubManagerBoolean(hubManagerBooleanRequest);
    }
}