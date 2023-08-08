package com.mhhy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mhhy.mapper.NodeMapper;
import com.mhhy.model.entity.NodeEntity;
import com.mhhy.service.NodeService;
import org.springframework.stereotype.Service;

@Service("nodeService")
public class NodeServiceImpl  extends ServiceImpl<NodeMapper, NodeEntity> implements NodeService {
}
