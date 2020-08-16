package io.netty.example.study.client.codec;


import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 第3步：对客户端来说，此时收到服务端响应，先进行解码，处理粘包半包
 */
public class OrderFrameDecoder extends LengthFieldBasedFrameDecoder {
    public OrderFrameDecoder() {
        super(Integer.MAX_VALUE, 0, 2, 0, 2);
    }
}
