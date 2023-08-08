package com.mhhy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mhhy.model.entity.ServerInfoEntity;
import com.mhhy.model.entity.UserEntity;

import java.util.List;

public interface ServerInfoService extends IService<ServerInfoEntity> {
    List<ServerInfoEntity> getServerList();
}

