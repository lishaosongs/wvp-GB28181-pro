package com.genersoft.iot.vmp.jt1078.config;

import com.genersoft.iot.vmp.jt1078.codec.netty.TcpServer;
import com.genersoft.iot.vmp.jt1078.service.Ijt1078Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author QingtaiJiang
 * @date 2023/4/27 19:35
 * @email qingtaij@163.com
 */
@Order(Integer.MIN_VALUE)
@Configuration
@ConditionalOnProperty(value = "jt1078.enable", havingValue = "true")
public class JT1078AutoConfiguration {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private Ijt1078Service service;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public TcpServer jt1078Server(@Value("${jt1078.port}") Integer port) {
        return new TcpServer(port, applicationEventPublisher, service);
    }
}
