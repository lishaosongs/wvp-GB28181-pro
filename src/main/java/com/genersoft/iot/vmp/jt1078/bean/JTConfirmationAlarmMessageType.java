package com.genersoft.iot.vmp.jt1078.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "人工确认报警类型")
public class JTConfirmationAlarmMessageType {
    @Schema(description = "确认紧急报警")
    private boolean urgent;
    @Schema(description = "确认危险预警")
    private boolean alarmDangerous;
    @Schema(description = "确认进出区域报警")
    private boolean alarmRegion;
    @Schema(description = "确认进出路线报警")
    private boolean alarmRoute;
    @Schema(description = "确认路段行驶时间不足/过长报警")
    private boolean alarmTravelTime;
    @Schema(description = "确认车辆非法点火报警")
    private boolean alarmIllegalIgnition;
    @Schema(description = "确认车辆非法位移报警")
    private boolean alarmIllegalDisplacement;

    public long encode(){
        long result = 0L;
        if (urgent) {
            result |= 0x01;
        }
        if (alarmDangerous) {
            result |= (0x01 << 3);
        }
        if (alarmRegion) {
            result |= (0x01 << 20);
        }
        if (alarmRoute) {
            result |= (0x01 << 21);
        }
        if (alarmTravelTime) {
            result |= (0x01 << 22);
        }
        if (alarmIllegalIgnition) {
            result |= (0x01 << 27);
        }
        if (alarmIllegalDisplacement) {
            result |= (0x01 << 28);
        }
        return result;
    }


    @Override
    public String toString() {
        return "JConfirmationAlarmMessageType{" +
                "urgent=" + urgent +
                ", alarmDangerous=" + alarmDangerous +
                ", alarmRegion=" + alarmRegion +
                ", alarmRoute=" + alarmRoute +
                ", alarmTravelTime=" + alarmTravelTime +
                ", alarmIllegalIgnition=" + alarmIllegalIgnition +
                ", alarmIllegalDisplacement=" + alarmIllegalDisplacement +
                '}';
    }
}
