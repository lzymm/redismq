package com.lzy.redismq.config;

import com.lzy.redismq.annotation.RedisMQListener;
import com.lzy.redismq.error.DefaultErrorHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.util.ErrorHandler;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * listener 端点
 */
@Data
@Slf4j(topic = "redis-mq:RedisMQListenerEndpoint")
public class RedisMQListenerEndpoint {
    /**
     * redis stream key
     */
    private String stream;

    /**
     * listener 唯一id
     */
    private String id;
    /**
     * 消费者组
     */
    private String group;

    /**
     * 消费者名（数组） {C1,C2,C3}
     */
    private Collection<String> names;

    /**
     * 是否自动确认，默认 true
     */
    private boolean autoAck = true;

    /**
     * 每次拉去消息的数量，默认10
     */
    private int perPollSize = 10;

    /**
     * 拉取数据超时时间（s），默认2s
     */
    private int pollTimeoutSeconds = 2;
    /**
     * 默认错误处理器
     */
    private ErrorHandler errorHandler = new DefaultErrorHandler();

    private Executor taskExecutor = new SimpleAsyncTaskExecutor();


    private String beanName;
    private Object bean;
    private Method method;
    private RedisMQStreamHelper redisMQStreamHelper;

    public static RedisMQListenerEndpoint buildListerEndpoint(RedisMQStreamHelper redisMQStreamHelper,RedisMQListener redisMQListener, Method method, Object bean,String beanName) {
        try {

            String containerIdPrefix = "RedisMQListenerContainer#";
            RedisMQListenerEndpoint endpoint = new RedisMQListenerEndpoint();
            endpoint.setRedisMQStreamHelper(redisMQStreamHelper);
            endpoint.setId(containerIdPrefix+ redisMQStreamHelper.createUniqNum());
            endpoint.setBeanName(beanName);
            endpoint.setBean(bean);
            endpoint.setMethod(method);
            endpoint.setAutoAck(redisMQListener.autoAck());
            endpoint.setPerPollSize(redisMQListener.perPollSize());
            endpoint.setPollTimeoutSeconds(redisMQListener.pollTimeoutSeconds());
            endpoint.setErrorHandler(redisMQListener.errorHandler().getDeclaredConstructor().newInstance());
            endpoint.setTaskExecutor(redisMQListener.taskExecutor().getDeclaredConstructor().newInstance());


            String stream = redisMQListener.stream();
            String group = redisMQListener.group();
            String[] names = redisMQListener.names();
            Assert.hasText(stream, "@RedisListener's stream is empty!!");
            Assert.hasText(group, "@RedisListener's group is empty!!");

            if (names.length == 0)
                log.warn("@RedisListener's names is empty,will set default value by group={}", group);
            endpoint.setStream(stream);
            endpoint.setGroup(group);
            endpoint.setNames(names.length == 0 ? List.of(group) : List.of(names));
            return endpoint;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
