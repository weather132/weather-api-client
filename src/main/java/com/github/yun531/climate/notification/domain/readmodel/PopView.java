package com.github.yun531.climate.notification.domain.readmodel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 알림/판정 전용 POP Projection(읽기 모델).
 * - ForecastSnap(원본 스냅샷)에서 POP만 뽑아 26시간/7일 규격으로 정규화한다.
 */
public record PopView(
        Hourly hourly,
        Daily daily,
        LocalDateTime announceTime
) {
    public static final int HOURLY_SIZE = 26;
    public static final int DAILY_SIZE = 7;

    public PopView {
        Objects.requireNonNull(hourly, "hourly must not be null");
        Objects.requireNonNull(daily, "daily must not be null");
    }

    /** ======================= Pair ======================= */
    public record Pair(PopView current, PopView previous) {}

    /** ======================= Hourly ======================= */
    public record Hourly(List<Pop> pops) {

        /** @param pop null 이면 "데이터 없음" */
        public record Pop(LocalDateTime effectiveTime, Integer pop) {
            public static Pop empty() {
                return new Pop(null, null);
            }
        }

        public Hourly {
            pops = (pops == null) ? List.of() : List.copyOf(pops);
            if (pops.size() != HOURLY_SIZE) {
                throw new IllegalArgumentException(
                        "HourlySeries must have " + HOURLY_SIZE + " points");
            }
        }

        /**
         * 가용 데이터로 Hourly를 생성. HOURLY_SIZE 미만이면 empty로 패딩.
         */
        public static Hourly padded(List<Pop> available) {
            List<Pop> pops = new ArrayList<>(HOURLY_SIZE);
            for (int i = 0; i < HOURLY_SIZE; i++) {
                pops.add(i < available.size() ? available.get(i) : Pop.empty());
            }
            return new Hourly(pops);
        }
    }

    /** ======================= Daily ======================= */
    public record Daily(List<Pop> pops) {

        /** @param am null 이면 "데이터 없음", @param pm null 이면 "데이터 없음" */
        public record Pop(Integer am, Integer pm) {
            public static Pop empty() {
                return new Pop(null, null);
            }
        }

        public Daily {
            pops = (pops == null) ? List.of() : List.copyOf(pops);
            if (pops.size() != DAILY_SIZE) {
                throw new IllegalArgumentException(
                        "DailySeries must have " + DAILY_SIZE + " dailyPoints");
            }
        }

        public Pop get(int dayOffset0to6) {
            return pops.get(dayOffset0to6);
        }
    }
}