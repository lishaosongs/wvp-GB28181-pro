package com.genersoft.iot.vmp.jt1078.proc.entity;

import com.genersoft.iot.vmp.jt1078.proc.response.Rs;
import lombok.Data;

/**
 * @author QingtaiJiang
 * @date 2023/4/27 18:23
 * @email qingtaij@163.com
 */
@Data
public class Cmd {
    String phoneNumber;
    Long packageNo;
    String msgId;
    String respId;
    Rs rs;

    public Cmd() {
    }

    public Cmd(Builder builder) {
        this.phoneNumber = builder.phoneNumber;
        this.packageNo = builder.packageNo;
        this.msgId = builder.msgId;
        this.respId = builder.respId;
        this.rs = builder.rs;
    }

    public static class Builder {
        String phoneNumber;
        Long packageNo;
        String msgId;
        String respId;
        Rs rs;

        public Builder setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber.replaceFirst("^0*", "");
            return this;
        }

        public Builder setPackageNo(Long packageNo) {
            this.packageNo = packageNo;
            return this;
        }

        public Builder setMsgId(String msgId) {
            this.msgId = msgId;
            return this;
        }

        public Builder setRespId(String respId) {
            this.respId = respId;
            return this;
        }

        public Builder setRs(Rs re) {
            this.rs = re;
            return this;
        }

        public Cmd build() {
            return new Cmd(this);
        }
    }


    @Override
    public String toString() {
        return "Cmd{" +
                "devId='" + phoneNumber + '\'' +
                ", packageNo=" + packageNo +
                ", msgId='" + msgId + '\'' +
                ", respId='" + respId + '\'' +
                ", rs=" + rs +
                '}';
    }
}
