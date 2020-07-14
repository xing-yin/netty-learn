package com.example.book.guide.ch2.bio;

/**
 * @author Alan Yin
 * @date 2020/7/13
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 客户端通过 socket 创建，发送查询时间服务器的指令，
 * 然后读取服务端响应并打印结果，随后关闭连接，释放资源，退出程序。
 */
public class TimeClient {

    public static void main(String[] args) throws IOException {
        int port = 8081;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 使用默认值
            }
        }
        Socket socket = null;
        BufferedReader in = null;
        PrintWriter out = null;

        // 常规写法
        try {
            socket = new Socket("127.0.0.1", port);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            // 向服务端发送"QUERY TIME ORDER"，然后通过 BufferedReader 的readLine 读取响应并打印
            out.println("QUERY TIME ORDER");
            System.out.println("Send order 2 server succeed.");
            String resp = in.readLine();
            System.out.println("Now is : " + resp);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                out = null;
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                in = null;
            }
            socket = null;
        }

//        // 简化写法
//        try (
//                Socket socket2 = new Socket("127.0.0.1", port);
//                BufferedReader in2 = new BufferedReader(new InputStreamReader(
//                        socket2.getInputStream()));
//                PrintWriter out2 = new PrintWriter(socket.getOutputStream(), true)) {
//            out2.println("QUERY TIME ORDER");
//            System.out.println("Send order 2 server succeed.");
//            String resp = in2.readLine();
//            System.out.println("Now is : " + resp);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
