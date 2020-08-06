package com.example.book.guide.ch4.correct;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 支持 TCP 粘包的 TimeServerHandler
 *
 * @author Alan Yin
 * @date 2020/7/16
 */

public class TimeServerHandler extends ChannelHandlerAdapter {

    private int counter;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String body = (String) msg;
        ///todo 不同点: 可以发现接收到 msg 后就是删除回车换行符后的请求消息，
        // 无需额外考虑处理半包问题，也不需要对请求消息进行编码，非常简洁(思考一个问题：为啥解决半包问题能生效？)
        System.out.println("The time server receive order : " + body + ";the counter is:" + ++counter);
        String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new java.util.Date(
                System.currentTimeMillis()).toString() : "BAD ORDER";
        currentTime = currentTime + System.getProperty("line.separator");
        ByteBuf response = Unpooled.copiedBuffer(currentTime.getBytes());
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
