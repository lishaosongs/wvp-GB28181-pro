package com.genersoft.iot.vmp.jt1078.proc.request;

import com.genersoft.iot.vmp.jt1078.annotation.MsgId;
import com.genersoft.iot.vmp.jt1078.proc.Header;
import com.genersoft.iot.vmp.jt1078.proc.response.Rs;
import com.genersoft.iot.vmp.jt1078.service.Ijt1078Service;
import com.genersoft.iot.vmp.jt1078.session.Session;
import com.genersoft.iot.vmp.jt1078.session.SessionManager;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 摄像头立即拍摄命令应答
 */
@Setter
@Getter
@MsgId(id = "0805")
public class J0805 extends Re {

    private int respNo;
    /**
     * 0：成功/确认；1：失败；2：消息有误；3：不支持
     */
    private int result;

    /**
     * 表示拍摄成功的多媒体个数
     */
    private List<Long> ids = new ArrayList<>();

    @Override
    protected Rs decode0(ByteBuf buf, Header header, Session session) {
        respNo = buf.readUnsignedShort();
        result = buf.readUnsignedByte();
        if (result == 0) {
            int length = buf.readUnsignedShort();
            for (int i = 0; i < length; i++) {
                ids.add(buf.readUnsignedInt());
            }
        }
        SessionManager.INSTANCE.response(header.getPhoneNumber(), "0805", null, ids);
        return null;
    }

    @Override
    protected Rs handler(Header header, Session session, Ijt1078Service service) {
        SessionManager.INSTANCE.response(header.getPhoneNumber(), "0001", (long) respNo, result);
        return null;
    }

    @Override
    public ApplicationEvent getEvent() {
        return null;
    }
}
