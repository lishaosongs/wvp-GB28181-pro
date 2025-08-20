package com.genersoft.iot.vmp.jt1078.proc.response;

import com.genersoft.iot.vmp.jt1078.annotation.MsgId;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * 文件上传控制
 *
 */
@Setter
@Getter
@MsgId(id = "9207")
public class J9207 extends Rs {

    // 对应平台文件上传消息的流水号
    Integer respNo;

    // 控制： 0：暂停； 1：继续； 2：取消
    private int control;

    @Override
    public ByteBuf encode() {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeShort(respNo);
        buffer.writeByte(control);
        return buffer;
    }


    @Override
    public String toString() {
        return "J9207{" +
                "respNo=" + respNo +
                ", control=" + control +
                '}';
    }
}
