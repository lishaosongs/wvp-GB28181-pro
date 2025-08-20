package com.genersoft.iot.vmp.jt1078.proc.request;

import com.genersoft.iot.vmp.jt1078.annotation.MsgId;
import com.genersoft.iot.vmp.jt1078.proc.Header;
import com.genersoft.iot.vmp.jt1078.proc.response.J8001;
import com.genersoft.iot.vmp.jt1078.proc.response.Rs;
import com.genersoft.iot.vmp.jt1078.service.Ijt1078Service;
import com.genersoft.iot.vmp.jt1078.session.Session;
import com.genersoft.iot.vmp.jt1078.session.SessionManager;
import com.genersoft.iot.vmp.utils.DateUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 终端上传音视频资源列表
 *
 * @author QingtaiJiang
 * @date 2023/4/28 10:36
 * @email qingtaij@163.com
 */
@Setter
@Getter
@MsgId(id = "1205")
public class J1205 extends Re {

    Integer respNo;

    private List<JRecordItem> recordList = new ArrayList<>();

    @Override
    protected Rs decode0(ByteBuf buf, Header header, Session session) {
        respNo = buf.readUnsignedShort();
        long size = buf.readUnsignedInt();

        for (int i = 0; i < size; i++) {
            JRecordItem item = new JRecordItem();
            item.setChannelId(buf.readUnsignedByte());
            String startTime = ByteBufUtil.hexDump(buf.readSlice(6));
            item.setStartTime(DateUtil.jt1078Toyyyy_MM_dd_HH_mm_ss(startTime));
            String endTime = ByteBufUtil.hexDump(buf.readSlice(6));
            item.setEndTime(DateUtil.jt1078Toyyyy_MM_dd_HH_mm_ss(endTime));
            item.setAlarmSign(buf.readLong());
            item.setMediaType(buf.readUnsignedByte());
            item.setStreamType(buf.readUnsignedByte());
            item.setStorageType(buf.readUnsignedByte());
            item.setSize(buf.readUnsignedInt());
            recordList.add(item);
        }
        return null;
    }

    @Override
    protected Rs handler(Header header, Session session, Ijt1078Service service) {
        SessionManager.INSTANCE.response(header.getPhoneNumber(), "1205", (long) respNo, recordList);
        J8001 j8001 = new J8001();
        j8001.setRespNo(header.getSn());
        j8001.setRespId(header.getMsgId());
        j8001.setResult(J8001.SUCCESS);
        return j8001;
    }


    @Setter
    @Getter
    public static class JRecordItem {

        // 逻辑通道号
        private int channelId;

        // 开始时间
        private String startTime;

        // 结束时间
        private String endTime;

        // 报警标志
        private long alarmSign;

        // 音视频资源类型
        private int mediaType;

        // 码流类型
        private int streamType = 1;

        // 存储器类型
        private int storageType;

        // 文件大小
        private long size;

        @Override
        public String toString() {
            return "JRecordItem{" +
                    "channelId=" + channelId +
                    ", startTime='" + startTime + '\'' +
                    ", endTime='" + endTime + '\'' +
                    ", warn=" + alarmSign +
                    ", mediaType=" + mediaType +
                    ", streamType=" + streamType +
                    ", storageType=" + storageType +
                    ", size=" + size +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "J1205{" +
                "respNo=" + respNo +
                ", recordList=" + recordList +
                '}';
    }

    @Override
    public ApplicationEvent getEvent() {
        return null;
    }
}
