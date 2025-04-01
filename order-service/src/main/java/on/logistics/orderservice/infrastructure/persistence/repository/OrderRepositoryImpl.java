package on.logistics.orderservice.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import on.logistics.orderservice.domain.entity.Order;
import on.logistics.orderservice.domain.repository.OrderRepository;
import on.logistics.orderservice.domain.repository.dtos.SearchOrderPageDto;
import on.logistics.orderservice.infrastructure.persistence.jpa.OrderJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Page<Order> searchOrderPage(SearchOrderPageDto searchOrderPageDto) {
        return orderJpaRepository.searchOrderPage(searchOrderPageDto);
    }

    @Override
    public Optional<Order> findOrderByVendorOrderId(UUID vendorOrderId) {
        return orderJpaRepository.findOrderByVendorOrderId(vendorOrderId);
    }

}
