package com.mhhy.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.code.kaptcha.Producer;
import com.mhhy.annotation.Login;
import com.mhhy.common.BaseResult;
import com.mhhy.model.entity.UserEntity;
import com.mhhy.model.req.FindUserReq;
import com.mhhy.model.req.NodeRegisterReq;
import com.mhhy.model.req.RegisterReq;
import com.mhhy.model.req.ReqBase;
import com.mhhy.model.resp.LoginResp;
import com.mhhy.service.*;
import com.mhhy.util.*;
import com.ospn.common.OsnUtils;
import com.ospn.utils.CryptUtils;
import io.swagger.annotations.ApiOperation;
import lombok.AccessLevel;
import lombok.Setter;
import org.bouncycastle.jce.provider.symmetric.ARC4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api/user")
@CrossOrigin(origins = "*")
@Setter(value = AccessLevel.PRIVATE, onMethod_ = @Autowired)
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    LoginService loginService;

    @Autowired
    Producer kaptchaProduer;

    @Autowired
    KaptchaService kaptchaService;

    @Autowired
    SmsService smsService;

    @Autowired
    LtpServer ltpServer;

    @Login
    @ApiOperation("修改密码")
    @CrossOrigin(origins = "*")
    @PostMapping("updatePassword")
    public BaseResult updatePassword(@RequestBody NodeRegisterReq req) {
        return userService.updatePassword(req.getData());
    }

    @Login
    @ApiOperation("发送验证码")
    @CrossOrigin(origins = "*")
    @PostMapping("sendSms")
    public BaseResult sendSms(@RequestBody NodeRegisterReq req) {

        UserEntity user = RequestUtil.getMainUser();
        if (user == null) {
            return BaseResult.err("user not login.");
        }

        return smsService.sendCode(user.getUsername(), "372100");
    }


    @Login
    @ApiOperation("忘记密码")
    @CrossOrigin(origins = "*")
    @PostMapping("forgetPassword")
    public BaseResult forgetPassword(@RequestBody NodeRegisterReq req) {

        UserEntity user = RequestUtil.getMainUser();
        if (user == null) {
            return BaseResult.err("user not login.");
        }


        JSONObject param = RequestUtil.getParam(user.getImId(), req.getData());
        if (param == null) {
            return BaseResult.err("format error.");
        }

        return forgetPassword(param, user);
    }

    private BaseResult forgetPassword(JSONObject param, UserEntity user) {

        try {
            String phoneCode = param.getString("code");
            String newPassword = param.getString("newPassword");

            String redisCode = smsService.getCode(user.getUsername());
            if (redisCode == null) {
                return BaseResult.err("no phone code");
            }
            if (!redisCode.equalsIgnoreCase(phoneCode)) {
                return BaseResult.err("code error.");
            }

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

    @Login
    @ApiOperation("是否设置密码")
    @CrossOrigin(origins = "*")
    @PostMapping("isSetPassword")
    public BaseResult isSetPassword() {

        try {
            UserEntity user = RequestUtil.getMainUser();
            if (user == null) {
                return BaseResult.err("user not login.");
            }

            JSONObject result = new JSONObject();
            if (user.isSetPassword()) {
                result.put("isSetPassword", "yes");
            } else {
                result.put("isSetPassword", "no");
            }

            String key = RedisUtil.get("Communicationkey:"+user.getImId());
            return new BaseResult(result.toString(), key);

        } catch (Exception e) {

        }

        return BaseResult.err("");
    }


    @PostMapping("/register")
    @CrossOrigin(origins = "*")
    @ApiOperation(value = "注册", response = LoginResp.class)
    public BaseResult register(@RequestBody RegisterReq req){

        if (!RegexEnum.PHONE.isMatcher(req.getUsername())) {
            return BaseResult.err("phone number format error.");
        }

        // 验证图片
        String picCode = kaptchaService.getPicCode(req.getUsername());
        if (picCode == null) {
            return BaseResult.err("pic code check failed.");
        }

        String smsCode = smsService.getCode(req.getUsername());
        if (smsCode == null) {
            return BaseResult.err("sms check failed.");
        }

        // 使用picCode + smsCode 进行解密
        String aesKey = picCode + smsCode;

        //System.out.println("\naesKey:"+aesKey);
        System.out.println("\n================ aesKey:" + aesKey);

        String decData = OsnUtils.aesDecrypt(req.getData(), aesKey);
        if (decData == null) {
            return BaseResult.err("");
        }

        smsService.removeSms(req.getUsername());

        RegisterReq req2 = new RegisterReq(decData);

        return loginService.register5(req2, aesKey, "123", ltpServer.getAppId());
    }

    @Login
    @ApiOperation("检查token是否过期")
    @CrossOrigin(origins = "*")
    @PostMapping("/tokenValid")
    public BaseResult tokenValid() {
        UserEntity user = RequestUtil.getMainUser();
        if (user == null) {
            return BaseResult.err("user not login.");
        }
        return BaseResult.success();
    }

    @Login
    @ApiOperation("获取配置信息")
    @CrossOrigin(origins = "*")
    @GetMapping("/config")
    public BaseResult getConfig() {

        /*if (userService.getUser(req.getUser()) == null) {
            return BaseResult.err("not register user");
        }*/

        UserEntity user = RequestUtil.getMainUser();
        if (user == null) {
            return BaseResult.err("user not login.");
        }
        JSONObject json = loginService.getConfig();


        String response = ResponseUtil.createResponse(user.getImId(), json.toString());
        return BaseResult.success(200, response);
    }


    @Login
    @ApiOperation("查询用户")
    @CrossOrigin(origins = "*")
    @PostMapping("/findUser")
    public BaseResult findUser(@RequestBody ReqBase req) {

        UserEntity user = RequestUtil.getMainUser();
        if (user == null) {
            System.out.println("[findUser] error, user not login.");
            return BaseResult.err("user not login.");
        }

        JSONObject param = RequestUtil.getParam(user.getImId(), req.getData());
        if (param == null) {
            System.out.println("[findUser] error, decrypt error.");
            return BaseResult.err("decrypt error.");
        }


        String osnId = userService.findUser(param.getString("phoneOrEmail"));
        JSONObject json = new JSONObject();
        json.put("osnId", osnId);


        String response = ResponseUtil.createResponse(user.getImId(), json.toString());
        return BaseResult.success(200, response);
    }

    @Login
    @ApiOperation("create group by code")
    @CrossOrigin(origins = "*")
    @PostMapping("/createGroup")
    public BaseResult createGroupByCode(@RequestBody ReqBase req) {

        UserEntity user = RequestUtil.getMainUser();
        if (user == null) {
            return BaseResult.err("user not login.");
        }


        JSONObject param = RequestUtil.getParam(user.getImId(), req.getData());
        if (param == null) {
            return BaseResult.err("format error.");
        }

        JSONObject json = null;
        String groupName = param.getString("groupName");
        if (groupName != null) {

            BaseResult result = createGroup(param, user.getImId());
            if (result.getCode() == 200) {
                json = (JSONObject) result.getData();
            } else {
                return result;
            }

        } else {

            BaseResult result = getGroup(param);
            if (result.getCode() == 200) {
                json = (JSONObject) result.getData();
            } else {
                return result;
            }

        }


        String response = ResponseUtil.createResponse(user.getImId(), json.toString());
        return BaseResult.success(200, response);
    }


    private BaseResult createGroup(JSONObject param, String user) {
        String code = param.getString("code");
        String coordinate = param.getString("coordinate");
        String groupName = param.getString("groupName");

        String groupId = RedisUtil.get("face2face"+ code);
        if (groupId != null) {
            return BaseResult.err("code repeat");
        }

        // create group
        groupId = loginService.createGroup(groupName, user);
        if (groupId == null) {
            return BaseResult.err("create group failed.");
        }

        // 10分钟有效
        RedisUtil.setEx("face2face"+ code, groupId, 60 * 10);

        JSONObject result = new JSONObject();
        result.put("osnId", groupId);

        return BaseResult.success(200, result);
    }

    private BaseResult getGroup(JSONObject param) {

        String code = param.getString("code");
        String coordinate = param.getString("coordinate");
        String groupId = RedisUtil.get("face2face"+ code);
        if (groupId == null) {
            return BaseResult.err("no group");
        }

        JSONObject result = new JSONObject();
        result.put("osnId", groupId);

        return BaseResult.success(200, result);
    }


    @Login
    @ApiOperation("create group by code")
    @CrossOrigin(origins = "*")
    @PostMapping("/findByPos")
    public BaseResult findUserByPos(@RequestBody ReqBase req) {

        UserEntity user = RequestUtil.getMainUser();
        if (user == null) {
            return BaseResult.err("user not login.");
        }


        JSONObject param = RequestUtil.getParam(user.getImId(), req.getData());
        if (param == null) {
            return BaseResult.err("format error.");
        }

        JSONObject json = findUserByPos(user.getImId(), param);

        String response = ResponseUtil.createResponse(user.getImId(), json.toString());
        return BaseResult.success(200, response);
    }

    private JSONObject findUserByPos(String user, JSONObject param) {

        try {
            String coordinate = param.getString("coordinate");
            RedisUtil.setEx("user_pos_" + user, coordinate, 60*3);
            RedisUtil.addSet("position", user);


            Set<String> sets = RedisUtil.members("position");
            List<String> set2 = new ArrayList<>();
            for (String person : sets) {
                if (RedisUtil.get("user_pos_" + person) != null) {
                    set2.add(person);
                } else {
                    RedisUtil.removeSet("position", person);
                }
            }

            JSONObject result = new JSONObject();
            result.put("users", set2);
            return result;
        } catch (Exception e) {

        }
        return new JSONObject();
    }


    @Login
    @ApiOperation("create group by code")
    @CrossOrigin(origins = "*")
    @PostMapping("/findByAddressBook")
    public BaseResult findUserByAddressBook(@RequestBody ReqBase req) {

        UserEntity user = RequestUtil.getMainUser();
        if (user == null) {
            return BaseResult.err("user not login.");
        }


        JSONObject param = RequestUtil.getParam(user.getImId(), req.getData());
        if (param == null) {
            return BaseResult.err("format error.");
        }

        JSONObject json = findUserByAddressBook(param);

        String response = ResponseUtil.createResponse(user.getImId(), json.toString());
        return BaseResult.success(200, response);
    }

    private JSONObject findUserByAddressBook(JSONObject param) {
        try {
            Map<String, String> result = new HashMap<>();
            List<String> addressBook = param.getObject("addressBook", TypeReference.LIST_STRING);
            for (String phone : addressBook) {
                String osnId = userService.findUser(phone);
                if (osnId != null) {
                    result.put(phone, osnId);
                }
            }
            JSONObject json = new JSONObject();
            json.put("result", result);
            return json;

        } catch (Exception e) {

        }
        return new JSONObject();
    }


    @Login
    @ApiOperation("find user by short name")
    @CrossOrigin(origins = "*")
    @PostMapping("/setShortName")
    public BaseResult setShortName(@RequestBody ReqBase req) {

        UserEntity user = RequestUtil.getMainUser();
        if (user == null) {
            return BaseResult.err("user not login.");
        }


        JSONObject param = RequestUtil.getParam(user.getImId(), req.getData());
        if (param == null) {
            return BaseResult.err("format error.");
        }

        String shortName = param.getString("shortName");
        user.setNickName(shortName);
        if (userService.updateById(user)){
            return BaseResult.success(200, "success.");
        }

        return BaseResult.err("");
    }

    @Login
    @ApiOperation("find user by short name")
    @CrossOrigin(origins = "*")
    @PostMapping("/findByShort")
    public BaseResult findUserByShortName(@RequestBody ReqBase req) {

        UserEntity user = RequestUtil.getMainUser();
        if (user == null) {
            return BaseResult.err("user not login.");
        }


        JSONObject param = RequestUtil.getParam(user.getImId(), req.getData());
        if (param == null) {
            return BaseResult.err("format error.");
        }


        JSONObject json = findUserByShortName(user.getImId(), param);

        String response = ResponseUtil.createResponse(user.getImId(), json.toString());
        return BaseResult.success(200, response);
    }



    private JSONObject findUserByShortName(String user, JSONObject param) {

        JSONObject result = new JSONObject();
        try {
            String shortName = param.getString("shortName");

            List<UserEntity> userList = userService.list(new LambdaQueryWrapper<UserEntity>()
                    .eq(UserEntity::getNickName, shortName));

            List<String> users = new ArrayList<>();

            for (UserEntity ue : userList) {
                users.add(ue.getImId());
            }
            result.put("users", users);
        } catch (Exception e) {

        }
        return result;
    }



}
