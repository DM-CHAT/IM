package com.mhhy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mhhy.mapper.ImAccountMapper;
import com.mhhy.model.entity.ImAccountEntity;
import com.mhhy.service.ImAccountService;
import org.springframework.stereotype.Service;

@Service
public class ImAccountServiceImpl extends ServiceImpl<ImAccountMapper, ImAccountEntity> implements ImAccountService {
}
