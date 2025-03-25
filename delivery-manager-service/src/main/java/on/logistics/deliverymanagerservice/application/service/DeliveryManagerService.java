package on.logistics.deliverymanagerservice.application.service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import on.logistics.deliverymanagerservice.application.dtos.AssignDeliveryManagerRequestDto;
import on.logistics.deliverymanagerservice.application.dtos.CreateDeliveryManagerRequestDto;
import on.logistics.deliverymanagerservice.application.dtos.SearchDeliveryManagerRequestDto;
import on.logistics.deliverymanagerservice.application.dtos.UpdateDeliveryManagerRequestDto;
import on.logistics.deliverymanagerservice.application.dtos.ValidDeliveryManagerRequestDto;
import on.logistics.deliverymanagerservice.domain.entity.DeliveryAssignment;
import on.logistics.deliverymanagerservice.domain.entity.DeliveryManager;
import on.logistics.deliverymanagerservice.domain.entity.DeliveryType;
import on.logistics.deliverymanagerservice.domain.entity.UserSummary;
import on.logistics.deliverymanagerservice.domain.entity.dtos.CreateDeliveryManagerDto;
import on.logistics.deliverymanagerservice.domain.entity.dtos.SearchDeliveryManagerDto;
import on.logistics.deliverymanagerservice.domain.entity.repository.DeliveryAssignmentRepository;
import on.logistics.deliverymanagerservice.domain.entity.repository.DeliveryManagerRepository;
import on.logistics.deliverymanagerservice.domain.entity.repository.UserSummaryRepository;
import on.logistics.deliverymanagerservice.exception.DeliveryManagerException;
import on.logistics.deliverymanagerservice.exception.DeliveryManagerExceptionCode;
import on.logistics.deliverymanagerservice.global.application.dtos.PageDto;
import on.logistics.deliverymanagerservice.global.domain.Passport;
import on.logistics.deliverymanagerservice.global.enums.AuthRole;
import on.logistics.deliverymanagerservice.global.util.PassportUtil;
import on.logistics.deliverymanagerservice.infrastructure.clients.hub.HubServiceClient;
import on.logistics.deliverymanagerservice.infrastructure.clients.user.UserServiceClient;
import on.logistics.deliverymanagerservice.infrastructure.clients.user.feign.dtos.response.GetUserInfoResponse;
import on.logistics.deliverymanagerservice.presentation.dtos.response.AssignDeliveryManagerResponse;
import on.logistics.deliverymanagerservice.presentation.dtos.response.CreateDeliveryManagerResponse;
import on.logistics.deliverymanagerservice.presentation.dtos.response.GetDeliveryManagerResponse;
import on.logistics.deliverymanagerservice.presentation.dtos.response.SearchDeliveryManagerResponse;
import on.logistics.deliverymanagerservice.presentation.dtos.response.UpdateDeliveryManagerResponse;
import on.logistics.deliverymanagerservice.presentation.dtos.response.ValidDeliveryManagerResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryManagerService {

    private final DeliveryManagerRepository deliveryManagerRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final UserSummaryRepository userSummaryRepository;
    private final UserServiceClient userServiceClient;
    private final HubServiceClient hubServiceClient;
    private final PassportUtil passportUtil;

    @Transactional(readOnly = true)
    public GetDeliveryManagerResponse getDeliveryManager(UUID id,
        HttpServletRequest passportRequest) {
        DeliveryManager deliveryManager = findDeliveryManagerById(id);
        Passport passport = getPassport(passportRequest);
        if (!(validateMaster(passport) || validateHubManager(passport) || validateDeliveryManager(
            passport, deliveryManager.getUserId()))) {
            throw new DeliveryManagerException(
                DeliveryManagerExceptionCode.DELIVERY_MANAGER_NOT_FOUND);
        }

        UserSummary userSummary = userSummaryRepository.findByUserId(deliveryManager.getUserId())
            .orElseThrow(() -> new DeliveryManagerException(
                DeliveryManagerExceptionCode.DELIVERY_MANAGER_NOT_FOUND));

        return GetDeliveryManagerResponse.of(deliveryManager, userSummary.getNickname(),
            userSummary.getSlackEmail());
    }

    @Transactional(readOnly = true)
    public PageDto<SearchDeliveryManagerResponse> searchDeliveryManager(
        SearchDeliveryManagerRequestDto requestDto) {
        Passport passport = getPassport(requestDto.passportRequest());
        if (!(validateMaster(passport) || validateHubManager(passport))) {
            throw new DeliveryManagerException(
                DeliveryManagerExceptionCode.DELIVERY_MANAGER_NOT_FOUND);
        }

        UUID hubId = null;
        if (passport.getRole().equals(AuthRole.HUB_MANAGER)) {
            hubId = UUID.fromString(
                hubServiceClient.getHubIdByUserId(passport.getUserId()).hubId());
        }

        final var searchDeliveryManagerDto = SearchDeliveryManagerDto.of(requestDto,
            AuthRole.valueOf(passport.getRole()), hubId);
        PageDto<SearchDeliveryManagerResponse> responsePageDto = deliveryManagerRepository.searchDeliveryManager(
            searchDeliveryManagerDto);
        return responsePageDto;
    }

    @Transactional
    public CreateDeliveryManagerResponse createDeliveryManager(
        CreateDeliveryManagerRequestDto requestDto) {
        Passport passport = getPassport(requestDto.passportRequest());
        if (!(validateMaster(passport) || validateHubManager(passport))) {
            throw new DeliveryManagerException(
                DeliveryManagerExceptionCode.DELIVERY_MANAGER_NOT_FOUND);
        }

        Integer sequence = getAssignedSequence(requestDto.hubId(), requestDto.deliveryType());
        CreateDeliveryManagerDto createDeliveryManagerDto = CreateDeliveryManagerDto.of(requestDto,
            sequence);
        DeliveryManager deliveryManager = DeliveryManager.create(createDeliveryManagerDto);
        DeliveryManager savedDeliveryManager = deliveryManagerRepository.save(deliveryManager);

        GetUserInfoResponse response = userServiceClient.getUserInfo(
            String.valueOf(deliveryManager.getUserId()));
        UserSummary userSummary = UserSummary.create(response.userId(),
            response.nickname(),
            response.slackEmail());
        userSummaryRepository.save(userSummary);
        return CreateDeliveryManagerResponse.of(savedDeliveryManager.getId());
    }

    @Transactional
    public UpdateDeliveryManagerResponse updateDeliveryManager(
        UpdateDeliveryManagerRequestDto requestDto) {
        Passport passport = getPassport(requestDto.passportRequest());
        if (!(validateMaster(passport) || validateHubManager(passport))) {
            throw new DeliveryManagerException(
                DeliveryManagerExceptionCode.DELIVERY_MANAGER_NOT_FOUND);
        }

        DeliveryManager deliveryManager = findDeliveryManagerById(requestDto.deliveryManagerId());
        if (deliveryManager.getType() == requestDto.deliveryType()) {
            throw new DeliveryManagerException(DeliveryManagerExceptionCode.SAME_DELIVERY_TYPE);
        }
        int sequence = getAssignedSequence(deliveryManager.getHubId(), requestDto.deliveryType());
        deliveryManager.update(requestDto.deliveryType(), sequence);

        UserSummary userSummary = userSummaryRepository.findByUserId(deliveryManager.getUserId())
            .orElseThrow(() -> new DeliveryManagerException(
                DeliveryManagerExceptionCode.DELIVERY_MANAGER_NOT_FOUND));
        return UpdateDeliveryManagerResponse.of(deliveryManager, userSummary.getNickname(),
            userSummary.getSlackEmail());
    }

    @Transactional
    public void deleteDeliveryManager(final UUID id, HttpServletRequest passportRequest) {
        Passport passport = getPassport(passportRequest);
        if (!validateMaster(passport)) {
            throw new DeliveryManagerException(
                DeliveryManagerExceptionCode.DELIVERY_MANAGER_NOT_FOUND);
        }

        DeliveryManager deliveryManager = findDeliveryManagerById(id);
        deliveryManagerRepository.delete(deliveryManager);
    }

    @Transactional
    public AssignDeliveryManagerResponse assignDeliveryManager(
        AssignDeliveryManagerRequestDto requestDto) {
        DeliveryManager lastAssigned = deliveryManagerRepository
            .findLastAssignedManager(requestDto.hubId(), requestDto.type())
            .orElse(null);

        DeliveryManager nextManager;
        if (lastAssigned != null) {
            nextManager = deliveryManagerRepository.findNextDeliveryManager(requestDto.hubId(),
                    lastAssigned.getSequence())
                .orElseGet(() -> findFirstByHubIdOrderBySequenceAsc(requestDto.hubId(),
                    requestDto.type()));
        } else {
            nextManager = findFirstByHubIdOrderBySequenceAsc(requestDto.hubId(), requestDto.type());
        }

        nextManager.updateLastAssignedAt(LocalDateTime.now());
        deliveryManagerRepository.save(nextManager);
        DeliveryAssignment deliveryAssignment = DeliveryAssignment.create(
            requestDto.hubId(),
            nextManager.getId(),
            requestDto.deliveryId()
        );
        deliveryAssignmentRepository.save(deliveryAssignment);
        return AssignDeliveryManagerResponse.of(nextManager.getUserId());
    }

    @Transactional
    public ValidDeliveryManagerResponse validDeliveryManager(
        ValidDeliveryManagerRequestDto requestDto) {
        boolean isExistDeliveryManager = deliveryManagerRepository.findByIdAndUserId(
            requestDto.deliveryManager(), requestDto.userId()).isPresent();
        return ValidDeliveryManagerResponse.of(isExistDeliveryManager);
    }

    private DeliveryManager findDeliveryManagerById(UUID id) {
        return deliveryManagerRepository.findByIdAndIsDeleted(id, false)
            .orElseThrow(() -> new DeliveryManagerException(
                DeliveryManagerExceptionCode.DELIVERY_MANAGER_NOT_FOUND));
    }

    private Integer getAssignedSequence(UUID hubId, DeliveryType deliveryType) {
        int maxSequence = deliveryManagerRepository
            .findMaxSequenceByHubIdAndType(hubId, deliveryType);
        return maxSequence + 1;
    }

    private DeliveryManager findFirstByHubIdOrderBySequenceAsc(UUID hubId, DeliveryType type) {
        return deliveryManagerRepository.findFirstByHubIdOrderBySequenceAsc(hubId, type)
            .orElseThrow(() -> new DeliveryManagerException(
                DeliveryManagerExceptionCode.DELIVERY_MANAGER_NOT_FOUND));
    }

    private Passport getPassport(HttpServletRequest passportRequest) {
        return passportUtil.getPassportByHttpServletRequest(passportRequest);
    }

    private boolean validateMaster(Passport passport) {
        return passport.getRole().equals(AuthRole.MASTER.name());
    }

    private boolean validateHubManager(Passport passport) {
        return passport.getRole().equals(AuthRole.HUB_MANAGER.name());
    }

    private boolean validateDeliveryManager(Passport passport, UUID userId) {
        if (!passport.getRole().equals(AuthRole.DELIVERY_MANAGER.name())) {
            return false;
        } else if (!passport.getUserId().equals(userId)) {
            return false;
        }
        return true;
    }

    private void validateCompanyManager(Passport passport) {
        if (!passport.getRole().equals(AuthRole.COMPANY_MANAGER.name())) {
            throw new DeliveryManagerException(
                DeliveryManagerExceptionCode.DELIVERY_MANGER_ACCESS_DENIED);
        }
    }
}
