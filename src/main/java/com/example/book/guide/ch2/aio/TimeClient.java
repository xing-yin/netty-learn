package com.example.book.guide.ch2.aio;

/**
 * @author Alan Yin
 * @date 2020/7/14
 */

public class TimeClient {

    public static void main(String[] args) {
        int port = 8081;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 使用默认值
            }
        }
        // 通过独立线程创建异步客户端 handler:实际项目不需要独立线程创建异步连接对象，因为底层有 jdk 系统回调实现
        new Thread(new AsyncTimeClientHandler("127.0.0.1", port),
                "AIO-AsyncTimeClientHandler-001").start();
    }
}
