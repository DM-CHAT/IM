package com.mhhy.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mhhy.common.BaseResult;
import com.mhhy.mapper.UserMapper;
import com.mhhy.model.entity.UserEntity;
import com.mhhy.service.UserService;
import com.mhhy.util.*;
import com.ospn.common.OsnUtils;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {
    @Override
    public BaseResult updatePassword(String data) {
        try {
            UserEntity user = RequestUtil.getMainUser();
            if (user == null) {
                return BaseResult.err("user not login.");
            }

            // 获取解密key
            // 解密data
            String plaintext = getPlaintext(data, user);
            if (plaintext == null) {

                return BaseResult.err("Communication key error.");
            }

            JSONObject jsonObject = JSONObject.parseObject(plaintext);
            if (user == null) {
                return BaseResult.err("get password error.");
            }

            // 如果有密码，还需要输入原始密码
            if (user.isSetPassword()) {
                String oldPwd = jsonObject.getString("oldPassword");
                if (!user.getPassword().equalsIgnoreCase(oldPwd)) {
                    return BaseResult.err("old password error.");
                }
            }


            String password = jsonObject.getString("password");

            //对新密码进行加密
            byte[] sha256Str = PasswordUtil.getSHA256Str(password);
            String newPassword = PasswordUtil.encoderBase64(sha256Str);
            user.setPassword(newPassword);
            this.updateById(user);

            return BaseResult.success();

        } catch (Exception e) {

        }
        return BaseResult.err("I do not known.");
    }

    private String getPlaintext(String encData, UserEntity user) {

        try {
            String key = RedisUtil.get("Communicationkey:"+user.getImId());
            System.out.println("[getPlaintext] encData:" + encData);
            System.out.println("[getPlaintext] user:" + user.getImId());
            System.out.println("[getPlaintext] key:" + key);
            String plaint = OsnUtils.aesDecrypt(encData, key);
            System.out.println("[getPlaintext] plaint:" + plaint);
            return plaint;
        } catch (Exception e) {

        }

        return null;
    }

    public String findUser(String username) {
        UserEntity user = this.getOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username));
        if (user != null) {
            return user.getImId();
        }
        return null;
    }

    public UserEntity getUser(String osnId) {
        UserEntity user = this.getOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getImId, osnId));

        return user;
    }


}
