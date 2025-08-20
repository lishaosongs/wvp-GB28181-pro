package com.genersoft.iot.vmp.media.abl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.genersoft.iot.vmp.conf.DynamicTask;
import com.genersoft.iot.vmp.conf.UserSetting;
import com.genersoft.iot.vmp.media.abl.bean.AblServerConfig;
import com.genersoft.iot.vmp.media.abl.bean.ConfigKeyId;
import com.genersoft.iot.vmp.media.abl.event.HookAblServerKeepaliveEvent;
import com.genersoft.iot.vmp.media.abl.event.HookAblServerStartEvent;
import com.genersoft.iot.vmp.media.event.mediaServer.MediaServerChangeEvent;
import com.genersoft.iot.vmp.media.event.mediaServer.MediaServerDeleteEvent;
import com.genersoft.iot.vmp.media.service.IMediaServerService;
import com.genersoft.iot.vmp.media.bean.MediaServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理zlm流媒体节点的状态
 */
@Component
public class ABLMediaServerStatusManger {

    private final static Logger logger = LoggerFactory.getLogger(ABLMediaServerStatusManger.class);

    private final Map<Object, MediaServer> offlineABLPrimaryMap = new ConcurrentHashMap<>();
    private final Map<Object, MediaServer> offlineAblsecondaryMap = new ConcurrentHashMap<>();
    private final Map<Object, Long> offlineAblTimeMap = new ConcurrentHashMap<>();

    @Autowired
    private ABLRESTfulUtils ablResTfulUtils;

    @Autowired
    private IMediaServerService mediaServerService;

    @Autowired
    private DynamicTask dynamicTask;

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${server.port}")
    private Integer serverPort;

    @Autowired
    private UserSetting userSetting;

    private final String type = "abl";

    @Async("taskExecutor")
    @EventListener
    public void onApplicationEvent(MediaServerChangeEvent event) {
        if (event.getMediaServerItemList() == null
                || event.getMediaServerItemList().isEmpty()) {
            return;
        }
        for (MediaServer mediaServer : event.getMediaServerItemList()) {
            if (!type.equals(mediaServer.getType())) {
                continue;
            }
            logger.info("[ABL-添加待上线节点] ID：" + mediaServer.getId());
            offlineABLPrimaryMap.put(mediaServer.getId(), mediaServer);
            offlineAblTimeMap.put(mediaServer.getId(), System.currentTimeMillis());
        }
        execute();
    }

    @Async("taskExecutor")
    @EventListener
    public void onApplicationEvent(HookAblServerStartEvent event) {
        if (event.getMediaServerItem() == null
                || !type.equals(event.getMediaServerItem().getType())
                || event.getMediaServerItem().isStatus()) {
            return;
        }
        MediaServer serverItem = mediaServerService.getOne(event.getMediaServerItem().getId());
        if (serverItem == null) {
            return;
        }
        logger.info("[ABL-HOOK事件-服务启动] ID：" + event.getMediaServerItem().getId());
        online(serverItem, null);
    }

    @Async("taskExecutor")
    @EventListener
    public void onApplicationEvent(HookAblServerKeepaliveEvent event) {
        if (event.getMediaServerItem() == null) {
            return;
        }
        MediaServer serverItem = mediaServerService.getOne(event.getMediaServerItem().getId());
        if (serverItem == null) {
            return;
        }
        logger.info("[ABL-HOOK事件-心跳] ID：" + event.getMediaServerItem().getId());
        online(serverItem, null);
    }

    @Async("taskExecutor")
    @EventListener
    public void onApplicationEvent(MediaServerDeleteEvent event) {
        if (event.getMediaServer() == null) {
            return;
        }
        logger.info("[ABL-节点被移除] ID：" + event.getMediaServer().getServerId());
        offlineABLPrimaryMap.remove(event.getMediaServer().getServerId());
        offlineAblsecondaryMap.remove(event.getMediaServer().getServerId());
        offlineAblTimeMap.remove(event.getMediaServer().getServerId());
    }

