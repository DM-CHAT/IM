package com.mhhy.controller;

import com.mhhy.common.BaseResult;
import com.mhhy.model.entity.DappEntity;
import com.mhhy.model.req.RegisterReq;
import com.mhhy.model.resp.LoginResp;
import com.mhhy.service.DappService;
import io.swagger.annotations.ApiOperation;
import lombok.AccessLevel;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/dapp")
@CrossOrigin(origins = "*")
@Setter(value = AccessLevel.PRIVATE, onMethod_ = @Autowired)
public class DappController {

    @Autowired
    DappService dappService;

    //@GetMapping("/list")
    @PostMapping("/list")
    @CrossOrigin(origins = "*")
    @ApiOperation(value = "list dapp", response = LoginResp.class)
    public BaseResult listDapp(){

        List<DappEntity> dappEntityList = dappService.list();

        return BaseResult.success(200, dappEntityList);
    }

    @GetMapping("/list2")
    @CrossOrigin(origins = "*")
    @ApiOperation(value = "list dapp", response = LoginResp.class)
    public BaseResult listDapp2(){

        List<DappEntity> dappEntityList = dappService.list();

        return BaseResult.success(200, dappEntityList);
    }

}
