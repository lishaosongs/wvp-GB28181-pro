package com.genersoft.iot.vmp.jt1078.bean;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "路线属性")
public class JTRouteAttribute {

    @Schema(description = "是否启用起始时间与结束时间的判断规则 ,false：否；true：是")
    private boolean ruleForTimeLimit;

    @Schema(description = "进区域是否报警给驾驶员,false：否；true：是")
    private boolean ruleForAlarmToDriverWhenEnter;

    @Schema(description = "进区域是否报警给平台 ,false：否；true：是")
    private boolean ruleForAlarmToPlatformWhenEnter;

    @Schema(description = "出区域是否报警给驾驶员,false：否；true：是")
    private boolean ruleForAlarmToDriverWhenExit;

    @Schema(description = "出区域是否报警给平台 ,false：否；true：是")
    private boolean ruleForAlarmToPlatformWhenExit;

    public ByteBuf encode(){
        ByteBuf byteBuf = Unpooled.buffer();
        short content = 0;
        if (ruleForTimeLimit) {
            content |= 1;
        }
        if (ruleForAlarmToDriverWhenEnter) {
            content |= (1 << 2);
        }
        if (ruleForAlarmToPlatformWhenEnter) {
            content |= (1 << 3);
        }
        if (ruleForAlarmToDriverWhenExit) {
            content |= (1 << 4);
        }
        if (ruleForAlarmToPlatformWhenExit) {
            content |= (1 << 5);
        }
        byteBuf.writeShort((short)(content & 0xffff));
        return byteBuf;
    }

    public static JTRouteAttribute decode(int attributeInt) {
        JTRouteAttribute attribute = new JTRouteAttribute();
        attribute.setRuleForTimeLimit((attributeInt & 1) == 1);
        attribute.setRuleForAlarmToDriverWhenEnter((attributeInt >> 2 & 1) == 1);
        attribute.setRuleForAlarmToPlatformWhenEnter((attributeInt >> 3 & 1) == 1);
        attribute.setRuleForAlarmToDriverWhenExit((attributeInt >> 4 & 1) == 1);
        attribute.setRuleForAlarmToPlatformWhenExit((attributeInt >> 5 & 1) == 1);
        return attribute;
    }

    @Override
    public String toString() {
        return "JTRouteAttribute{" +
                "ruleForTimeLimit=" + ruleForTimeLimit +
                ", ruleForAlarmToDriverWhenEnter=" + ruleForAlarmToDriverWhenEnter +
                ", ruleForAlarmToPlatformWhenEnter=" + ruleForAlarmToPlatformWhenEnter +
                ", ruleForAlarmToDriverWhenExit=" + ruleForAlarmToDriverWhenExit +
                ", ruleForAlarmToPlatformWhenExit=" + ruleForAlarmToPlatformWhenExit +
                '}';
    }
}
