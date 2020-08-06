package com.example.book.guide.ch8.protobuf;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * Protobuf 版本图书订购客户端
 *
 * @author Alan Yin
 * @date 2020/7/31
 */

public class SubReqClient {

    public static void main(String[] args) throws Exception {
        int port = 8085;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 采用默认值
            }
        }
        new SubReqClient().connect(port, "127.0.0.1");
    }

    /// todo 注意：ProtobufDecoder 仅仅负责解码，不支持半包，因此在 ProtobufDecoder 前面一定要有能处理半包的解码器
    /// todo 有 3 种方式可以选择:
    /// todo 方式1: 使用 netty 提供的 ProtobufVarint32FrameDecoder ，可以处理半包消息
    /// todo 方式2: 使用 netty 提供的通用半包解码器 LengthFieldBasedFrameDecoder
    /// todo 方式3: 继承 MessageToMessageDecoder ,自己处理半包消息
    private void connect(int port, String host) throws InterruptedException {
        // 配置客户端 NIO 线程组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            // ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2)));
                            /// todo 客户端需要解码的对象是订购响应，所以使用 SubscribeRespProto.SubscribeResp 作为入参
                            ch.pipeline().addLast(new ProtobufDecoder(
                                    SubscribeRespProto.SubscribeResp.getDefaultInstance()));
                            ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            ch.pipeline().addLast(new ProtobufEncoder());
                            ch.pipeline().addLast(new SubReqClientHandler());
                        }
                    });

            // 发起异步连接操作
            ChannelFuture f = b.connect(host, port).sync();

            // 等待客户端链路关闭
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

}
