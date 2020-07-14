package com.example.book.guide.ch2.pio;

import com.example.book.guide.ch2.TimeServerHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 伪异步 I/O 模式
 *
 * 创建一个线程池，收到新客户端连接时，将请求 socket 封装成一个 task,然后调用线程池的 execute 方法执行
 *
 * @author Alan Yin
 * @date 2020/7/13
 */

public class TimeServer {

    public static void main(String[] args) throws IOException {
        int port = 8081;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 使用默认值
            }
        }

        try (ServerSocket serverSocket = new ServerSocket(port);) {
            System.out.println("The time server is start in port : " + port);
            Socket socket = null;
            // [key-code]创建 IO 任务线程池
            TimeServerHandlerExecutePool singleExecutor = new TimeServerHandlerExecutePool(
                    50, 10000);
            while (true) {
                socket = serverSocket.accept();
                singleExecutor.execute(new TimeServerHandler(socket));
            }
        }
    }
}
