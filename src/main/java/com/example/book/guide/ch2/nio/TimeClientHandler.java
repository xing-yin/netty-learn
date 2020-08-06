package com.example.book.guide.ch2.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Alan Yin
 * @date 2020/7/14
 */

public class TimeClientHandler implements Runnable {

    private String host;

    private int port;

    private Selector selector;

    private SocketChannel socketChannel;

    private volatile boolean stop;

    public TimeClientHandler(String host, int port) {
        this.host = host == null ? "127.0.0.1" : host;
        this.port = port;
        try {
            // 初始化 NIO 多路复用器和 socketChannel 对象，需要设为异步非阻塞
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        // 46-51：用于发送连接请求，示例无需重连，故放在循环前
        try {
            doConnect();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // 54-76：在循环体中轮询多路复用器 selector。有就绪 channel.执行 handleInput 方法
        while (!stop) {
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectionKeys.iterator();
                SelectionKey key = null;
                while (it.hasNext()) {
                    key = it.next();
                    it.remove();
                    try {
                        handleInput(key);
                    } catch (Exception e) {
                        if (key != null) {
                            key.cancel();
                            key.channel().close();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        // 79-86：退出循环，释放资源，优雅退出： jdk 底层会自动释放所有与此 selector 关联资源，无需重复释放
        // 多路复用器关闭后，所有注册在上面的 Channel 和 Pipe 等资源都会被自动去注册并关闭，所以不需要重复释放资源
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 先对 connect 操作判断：
    // - 若成功，则将socketChannel注册到多路复用器 selector，注册 SelectionKey.OP_READ
    // - 若没有成功。说明服务端没有返回 tcp 握手应答消息，但不代表连接失败，将socketChannel注册到多路复用器 selector，
    // 注册 SelectionKey.OP_CONNECT，当服务端返回 syn-ack 消息后，selector 就能轮询到
    private void doConnect() throws IOException {
        // 如果直接连接成功，则注册到多路复用器上，发送请求消息，读应答
        if (socketChannel.connect(new InetSocketAddress(host, port))) {
            socketChannel.register(selector, SelectionKey.OP_READ);
            doWrite(socketChannel);
        } else {
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }
    }

    private void handleInput(SelectionKey key) throws IOException {
        // 108-118：
        // 先对 SelectionKey 判断，看其状态：
        // - 若处于连接状态。则服务端已返回 ack 应答消息。此时对连接结果判断，调用 finishConnect，
        // 若为 true,客户端连接成功，若为 false 或异常，连接你截失败
        //  > 为 true,将 SocketChannel 注册到多路复用器上，注册 SelectionKey.OP_READ 操作位，监听网络读操作，然后发请求消息给服务器
        if (key.isValid()) {
            //判断是否连接成功
            SocketChannel sc = (SocketChannel) key.channel();
            if (key.isConnectable()) {
                if (sc.finishConnect()) {
                    sc.register(selector, SelectionKey.OP_READ);
                    doWrite(sc);
                } else {
                    // 连接失败，进程退出
                    System.exit(1);
                }
            }

            // 121-138: 分析客户端如何读取时间服务器应答：
            // - 若客户端接受服务端应答，则 SocketChannel 可读，事先无法预判应答码流大小，预分配 1MB 接收缓冲区用于读取应答消息
            // 调用 read 异步读取，若读到消息，对消息解码，打印结果，最后设置 stop,退出循环
            if (key.isReadable()) {
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    System.out.println("The time server receive order : " + body);
                    this.stop = true;
                } else if (readBytes < 0) {
                    // 对链路关闭
                    key.cancel();
                    sc.close();
                } else {
                    // 读到 0 字节，忽略
                }
            }
        }
    }

    // 构造请求消息体，然后编码，写入发送缓冲区，最后调用 SocketChannel 的 write 方法发送
    private void doWrite(SocketChannel channel) throws IOException {
        byte[] bytes = "QUERY TIME ORDER".getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
        writeBuffer.put(bytes);
        writeBuffer.flip();
        channel.write(writeBuffer);
        // 判断缓冲区的数据全部发送完成
        if (!writeBuffer.hasRemaining()) {
            System.out.println("Send order 2 server succeed.");
        }
    }
}
