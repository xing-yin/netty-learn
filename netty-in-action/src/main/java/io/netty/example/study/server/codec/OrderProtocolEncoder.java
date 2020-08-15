package io.netty.example.study.server.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.example.study.common.ResponseMessage;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * 第4步: handler 发出去的内容(如 OrderServerProcessHandler)进行编码，转化为 ByteBuf
 */
public class OrderProtocolEncoder extends MessageToMessageEncoder<ResponseMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ResponseMessage responseMessage, List<Object> out) throws Exception {
        // 分配一个 ByteBuf(固定用法)
        ByteBuf buffer = ctx.alloc().buffer();
        responseMessage.encode(buffer);
        out.add(buffer);
    }
}
