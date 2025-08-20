package com.genersoft.iot.vmp.jt1078.proc.request;

import com.genersoft.iot.vmp.jt1078.annotation.MsgId;
import com.genersoft.iot.vmp.jt1078.proc.Header;
import com.genersoft.iot.vmp.jt1078.proc.response.J8001;
import com.genersoft.iot.vmp.jt1078.proc.response.Rs;
import com.genersoft.iot.vmp.jt1078.service.Ijt1078Service;
import com.genersoft.iot.vmp.jt1078.session.Session;
import com.genersoft.iot.vmp.jt1078.session.SessionManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * 数据上行透传
 */
@Setter
@Getter
@MsgId(id = "0900")
public class J0900 extends Re {

    /**
     * 透传消息类型, 0x00: GNSS 模块详细定位数据, 0X0B: 道路运输证 IC卡信息, 0X41: 串口1 透传, 0X42: 串口2 透传, 0XF0 ~ 0XFF: 用户自定义透传
     */
    private Integer type;

    /**
     * 透传消息内容
     */
    private byte[] content;

    @Override
    protected Rs decode0(ByteBuf buf, Header header, Session session) {
        type = (int)buf.readUnsignedByte();
        byte[] content = new byte[buf.readableBytes()];
        buf.readBytes(content);
        return null;
    }

    @Override
    protected Rs handler(Header header, Session session, Ijt1078Service service) {
        J8001 j8001 = new J8001();
        j8001.setRespNo(header.getSn());
        j8001.setRespId(header.getMsgId());
        return null;
    }

    @Override
    public ApplicationEvent getEvent() {
        return null;
    }

}
