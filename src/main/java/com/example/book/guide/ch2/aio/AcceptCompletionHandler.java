package com.example.book.guide.ch2.aio;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @author Alan Yin
 * @date 2020/7/14
 */

public class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, AsyncTimeServerHandler> {

    @Override
    public void completed(AsynchronousSocketChannel result, AsyncTimeServerHandler attachment) {
        // 从 attachment 获取 asynchronousServerSocketChannel，然后继续调用 accept
        // 为什么接收成功还要再调？上一步调用 accept 后，若有新客户端接入，系统会回调传入的 CompletionHandler的 completed方法，表示新客户端已经接入成功。
        // 因为一个 AsynchronousSocketChannel 可以接收上万个客户端，所以需要继续调用它的 accept 方法，接收其他客户端连接，最终形成循环。
        // ====> 即每接收一个客户读连接成功后，再异步接收新客户端连接。
        attachment.asynchronousServerSocketChannel.accept(attachment, this);
        // 链路建立成功后，服务端需接收客户端请求信息，新建 buffer
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // 异步读取，
        // - 参数 ByteBuffer：接收缓冲区，从异步 channel 读取数据包；
        // - 参数 attachment：异步 channel 携带的附件，通知回调时作为入参使用
        // - 参数 CompletionHandler：接收通知回调的业务 handler(如 ReadCompletionHandler)
        result.read(buffer, buffer, new ReadCompletionHandler(result));
    }

    @Override
    public void failed(Throwable exc, AsyncTimeServerHandler attachment) {
        exc.printStackTrace();
        attachment.latch.countDown();
    }
}
