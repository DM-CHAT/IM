package com.mhhy.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;

import java.util.Date;

@Data
@TableName("t_server")
public class ServerInfoEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String iconUrl;

    private String serviceName;

    private String url;

    private String serviceRemark;

    private String appRemark;

    private String appIntroduction;

    //private DateTimeLiteralExpression.DateTime createTime;
}
