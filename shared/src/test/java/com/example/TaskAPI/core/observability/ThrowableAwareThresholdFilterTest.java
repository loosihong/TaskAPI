package com.example.TaskAPI.core.observability;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.spi.FilterReply;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ThrowableAwareThresholdFilterTest {
    private final ThrowableAwareThresholdFilter filter = new ThrowableAwareThresholdFilter();

    private LoggingEvent eventAt(Level level, Throwable throwable) {
        LoggingEvent event = new LoggingEvent();

        event.setLevel(level);

        if (throwable != null) {
            event.setThrowableProxy(new ThrowableProxy(throwable));
        }

        return event;
    }

    @Nested
    class WhenThrowablePresent {
        @Test
        void decide_debugWithThrowable_returnsAccept() {
            assertThat(filter.decide(eventAt(Level.DEBUG, new RuntimeException())))
                    .isEqualTo(FilterReply.ACCEPT);
        }

        @Test
        void decide_infoWithThrowable_returnsAccept() {
            assertThat(filter.decide(eventAt(Level.INFO, new RuntimeException())))
                    .isEqualTo(FilterReply.ACCEPT);
        }

        @Test
        void decide_errorWithThrowable_returnsAccept() {
            assertThat(filter.decide(eventAt(Level.ERROR, new RuntimeException())))
                    .isEqualTo(FilterReply.ACCEPT);
        }
    }

    @Nested
    class WhenThrowableAbsent {
        @Test
        void decide_debugWithoutThrowable_returnsDeny() {
            assertThat(filter.decide(eventAt(Level.DEBUG, null)))
                    .isEqualTo(FilterReply.DENY);
        }

        @Test
        void decide_infoWithoutThrowable_returnsDeny() {
            assertThat(filter.decide(eventAt(Level.INFO, null)))
                    .isEqualTo(FilterReply.DENY);
        }

        @Test
        void decide_warnWithoutThrowable_returnsAccept() {
            assertThat(filter.decide(eventAt(Level.WARN, null)))
                    .isEqualTo(FilterReply.ACCEPT);
        }

        @Test
        void decide_errorWithoutThrowable_returnsAccept() {
            assertThat(filter.decide(eventAt(Level.ERROR, null)))
                    .isEqualTo(FilterReply.ACCEPT);
        }
    }

    @Nested
    class WhenLevelThresholdRaised {
        @Test
        void decide_warnWithoutThrowableAfterRaisingThresholdToError_returnsDeny() {
            filter.setLevel("ERROR");

            assertThat(filter.decide(eventAt(Level.WARN, null)))
                    .isEqualTo(FilterReply.DENY);
        }
    }
}
