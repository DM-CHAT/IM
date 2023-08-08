package com.mhhy.model.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;


@Data
@TableName("t_order")
public class OrderEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String userId;

    private String orderNo;

    private String tradeNo;

    private String payId;

    private String txid;

    private LocalDateTime createTime;

    private LocalDateTime completeTime;

    private int state;

    private int lifecycle;

    public JSONObject toJson(){
        JSONObject json = new JSONObject();
        json.put("user", userId);
        json.put("orderNo", orderNo);
        json.put("tradeNo", tradeNo);
        json.put("payId", payId);
        json.put("txid", txid);
        json.put("createTime", createTime);
        json.put("completeTime", completeTime);
        json.put("state", state);
        json.put("lifecycle", lifecycle);

        return json;
    }
}







