package on.logistics.orderservice.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import on.logistics.orderservice.domain.entity.Order;
import on.logistics.orderservice.domain.repository.dtos.SearchOrderPageDto;
import on.logistics.orderservice.fixture.FixtureFactory;
import on.logistics.orderservice.global.configuration.JpaAuditingConfig;
import on.logistics.orderservice.global.configuration.QuerydslConfig;
import on.logistics.orderservice.global.enums.AuthRole;
import on.logistics.orderservice.infrastructure.persistence.jpa.OrderJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
class OrderRepositoryImplTest {

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private EntityManager em;

    private Order order;

    @BeforeEach
    void setUp() {
        order = FixtureFactory.getOrder();
        em.persist(order);
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("주문 페이지 검색 테스트 - 주문자 업체 명으로 검색")
    void searchOrderPageByOrdererCompanyName() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        SearchOrderPageDto searchOrderPageDto = new SearchOrderPageDto(
            pageable,
            order.getOrderer().getUserId(),
            AuthRole.MASTER,
            null, null, null,
            order.getOrderer().getCompanyName().getValue(),
            null, null
        );

        // when
        Page<Order> orders = orderJpaRepository.searchOrderPage(searchOrderPageDto);

        // then
        assertThat(orders).isNotNull();
        assertThat(orders.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("주문 상세 조회 - 주문 ID로 검색")
    void findByIdAndOrdererUserId() {
        // given
        UUID orderId = order.getId();

        // when
        Optional<Order> orderOptional = orderJpaRepository.findById(orderId);

        // then
        assertThat(orderOptional).isPresent();
    }


    @Test
    void findOrderByVendorOrderId() {
        // given
        UUID vendorOrderId = order.getVendorOrders().get(0).getId();

        // when
        Optional<Order> orderOptional = orderJpaRepository.findOrderByVendorOrderId(vendorOrderId);

        // then
        assertThat(orderOptional).isPresent();
    }
}