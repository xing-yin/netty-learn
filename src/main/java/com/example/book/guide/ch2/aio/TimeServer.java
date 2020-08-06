package com.example.book.guide.ch2.aio;

import java.io.IOException;

/**
 * 异步 I/O 的 TimeServer
 *
 * @author Alan Yin
 * @date 2020/7/14
 */

public class TimeServer {

    public static void main(String[] args) throws IOException {
        int port = 8081;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 采用默认值
            }
        }
        // 首先创建异步时间服务处理类，然后启动线程将 AsyncTimeServerHandler 拉起，见 AsyncTimeServerHandler
        AsyncTimeServerHandler timeServerHandler = new AsyncTimeServerHandler(port);
        // 实际项目不需要启动独立线程处理 AsyncTimeServerHandler
        new Thread(timeServerHandler, "AIO-AsyncTimeServerHandler-001").start();
    }
}
