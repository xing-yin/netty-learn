package io.netty.example.study.client.handler;

import io.netty.handler.timeout.IdleStateHandler;

/**
 * 【安全增强】: 启用空闲监测
 */
public class ClientIdleCheckHandler extends IdleStateHandler {

    public ClientIdleCheckHandler() {
        // 客户端 5s 不发送数据就发一个 keepalive
        super(0, 5, 0);
    }

}
