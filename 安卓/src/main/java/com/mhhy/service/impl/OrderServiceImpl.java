package com.mhhy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mhhy.mapper.OrderMapper;
import com.mhhy.model.entity.OrderEntity;
import com.mhhy.service.OrderService;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements OrderService {
}
