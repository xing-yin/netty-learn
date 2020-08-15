package io.netty.example.study.server.codec;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 第1步: 处理 tcp 协议的粘包半包（固定长度）
 */
public class OrderFrameDecoder extends LengthFieldBasedFrameDecoder {

    public OrderFrameDecoder() {
        super(10240, 0, 2, 0, 2);
    }

}
