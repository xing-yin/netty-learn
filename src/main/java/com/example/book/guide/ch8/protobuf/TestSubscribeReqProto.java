package com.example.book.guide.ch8.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alan Yin
 * @date 2020/7/31
 */

public class TestSubscribeReqProto {

    public static void main(String[] args) throws InvalidProtocolBufferException {
        SubscribeReqProto.SubscribeReq req = createSubscribeReq();
        System.out.println("Before encode: " + req.toString());
        SubscribeReqProto.SubscribeReq req2 = decode(encode(req));
        System.out.println("after decode: " + req2.toString());
        System.out.println("Assert equal : req == req2" + req.equals(req2));
    }

    private static SubscribeReqProto.SubscribeReq decode(byte[] body) throws InvalidProtocolBufferException {
        // 将二进制 byte 数组解码为原始对象
        return SubscribeReqProto.SubscribeReq.parseFrom(body);
    }

    private static byte[] encode(SubscribeReqProto.SubscribeReq req) {
        return req.toByteArray();
    }

    private static SubscribeReqProto.SubscribeReq createSubscribeReq() {
        // 构架一个 builder 实例
        SubscribeReqProto.SubscribeReq.Builder builder = SubscribeReqProto.SubscribeReq.newBuilder();
        // 通过 builder 对 SubscribeReq 属性进行设置
        builder.setSubReqID(1);
        builder.setUserName("Alan");
        builder.setProductName("netty book");
        List<String> address = new ArrayList<>();
        address.add("bj");
        address.add("sh");
        address.add("hz");
        builder.addAllAddress(address);
        return builder.build();
    }


}
