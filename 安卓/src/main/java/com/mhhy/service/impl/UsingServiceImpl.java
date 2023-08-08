package com.mhhy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mhhy.mapper.UsingMapper;
import com.mhhy.model.entity.UsingEntity;
import com.mhhy.service.UsingService;
import org.springframework.stereotype.Service;

@Service
public class UsingServiceImpl extends ServiceImpl<UsingMapper, UsingEntity> implements UsingService {
}
