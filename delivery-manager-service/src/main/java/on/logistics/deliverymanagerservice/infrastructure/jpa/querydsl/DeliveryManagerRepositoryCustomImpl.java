package on.logistics.deliverymanagerservice.infrastructure.jpa.querydsl;

import static on.logistics.deliverymanagerservice.domain.entity.QDeliveryManager.deliveryManager;
import static on.logistics.deliverymanagerservice.domain.entity.QHubSummary.hubSummary;
import static on.logistics.deliverymanagerservice.domain.entity.QUserSummary.userSummary;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import on.logistics.deliverymanagerservice.domain.entity.DeliveryManager;
import on.logistics.deliverymanagerservice.domain.entity.DeliveryType;
import on.logistics.deliverymanagerservice.domain.entity.dtos.SearchDeliveryManagerDto;
import on.logistics.deliverymanagerservice.global.application.dtos.PageDto;
import on.logistics.deliverymanagerservice.global.enums.AuthRole;
import on.logistics.deliverymanagerservice.global.enums.PageSortBy;
import on.logistics.deliverymanagerservice.presentation.dtos.response.QSearchDeliveryManagerResponse;
import on.logistics.deliverymanagerservice.presentation.dtos.response.SearchDeliveryManagerResponse;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class DeliveryManagerRepositoryCustomImpl implements DeliveryManagerRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Integer findMaxSequenceByHubIdAndType(UUID hubId, DeliveryType deliveryType) {
        Integer maxSequence = jpaQueryFactory
            .select(deliveryManager.sequence.max())
            .from(deliveryManager)
            .where(
                deliveryManager.hubId.eq(hubId),
                deliveryManager.type.eq(deliveryType)
            )
            .fetchOne();

        return (maxSequence != null) ? maxSequence : 0;
    }

    @Override
    public Optional<DeliveryManager> findLastAssignedManager(UUID hubId, DeliveryType type) {
        return Optional.ofNullable(
            jpaQueryFactory.selectFrom(deliveryManager)
                .where(
                    deliveryManager.hubId.eq(hubId),
                    deliveryManager.type.eq(type),
                    deliveryManager.lastAssignedAt.isNotNull()
                )
                .orderBy(
                    deliveryManager.lastAssignedAt.desc()
                )
                .fetchFirst()
        );
    }

    @Override
    public Optional<DeliveryManager> findNextDeliveryManager(UUID hubId, Integer lastSequence) {
        return Optional.ofNullable(
            jpaQueryFactory.selectFrom(deliveryManager)
                .where(
                    deliveryManager.hubId.eq(hubId),
                    deliveryManager.sequence.gt(lastSequence)
                )
                .orderBy(deliveryManager.sequence.asc())
                .fetchFirst()
        );
    }

    @Override
    public Optional<DeliveryManager> findFirstByHubIdOrderBySequenceAsc(UUID hubId,
        DeliveryType type) {
        return Optional.ofNullable(
            jpaQueryFactory.selectFrom(deliveryManager)
                .where(
                    deliveryManager.hubId.eq(hubId),
                    deliveryManager.type.eq(type)
                )
                .orderBy(deliveryManager.sequence.asc())
                .fetchFirst()
        );
    }

    @Override
    public PageDto<SearchDeliveryManagerResponse> searchDeliveryManager(
        SearchDeliveryManagerDto requestDto) {
        List<SearchDeliveryManagerResponse> content = getDeliveryManagers(requestDto);
        boolean last = true;
        long totalElement = getTotalElement(requestDto);
        int totalPages = getTotalPages(totalElement, requestDto);

        return new PageDto<>(content, last, totalPages, totalElement);
    }

    private List<SearchDeliveryManagerResponse> getDeliveryManagers(
        SearchDeliveryManagerDto requestDto) {
        return jpaQueryFactory
            .select(new QSearchDeliveryManagerResponse(
                deliveryManager.id,
                userSummary.nickname,
                hubSummary.name,
                userSummary.slackEmail,
                deliveryManager.type.stringValue(),
                deliveryManager.sequence
            ))
            .from(deliveryManager)
            .join(hubSummary).on(deliveryManager.hubId.eq(hubSummary.id))
            .join(userSummary).on(deliveryManager.userId.eq(userSummary.id))
            .where(
                keywordContains(requestDto.keyword()),
                typeEquals(requestDto.hubType()),
                filterByRole(requestDto.authRole(), requestDto.hubId())
            )
            .orderBy(getOrderConditions(requestDto.pageable().getSort()))
            .offset(requestDto.pageable().getOffset())
            .limit(requestDto.pageable().getPageSize())
            .fetch();
    }

    private int getTotalPages(long totalElement, SearchDeliveryManagerDto requestDto) {
        return (int) Math.ceil((double) totalElement / requestDto.pageable().getPageSize());
    }

    private long getTotalElement(SearchDeliveryManagerDto requestDto) {
        return Optional.ofNullable(
                jpaQueryFactory
                    .select(deliveryManager.count())
                    .from(deliveryManager)
                    .join(hubSummary).on(deliveryManager.hubId.eq(hubSummary.id))
                    .join(userSummary).on(deliveryManager.userId.eq(userSummary.id))
                    .where(
                        keywordContains(requestDto.keyword()),
                        typeEquals(requestDto.hubType()),
                        filterByRole(requestDto.authRole(), requestDto.hubId())
                    )
                    .fetchOne())
            .orElse(0L);
    }

    private OrderSpecifier<?>[] getOrderConditions(Sort sort) {
        return sort.stream()
            .map(order -> {
                String sortBy = order.getProperty();
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;

                return switch (PageSortBy.valueOf(sortBy.toUpperCase())) {
                    case CREATED_AT -> new OrderSpecifier<>(direction, deliveryManager.createdAt);
                    case UPDATED_AT -> new OrderSpecifier<>(direction, deliveryManager.updatedAt);
                    case ID -> new OrderSpecifier<>(direction, deliveryManager.id);
                };
            })
            .toArray(OrderSpecifier[]::new);
    }

    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return userSummary.nickname.containsIgnoreCase(keyword)
            .or(hubSummary.type.containsIgnoreCase(keyword));
    }

    private BooleanExpression typeEquals(String hubType) {
        return hubType != null ? hubSummary.type.eq(hubType) : null;
    }

    private BooleanExpression filterByRole(AuthRole authRole, UUID hubId) {
        if (authRole == AuthRole.MASTER) {
            return null;
        }
        if (authRole == AuthRole.HUB_MANAGER && hubId != null) {
            return deliveryManager.hubId.eq(hubId);
        }
        return Expressions.FALSE;
    }
}
