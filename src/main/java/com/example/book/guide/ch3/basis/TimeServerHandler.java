package com.example.book.guide.ch3.basis;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 继承 ChannelHandlerAdapter，对网络事件读写
 *
 * @author Alan Yin
 * @date 2020/7/15
 */

public class TimeServerHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // ByteBuf 类似 jdk ByteBuffer,不过更强大。
        // 用 readableBytes 获取缓冲区可读字节数，根据可读字节数创建 byte 数组，再将缓冲区字节数组复制到新建 bytes 中
        ByteBuf buf = (ByteBuf) msg;
        byte[] request = new byte[buf.readableBytes()];
        buf.readBytes(request);
        String body = new String(request, "UTF-8");
        System.out.println("The time server receive order : " + body);
        String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new java.util.Date(
                System.currentTimeMillis()).toString() : "BAD ORDER";
        ByteBuf response = Unpooled.copiedBuffer(currentTime.getBytes());
        ctx.write(response);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 作用:将消息发送队列中消息写入 SocketChannel 中发送给对方。
        // 为了防止频繁唤醒 Selector 发消息， netty write 方法不直接将消息写 SocketChannel,
        // 调用 write 只是把待发送消息发送缓冲数组，再调用 flush 将缓冲区消息写 SocketChannel。
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 发生异常，关闭 ChannelHandlerContext，释放句柄等资源
        ctx.close();
    }
}
