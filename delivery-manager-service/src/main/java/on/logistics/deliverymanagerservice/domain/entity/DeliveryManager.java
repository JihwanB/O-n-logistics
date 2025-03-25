package on.logistics.deliverymanagerservice.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import on.logistics.deliverymanagerservice.domain.entity.dtos.CreateDeliveryManagerDto;
import on.logistics.deliverymanagerservice.global.domain.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SQLRestriction("is_deleted = false")
@SQLDelete(sql = "UPDATE p_delivery_manager SET is_deleted = true WHERE id = ?")
@Table(name = "p_delivery_manager")
public class DeliveryManager extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID hubId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryType type;

    @Column(nullable = false)
    private Integer sequence;

    private LocalDateTime lastAssignedAt;

    @Builder
    private DeliveryManager(UUID userId, UUID hubId, DeliveryType type, Integer sequence,
        LocalDateTime lastAssignedAt) {
        this.userId = userId;
        this.hubId = hubId;
        this.type = type;
        this.sequence = sequence;
        this.lastAssignedAt = lastAssignedAt;
    }

    public static DeliveryManager create(CreateDeliveryManagerDto dto) {
        return DeliveryManager.builder()
            .userId(dto.userId())
            .hubId(dto.hubId())
            .type(dto.deliveryType())
            .sequence(dto.sequence())
            .build();
    }

    public void update(DeliveryType type, int sequence) {
        this.type = type;
        this.sequence = sequence;
    }

    public void updateLastAssignedAt(LocalDateTime assignedAt) {
        this.lastAssignedAt = assignedAt;
    }
}
