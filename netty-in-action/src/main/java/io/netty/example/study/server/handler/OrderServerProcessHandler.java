package io.netty.example.study.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.example.study.common.Operation;
import io.netty.example.study.common.OperationResult;
import io.netty.example.study.common.RequestMessage;
import io.netty.example.study.common.ResponseMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * 第3步: handler 进行业务处理
 * SimpleChannelInboundHandler 会自动释放 ByteBuf
 */
@Slf4j
public class OrderServerProcessHandler extends SimpleChannelInboundHandler<RequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestMessage requestMessage) throws Exception {
        // 测试内存泄漏
        // ByteBuf memLeakByteBuf = ctx.alloc().buffer();

        Operation operation = requestMessage.getMessageBody();
        OperationResult operationResult = operation.execute();

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessageHeader(requestMessage.getMessageHeader());
        responseMessage.setMessageBody(operationResult);

        // 【安全增强】: 设置“高低水位线”等保护好自己(关键是应用程序自己判断)
        if (ctx.channel().isActive() && ctx.channel().isWritable()) {
            // 处理完后发出去
            ctx.writeAndFlush(responseMessage);
        } else {
            // 用日志标记一下不可读，避免 OOM
            log.error("not writable now, message dropped");
        }
    }


}
