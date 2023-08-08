package com.mhhy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mhhy.common.BaseResult;
import com.mhhy.model.entity.UserEntity;

public interface UserService extends IService<UserEntity> {
    BaseResult updatePassword(String data);
    String findUser(String username);
    UserEntity getUser(String osnId);
}
