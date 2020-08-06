package com.example.book.guide.ch7;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

/**
 * Msgpack 编码器
 * <p>
 * MsgpackEncoder 继承 MessageToByteEncoder，
 * 负责将 Object 类型的 pojo 对象编码为 byte 数组，然后写入 ByteBuf
 *
 * @author Alan Yin
 * @date 2020/7/27
 */

public class MsgpackEncoder extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        MessagePack messagePack = new MessagePack();
        // Serialize
        byte[] raw = messagePack.write(msg);
        out.writeBytes(raw);
    }
}
