package com.genersoft.iot.vmp.jt1078.proc.request;

import com.genersoft.iot.vmp.jt1078.annotation.MsgId;
import com.genersoft.iot.vmp.jt1078.bean.JTMediaDataInfo;
import com.genersoft.iot.vmp.jt1078.proc.Header;
import com.genersoft.iot.vmp.jt1078.proc.response.J8001;
import com.genersoft.iot.vmp.jt1078.proc.response.Rs;
import com.genersoft.iot.vmp.jt1078.service.Ijt1078Service;
import com.genersoft.iot.vmp.jt1078.session.Session;
import com.genersoft.iot.vmp.jt1078.session.SessionManager;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 存储多媒体数据检索应答
 *
 */
@Slf4j
@MsgId(id = "0802")
public class J0802 extends Re {

    @Override
    protected Rs decode0(ByteBuf buf, Header header, Session session) {
        int respNo = buf.readUnsignedShort();
        int length = buf.readUnsignedShort();
        if (length == 0) {
            log.info("[JT-存储多媒体数据检索应答]: {}", length);
            SessionManager.INSTANCE.response(header.getPhoneNumber(), "0802", (long) respNo, new ArrayList<>());
            return null;
        }
        List<JTMediaDataInfo> mediaDataInfoList = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            mediaDataInfoList.add(JTMediaDataInfo.decode(buf));
        }
        log.info("[JT-存储多媒体数据检索应答]: {}", mediaDataInfoList.size());
        SessionManager.INSTANCE.response(header.getPhoneNumber(), "0802", (long) respNo, mediaDataInfoList);
        return null;
    }

    @Override
    protected Rs handler(Header header, Session session, Ijt1078Service service) {
        J8001 j8001 = new J8001();
        j8001.setRespNo(header.getSn());
        j8001.setRespId(header.getMsgId());
        return j8001;
    }

    @Override
    public ApplicationEvent getEvent() {
        return null;
    }
}
