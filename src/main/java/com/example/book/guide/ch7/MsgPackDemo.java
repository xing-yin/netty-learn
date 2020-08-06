package com.example.book.guide.ch7;

import org.msgpack.MessagePack;
import org.msgpack.template.Templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * MsgPack 使用示例
 *
 * @author Alan Yin
 * @date 2020/7/27
 */

public class MsgPackDemo {

    public static void main(String[] args) throws IOException {
        // create serialize objects
        List<String> src = new ArrayList<>();
        src.add("msgpack");
        src.add("alan");
        src.add("yin");

        MessagePack msgPack = new MessagePack();
        // serialize
        byte[] raw = msgPack.write(src);
        // Deserialize directly using a template
        List<String> dst = msgPack.read(raw, Templates.tList(Templates.TString));
        System.out.println(dst.get(0));
        System.out.println(dst.get(1));
        System.out.println(dst.get(2));
    }
}
