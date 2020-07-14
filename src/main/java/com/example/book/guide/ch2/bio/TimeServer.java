package com.example.book.guide.ch2.bio;

import com.example.book.guide.ch2.TimeServerHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 同步阻塞 I/O 的 TimeServer
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
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("The time server is start in port:" + port);
            Socket socket = null;
            // 无限循环监听客户端连接：若没有连接，主线程阻塞在 serverSocket 的 accept 操作上
            while (true) {
                socket = serverSocket.accept();
                new Thread(new TimeServerHandler(socket)).start();
            }
        } finally {
            if (serverSocket != null) {
                System.out.println("The time server close");
                serverSocket.close();
                serverSocket = null;
            }
        }
    }

}
