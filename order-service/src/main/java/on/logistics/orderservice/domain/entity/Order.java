package on.logistics.orderservice.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import on.logistics.orderservice.domain.entity.dtos.CreateOrderDto;
import on.logistics.orderservice.global.domain.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "p_orders")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
@Getter
@SQLRestriction("is_deleted = false")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "productId", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "destination", nullable = false, length = 500)
    private String destination;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Orderer orderer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,
        fetch = FetchType.LAZY, orphanRemoval = true)
    private List<VendorOrder> vendorOrders;

    public static Order create(CreateOrderDto createOrderDto) {
        return Order.builder()
            .totalAmount(createOrderDto.totalAmount())
            .destination(createOrderDto.destination())
            .build();
    }

    public static Order create(Orderer orderer, VendorOrder vendorOrder) {
        Order returnedOrder = Order.builder()
            .totalAmount(vendorOrder.getTotalAmount())
            .destination(vendorOrder.getVendor().getVendorHubName().getValue())
            .build();

        Orderer returnedOrderer = Orderer.create(returnedOrder, vendorOrder.getVendor());

        VendorOrder returnedVendorOrder = createReturnedVendorOrder(
            orderer, vendorOrder, returnedOrder);

        returnedOrder.addDependencies(returnedOrderer, List.of(returnedVendorOrder));
        return returnedOrder;
    }

    private static VendorOrder createReturnedVendorOrder(
        Orderer orderer,
        VendorOrder vendorOrder,
        Order returnedOrder
    ) {
        VendorOrder returnedVendorOrder = VendorOrder.create(returnedOrder);

        Vendor returnedVendor = Vendor.create(orderer, returnedVendorOrder);
        List<OrderProduct> orderProducts = vendorOrder.getOrderProducts().stream()
            .map(orderProduct -> OrderProduct.create(orderProduct, returnedVendorOrder))
            .toList();
        returnedVendorOrder.addDependencies(returnedVendor, orderProducts);
        return returnedVendorOrder;
    }

    public void addDependencies(Orderer orderer, List<VendorOrder> vendorOrders) {
        addOrdererDependency(orderer);
        addVendorOrdersDependencies(vendorOrders);
    }

    public void addOrdererDependency(Orderer orderer) {
        this.orderer = orderer;
    }

    public void addVendorOrdersDependencies(List<VendorOrder> vendorOrders) {
        this.vendorOrders = vendorOrders;
    }

    public void removeVendorOrder(VendorOrder vendorOrder) {
        vendorOrders.remove(vendorOrder);
    }
}
