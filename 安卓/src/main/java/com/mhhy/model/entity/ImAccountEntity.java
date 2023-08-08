package com.mhhy.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("im_account")
public class ImAccountEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("osn id")
    private String osnId;

    @ApiModelProperty("ip")
    private String ip;

    @ApiModelProperty("1：管理员\\r\\n2：客服\\r\\n3：普通用户")
    private Integer state = 0;

}
