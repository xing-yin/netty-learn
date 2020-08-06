package com.example.book.guide.ch8.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Alan Yin
 * @date 2020/7/31
 */
@Data
public class SubscribeResp implements Serializable {

    /**
     * 默认序列ID
     */
    private static final long serialVersionUID = 2L;

    private int subReqID;

    private int respCode;

    private String desc;
}