    @Scheduled(fixedDelay = 10*1000)   //每隔10秒检查一次
    public void execute(){
        // 初次加入的离线节点会在30分钟内，每间隔十秒尝试一次，30分钟后如果仍然没有上线，则每隔30分钟尝试一次连接
        if (offlineABLPrimaryMap.isEmpty() && offlineAblsecondaryMap.isEmpty()) {
            return;
        }
        if (!offlineABLPrimaryMap.isEmpty()) {
            for (MediaServer mediaServerItem : offlineABLPrimaryMap.values()) {
                if (offlineAblTimeMap.get(mediaServerItem.getId()) <  System.currentTimeMillis() - 30*60*1000) {
                    offlineAblsecondaryMap.put(mediaServerItem.getId(), mediaServerItem);
                    offlineABLPrimaryMap.remove(mediaServerItem.getId());
                    continue;
                }
                logger.info("[ABL-尝试连接] ID：{}, 地址： {}:{}", mediaServerItem.getId(), mediaServerItem.getIp(), mediaServerItem.getHttpPort());
                JSONObject responseJson = ablResTfulUtils.getServerConfig(mediaServerItem);
                AblServerConfig ablServerConfig = null;
                if (responseJson == null) {
                    logger.info("[ABL-尝试连接]失败, ID：{}, 地址： {}:{}", mediaServerItem.getId(), mediaServerItem.getIp(), mediaServerItem.getHttpPort());
                    continue;
                }
                JSONArray data = responseJson.getJSONArray("params");
                if (data == null || data.isEmpty()) {
                    logger.info("[ABL-尝试连接]失败, ID：{}, 地址： {}:{}", mediaServerItem.getId(), mediaServerItem.getIp(), mediaServerItem.getHttpPort());
                }else {
                    ablServerConfig = AblServerConfig.getInstance(data);
                    initPort(mediaServerItem, ablServerConfig);
                    online(mediaServerItem, ablServerConfig);
                }
            }
        }
        if (!offlineAblsecondaryMap.isEmpty()) {
            for (MediaServer mediaServerItem : offlineAblsecondaryMap.values()) {
                if (offlineAblTimeMap.get(mediaServerItem.getId()) <  System.currentTimeMillis() - 30*60*1000) {
                    continue;
                }
                logger.info("[ABL-尝试连接] ID：{}, 地址： {}:{}", mediaServerItem.getId(), mediaServerItem.getIp(), mediaServerItem.getHttpPort());
                JSONObject responseJson = ablResTfulUtils.getServerConfig(mediaServerItem);
                AblServerConfig ablServerConfig = null;
                if (responseJson == null) {
                    logger.info("[ABL-尝试连接]失败, ID：{}, 地址： {}:{}", mediaServerItem.getId(), mediaServerItem.getIp(), mediaServerItem.getHttpPort());
                    offlineAblTimeMap.put(mediaServerItem.getId(), System.currentTimeMillis());
                    continue;
                }
                JSONArray data = responseJson.getJSONArray("params");
                if (data == null || data.isEmpty()) {
                    logger.info("[ABL-尝试连接]失败, ID：{}, 地址： {}:{}", mediaServerItem.getId(), mediaServerItem.getIp(), mediaServerItem.getHttpPort());
                    offlineAblTimeMap.put(mediaServerItem.getId(), System.currentTimeMillis());
                }else {
                    ablServerConfig = AblServerConfig.getInstance(data);
                    initPort(mediaServerItem, ablServerConfig);
                    online(mediaServerItem, ablServerConfig);
                }
            }
        }
    }

