package on.logistics.productservice.application.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import on.logistics.productservice.application.dto.CreateProductRequestDto;
import on.logistics.productservice.application.dto.SearchProductRequestDto;
import on.logistics.productservice.application.dto.UpdateIncreaseProductQuantityRequestDto;
import on.logistics.productservice.application.dto.UpdateProductRequestDto;
import on.logistics.productservice.application.dto.UpdateReduceProductQuantityRequestDto;
import on.logistics.productservice.global.application.dtos.PageDto;
import on.logistics.productservice.presentation.dtos.response.CreateProductResponse;
import on.logistics.productservice.presentation.dtos.response.GetProductResponse;
import on.logistics.productservice.presentation.dtos.response.SearchProductResponse;
import on.logistics.productservice.presentation.dtos.response.UpdateIncreaseProductQuantityResponse;
import on.logistics.productservice.presentation.dtos.response.UpdateProductResponse;
import on.logistics.productservice.presentation.dtos.response.UpdateReduceProductQuantityResponse;

public interface ProductService {

    CreateProductResponse createProduct(CreateProductRequestDto requestDto);

    PageDto<SearchProductResponse> searchProduct(SearchProductRequestDto requestDto);

    GetProductResponse getProduct(UUID id);

    UpdateProductResponse updateProduct(UpdateProductRequestDto requestDto);

    void deleteProduct(UUID id, HttpServletRequest httpServletRequest);

    UpdateReduceProductQuantityResponse updateReduceProductQuantity(
        UpdateReduceProductQuantityRequestDto requestDto);

    UpdateIncreaseProductQuantityResponse updateIncreaseProductQuantity(
        UpdateIncreaseProductQuantityRequestDto requestDto);

    UpdateReduceProductQuantityResponse updateApiReduceProductQuantity(
        UpdateReduceProductQuantityRequestDto requestDto);
}
