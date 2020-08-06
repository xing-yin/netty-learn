package com.example.book.guide.ch6.serializable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 缺点1：序列化码流太大
 *
 * @author Alan Yin
 * @date 2020/7/23
 */

public class TestUserInfo {

    public static void main(String[] args) throws IOException {
        UserInfo info = new UserInfo();
        info.buildUserId(10).buildUsername("alan yin");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bos);
        os.writeObject(info);
        os.flush();
        os.close();

        byte[] b = bos.toByteArray();
        System.out.println("The jdk serializable length is:" + b.length);
        bos.close();
        System.out.println("============================");
        System.out.println("The byte array serializable length is : " + info.codeC().length);

        /**
         *
         The jdk serializable length is:125
         ============================
         The byte array serializable length is : 16
         */
    }
}