    private void online(MediaServer mediaServerItem, AblServerConfig config) {
        offlineABLPrimaryMap.remove(mediaServerItem.getId());
        offlineAblsecondaryMap.remove(mediaServerItem.getId());
        offlineAblTimeMap.remove(mediaServerItem.getId());
        if (!mediaServerItem.isStatus()) {
            logger.info("[ABL-连接成功] ID：{}, 地址： {}:{}", mediaServerItem.getId(), mediaServerItem.getIp(), mediaServerItem.getHttpPort());
            mediaServerItem.setStatus(true);
            mediaServerItem.setHookAliveInterval(10F);
            mediaServerService.update(mediaServerItem);
            if(mediaServerItem.isAutoConfig()) {
                if (config == null) {
                    JSONObject responseJSON = ablResTfulUtils.getServerConfig(mediaServerItem);
                    JSONArray data = responseJSON.getJSONArray("params");
                    if (data != null && !data.isEmpty()) {
                        config = AblServerConfig.getInstance(data);
                    }
                }
                if (config != null) {
                    initPort(mediaServerItem, config);
                    setAblConfig(mediaServerItem, false, config);
                }
            }
            mediaServerService.update(mediaServerItem);
        }
        // 设置两次心跳未收到则认为zlm离线
        String key = "ABL-keepalive-" + mediaServerItem.getId();
        dynamicTask.startDelay(key, ()->{
            logger.warn("[ABL-心跳超时] ID：{}", mediaServerItem.getId());
            mediaServerItem.setStatus(false);
            offlineABLPrimaryMap.put(mediaServerItem.getId(), mediaServerItem);
            offlineAblTimeMap.put(mediaServerItem.getId(), System.currentTimeMillis());
            // TODO 发送离线通知
            mediaServerService.update(mediaServerItem);
        }, (int)(mediaServerItem.getHookAliveInterval() * 2 * 1000));
    }
    private void initPort(MediaServer mediaServerItem, AblServerConfig ablServerConfig) {
        // 端口只会从配置中读取一次，一旦自己配置或者读取过了将不在配置
//        if (mediaServerItem.getHttpSSlPort() == 0) {
//            mediaServerItem.setHttpSSlPort(ablServerConfig.getHttpSSLport());
//        }
        if (mediaServerItem.getRtmpPort() != ablServerConfig.getRtmpPort()) {
            mediaServerItem.setRtmpPort(ablServerConfig.getRtmpPort());
        }
//        if (mediaServerItem.getRtmpSSlPort() == 0) {
//            mediaServerItem.setRtmpSSlPort(ablServerConfig.getRtmpSslPort());
//        }
        if (mediaServerItem.getRtspPort() != ablServerConfig.getRtspPort()) {
            mediaServerItem.setRtspPort(ablServerConfig.getRtspPort());
        }
        if (mediaServerItem.getFlvPort() != ablServerConfig.getHttpFlvPort()) {
            mediaServerItem.setFlvPort(ablServerConfig.getHttpFlvPort());
        }
        if (mediaServerItem.getWsFlvPort() != ablServerConfig.getWsPort()) {
            mediaServerItem.setWsFlvPort(ablServerConfig.getWsPort());
        }
        if (mediaServerItem.getRtpProxyPort() != ablServerConfig.getPsTsRecvPort()) {
            mediaServerItem.setRtpProxyPort(ablServerConfig.getPsTsRecvPort());
        }
        if (mediaServerItem.getRtpProxyPort() != ablServerConfig.getJtt1078RecvPort()) {
            mediaServerItem.setJttProxyPort(ablServerConfig.getJtt1078RecvPort());
        }
//        if (mediaServerItem.getRtspSSLPort() == 0) {
//            mediaServerItem.setRtspSSLPort(ablServerConfig.getRtspSSlport());
//        }
//        if (mediaServerItem.getRtpProxyPort() == 0) {
//            mediaServerItem.setRtpProxyPort(ablServerConfig.getRtpProxyPort());
//        }
        mediaServerItem.setHookAliveInterval(10F);
    }

    public void setAblConfig(MediaServer mediaServerItem, boolean restart, AblServerConfig config) {
        try {
            if (config.getHookEnable() == 0) {
                logger.info("[媒体服务节点-ABL]  开启HOOK功能 ：{}", mediaServerItem.getId());
                JSONObject responseJSON = ablResTfulUtils.setConfigParamValue(mediaServerItem, "hook_enable", "1");
                if (responseJSON.getInteger("code") == 0) {
                    logger.info("[媒体服务节点-ABL]  开启HOOK功能成功 ：{}", mediaServerItem.getId());
                }else {
                    logger.info("[媒体服务节点-ABL]  开启HOOK功能失败 ：{}->{}", mediaServerItem.getId(), responseJSON.getString("memo"));
                }
            }
        }catch (Exception e) {
            logger.info("[媒体服务节点-ABL]  开启HOOK功能失败 ：{}", mediaServerItem.getId(), e);
        }
        // 设置相关的HOOK
        String[] hookUrlArray = {
                "on_stream_arrive",
                "on_stream_none_reader",
                "on_record_mp4",
                "on_stream_disconnect",
                "on_stream_not_found",
                "on_server_started",
                "on_publish",
                "on_play",
                "on_record_progress",
                "on_server_keepalive",
                "on_stream_not_arrive",
                "on_delete_record_mp4",
        };

        String protocol = sslEnabled ? "https" : "http";
        String hookPrefix = String.format("%s://%s:%s/index/hook/abl", protocol, mediaServerItem.getHookIp(), serverPort);
        Field[] fields = AblServerConfig.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                if (field.isAnnotationPresent(ConfigKeyId.class)) {
                    ConfigKeyId configKeyId = field.getAnnotation(ConfigKeyId.class);
                    for (String hook : hookUrlArray) {
                        if (configKeyId.value().equals(hook)) {
                            String hookUrl =  String.format("%s/%s", hookPrefix, hook);
                            field.setAccessible(true);
                            // 利用反射获取值后对比是否与配置中相同，不同则进行设置
                            if (!hookUrl.equals(field.get(config))) {
                                JSONObject responseJSON = ablResTfulUtils.setConfigParamValue(mediaServerItem, hook, hookUrl);
                                if (responseJSON.getInteger("code") == 0) {
                                    logger.info("[媒体服务节点-ABL]  设置HOOK {} 成功 ：{}", hook, mediaServerItem.getId());
                                }else {
                                    logger.info("[媒体服务节点-ABL]  设置HOOK {} 失败 ：{}->{}", hook, mediaServerItem.getId(), responseJSON.getString("memo"));
                                }
                            }
                        }
                    }
                }
            }catch (Exception e) {
                logger.info("[媒体服务节点-ABL]  设置HOOK 失败 ：{}", mediaServerItem.getId(), e);
            }
        }




