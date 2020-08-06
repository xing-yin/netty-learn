package com.example.book.guide.ch7;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.msgpack.MessagePack;

import java.util.List;

/**
 * Msgpack 解码器
 *
 * @author Alan Yin
 * @date 2020/7/27
 */

public class MsgpackDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        final byte[] array;
        final int length = msg.readableBytes();
        array = new byte[length];
        // todo 先从数据报 msg 中获取需要解码的 byte 数组，然后调用 messagePack 的 read 方法将其反序列化为 object 对象，
        // 将解码后的对象加入到解码列表 out 中，从而完成解码操作。
        msg.getBytes(msg.readerIndex(), array, 0, length);
        MessagePack messagePack = new MessagePack();
        out.add(messagePack.read(array));
    }
}
