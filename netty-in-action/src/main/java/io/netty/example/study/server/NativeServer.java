package io.netty.example.study.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.example.study.server.codec.OrderFrameDecoder;
import io.netty.example.study.server.codec.OrderFrameEncoder;
import io.netty.example.study.server.codec.OrderProtocolDecoder;
import io.netty.example.study.server.codec.OrderProtocolEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;

/**
 * 优化使用: 为不同平台开启 Native
 * <p>
 * 如将 Epoll 替换为 Epoll
 * 备注:此处仅仅做演示，代码无法运行，因为替换后只能运行在 linux 系统中
 */
@Slf4j
public class NativeServer {

    public static void main(String[] args) throws InterruptedException, ExecutionException, CertificateException, SSLException {

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap.channel(EpollServerSocketChannel.class);
        serverBootstrap.option(EpollChannelOption.SO_BACKLOG, 1024);
        serverBootstrap.childOption(EpollChannelOption.TCP_NODELAY, true);
        // 使用 Netty 自带的日志 handler(便于观察启动情况)
        serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));

        //【跟踪诊断】：完善“线程名” (thread)
        EpollEventLoopGroup bossGroup = new EpollEventLoopGroup(0, new DefaultThreadFactory("boss"));
        EpollEventLoopGroup workGroup = new EpollEventLoopGroup(0, new DefaultThreadFactory("worker"));
        UnorderedThreadPoolEventExecutor businessGroup = new UnorderedThreadPoolEventExecutor(10, new DefaultThreadFactory("business"));
        EpollEventLoopGroup eventLoopGroupForTrafficShaping = new EpollEventLoopGroup(0, new DefaultThreadFactory("TS"));

        try {
            serverBootstrap.group(bossGroup, workGroup);

            //log
            //【跟踪诊断】：使用好 Netty 的日志
            LoggingHandler debugLogHandler = new LoggingHandler(LogLevel.DEBUG);
            LoggingHandler infoLogHandler = new LoggingHandler(LogLevel.INFO);

            serverBootstrap.childHandler(new ChannelInitializer<EpollSocketChannel>() {
                @Override
                protected void initChannel(EpollSocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    //  -> OrderServerProcessHandler -> OrderProtocolDecoder ->OrderFrameEncoder
                    pipeline.addLast("frameDecoder", new OrderFrameDecoder());
                    pipeline.addLast("frameEncoder", new OrderFrameEncoder());
                    pipeline.addLast("protocolDecoder", new OrderProtocolDecoder());
                    pipeline.addLast("protocolEncoder", new OrderProtocolEncoder());

                    pipeline.addLast("infolog", infoLogHandler);

                }
            });

            ChannelFuture channelFuture = serverBootstrap.bind(8090).sync();

            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
            businessGroup.shutdownGracefully();
            eventLoopGroupForTrafficShaping.shutdownGracefully();
        }

    }

}
