package io.netty.example.study.client.codec;


import io.netty.handler.codec.LengthFieldPrepender;

/**
 * 第2步：对客户端来说，解决粘包半包
 */
public class OrderFrameEncoder extends LengthFieldPrepender {
    public OrderFrameEncoder() {
        super(2);
    }
}
