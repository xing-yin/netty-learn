package com.example.book.guide.ch2.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

/**
 * @author Alan Yin
 * @date 2020/7/14
 */

public class AsyncTimeServerHandler implements Runnable {

    private int port;

    CountDownLatch latch;

    AsynchronousServerSocketChannel asynchronousServerSocketChannel;

    public AsyncTimeServerHandler(int port) {
        this.port = port;
        try {
            // 创建一个 AsynchronousServerSocketChannel，绑定监听端口
            asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
            asynchronousServerSocketChannel.bind(new InetSocketAddress(port));
            System.out.println("The time server is start in port : " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // 初始化 CountDownLatch，作用在完成一组操作之前，允许当前线程一直阻塞
        // 本例让线程在此阻塞，防止服务端执行完退出
        latch = new CountDownLatch(1);
        doAccept();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doAccept() {
        // 接收客户端连接，因为异步，可以传递 CompletionHandler 实例(AcceptCompletionHandler)接收 accept 操作成功通知消息
        asynchronousServerSocketChannel.accept(this, new AcceptCompletionHandler());
    }
}
