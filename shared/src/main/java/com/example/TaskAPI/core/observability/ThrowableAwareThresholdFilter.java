package com.example.TaskAPI.core.observability;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class ThrowableAwareThresholdFilter extends Filter<ILoggingEvent> {
    private Level level = Level.WARN;

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event.getThrowableProxy() != null) {
            return FilterReply.ACCEPT;
        }

        return event.getLevel().isGreaterOrEqual(level) ? FilterReply.ACCEPT : FilterReply.DENY;
    }

    public void setLevel(String level) {
        this.level = Level.toLevel(level);
    }
}
