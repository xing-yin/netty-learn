package com.example.book.guide.ch5.delimiter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Alan Yin
 * @date 2020/7/22
 */
@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelHandlerAdapter {

    private int counter = 0;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // DelimiterBasedFrameDecoder 自动对请求消息进行了解码，后续的 ChannelHandler 接收到的 msg 就是完整的消息包
        // 第二个 ChannelHandler 是 StringDecoder，将 Bytebuf 解码成字符串对象
        // 第三个 EchoServerHandler 接收到的 msgh 就是解码后的字符串对象
        String body = (String) msg;
        System.out.println("This is " + ++counter + " times receive client : [" + body + "]");
        // 由于设置 DelimiterBasedFrameDecoder 过滤掉分隔符，所以返回客户端需要在请求消息尾部凭借分隔符 "$_"
        // 最后创建 Bytebuf,将原始消息重新返回客户端
        body += "$_";
        ByteBuf echo = Unpooled.copiedBuffer(body.getBytes());
        ctx.writeAndFlush(echo);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        // 发生异常，关闭链路
        ctx.close();
    }
}
