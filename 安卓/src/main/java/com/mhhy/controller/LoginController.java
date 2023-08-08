package com.mhhy.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.code.kaptcha.Producer;
import com.mhhy.common.BaseResult;
import com.mhhy.enums.ResultCodeEnum;
import com.mhhy.model.entity.ImAccountEntity;
import com.mhhy.model.entity.UserEntity;
import com.mhhy.model.req.DappRequest;
import com.mhhy.model.req.LoginReq;
import com.mhhy.model.req.RegisterReq;
import com.mhhy.model.req.ReqBase;
import com.mhhy.model.resp.LoginResp;
import com.mhhy.service.*;
import com.mhhy.util.*;
import com.mhhy.util.token.JwtUtil;
import com.ospn.command.CmdReDappLogin;
import com.ospn.common.OsnUtils;
import io.swagger.annotations.ApiOperation;
import lombok.AccessLevel;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.ospn.common.OsnUtils.aesDecrypt;
import static com.ospn.common.OsnUtils.aesEncrypt;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("api/login")
@Setter(value = AccessLevel.PRIVATE, onMethod_ = @Autowired)
public class LoginController {

    @Autowired
    LoginService loginService;
    @Autowired
    ImAccountService imAccountService;
    @Autowired
    UserService userService;

    @Autowired
    Producer kaptchaProduer;

    @Autowired
    KaptchaService kaptchaService;

    @Autowired
    LtpServer ltpServer;

    @Autowired
    SmsService smsService;


    @PostMapping("/login")
    @CrossOrigin(origins = "*")
    @ApiOperation(value = "登录", response = LoginResp.class)
    public BaseResult login(@RequestBody LoginReq req){
        return loginService.login(req);
    }


    @ApiOperation("dapp login")
    @CrossOrigin(origins = "*")
    @PostMapping("/dappLogin")
    public JSONObject dappLogin(@RequestBody DappRequest cmd) {
        //log.info("传递的参数是：{}", cmd);

        JSONObject ret = null;
        if (cmd.command.equalsIgnoreCase("GetServerInfo")) {

            // key: dapp_session_user
            String session = RedisUtil.get("dapp_session_" + cmd.user);

            if (session != null) {
                // 返回数据
                ret = ltpServer.ltpData.getServerInfo(cmd.getCmdGetServerInfo(session));
            } else {
                ret = ltpServer.ltpData.getServerInfo(cmd.getCmdGetServerInfo());
                session = ret.getString("session");
                RedisUtil.setEx("dapp_session_" + cmd.user, session, 60);
            }

        } else if (cmd.command.equalsIgnoreCase("Login")) {

            CmdReDappLogin cmdRe = null;
            try {

                UserEntity userEntity = userService.getOne(
                        new QueryWrapper<UserEntity>()
                                .eq("im_id", cmd.user)
                );
                if (userEntity != null){
                    cmdRe.setError("1007:no user");
                    return cmdRe.toJson();
                }

                String session = RedisUtil.get("dapp_session_" + cmd.user);
                if (session == null) {
                    // 没有session，返回错误信息
                    cmdRe = new CmdReDappLogin();
                    cmdRe.setError("1001:need session");
                    return cmdRe.toJson();
                }


                cmdRe = ltpServer.ltpData.login(cmd.getCmdDappLogin(session));

                if (cmdRe.errCode.equalsIgnoreCase("0:success")) {

                    // 增加sessionKey
                    // 生成jwtToken


                    String randomToInviteCode = RandomUtil.getRandomToInviteCode(16);
                    String token = JwtUtil.createJwtToken(userEntity.getId(), 315360000L);
                    //把之前的token移除
                    JwtUtil.removeTokenByUserId(userEntity.getId());
                    //把token存入redis
                    JwtUtil.putTokenToRedis(userEntity.getId(), token);
                    JSONObject tokenJson = new JSONObject();
                    // TODO 生成通讯密钥拼接在token后面用中竖线隔开，小程序端需要用中划线分割出token
                    tokenJson.put("token", token);
                    tokenJson.put("communicationKey", randomToInviteCode);
                    // log.info("-------token-------" + tokenJson.get("token"));
                    //将通讯密钥存入redis
                    RedisUtil.set("Communicationkey:"+cmd.user, randomToInviteCode);

                    cmdRe.addSessionKey(cmd.user, tokenJson.toString().getBytes());

                    return cmdRe.toJson();

                } else {
                    ret = cmdRe.toJson();
                }


                return cmdRe.toJson();


            } catch (Exception e) {
                cmdRe.setError("1006:exception");
            }
        }

        return ret;
    }

    @PostMapping("/forgetPassword")
    @CrossOrigin(origins = "*")
    @ApiOperation(value = "登录", response = LoginResp.class)
    public BaseResult forgetPassword(@RequestBody LoginReq req){

        try {

            UserEntity user = userService.getOne(new LambdaQueryWrapper<UserEntity>()
                    .eq(UserEntity::getUsername, req.getUsername())
            );
            if (user == null) {
                return BaseResult.err("no user");
            }

            String smsCode = smsService.getCode(req.getUsername());
            if (smsCode == null) {
                return BaseResult.err("no phone code");
            }

            String decString = OsnUtils.aesDecrypt(req.getData(), smsCode);
            if (decString == null) {
                return BaseResult.err("sms code error.");
            }

            JSONObject param = JSONObject.parseObject(decString);
            String newPassword = param.getString("newPassword");

            byte[] sha256Str = PasswordUtil.getSHA256Str(newPassword);
            String newPasswordBase64 = PasswordUtil.encoderBase64(sha256Str);
            user.setPassword(newPasswordBase64);
            if (userService.updateById(user)) {
                return BaseResult.success(200, "success.");
            }


        } catch (Exception e) {

        }


        return BaseResult.err("");
    }



