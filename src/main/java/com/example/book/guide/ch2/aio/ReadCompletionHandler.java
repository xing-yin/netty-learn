package com.example.book.guide.ch2.aio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @author Alan Yin
 * @date 2020/7/14
 */

public class ReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {

    private AsynchronousSocketChannel socketChannel;

    public ReadCompletionHandler(AsynchronousSocketChannel channel) {
        if (channel == null) {
            // 传递 AsynchronousSocketChannel 用于读取半包消息和发送应答
            this.socketChannel = channel;
        }
    }

    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        // 28-40：读取到消息后处理
        // 先对 attachment filp,为后续从缓冲区读数据做准备
        attachment.flip();
        // 创建合适数组，通过 new String 创建请求消息，对消息判断，若为 "QUERY TIME ORDER"则获取当前时间，调用 doWrite 发送客户端
        byte[] body = new byte[attachment.remaining()];
        attachment.get(body);
        try {
            String req = new String(body, "UTF-8");
            System.out.println("The time server receive order : " + req);
            String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(req) ? new java.util.Date(
                    System.currentTimeMillis()).toString() : "BAD ORDER";
            doWrite(currentTime);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void doWrite(String currentTime) {
        if (currentTime != null && currentTime.trim().length() > 0) {
            // 调用 AsynchronousSocketChannel 异步 write 方法，有 3个参数（参照 AcceptCompletionHandler）
            byte[] bytes = (currentTime).getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();
            // 实现 write 异步回调 CompletionHandler
            socketChannel.write(writeBuffer, writeBuffer,
                    new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer byteBuffer) {
                            // 如果没有发送完成，继续发送
                            if (byteBuffer.hasRemaining()) {
                                socketChannel.write(byteBuffer, byteBuffer, this);
                            }
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            try {
                                socketChannel.close();
                            } catch (IOException e) {
                                // ignore on close
                            }
                        }
                    });
        }
    }

    // 发生异常是，对异常判断，若是 I/O 异常，就关闭链路，释放资源；若是其他异常，按照业务自己处理
    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        try {
            this.socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
