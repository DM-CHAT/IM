package com.mhhy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mhhy.mapper.DappMapper;
import com.mhhy.model.entity.DappEntity;
import com.mhhy.service.DappService;
import org.springframework.stereotype.Service;

@Service
public class DappServiceImpl
        extends ServiceImpl<DappMapper, DappEntity>
        implements DappService {

}