    @ApiOperation(value="手机号登录",notes = "给的TOKEN 放进headers {Authorization:TOKEN}")
    @PostMapping("/phone-login")
    @CrossOrigin(origins = "*")
    public BaseResult loginByPhone(@RequestBody LoginReq req){


        UserEntity user = userService.getOne(new QueryWrapper<UserEntity>()
                .eq("username", req.getUsername()));
        if (user == null) {
            RegisterReq registerReq = new RegisterReq();
            registerReq.setUsername(req.getUsername());
            registerReq.setSmsCode(req.getSmsCode());
            registerReq.setPassword(req.getPassword());
            registerReq.setPicCode(req.getPicCode());
            registerReq.setNickName(req.getNickName());
            return loginService.register2(registerReq);
        }

        return loginService.login(req);
    }


    @PostMapping("/register")
    @CrossOrigin(origins = "*")
    @ApiOperation(value = "注册", response = LoginResp.class)
    public BaseResult register(@RequestBody RegisterReq req){

        // 验证图片
        /*if (!kaptchaService.check2(req.getUsername(), req.getPicCode())) {
            return BaseResult.err("Kaptcha failed.");
        }

        // 验证手机验证码
        if (!req.getSmsCode().equalsIgnoreCase("372100")) {
            return BaseResult.err("sms check failed.");
        }*/

        if (!RegexEnum.PHONE.isMatcher(req.getUsername())) {
            return BaseResult.err("phone number format error.");
        }

        return loginService.register2(req);
    }


    @PostMapping("/getChallenge")
    @CrossOrigin(origins = "*")
    @ApiOperation(value = "获取登录的随机数", response = LoginResp.class)
    public BaseResult getChallenge(@RequestBody RegisterReq req){
        return loginService.getChallenge(req.getUsername());
    }

    @GetMapping("/gen")
    @CrossOrigin(origins = "*")
    @ApiOperation(value = "获取登录的随机数", response = LoginResp.class)
    public BaseResult createAccounts(){


        for (int i = 0;i<10000; i ++) {
            createAndSaveImAccount();
        }


        /*JSONObject imInfo = createAccount();
        if (imInfo != null) {
            ImAccountEntity imAccountEntity = new ImAccountEntity();
            imAccountEntity.setUsername(imInfo.getString("username"));
            imAccountEntity.setPassword(imInfo.getString("password"));
            imAccountEntity.setOsnId(imInfo.getString("osnID"));
            imAccountEntity.setIp(imInfo.getString("IP"));
            imAccountService.save(imAccountEntity);
        }*/

        return BaseResult.success();
    }

    private JSONObject createAccount() {

        try {
            String username = UUID.randomUUID().toString().replace("-", "").substring(2, 22);
            String password = UUID.randomUUID().toString().replace("-", "").substring(2, 22);

            RegisterReq req1 = new RegisterReq();
            req1.setUsername(username);
            req1.setPassword(password);

            JSONObject result = loginService.register3(req1);
            if (result != null) {
                result.put("username", username);
                result.put("password", password);
                return result;
            }


        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    private void createAndSaveImAccount() {
        JSONObject imInfo = createAccount();
        if (imInfo != null) {
            ImAccountEntity imAccountEntity = new ImAccountEntity();
            imAccountEntity.setUsername(imInfo.getString("username"));
            imAccountEntity.setPassword(imInfo.getString("password"));
            imAccountEntity.setOsnId(imInfo.getString("osnID"));
            imAccountEntity.setIp(imInfo.getString("IP"));
            imAccountService.save(imAccountEntity);
        }
    }


    @PostMapping("/loginByMnemonic")
    @CrossOrigin(origins = "*")
    @ApiOperation(value = "登录", response = LoginResp.class)
    public BaseResult loginByMnemonic(@RequestBody LoginReq req){

        String user = req.getUser();

        // 从user 表中查找
        UserEntity userEntity = userService.getUser(user);
        if (userEntity == null) {
            // 进入注册环节
            userEntity = loginService.register6(user, ltpServer.getAppId());
        }

        if (userEntity == null) {
            System.out.print("[loginByMnemonic] login failed.");
            return BaseResult.err("");
        }

        // 返回数据
        JSONObject config = loginService.getConfig();




        JSONObject result = new JSONObject();
        result.put("osn_node", config.getString("HOST_IP"));

        String token = JwtUtil.createJwtToken(userEntity.getId(), 315360000L);
        //把之前的token移除
        JwtUtil.removeTokenByUserId(userEntity.getId());
        //把token存入redis
        JwtUtil.putTokenToRedis(userEntity.getId(), token);

        result.put("token", token);
        result.put("language", "zh");
        result.put("json", config);


        return BaseResult.success(200, result);
    }



}
