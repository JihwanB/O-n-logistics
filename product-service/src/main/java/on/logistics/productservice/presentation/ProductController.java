package on.logistics.productservice.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import on.logistics.productservice.application.dto.CreateProductRequestDto;
import on.logistics.productservice.application.dto.SearchProductRequestDto;
import on.logistics.productservice.application.dto.UpdateIncreaseProductQuantityRequestDto;
import on.logistics.productservice.application.dto.UpdateProductRequestDto;
import on.logistics.productservice.application.dto.UpdateReduceProductQuantityRequestDto;
import on.logistics.productservice.application.service.ProductService;
import on.logistics.productservice.global.application.dtos.PageDto;
import on.logistics.productservice.global.presentation.dtos.CommonResponse;
import on.logistics.productservice.presentation.dtos.request.CreateProductRequest;
import on.logistics.productservice.presentation.dtos.request.UpdateIncreaseProductQuantityRequest;
import on.logistics.productservice.presentation.dtos.request.UpdateProductRequest;
import on.logistics.productservice.presentation.dtos.request.UpdateReduceProductQuantityRequest;
import on.logistics.productservice.presentation.dtos.response.CreateProductResponse;
import on.logistics.productservice.presentation.dtos.response.GetProductResponse;
import on.logistics.productservice.presentation.dtos.response.SearchProductResponse;
import on.logistics.productservice.presentation.dtos.response.UpdateIncreaseProductQuantityResponse;
import on.logistics.productservice.presentation.dtos.response.UpdateProductResponse;
import on.logistics.productservice.presentation.dtos.response.UpdateReduceProductQuantityResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<CommonResponse<CreateProductResponse>> createProduct(
        @Valid @RequestBody CreateProductRequest createProductRequest,
        HttpServletRequest httpServletRequest) {
        CreateProductRequestDto requestDto = CreateProductRequestDto.from(createProductRequest,
            httpServletRequest);
        CreateProductResponse response = productService.createProduct(requestDto);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<CommonResponse<PageDto<SearchProductResponse>>> getProduct(
        @RequestParam(required = false) String name, @PageableDefault Pageable pageable) {
        SearchProductRequestDto requestDto = SearchProductRequestDto.from(name, pageable);
        PageDto<SearchProductResponse> response = productService.searchProduct(requestDto);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<GetProductResponse>> getProduct(@PathVariable UUID id) {
        GetProductResponse response = productService.getProduct(id);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<UpdateProductResponse>> updateProduct(
        @PathVariable UUID id, @Valid @RequestBody UpdateProductRequest updateProductRequest,
        HttpServletRequest httpServletRequest) {
        UpdateProductRequestDto requestDto = UpdateProductRequestDto.from(id, updateProductRequest,
            httpServletRequest);
        UpdateProductResponse response = productService.updateProduct(requestDto);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteProduct(@PathVariable UUID id,
        HttpServletRequest httpServletRequest) {
        productService.deleteProduct(id, httpServletRequest);
        return ResponseEntity.ok(CommonResponse.success());
    }

    @PutMapping("/reduce/quantity/{id}")
    public ResponseEntity<CommonResponse<UpdateReduceProductQuantityResponse>> updateReduceProductQuantity(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateReduceProductQuantityRequest updateReduceProductQuantityRequest,
        HttpServletRequest httpServletRequest) {
        UpdateReduceProductQuantityRequestDto requestDto = UpdateReduceProductQuantityRequestDto.from(
            id, updateReduceProductQuantityRequest, httpServletRequest);
        UpdateReduceProductQuantityResponse response = productService.updateApiReduceProductQuantity(
            requestDto);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PutMapping("/increase/quantity/{id}")
    public ResponseEntity<CommonResponse<UpdateIncreaseProductQuantityResponse>> updateIncreaseProductQuantity(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateIncreaseProductQuantityRequest updateIncreaseProductQuantityRequest,
        HttpServletRequest httpServletRequest) {
        UpdateIncreaseProductQuantityRequestDto requestDto = UpdateIncreaseProductQuantityRequestDto.from(
            id, updateIncreaseProductQuantityRequest, httpServletRequest);
        UpdateIncreaseProductQuantityResponse response = productService.updateIncreaseProductQuantity(
            requestDto);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
