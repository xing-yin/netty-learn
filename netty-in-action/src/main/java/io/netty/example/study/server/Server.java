package io.netty.example.study.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.study.server.codec.OrderFrameDecoder;
import io.netty.example.study.server.codec.OrderFrameEncoder;
import io.netty.example.study.server.codec.OrderProtocolDecoder;
import io.netty.example.study.server.codec.OrderProtocolEncoder;
import io.netty.example.study.server.handler.AuthHandler;
import io.netty.example.study.server.handler.MetricsHandler;
import io.netty.example.study.server.handler.OrderServerProcessHandler;
import io.netty.example.study.server.handler.ServerIdleCheckHandler;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.IpSubnetFilterRule;
import io.netty.handler.ipfilter.RuleBasedIpFilter;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;

/**
 * 实现步骤1：4(codec 目录)+1(handler,如 OrderServerProcessHandler)
 * 实现步骤2：实现 server
 */
@Slf4j
public class Server {

    public static void main(String[] args) throws InterruptedException, ExecutionException, CertificateException, SSLException {

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap.channel(NioServerSocketChannel.class);
        // todo: 系统必调参数: SO_BACKLOG TCP_NODELAY
        serverBootstrap.option(NioChannelOption.SO_BACKLOG, 1024);
        serverBootstrap.childOption(NioChannelOption.TCP_NODELAY, true);
        // 使用 Netty 自带的日志 handler(便于观察启动情况)
        serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));

        //【跟踪诊断】：完善“线程名” (thread)
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("boss"));
        NioEventLoopGroup workGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("worker"));
        // 【IO密集型任务】使用独立的线程池处理，如 UnorderedThreadPoolEventExecutor， 线程池被业务处理程序共享
        /// todo 为什么独立线程池不用 NioEventLoopGroup，而用 UnorderedThreadPoolEventExecutor？
        // 因为 NioEventLoopGroup 源码中单个线程设置为 true 时，仅使用同一个线程；
        // UnorderedThreadPoolEventExecutor 的 EventExecutor next()方法是返回自身(不是其中线程)
        UnorderedThreadPoolEventExecutor businessGroup = new UnorderedThreadPoolEventExecutor(10, new DefaultThreadFactory("business"));
        NioEventLoopGroup eventLoopGroupForTrafficShaping = new NioEventLoopGroup(0, new DefaultThreadFactory("TS"));

        try {
            serverBootstrap.group(bossGroup, workGroup);

            //【跟踪诊断】：应用可视化
            //metrics
            MetricsHandler metricsHandler = new MetricsHandler();

            // 【优化使用】：使用流量整形
            //trafficShaping
            GlobalTrafficShapingHandler globalTrafficShapingHandler = new GlobalTrafficShapingHandler(eventLoopGroupForTrafficShaping, 10 * 1024 * 1024, 10 * 1024 * 1024);

            //【安全增强】:简单有效的黑白名单
            //ipfilter
            // 黑名单测试
//             IpSubnetFilterRule ipSubnetFilterRule = new IpSubnetFilterRule("127.0.0.1", 16, IpFilterRuleType.REJECT);
            IpSubnetFilterRule ipSubnetFilterRule = new IpSubnetFilterRule("127.1.1.1", 16, IpFilterRuleType.REJECT);
            RuleBasedIpFilter ruleBasedIpFilter = new RuleBasedIpFilter(ipSubnetFilterRule);

            //【安全增强】: 少不了的自定义授权
            //auth
            AuthHandler authHandler = new AuthHandler();

            //【安全增强】: 拿来即用的 SSL - 轻松融入案例
            //ssl
            SelfSignedCertificate selfSignedCertificate = new SelfSignedCertificate();
            // 打印证书位置
            log.info("certificate position:" + selfSignedCertificate.certificate().toString());
            SslContext sslContext = SslContextBuilder.forServer(selfSignedCertificate.certificate(),
                    selfSignedCertificate.privateKey()).build();

            //log
            //【跟踪诊断】：使用好 Netty 的日志
            LoggingHandler debugLogHandler = new LoggingHandler(LogLevel.DEBUG);
            LoggingHandler infoLogHandler = new LoggingHandler(LogLevel.INFO);

            serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();

                    //【跟踪诊断】：完善 “Handler ”名称
                    pipeline.addLast("debegLog", debugLogHandler);

                    // 【安全增强】:简单有效的黑白名单
                    pipeline.addLast("ipFilter", ruleBasedIpFilter);

                    //【优化使用】：使用流量整形
                    pipeline.addLast("tsHandler", globalTrafficShapingHandler);

                    //【跟踪诊断】：应用可视化
                    pipeline.addLast("metricHandler", metricsHandler);

                    // 【安全增强】: 启用空闲监测（不共享）
                    pipeline.addLast("idleHandler", new ServerIdleCheckHandler());

                    //【安全增强】: 拿来即用的 SSL - 轻松融入案例
                    // todo 注意 ssl 加入的位置
                    pipeline.addLast("ssl", sslContext.newHandler(ch.alloc()));

                    // todo 需要注意添加的顺序(顺序不对就不 work)
                    // todo OrderFrameDecoder-> OrderProtocolDecoder
                    //  -> OrderServerProcessHandler -> OrderProtocolDecoder ->OrderFrameEncoder
                    pipeline.addLast("frameDecoder", new OrderFrameDecoder());
                    pipeline.addLast("frameEncoder", new OrderFrameEncoder());
                    pipeline.addLast("protocolDecoder", new OrderProtocolDecoder());
                    pipeline.addLast("protocolEncoder", new OrderProtocolEncoder());

                    pipeline.addLast("infolog", infoLogHandler);

                    // 【优化使用】: 方式2 -增强写，延迟与吞吐量的抉择 （FlushConsolidationHandler 没有标记为 sharable,不能提到外面去）
                    pipeline.addLast("flushEnhance", new FlushConsolidationHandler(10, true));

                    //【安全增强】: 少不了的自定义授权
                    /// todo 注意：authHandler 需要放在 OrderProtocolEncoder 之后，因为接收的参数(泛型)为 RequestMessage
                    pipeline.addLast("auth", authHandler);

                    // 【IO密集型任务】独立的线程池处理
                    pipeline.addLast(businessGroup, new OrderServerProcessHandler());
//                    // 【IO密集型任务】错误用法：
//                    pipeline.addLast(eventLoopGroupForTrafficShaping, new OrderServerProcessHandler());
//                    // 日志打印：一直使用同一个线程，没有使用上线程池中的多线程
//                    // 14:57:01 [business-4-9] OrderOperation: order's executing startup with orderRequest: OrderOperation(tableId=1001, dish=tudou)
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
