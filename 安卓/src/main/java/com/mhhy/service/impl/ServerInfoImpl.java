package com.mhhy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mhhy.mapper.ServerInfoMapper;
import com.mhhy.model.entity.ServerInfoEntity;
import com.mhhy.service.ServerInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("serverInfoService")
public class ServerInfoImpl extends ServiceImpl<ServerInfoMapper, ServerInfoEntity> implements ServerInfoService {
    public List<ServerInfoEntity> getServerList() {
        List list = this.list();
        return list;
    }
}
