package com.genersoft.iot.vmp.jt1078.session;

import com.genersoft.iot.vmp.jt1078.proc.Header;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author QingtaiJiang
 * @date 2023/4/27 18:54
 * @email qingtaij@163.com
 */
@Slf4j
public class Session {

    public static final AttributeKey<Session> KEY = AttributeKey.newInstance(Session.class.getName());

    // Netty的channel
    protected final Channel channel;

    // 原子类的自增ID
    private final AtomicInteger serialNo = new AtomicInteger(0);

    // 是否注册成功
    @Getter
    private boolean registered = false;

    // 设备手机号
    @Getter
    private String phoneNumber;

    // 设备手机号
    @Setter
    @Getter
    private String authenticationCode;

    // 创建时间
    @Getter
    private final long creationTime;

    // 协议版本号
    @Getter
    private Integer protocolVersion;

    @Getter
    private Header header;

    protected Session(Channel channel) {
        this.channel = channel;
        this.creationTime = System.currentTimeMillis();
    }

    public void writeObject(Object message) {
        log.info("<<<<<<<<<< cmd{},{}", this, message);
        channel.writeAndFlush(message);
    }

    /**
     * 获得下一个流水号
     *
     * @return 流水号
     */
    public int nextSerialNo() {
        int current;
        int next;
        do {
            current = serialNo.get();
            next = current > 0xffff ? 0 : current;
        } while (!serialNo.compareAndSet(current, next + 1));
        return next;
    }

    /**
     * 注册session
     *
     * @param devId 设备ID
     */
    public void register(String devId, Integer version, Header header) {
        this.phoneNumber = devId;
        this.registered = true;
        this.protocolVersion = version;
        this.header = header;
        SessionManager.INSTANCE.put(devId, this);
    }

    @Override
    public String toString() {
        return "[" +
                "phoneNumber=" + phoneNumber +
                ", reg=" + registered +
                ", version=" + protocolVersion +
                ",ip=" + channel.remoteAddress() +
                ']';
    }

    public void unregister() {
        channel.close();
        SessionManager.INSTANCE.remove(this.phoneNumber);
    }

    public InetSocketAddress getLoadAddress() {
        return (InetSocketAddress)channel.localAddress();
    }
}
