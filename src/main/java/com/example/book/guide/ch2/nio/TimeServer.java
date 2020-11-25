package com.example.book.guide.ch2.nio;

/**
 * NIO 创建的 TimeServer
 *
 * @author Alan Yin
 * @date 2020/7/13
 */

public class TimeServer {

    public static void main(String[] args) {
        int port = 8081;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 使用默认值
            }
        }

        // 创建多路复用类，是一个独立线程，负责轮询多路复用器 selector,可以处理多个客户端的并发接入
        MultiplexerTimeServer timeServer = new MultiplexerTimeServer(port);
        new Thread(timeServer, "NIO-MultiplexerTimeServer_001").start();
    }
}
