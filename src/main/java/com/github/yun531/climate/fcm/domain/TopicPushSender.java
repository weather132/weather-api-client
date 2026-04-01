package com.github.yun531.climate.fcm.domain;

/**
 * 토픽 푸시 전송 계약.
 * - PushFailedException 으로 실패를 전파.
 */
public interface TopicPushSender {

    String push(TopicPushMessage message, boolean dryRun);
}
