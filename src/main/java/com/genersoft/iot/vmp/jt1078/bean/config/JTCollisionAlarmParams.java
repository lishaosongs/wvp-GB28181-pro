package com.genersoft.iot.vmp.jt1078.bean.config;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;

/**
 * 碰撞报警参数设置
 */
@Setter
@Getter
public class JTCollisionAlarmParams implements JTDeviceSubConfig{

    /**
     * 碰撞时间 单位为毫秒(ms)
     */
    private int collisionAlarmTime;

    /**
     * 碰撞加速度 单位为0.1g,设置范围为0~79,默认为10
     */
    private int collisionAcceleration;

    @Override
    public ByteBuf encode() {
        ByteBuf byteBuf = Unpooled.buffer();
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (collisionAlarmTime & 0xff);
        bytes[1] = (byte) (collisionAcceleration & 0xff);
        byteBuf.writeBytes(bytes);
        return byteBuf;
    }
}
