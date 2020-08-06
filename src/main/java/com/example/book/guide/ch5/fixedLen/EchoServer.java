package com.example.book.guide.ch5.fixedLen;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 利用 FixedLengthFrameDecoder 解码器，无论一次收到多少数据报，都会按照设置的固定长度进行解码
 * 如果是半包消息，它会缓存半包消息并等待下个包到达后进行拼包，直到读到一个完整的包
 *
 * @author Alan Yin
 * @date 2020/7/17
 */

public class EchoServer {


    public static void main(String[] args) throws Exception {
        int port = 8086;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 采用默认值
            }
        }
        new EchoServer().bind(port);
    }

    private void bind(int port) throws InterruptedException {
        // 配置服务端的 NIO 线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            /// todo 将 FixedLengthFrameDecoder 添加到 pipeline 中,固定长度设置为 20
                            // 注释掉下面一行，就会出现服务端的粘包问题
                            ch.pipeline().addLast(new FixedLengthFrameDecoder(20));
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new EchoServerHandler());
                        }
                    });

            // 绑定端口，同步等待成功
            ChannelFuture f = b.bind(port).sync();

            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } finally {
            // 优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}

/// todo 用 telnet 命令模拟客户端操作
// 1.telnet localhost 8086  连接服务端
// 2.执行 Hi, Alan. Welcome to Netty   ===》按照 20 截断打印"Hi, Alan. Welcome to"
