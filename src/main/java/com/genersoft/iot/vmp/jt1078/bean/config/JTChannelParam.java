package com.genersoft.iot.vmp.jt1078.bean.config;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 单独视频通道参数设置
 */
@Setter
@Getter
public class JTChannelParam implements JTDeviceSubConfig {

    /**
     * 单独通道视频参数设置列表
     */
    private List<JTAloneChanel> jtAloneChanelList;

    @Override
    public ByteBuf encode() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(jtAloneChanelList.size());
        for (JTAloneChanel jtAloneChanel : jtAloneChanelList) {
            if (jtAloneChanel == null) {
                continue;
            }
            byteBuf.writeBytes(jtAloneChanel.encode());
        }
        return byteBuf;
    }

    public static JTChannelParam decode(ByteBuf byteBuf) {
        JTChannelParam channelParam = new JTChannelParam();
        int length = byteBuf.readUnsignedByte();
        List<JTAloneChanel> jtAloneChanelList = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            jtAloneChanelList.add(JTAloneChanel.decode(byteBuf));
        }
        channelParam.setJtAloneChanelList(jtAloneChanelList);
        return channelParam;
    }
}
