package com.genersoft.iot.vmp.jt1078.proc.response;

import com.genersoft.iot.vmp.jt1078.annotation.MsgId;
import com.genersoft.iot.vmp.jt1078.bean.JTDeviceConfig;
import com.genersoft.iot.vmp.jt1078.bean.common.ConfigAttribute;
import com.genersoft.iot.vmp.jt1078.bean.config.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 设置终端参数
 */
@Getter
@MsgId(id = "8103")
public class J8103 extends Rs {

    private final static Logger log = LoggerFactory.getLogger(J8103.class);

    private JTDeviceConfig config;

    @Override
    public ByteBuf encode() {
        ByteBuf buffer = Unpooled.buffer();
        Class<? extends JTDeviceConfig> configClass = config.getClass();
        Field[] declaredFields = configClass.getDeclaredFields();
        Map<Field, ConfigAttribute> fieldConfigAttributeMap = new HashMap<>();
        for (Field field : declaredFields) {
            try{
                Method method = configClass.getDeclaredMethod("get" + StringUtils.capitalize(field.getName()));
                Object invoke = method.invoke(config);
                if (invoke == null) {
                    continue;
                }
                ConfigAttribute configAttribute = field.getAnnotation(ConfigAttribute.class);
                if (configAttribute != null) {
                    fieldConfigAttributeMap.put(field, configAttribute);
                }
            }catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                log.error("[设置终端参数 ] 编码失败", e );
            }

        }
        buffer.writeByte(fieldConfigAttributeMap.size());

        if (!fieldConfigAttributeMap.isEmpty()) {
            for (Field field : fieldConfigAttributeMap.keySet()) {
                try{
                    ConfigAttribute configAttribute = fieldConfigAttributeMap.get(field);
                    buffer.writeInt((int) (configAttribute.id() & 0xffff));
                    switch (configAttribute.type()) {
                        case "Long":
                            buffer.writeByte(4);
                            field.setAccessible(true);
                            long longVal = (long)field.get(config);
                            buffer.writeInt((int) (longVal & 0xffffffffL));
                            continue;
                        case "String":
                            field.setAccessible(true);
                            String stringVal = (String)field.get(config);
                            buffer.writeByte(stringVal.getBytes(Charset.forName("GBK")).length);
                            buffer.writeCharSequence(stringVal, Charset.forName("GBK"));
                            continue;
                        case "Integer":
                            buffer.writeByte(2);
                            field.setAccessible(true);
                            Integer integerVal = (Integer)field.get(config);
                            buffer.writeShort((short)(integerVal & 0xffff));
                            continue;
                        case "Short":
                            buffer.writeByte(1);
                            field.setAccessible(true);
                            Short shortVal = (Short)field.get(config);
                            buffer.writeByte((int) (shortVal & 0xff));
                            continue;
                        case "IllegalDrivingPeriods":
                        case "CollisionAlarmParams":
                        case "CameraTimer":
                        case "GnssPositioningMode":
                        case "VideoParam":
                        case "ChannelListParam":
                        case "ChannelParam":
                        case "AlarmRecordingParam":
                        case "AlarmShielding":
                        case "VideoAlarmBit":
                        case "AnalyzeAlarmParam":
                        case "AwakenParam":
                        case "AlarmSign":
                            field.setAccessible(true);
                            JTDeviceSubConfig subConfig = (JTDeviceSubConfig)field.get(config);
                            ByteBuf byteBuf = subConfig.encode();
                            buffer.writeByte(byteBuf.readableBytes());
                            buffer.writeBytes(byteBuf);
                            continue;
                    }
                }catch (Exception e) {
                    log.error("[设置终端参数 ] 编码失败", e );
                }
            }
        }
        return buffer;
    }

    public void setConfig(JTDeviceConfig config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return "J8103{" +
                "config=" + config +
                '}';
    }
}