//        Map<String, Object> param = new HashMap<>();
//        param.put("api.secret",mediaServerItem.getSecret()); // -profile:v Baseline
//        if (mediaServerItem.getRtspPort() != 0) {
//            param.put("ffmpeg.snap", "%s -rtsp_transport tcp -i %s -y -f mjpeg -frames:v 1 %s");
//        }
//        param.put("hook.enable","1");
//        param.put("hook.on_flow_report","");
//        param.put("hook.on_play",String.format("%s/on_play", hookPrefix));
//        param.put("hook.on_http_access","");
//        param.put("hook.on_publish", String.format("%s/on_publish", hookPrefix));
//        param.put("hook.on_record_ts","");
//        param.put("hook.on_rtsp_auth","");
//        param.put("hook.on_rtsp_realm","");
//        param.put("hook.on_server_started",String.format("%s/on_server_started", hookPrefix));
//        param.put("hook.on_shell_login","");
//        param.put("hook.on_stream_changed",String.format("%s/on_stream_changed", hookPrefix));
//        param.put("hook.on_stream_none_reader",String.format("%s/on_stream_none_reader", hookPrefix));
//        param.put("hook.on_stream_not_found",String.format("%s/on_stream_not_found", hookPrefix));
//        param.put("hook.on_server_keepalive",String.format("%s/on_server_keepalive", hookPrefix));
//        param.put("hook.on_send_rtp_stopped",String.format("%s/on_send_rtp_stopped", hookPrefix));
//        param.put("hook.on_rtp_server_timeout",String.format("%s/on_rtp_server_timeout", hookPrefix));
//        param.put("hook.on_record_mp4",String.format("%s/on_record_mp4", hookPrefix));
//        param.put("hook.timeoutSec","30");
//        param.put("hook.alive_interval", mediaServerItem.getHookAliveInterval());
//        // 推流断开后可以在超时时间内重新连接上继续推流，这样播放器会接着播放。
//        // 置0关闭此特性(推流断开会导致立即断开播放器)
//        // 此参数不应大于播放器超时时间
//        // 优化此消息以更快的收到流注销事件
//        param.put("protocol.continue_push_ms", "3000" );
//        // 最多等待未初始化的Track时间，单位毫秒，超时之后会忽略未初始化的Track, 设置此选项优化那些音频错误的不规范流，
//        // 等zlm支持给每个rtpServer设置关闭音频的时候可以不设置此选项
//        if (mediaServerItem.isRtpEnable() && !ObjectUtils.isEmpty(mediaServerItem.getRtpPortRange())) {
//            param.put("rtp_proxy.port_range", mediaServerItem.getRtpPortRange().replace(",", "-"));
//        }
//
//        if (!ObjectUtils.isEmpty(mediaServerItem.getRecordPath())) {
//            File recordPathFile = new File(mediaServerItem.getRecordPath());
//            param.put("protocol.mp4_save_path", recordPathFile.getParentFile().getPath());
//            param.put("protocol.downloadRoot", recordPathFile.getParentFile().getPath());
//            param.put("record.appName", recordPathFile.getName());
//        }
//
//        JSONObject responseJSON = ablResTfulUtils.setConfigParamValue(mediaServerItem, param);
//
//        if (responseJSON != null && responseJSON.getInteger("code") == 0) {
//            if (restart) {
//                logger.info("[媒体服务节点] 设置成功,开始重启以保证配置生效 {} -> {}:{}",
//                        mediaServerItem.getId(), mediaServerItem.getIp(), mediaServerItem.getHttpPort());
//                ablResTfulUtils.restartServer(mediaServerItem);
//            }else {
//                logger.info("[媒体服务节点] 设置成功 {} -> {}:{}",
//                        mediaServerItem.getId(), mediaServerItem.getIp(), mediaServerItem.getHttpPort());
//            }
//        }else {
//            logger.info("[媒体服务节点] 设置媒体服务节点失败 {} -> {}:{}",
//                    mediaServerItem.getId(), mediaServerItem.getIp(), mediaServerItem.getHttpPort());
//        }
    }

}
