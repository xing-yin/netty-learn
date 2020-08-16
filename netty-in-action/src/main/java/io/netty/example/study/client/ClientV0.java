package io.netty.example.study.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.study.client.codec.OrderFrameDecoder;
import io.netty.example.study.client.codec.OrderFrameEncoder;
import io.netty.example.study.client.codec.OrderProtocolDecoder;
import io.netty.example.study.client.codec.OrderProtocolEncoder;
import io.netty.example.study.client.handler.ClientIdleCheckHandler;
import io.netty.example.study.client.handler.KeepaliveHandler;
import io.netty.example.study.common.RequestMessage;
import io.netty.example.study.common.auth.AuthOperation;
import io.netty.example.study.common.order.OrderOperation;
import io.netty.example.study.util.IdUtil;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;
import java.util.concurrent.ExecutionException;

public class ClientV0 {

    public static void main(String[] args) throws InterruptedException, ExecutionException, SSLException {

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        // 【参数调优】: 默认 30 s
        bootstrap.option(NioChannelOption.CONNECT_TIMEOUT_MILLIS, 10 * 1000);

        NioEventLoopGroup group = new NioEventLoopGroup();
        try {

            bootstrap.group(group);

            //【安全增强】: 启用空闲监测
            KeepaliveHandler keepaliveHandler = new KeepaliveHandler();
            LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);

            SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();
            //【安全增强】: 拿来即用的 SSL - 轻松融入案例
            // 下面这行，先直接信任自签证书，以避免没有看到ssl那节课程的同学运行不了；
            // 学完ssl那节后，可以去掉下面这行代码，安装证书，安装方法参考课程，执行命令参考 resources/ssl.txt里面
            sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
            SslContext sslContext = sslContextBuilder.build();

            /// todo 对于客户端来说，不区分 childHandler,都是 handler
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();

                    //【安全增强】: 启用空闲监测
                    pipeline.addLast(new ClientIdleCheckHandler());

                    //【安全增强】: 拿来即用的 SSL - 轻松融入案例
                    pipeline.addLast(sslContext.newHandler(ch.alloc()));

                    pipeline.addLast(new OrderFrameDecoder());
                    pipeline.addLast(new OrderFrameEncoder());
                    pipeline.addLast(new OrderProtocolEncoder());
                    pipeline.addLast(new OrderProtocolDecoder());

                    pipeline.addLast(loggingHandler);
                    pipeline.addLast(keepaliveHandler);

                }
            });

            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8090);

            channelFuture.sync();

            //【安全增强】: 自定义授权(仅存在admin)
            AuthOperation authOperation = new AuthOperation("admin", "password");
//            AuthOperation authOperation = new AuthOperation("admin2", "password"); // admin2 不存在，连接会失败
            channelFuture.channel().writeAndFlush(new RequestMessage(IdUtil.nextId(), authOperation));

            RequestMessage requestMessage = new RequestMessage(IdUtil.nextId(), new OrderOperation(1001, "tudou"));
            channelFuture.channel().writeAndFlush(requestMessage);

// ---------------------------------------------------------------------------------------------------
//            // 测试[内存泄漏]
//            // 1.Server 增加 jvm 参数 -Dio.netty.leakDetection.level=PARANOID
//            // 2. OrderServerProcessHandler 增加 ByteBuf memLeakByteBuf = ctx.alloc().buffer(); 不释放内存
//            for (int i = 0; i < 100000; i++) {
//                channelFuture.channel().writeAndFlush(requestMessage);
//            }
// ---------------------------------------------------------------------------------------------------

// ---------------------------------------------------------------------------------------------------
//            // 测试[IO密集型的独立线程池]
//            for (int i = 0; i < 20; i++) {
//                channelFuture.channel().writeAndFlush(requestMessage);
//            }
//            // 日志打印信息如下:
//            14:49:59 [business-4-2] OrderOperation: order's executing complete
//            14:49:59 [business-4-8] OrderOperation: order's executing complete
//            14:49:59 [business-4-10] OrderOperation: order's executing complete
//            14:49:59 [business-4-9] OrderOperation: order's executing complete
//            14:49:59 [business-4-7] OrderOperation: order's executing complete
//            14:49:59 [business-4-6] OrderOperation: order's executing complete
//            14:49:59 [business-4-5] OrderOperation: order's executing complete
//            14:49:59 [business-4-4] OrderOperation: order's executing complete
//            14:49:59 [business-4-1] OrderOperation: order's executing complete
//            14:49:59 [business-4-3] OrderOperation: order's executing complete
// ---------------------------------------------------------------------------------------------------

            channelFuture.channel().closeFuture().sync();

        } finally {
            group.shutdownGracefully();
        }
    }

}
