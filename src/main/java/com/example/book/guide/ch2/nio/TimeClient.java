package com.example.book.guide.ch2.nio;

/**
 * 时间处理客户端
 *
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

        new Thread(new TimeClientHandler("127.0.0.1", port), "TimeClient-001")
                .start();
    }
}
