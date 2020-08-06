package com.example.book.guide.ch8.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Alan Yin
 * @date 2020/7/31
 */
@Data
public class SubscribeReq implements Serializable {

    /**
     * 默认的序列号ID
     */
    private static final long serialVersionUID = 1L;

    private int subReqID;

    private String userName;

    private String productName;

    private String phoneNumber;

    private String address;

}
