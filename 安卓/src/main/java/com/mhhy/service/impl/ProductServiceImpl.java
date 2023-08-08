package com.mhhy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mhhy.mapper.ProductMapper;
import com.mhhy.model.entity.ProductEntity;
import com.mhhy.service.ProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, ProductEntity> implements ProductService {
}
