package com.example.TaskAPI.infrastructure.config;

import com.example.TaskAPI.core.observability.SqlStats;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DataSourceProxyPostProcessor implements BeanPostProcessor {
    private static final Logger SQL_LOG = LoggerFactory.getLogger("com.example.TaskAPI.sql");
    private static final long SLOW_QUERY_MS = 0L;

    @NullMarked
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (!(bean instanceof DataSource dataSource)
                || bean instanceof ProxyDataSource) {
            return bean;
        }

        return ProxyDataSourceBuilder.create(dataSource)
                .name("taskapi-ds")
                .listener(new TimingListener())
                .build();
    }

    private static final class TimingListener implements QueryExecutionListener {
        @Override
        public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        }

        @Override
        public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
            long elapsed = execInfo.getElapsedTime();
            SqlStats.record(elapsed);

            if (elapsed >= SLOW_QUERY_MS || !execInfo.isSuccess()) {
                SQL_LOG.atInfo()
                        .setMessage("sql_execution")
                        .addKeyValue("db.duration_ms", elapsed)
                        .addKeyValue("db.batch", execInfo.isBatch())
                        .addKeyValue("db.batch_size", execInfo.getBatchSize())
                        .addKeyValue("db.success", execInfo.isSuccess())
                        .addKeyValue("db.statement", queryInfoList.stream()
                                .map(QueryInfo::getQuery)
                                .collect(Collectors.joining("; ")))
                        .log();
            }
        }
    }
}
