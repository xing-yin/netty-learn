package com.example.book.guide.ch3.basis;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * netty 实现的服务端
 *
 * @author Alan Yin
 * @date 2020/7/15
 */

public class TimeServer {

    public static void main(String[] args) throws InterruptedException {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 采用默认值
            }
        }
        new TimeServer().bind(port);
    }

    /**
     * 绑定端口
     *
     * @param port
     */
    private void bind(int port) throws InterruptedException {
        // 36-40:NioEventLoopGroup 是线程组，包含一组 NIO 线程，用于网络事件处理，就是 Reactor 线程组
        // bossGroup 接收客户端连接；workerGroup 进行 SocketChannel 网络读写
        // 配置服务端的 NIO 线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 43-49： ServerBootstrap 是 Netty 启动 NIO 服务端的辅助启动类(降低开发难度)
            // group 方法传入线程组，接着创建 NioServerSocketChannel ，对应 jdk nio 中的 ServerSocketChannel
            // 然后配置 tcp 参数，最后绑定 I/O 事件处理类(类似 Reactor 模式中的 Handler 类)，用于处理网络 I/O 事件(如记录日志、消息编码)
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChildChannelHandler());

            // 绑定端口，同步等待成功，调用 sync 等待操作完成，ChannelFuture 类似于 jdk Future,用于异步操作通知回调
            ChannelFuture f = b.bind(port).sync();

            // 等待服务端监听端口关闭，等待服务端链路关闭后 main 退出
            f.channel().closeFuture().sync();
        } finally {
            // 优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new TimeServerHandler());
        }
    }
}
