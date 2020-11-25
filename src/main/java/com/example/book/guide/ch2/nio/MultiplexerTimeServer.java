package com.example.book.guide.ch2.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * 多路复用器类
 *
 * @author Alan Yin
 * @date 2020/7/13
 */

public class MultiplexerTimeServer implements Runnable {

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

    private volatile boolean stop;

    /**
     * 资源初始化:初始化多路复用器、绑定端口
     * <p>
     * 创建多路复用器 selector、serverSocketChannel，对 channel 和 tcp 参数进行配置
     *
     * @param port
     */
    public MultiplexerTimeServer(int port) {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(port), 1024);
            // 将 serverSocketChannel 注册到 selector
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("The time server is start in port : " + port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public boolean isStop() {
        return stop;
    }

    @Override
    public void run() {

        // 55-78：在线程的 while 循环中循环遍历 selector,休眠时间为 1s。
        // 无论是否有读写事件，selector 每隔 1秒被唤醒一次。
        // 当有处于就绪状态的 channel 时，selector 将返回该 channel 的 SelectionKey 集合，通过对 channel 集合迭代，进行网路异步读写操作。
        while (!stop) {
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                SelectionKey key = null;
                while (iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();
                    try {
                        handleInput(key);
                    } catch (Exception e) {
                        if (key != null) {
                            key.cancel();
                            if (key.channel() != null) {
                                key.channel().close();
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        // 多路复用器关闭后，所有注册在上面的 Channel 和 Pipe 等资源都会被自动去注册并关闭，所以不需要重复释放资源
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {

            // 96-105：处理新接入的请求消息,根据 SelectionKey 操作位判断即可获知网络事件类型(如读)，
            // 通过 ServerSocketChannel 的 accept 接收客户端连接请求并创建 SocketChannel 实例。
            // 完成这些步骤后，相当于完成 tcp 三次握手，tcp 物理链路正式建立。
            // ⚠️注意：要将新创建的 SocketChannel 设为异步非阻塞，同时可以设置 tcp 参数(如缓冲区大小)。

            // 处理新接入的请求消息
            if (key.isAcceptable()) {
                // Accept the new connection
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel sc = ssc.accept();
                sc.configureBlocking(false);
                // Add the new connection to the selector
                sc.register(selector, SelectionKey.OP_READ);
            }

            // 111-137: 读取客户端请求消息，首先创建一个 ByteBuffer(此处为 1M）缓冲区。
            // 然后调用 SocketChannel 的 read 方法读取请求码流。
            // 注意！由于 SocketChannel 已设为异步非阻塞，因此它的 read 非阻塞。使用返回值判断，看读到的字节数，有 3种可能：
            // a.返回值>0:读到了字节，对字节进行编解码
            // b.返回值=0: 没有读取到字节，正常场景，忽略
            // c.返回值为 -1:链路已关闭，需要关闭 SocketChannel，释放资源
            // 读取到码流后，进行解码。先对 readBuffer flip，然后根据缓冲区可读字节个数创建字节数组，
            // 调用 ByteBuffer 的 get 操作将缓冲区可读字节数组复制到新创建的字节数组中，最后调用字符串的构造函数创建请求消息体
            if (key.isReadable()) {
                // Read the data
                SocketChannel sc = (SocketChannel) key.channel();
                // 1M
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    /// todo flip：作用是将缓冲区当前的 limit 设置为 position, position 设置为 0，用于后续对缓冲区的读取操作。
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, StandardCharsets.UTF_8);
                    System.out.println("The time server receive order : " + body);
                    String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body)
                            ? new java.util.Date(System.currentTimeMillis()).toString()
                            : "BAD ORDER";
                    // 异步发送客户端
                    doWrite(sc, currentTime);
                } else if (readBytes < 0) {
                    // 对端链路关闭
                    key.cancel();
                    sc.close();
                } else {
                    // 读到 0字节，忽略
                    ;
                }
            }
        }
    }

    /**
     * 将应答消息异步发送客户端
     *
     * @param channel
     * @param response
     * @throws IOException
     */
    private void doWrite(SocketChannel channel, String response) throws IOException {
        // 先将字符串编码成字节数组，创建 ByteBuffer 并将字节数组复制到缓冲区中，然后对缓冲区 flip,
        // 最后调用 SocketChannel 的 write 方法将缓冲区中的字节数组发送出去。
        // 注意！由于 SocketChannel 是异步非阻塞，不保证一次能把需要发送的字节数组发送完，会出现"写半包"问题。
        if (response != null && response.trim().length() > 0) {
            byte[] bytes = response.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();
            channel.write(writeBuffer);
        }
    }
}
