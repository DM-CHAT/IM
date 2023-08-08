package com.mhhy.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_user")
public class UserEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("im id")
    private String imId;

    @ApiModelProperty("im用户名")
    private String imUsername;

    @ApiModelProperty("im密码")
    private String imPwd;

    @ApiModelProperty("im node")
    private String imNode;

    @ApiModelProperty("nick name")
    private String nickName;

    @ApiModelProperty("1：管理员\\r\\n2：客服\\r\\n3：普通用户")
    private Integer state;

    @ApiModelProperty("0：未启用\\r\\n1：正常\\r\\n2：禁用")
    private Integer type;

    private Date createTime;


    public boolean isSetPassword() {
        if (password == null) {
            return false;
        }
        if (password.length() == 0) {
            return false;
        }
        return true;
    }



}
