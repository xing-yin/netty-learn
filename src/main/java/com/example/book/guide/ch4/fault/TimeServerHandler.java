package com.example.book.guide.ch4.fault;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Alan Yin
 * @date 2020/7/16
 */

public class TimeServerHandler extends ChannelHandlerAdapter {

    private int counter;

    // 变化点：
    // 每读到一条消息后，计一次数，然后发送应答消息给客户端
    // 按照设计，服务端接收到的消息总数应该和客户端发送消息总数相同，而且请求消息删除回车换行符后应该为 "QUERY TIME ORDER"
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8")
                .substring(0, req.length - System.getProperty("line.separator").length());
        System.out.println("The time server receive order : " + body
                + " ; the counter is : " + ++counter);
        String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new java.util.Date(
                System.currentTimeMillis()).toString() : "BAD ORDER";
        currentTime = currentTime + System.getProperty("line.separator");
        ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
        ctx.writeAndFlush(resp);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}