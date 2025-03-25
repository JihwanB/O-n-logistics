package on.logistics.slackservice.application;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import on.logistics.slackservice.application.dtos.SlackMessageRequestDto;
import on.logistics.slackservice.application.dtos.UpdateSlackMessageRequestDto;
import on.logistics.slackservice.application.util.SlackUtil;
import on.logistics.slackservice.domain.dtos.CreateSlackMessageDto;
import on.logistics.slackservice.domain.entity.Slack;
import on.logistics.slackservice.domain.repository.SlackMessageRepository;
import on.logistics.slackservice.exception.SlackException;
import on.logistics.slackservice.exception.SlackExceptionCode;
import on.logistics.slackservice.presentation.dtos.ReadSlackMessageResponse;
import on.logistics.slackservice.presentation.dtos.SlackMessageResponse;
import on.logistics.slackservice.presentation.dtos.UpdateSlackMessageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlackMessageServiceImpl implements SlackMessageService {

    private final SlackMessageRepository slackMessageRepository;
    private final SlackUtil slackUtil;

    @Override
    @Transactional
    public SlackMessageResponse sendSlackMessage(SlackMessageRequestDto requestDto) {
        log.info("슬랙 메시지 발송 요청, 발신자: {}, 수신자: {}", requestDto.slackSendEmail(),
            requestDto.slackReceiveEmail());
        try {
            slackUtil.sendMessageToUser(requestDto);
        } catch (Exception e) {
            throw new SlackException(SlackExceptionCode.MESSAGE_SEND_FAILED);
        }
        CreateSlackMessageDto dto = SlackMessageRequestDto.from(requestDto);
        Slack slack = Slack.create(dto);
        Slack saved = slackMessageRepository.save(slack);
        log.info("슬랙 메시지 발송 성공");
        return SlackMessageResponse.from(saved);
    }

    @Override
    public ReadSlackMessageResponse getSlackMessage(UUID id) {
        log.info("슬랙 메시지 조회 요청");
        Slack slack = getOrElseThrow(id);
        log.info("슬랙 메시지 조회 성공, id: {}", id);
        return ReadSlackMessageResponse.from(slack);
    }

    @Override
    @Transactional
    public UpdateSlackMessageResponse updateSlackMessage(UpdateSlackMessageRequestDto requestDto) {
        log.info("슬랙 메시지 수정 요청, id: {}", requestDto.id());
        log.info("수정 메시지: {}", requestDto.message());
        Slack slack = getOrElseThrow(requestDto.id());
        slack.updateMessage(requestDto);
        log.info("슬랙 메시지 수정 성공, id: {}", requestDto.id());
        return UpdateSlackMessageResponse.from(slack);
    }

    @Override
    @Transactional
    public void deleteMessage(UUID id) {
        log.info("슬랙 메시지 삭제 요청, id: {}", id);
        Slack slack = getOrElseThrow(id);
        slack.deleteSoftly();
        log.info("슬랙 메시지 삭제 완료: id:{}", id);
    }

    private Slack getOrElseThrow(UUID id) {
        return slackMessageRepository.findById(id)
            .orElseThrow(() -> new SlackException(SlackExceptionCode.SLACK_MESSAGE_NOT_FOUND));
    }

}
