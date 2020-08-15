package io.netty.example.study.common;

import lombok.Data;

/**
 * 参见数据结构定义
 */
@Data
public class MessageHeader {

    private int version = 1;
    private int opCode;
    private long streamId;

}
