package com.example.book.guide.ch6.serializable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

/**
 * 缺点2:
 *
 * @author Alan Yin
 * @date 2020/7/23
 */

public class PerformTestUserInfo {

    public static void main(String[] args) throws IOException {
        UserInfo info = new UserInfo();
        info.buildUserId(10).buildUsername("alan yin");
        int loop = 1000_000;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream os = null;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            bos = new ByteArrayOutputStream();
            os = new ObjectOutputStream(bos);
            os.writeObject(info);
            os.flush();
            os.close();
            // java 序列化
            byte[] b = bos.toByteArray();
            bos.close();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("The jdk serializable cost time is  :  " + (endTime - startTime) + " ms");

        System.out.println("-----------------");

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            // 二进制编码
            byte[] b = info.codeC(buffer);
        }
        endTime = System.currentTimeMillis();
        System.out.println("The byte array serializable cost time is  : " + (endTime - startTime) + " ms");

        /**
         *
         The jdk serializable cost time is  : 1601 ms
         -----------------
         The byte array serializable cost time is  : 114 ms
         */

    }
}
