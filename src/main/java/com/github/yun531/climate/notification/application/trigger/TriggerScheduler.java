package com.github.yun531.climate.notification.application.trigger;

import com.github.yun531.climate.common.log.MdcContext;
import com.github.yun531.climate.common.log.TraceIdGenerator;
import com.github.yun531.climate.common.time.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@Profile("!test & !integration-test")
@RequiredArgsConstructor
public class TriggerScheduler {

    private final TriggerPushSender sender;

    // 개발/검증 중에는 true로 두면 실제 발송 없이 검증만 수행
    private static final boolean DRY_RUN = false;

    // 08~23시(3시간 간격)의 15분에만 hourly 전송  // todo: db 갱신이 정각+10분에 스캐줄러에 걸림, 다음주에 이벤트 형식으로 수정?
    @Scheduled(cron = "0 15 8-23/3 * * *")
    public void triggerHourly() {
        try (var ignored = MdcContext.of(Map.of(
                "traceId", TraceIdGenerator.generate(),
                "job", "trigger-hourly"))) {

            var now = TimeUtil.nowTruncatedToMinute();
            int hour = now.getHour();
            long startNanos = System.nanoTime();

            try {
                String messageId = sender.sendHourly(now, hour, DRY_RUN);
                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
                log.info("[TriggerHourly] 트리거 발송 완료. hour={} dryRun={} messageId={} elapsedMs={}",
                        hour, DRY_RUN, messageId, elapsedMs);
            } catch (Exception e) {
                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
                log.error("[TriggerHourly] 트리거 발송 실패. hour={} dryRun={} elapsedMs={}",
                        hour, DRY_RUN, elapsedMs, e);
            }
        }
    }

    // 매 시간(00~23) + 15분 마다 daily 전송
    @Scheduled(cron = "0 15 * * * *")
    public void triggerDaily() {
        try (var ignored = MdcContext.of(Map.of(
                "traceId", TraceIdGenerator.generate(),
                "job", "trigger-daily"))) {

            var now = TimeUtil.nowTruncatedToMinute();
            int hour = now.getHour();
            long startNanos = System.nanoTime();

            try {
                String messageId = sender.sendDaily(now, hour, DRY_RUN);
                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
                log.info("[TriggerDaily] 트리거 발송 완료. hour={} dryRun={} messageId={} elapsedMs={}",
                        hour, DRY_RUN, messageId, elapsedMs);
            } catch (Exception e) {
                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
                log.error("[TriggerDaily] 트리거 발송 실패. hour={} dryRun={} elapsedMs={}",
                        hour, DRY_RUN, elapsedMs, e);
            }
        }
    }
}