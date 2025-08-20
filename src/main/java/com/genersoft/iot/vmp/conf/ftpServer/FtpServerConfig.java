package com.genersoft.iot.vmp.conf.ftpServer;

import lombok.extern.slf4j.Slf4j;
import org.apache.ftpserver.*;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnProperty(value = "ftp.enable", havingValue = "true")
@Slf4j
public class FtpServerConfig {

    @Autowired
    private UserManager userManager;

    @Autowired
    private FtpFileSystemFactory fileSystemFactory;

    @Autowired
    private Ftplet ftplet;

    @Autowired
    private FtpSetting ftpSetting;

    /**
     * ftp server init
     */
    @Bean
    public FtpServer ftpServer() {
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory listenerFactory = new ListenerFactory();
        // 1、设置服务端口
        listenerFactory.setPort(ftpSetting.getPort());
        // 2、设置被动模式数据上传的接口范围,云服务器需要开放对应区间的端口给客户端
        DataConnectionConfigurationFactory dataConnectionConfFactory = new DataConnectionConfigurationFactory();
        dataConnectionConfFactory.setPassivePorts(ftpSetting.getPassivePorts());
        listenerFactory.setDataConnectionConfiguration(dataConnectionConfFactory.createDataConnectionConfiguration());
        // 4、替换默认的监听器
        Listener listener = listenerFactory.createListener();
        serverFactory.addListener("default", listener);
        // 5、配置自定义用户事件
        Map<String, org.apache.ftpserver.ftplet.Ftplet> ftpLets = new HashMap<>();
        ftpLets.put("ftpService", ftplet);
        serverFactory.setFtplets(ftpLets);
        // 6、读取用户的配置信息
        // 6.2、设置用信息
        serverFactory.setUserManager(userManager);
        serverFactory.setFileSystem(fileSystemFactory);
        // 7、实例化FTP Server
        FtpServer server = serverFactory.createServer();
        try {
            server.start();
            if (!server.isStopped()) {
                log.info("[FTP服务] 已启动, 端口： {}", ftpSetting.getPort());
            }
        } catch (FtpException e) {
            log.info("[FTP服务] 启动失败 ", e);
        }
        return server;
    }
}
