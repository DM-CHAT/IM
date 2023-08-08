package com.mhhy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mhhy.common.BaseResult;
import com.mhhy.enums.ResultCodeEnum;
import com.mhhy.enums.UserStateEnum;
import com.mhhy.enums.UserTypeEnum;
import com.mhhy.model.entity.UserEntity;
import com.mhhy.model.req.LoginReq;
import com.mhhy.model.req.RegisterReq;
import com.mhhy.service.LoginService;
import com.mhhy.service.LtpServer;
import com.mhhy.service.UserService;
import com.mhhy.util.Aes128Util;
import com.mhhy.util.PasswordUtil;
import com.mhhy.util.RandomUtil;
import com.mhhy.util.RedisUtil;
import com.mhhy.util.token.JwtUtil;
import com.ospn.common.ECUtils;
import com.ospn.common.OsnUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
//@Setter(value = AccessLevel.PRIVATE, onMethod_ = @Autowired)
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserService userService;

    @Autowired
    private LtpServer ltpServer;

    @Value("${IM.imUrl}")
    private String mImUrl;

    @Value("${IM.imPwd}")
    private String mImPwd;

    @Value("${IM.LOGIN_URL}")
    private String LOGIN_URL;//: http://39.100.147.50:8300/admin

    @Value("${IM.HOST_IP}")
    private String HOST_IP;//: 39.100.147.50

    @Value("${IM.PEFIX_URL}")
    private String PEFIX_URL;//: http://39.100.147.50:8334

    @Value("${IM.APP_DEVICE}")
    private String APP_DEVICE;//: http://39.100.147.50:8334/api/im/device

    @Value("${IM.SET_NAME}")
    private String SET_NAME;//: http://39.100.147.50:8334/api/im/setName

    @Value("${IM.KEFU_LIST}")
    private String KEFU_LIST;//: http://39.100.147.50:8334/api/im/custormer

    @Value("${IM.QueryAplets}")
    private String QueryAplets;//: http://39.100.147.50:8334/api/im/findOne

    @Value("${IM.GroupList}")
    private String GroupList;//: http://47.92.123.66:8882/program/list

    @Value("${IM.AddGroup}")
    private String AddGroup;//: http://47.92.123.66:8882/groupProgram/add

    @Value("${IM.ENDPOINT}")
    private String ENDPOINT;//: https://oss-ap-southeast-1.aliyuncs.com

    //@Value("${IM.RemoteTempFilePath}")
    //private String RemoteTempFilePath;//: https://zolo-image1.oss-ap-southeast-1.aliyuncs.com/

    @Value("${IM.AccessKeyId}")
    private String AccessKeyId;//: LTAI5tQ2JnUwihLqmPSvHDkB

    @Value("${IM.AccessKeySecret}")
    private String AccessKeySecret;//: DZB9JYIrlt6gJeJVtIwP9xlqZkWgT3

    @Value("${IM.BUCKETNAME}")
    private String BUCKETNAME;//: zolo-image1

    @Value("${IM.UserPortraitDirectory}")
    private String UserPortraitDirectory;//: userPortrait/

    @Value("${IM.GroupPortraitDirectory}")
    private String GroupPortraitDirectory;//: groupPortrait/

    @Value("${IM.TempDirectory}")
    private String TempDirectory;//: temp/

    @Value("${IM.MainDapp}")
    private String MainDapp;

    @Value("${IM.WEBRTCBASE}")
    private String WEBRTCBASE;

    @Value("${IM.WEBRTCHOST}")
    private String WEBRTCHOST;


    /*@Value("${IM.MainDapp}")
    private String MainDapp;

    @Value("${IM.Wallet}")
    private String Wallet;*/





    static String sessionFlag = "session_01:";
    public static String conversationFlag = "conversation01_id:";


    //private final static OdcProperties properties = SpringContextHolder.getBean("odcProperties", OdcProperties.class);

    @Override
    public BaseResult register(RegisterReq registerReq) {

        UserEntity one = userService.getOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, registerReq.getUsername())
                .last("limit 1")
        );

        if (one != null){
            return BaseResult.err(ResultCodeEnum.ACCOUNT_ALREADY.getRemark());
        }



        String[] keys = ECUtils.createOsnID2("user", registerReq.getPassword());
        if (keys == null) {
            return BaseResult.err("gen account error.");
        }
        String osnId = keys[0];
        if (osnId.length() < 20) {
            return BaseResult.err("gen account error.");
        }

        //String username2 = UUID.randomUUID().toString().replace("-", "").substring(2, 22);
        //String password2 = UUID.randomUUID().toString().replace("-", "").substring(2, 22);

        //String osnId = keys[0];     //registerOsn(username2, password2);

        if (register2im(osnId, ltpServer.ltpData.apps.osnID, null) == null){
            return BaseResult.err("register to im error.");
        }

        //对密码进行sha256加密
        byte[] sha256Str = PasswordUtil.getSHA256Str(registerReq.getPassword());
        //对加密的数据进行base64编码
        String password = PasswordUtil.encoderBase64(sha256Str);
        one = new UserEntity();
        one.setUsername(registerReq.getUsername());
        one.setPassword(password);
        one.setType(UserTypeEnum.NORMAL.getCode());
        one.setState(UserStateEnum.NORMAL.getCode());
        one.setCreateTime(new Date());
        one.setImNode(HOST_IP);
        one.setImId(osnId);
        one.setImUsername(osnId);


        byte[] data = Base64.getDecoder().decode(keys[2]);

        String key2 = OsnUtils.aesEncrypt(data, sha256Str);

        String pwd2 = "VER2-" + keys[1] +"-"+ key2;
        one.setImPwd(pwd2);
        try {
            userService.save(one);
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println(e);
        }

        return BaseResult.success("register success");
    }

    @Override
    public BaseResult register2(RegisterReq registerReq) {

        UserEntity one = userService.getOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, registerReq.getUsername())
                .last("limit 1")
        );

        if (one != null){

            JSONObject resultJson = new JSONObject();
            resultJson.put("osn_id", one.getImId());
            resultJson.put("osn_username", one.getImUsername());
            resultJson.put("osn_password", one.getImPwd());
            resultJson.put("osn_node", one.getImNode());
            resultJson.put("service_id", ltpServer.getAppId());

            if (!one.isSetPassword()) {
                resultJson.put("password", "null");
            }



            String randomToInviteCode = RandomUtil.getRandomToInviteCode(16);
            String token = JwtUtil.createJwtToken(one.getId(), 315360000L);
            //把之前的token移除
            JwtUtil.removeTokenByUserId(one.getId());
            //把token存入redis
            JwtUtil.putTokenToRedis(one.getId(), token);

            // log.info("-------token-------" + tokenJson.get("token"));
            //将通讯密钥存入redis
            RedisUtil.set("Communicationkey:"+one.getImId(), randomToInviteCode);

            resultJson.put("token", token);
            resultJson.put("communicationKey", randomToInviteCode);





            /*if (registerReq.getPassword() != null) {
                System.out.println("req password : " + registerReq.getPassword());
                String temp = PasswordUtil.encoderBase64(PasswordUtil.getSHA256Str(registerReq.getPassword()));
                return new BaseResult(resultJson.toString(), temp);
            }

            if (registerReq.getSmsCode() != null) {
                System.out.println("req sms code : " + registerReq.getSmsCode());
                String temp = PasswordUtil.encoderBase64(PasswordUtil.getSHA256Str(registerReq.getSmsCode()));
                return new BaseResult(resultJson.toString(), temp);
            }*/

            resultJson.put("json", getConfig());

            System.out.println("[register] result:" + resultJson);
            return BaseResult.success(200, resultJson);

            //return BaseResult.err(ResultCodeEnum.ACCOUNT_ALREADY.getRemark());
        }




        String username2 = UUID.randomUUID().toString().replace("-", "").substring(2, 22);
        String password2 = UUID.randomUUID().toString().replace("-", "").substring(2, 22);

        String osnId = registerOsn(username2, password2);
        if (osnId == null) {
            return BaseResult.err(ResultCodeEnum.IM_REGISTER_FAILED.getRemark());
        }
        if (osnId.length() == 0) {
            return BaseResult.err(ResultCodeEnum.IM_REGISTER_FAILED.getRemark());
        }


        one = new UserEntity();
        one.setUsername(registerReq.getUsername());
        if (registerReq.getPassword() != null) {
            //对密码进行sha256加密
            byte[] sha256Str = PasswordUtil.getSHA256Str(registerReq.getPassword());
            //对加密的数据进行base64编码
            String password = PasswordUtil.encoderBase64(sha256Str);
            one.setPassword(password);
        }

        one.setType(UserTypeEnum.NORMAL.getCode());
        one.setState(UserStateEnum.NORMAL.getCode());
        one.setCreateTime(new Date());
        one.setImNode(HOST_IP);
        one.setImId(osnId);
        one.setImUsername(username2);
        one.setImPwd(password2);
        try {
            userService.save(one);
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println(e);
            return BaseResult.err(ResultCodeEnum.DB_ERROR.getRemark());
        }

        JSONObject resultJson = new JSONObject();
        resultJson.put("osn_id", one.getImId());
        resultJson.put("osn_username", one.getImUsername());
        resultJson.put("osn_password", one.getImPwd());
        resultJson.put("osn_node", one.getImNode());
        resultJson.put("service_id", ltpServer.getAppId());

        if (!one.isSetPassword()) {
            resultJson.put("password", "null");
        }

        String randomToInviteCode = RandomUtil.getRandomToInviteCode(16);
        String token = JwtUtil.createJwtToken(one.getId(), 315360000L);
        //把之前的token移除
        JwtUtil.removeTokenByUserId(one.getId());
        //把token存入redis
        JwtUtil.putTokenToRedis(one.getId(), token);

        // log.info("-------token-------" + tokenJson.get("token"));
        //将通讯密钥存入redis
        RedisUtil.set("Communicationkey:"+one.getImId(), randomToInviteCode);

        resultJson.put("token", token);
        resultJson.put("communicationKey", randomToInviteCode);

        /*if (registerReq.getPassword() != null) {
            System.out.println("req password : " + registerReq.getPassword());
            String temp = PasswordUtil.encoderBase64(PasswordUtil.getSHA256Str(registerReq.getPassword()));
            return new BaseResult(resultJson.toString(), temp);
        }

        if (registerReq.getSmsCode() != null) {
            System.out.println("req sms code : " + registerReq.getSmsCode());
            String temp = PasswordUtil.encoderBase64(PasswordUtil.getSHA256Str(registerReq.getSmsCode()));
            return new BaseResult(resultJson.toString(), temp);
        }*/
        resultJson.put("json", getConfig());
        System.out.println("[register] result:" + resultJson);
        return BaseResult.success(200, resultJson);
    }

    public BaseResult register4(RegisterReq registerReq, String key) {

        //String aesKey = PasswordUtil.encoderBase64(PasswordUtil.getSHA256Str(key));

        UserEntity one = userService.getOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, registerReq.getUsername())
                .last("limit 1")
        );

        if (one != null){

            JSONObject resultJson = new JSONObject();
            resultJson.put("osn_id", one.getImId());
            resultJson.put("osn_username", one.getImUsername());
            resultJson.put("osn_password", one.getImPwd());
            resultJson.put("osn_node", one.getImNode());
            resultJson.put("service_id", ltpServer.getAppId());

            if (!one.isSetPassword()) {
                resultJson.put("password", "null");
            }

            String randomToInviteCode = RandomUtil.getRandomToInviteCode(16);
            String token = JwtUtil.createJwtToken(one.getId(), 315360000L);
            //把之前的token移除
            JwtUtil.removeTokenByUserId(one.getId());
            //把token存入redis
            JwtUtil.putTokenToRedis(one.getId(), token);

            // log.info("-------token-------" + tokenJson.get("token"));
            //将通讯密钥存入redis
            RedisUtil.set("Communicationkey:"+one.getImId(), randomToInviteCode);

            resultJson.put("token", token);
            resultJson.put("communicationKey", randomToInviteCode);

            resultJson.put("json", getConfig());

            return new BaseResult(resultJson.toString(), key);

            /*if (registerReq.getPassword() != null) {
                System.out.println("req password : " + registerReq.getPassword());
                String temp = PasswordUtil.encoderBase64(PasswordUtil.getSHA256Str(registerReq.getPassword()));
                return new BaseResult(resultJson.toString(), temp);
            }

            if (registerReq.getSmsCode() != null) {
                System.out.println("req sms code : " + registerReq.getSmsCode());
                String temp = PasswordUtil.encoderBase64(PasswordUtil.getSHA256Str(registerReq.getSmsCode()));
                return new BaseResult(resultJson.toString(), temp);
            }*/

            //return BaseResult.success(200, resultJson);

            //return BaseResult.err(ResultCodeEnum.ACCOUNT_ALREADY.getRemark());
        }




        String username2 = UUID.randomUUID().toString().replace("-", "").substring(2, 22);
        String password2 = UUID.randomUUID().toString().replace("-", "").substring(2, 22);

        String osnId = registerOsn(username2, password2);
        if (osnId == null) {
            return BaseResult.err(ResultCodeEnum.IM_REGISTER_FAILED.getRemark());
        }
        if (osnId.length() == 0) {
            return BaseResult.err(ResultCodeEnum.IM_REGISTER_FAILED.getRemark());
        }


        one = new UserEntity();
        one.setUsername(registerReq.getUsername());
        if (registerReq.getPassword() != null) {
            //对密码进行sha256加密
            byte[] sha256Str = PasswordUtil.getSHA256Str(registerReq.getPassword());
            //对加密的数据进行base64编码
            String password = PasswordUtil.encoderBase64(sha256Str);
            one.setPassword(password);
        }

        one.setType(UserTypeEnum.NORMAL.getCode());
        one.setState(UserStateEnum.NORMAL.getCode());
        one.setCreateTime(new Date());
        one.setImNode(HOST_IP);
        one.setImId(osnId);
        one.setImUsername(username2);
        one.setImPwd(password2);
        try {
            userService.save(one);
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println(e);
            return BaseResult.err(ResultCodeEnum.DB_ERROR.getRemark());
        }

        JSONObject resultJson = new JSONObject();
        resultJson.put("osn_id", one.getImId());
        resultJson.put("osn_username", one.getImUsername());
        resultJson.put("osn_password", one.getImPwd());
        resultJson.put("osn_node", one.getImNode());
        resultJson.put("service_id", ltpServer.getAppId());

        if (!one.isSetPassword()) {
            resultJson.put("password", "null");
        }

        String randomToInviteCode = RandomUtil.getRandomToInviteCode(16);
        String token = JwtUtil.createJwtToken(one.getId(), 315360000L);
        //把之前的token移除
        JwtUtil.removeTokenByUserId(one.getId());
        //把token存入redis
        JwtUtil.putTokenToRedis(one.getId(), token);

        // log.info("-------token-------" + tokenJson.get("token"));
        //将通讯密钥存入redis
        RedisUtil.set("Communicationkey:"+one.getImId(), randomToInviteCode);

        resultJson.put("token", token);
        resultJson.put("communicationKey", randomToInviteCode);

        /*if (registerReq.getPassword() != null) {
            System.out.println("req password : " + registerReq.getPassword());
            String temp = PasswordUtil.encoderBase64(PasswordUtil.getSHA256Str(registerReq.getPassword()));
            return new BaseResult(resultJson.toString(), temp);
        }

        if (registerReq.getSmsCode() != null) {
            System.out.println("req sms code : " + registerReq.getSmsCode());
            String temp = PasswordUtil.encoderBase64(PasswordUtil.getSHA256Str(registerReq.getSmsCode()));
            return new BaseResult(resultJson.toString(), temp);
        }*/

        return new BaseResult(resultJson.toString(), key);
    }

    public BaseResult register5(RegisterReq registerReq, String key, String UserPwd, String owner2) {

        UserEntity one = userService.getOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, registerReq.getUsername())
                .last("limit 1")
        );

        if (one != null){

            JSONObject resultJson = new JSONObject();
            resultJson.put("osn_id", one.getImId());
            resultJson.put("osn_username", one.getImUsername());
            resultJson.put("osn_password", one.getImPwd());
            resultJson.put("osn_node", one.getImNode());
            resultJson.put("service_id", ltpServer.getAppId());

            if (!one.isSetPassword()) {
                resultJson.put("password", "null");
            }

            String randomToInviteCode = RandomUtil.getRandomToInviteCode(16);
            String token = JwtUtil.createJwtToken(one.getId(), 315360000L);
            //把之前的token移除
            JwtUtil.removeTokenByUserId(one.getId());
            //把token存入redis
            JwtUtil.putTokenToRedis(one.getId(), token);

            // log.info("-------token-------" + tokenJson.get("token"));
            //将通讯密钥存入redis
            RedisUtil.set("Communicationkey:"+one.getImId(), randomToInviteCode);

            resultJson.put("token", token);
            resultJson.put("communicationKey", randomToInviteCode);

            resultJson.put("json", getConfig());

            return new BaseResult(resultJson.toString(), key);
        }


        // 创建账号
        String[] keyPair = ECUtils.createOsnID2("user", UserPwd);
        String privateKey = "VER2-" + keyPair[1] + "-" + keyPair[2];


        String osnId = register2im(keyPair[0], owner2, null);
        if (osnId == null) {
            return BaseResult.err(ResultCodeEnum.IM_REGISTER_FAILED.getRemark());
        }
        if (osnId.length() == 0) {
            return BaseResult.err(ResultCodeEnum.IM_REGISTER_FAILED.getRemark());
        }


        one = new UserEntity();
        one.setUsername(registerReq.getUsername());
        if (registerReq.getPassword() != null) {
            //对密码进行sha256加密
            byte[] sha256Str = PasswordUtil.getSHA256Str(registerReq.getPassword());
            //对加密的数据进行base64编码
            String password = PasswordUtil.encoderBase64(sha256Str);
            one.setPassword(password);
        }

        one.setType(UserTypeEnum.NORMAL.getCode());
        one.setState(UserStateEnum.NORMAL.getCode());
        one.setCreateTime(new Date());
        one.setImNode(HOST_IP);
        one.setImId(osnId);
        one.setImUsername(osnId);
        one.setImPwd(privateKey);
        try {
            userService.save(one);
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResult.err(ResultCodeEnum.DB_ERROR.getRemark());
        }

        JSONObject resultJson = new JSONObject();
        resultJson.put("osn_id", one.getImId());
        resultJson.put("osn_username", one.getImUsername());
        resultJson.put("osn_password", one.getImPwd());
        resultJson.put("osn_node", one.getImNode());
        resultJson.put("service_id", ltpServer.getAppId());

        if (!one.isSetPassword()) {
            resultJson.put("password", "null");
        }

        String randomToInviteCode = RandomUtil.getRandomToInviteCode(16);
        String token = JwtUtil.createJwtToken(one.getId(), 315360000L);
        //把之前的token移除
        JwtUtil.removeTokenByUserId(one.getId());
        //把token存入redis
        JwtUtil.putTokenToRedis(one.getId(), token);

        // log.info("-------token-------" + tokenJson.get("token"));
        //将通讯密钥存入redis
        RedisUtil.set("Communicationkey:"+one.getImId(), randomToInviteCode);

        resultJson.put("token", token);
        resultJson.put("communicationKey", randomToInviteCode);


        return new BaseResult(resultJson.toString(), key);
    }

    public UserEntity register6(String imId, String owner2) {

        String osnId = register2im(imId, owner2, null);
        if (osnId == null) {
            return null;
        }
        if (osnId.length() == 0) {
            return null;
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("U"+ System.currentTimeMillis());
        userEntity.setPassword("");
        userEntity.setType(UserTypeEnum.NORMAL.getCode());
        userEntity.setState(UserStateEnum.NORMAL.getCode());
        userEntity.setCreateTime(new Date());
        userEntity.setImNode(HOST_IP);
        userEntity.setImId(osnId);
        userEntity.setImUsername(osnId);
        userEntity.setImPwd("");
        try {
            userService.save(userEntity);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return userEntity;
    }

    @Override
    public JSONObject register3(RegisterReq req) {

        String osnId = registerOsn(req.getUsername(), req.getPassword(), req.getNickName());
        if (osnId == null) {
            return null;
        }
        if (osnId.length() == 0) {
            return null;
        }

        JSONObject result = new JSONObject();
        result.put("osnID", osnId);
        result.put("IP", HOST_IP);
        return result;
    }


    @Override
    public BaseResult login(LoginReq loginReq) {

        UserEntity one = userService.getOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, loginReq.getUsername())
                .last("limit 1")
        );

        if (one == null) {
            return BaseResult.err(ResultCodeEnum.ACCOUNT_NOT_EXIST.getRemark());
        }

        try {
            //获取数据库里面的密码，进行base64解码
            byte[] bytes = PasswordUtil.decodeBase64(one.getPassword());
            //解码之后的数据为key，进行对前端传递过来的密文进行aes解密
            //String random = PasswordUtil.decrypt(loginReq.getPassword(), PasswordUtil.parseByte2HexStr(bytes));

            //byte[] encrypted1 = Base64.getDecoder().decode(content);
            byte[] decData = OsnUtils.aesDecrypt(loginReq.getPassword(), bytes);
            String random = new String(decData);
            //比对redis里面的随机数
            String result = RedisUtil.get(sessionFlag + loginReq.getUsername());
            if (result == null){
                return BaseResult.err(ResultCodeEnum.PASSWORD_ERROR.getRemark());
            }
            System.out.println("[login] from user:" + random);
            System.out.println("[login] from redis:" + result);
            if (result.equals(random)){
                //密码正确---生成token
                String token = JwtUtil.createJwtToken(one.getId(), 315360000L);
                //清除之前的token
                JwtUtil.removeTokenByUserId(one.getId());
                //把token存入redis
                JwtUtil.putTokenToRedis(one.getId(), token);

                // 1. 生成会话aes 密钥
                // 2. 存到session 表里面
                // 3. 用password base64 decode以后，加密 token 和 会话密钥
                // String uuid = UUID.randomUUID().toString();
                String randomToInviteCode = RandomUtil.getRandomToInviteCode(16);
                RedisUtil.set("Communicationkey:"+one.getImId(), randomToInviteCode);

                //RedisUtil.setEx(conversationFlag+one.getId(), uuid, 315360000L);

                //token 和会话密钥拼接
                //String tokenConversationId = token + ":" + uuid;

                //密码aes加密返回
                //String aesEncode = new String(OsnUtils.aesEncrypt(randomToInviteCode.getBytes(), bytes));

                JSONObject json = new JSONObject();
                json.put("token", token);
                //json.put("refreshToken", token);
                json.put("communicationKey", randomToInviteCode);

                //
                json.put("osn_id", one.getImId());
                json.put("osn_username", one.getImUsername());
                json.put("osn_password", one.getImPwd());
                json.put("osn_node", one.getImNode());


                // put "json"
                json.put("json", getConfig());

                if (loginReq.getSmsCode() != null) {
                    return new BaseResult(json.toString(), loginReq.getSmsCode());
                }
                return new BaseResult(json.toString(), one.getPassword());

            }else {
                return BaseResult.err(ResultCodeEnum.PASSWORD_ERROR.getRemark());
            }
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println(e);
        }

        return BaseResult.err("I do not know.");

    }

    public JSONObject getConfig() {
        JSONObject json = new JSONObject();
        /*json.put("LOGIN_URL", LOGIN_URL);
        json.put("HOST_IP", HOST_IP);
        json.put("PEFIX_URL", PEFIX_URL);
        json.put("APP_DEVICE", APP_DEVICE);
        json.put("SET_NAME", SET_NAME);
        json.put("KEFU_LIST", KEFU_LIST);
        json.put("QueryAplets", QueryAplets);
        json.put("GroupList", GroupList);
        json.put("AddGroup", AddGroup);*/
        json.put("HOST_IP", HOST_IP);
        json.put("ENDPOINT", ENDPOINT);
        json.put("AccessKeyId", AccessKeyId);
        json.put("AccessKeySecret", AccessKeySecret);
        json.put("BUCKETNAME", BUCKETNAME);
        json.put("UserPortraitDirectory", UserPortraitDirectory);
        json.put("GroupPortraitDirectory", GroupPortraitDirectory);
        json.put("TempDirectory", TempDirectory);
        json.put("WEBRTCBASE", WEBRTCBASE);
        json.put("WEBRTCHOST", WEBRTCHOST);
        //json.put("MainDapp", MainDapp);
        json.put("UserServiceId",ltpServer.getAppId());


        return json;
    }

    /**
     * 获取随机数
     * @param
     * @return
     */
    @Override
    public BaseResult getChallenge(String username) {

        try {

            UserEntity one = userService.getOne(new LambdaQueryWrapper<UserEntity>()
                    .eq(UserEntity::getUsername, username)
                    .last("limit 1")
            );
            if (one == null){
                return BaseResult.err(ResultCodeEnum.ACCOUNT_NOT_EXIST.getRemark());
            }
            String random = RandomUtil.getRandom();
            System.out.println("gen random : " + username +
                    " + " + random);
            RedisUtil.setEx(sessionFlag+username, random, 60);

            JSONObject json = new JSONObject();
            json.put("challenge", random);
            return new BaseResult(json.toString(), one.getPassword());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return BaseResult.err("I do not know.");
    }

    public String createGroup(String groupName, String owner){
        String osnID = "" ;
        try {
            String imUrl = mImUrl;
            System.out.println("[registerOsn] imUrl:" + imUrl);
            String imPwd = mImPwd;
            System.out.println("[registerOsn] imPwd:" + imPwd);
            URL url = new URL(imUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            JSONObject json = new JSONObject();
            json.put("name", groupName);
            json.put("owner", owner);
            json.put("portrait", "");
            String body = aesEncrypt(json.toString(),imPwd);
            json.clear();
            json.put("command", "createGroup");
            json.put("data", body);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(json.toString().getBytes());
            outputStream.flush();
            outputStream.close();

            if(httpURLConnection.getResponseCode() == 200){
                InputStream responseStream = httpURLConnection.getInputStream();
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead = 0;
                while ((bytesRead = responseStream.read(buffer)) > 0) {
                    byteStream.write(buffer, 0, bytesRead);
                }
                String responseBody = byteStream.toString("utf-8");
                System.out.println("[createGroup] response:" + responseBody);
                JSONObject jsonObject = JSONObject.parseObject(responseBody);
                if(!("0:success".equals(jsonObject.getString("errCode")))){
                    return "";
                }
                json = JSON.parseObject(responseBody);
                body = json.getString("data");
                if(body != null){

                    body = aesDecrypt(body,imPwd);
                    //System.out.println(body);
                    jsonObject = JSONObject.parseObject(body);
                    osnID = jsonObject.getString("groupId");
                    System.out.println(jsonObject.getString("osnID"));
                }
            } else {
                System.out.println("error: "+httpURLConnection.getResponseCode());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return osnID;
    }



    private String registerOsn(String username, String password){
        String osnID = "" ;
        try {
            String imUrl = mImUrl;
            System.out.println("[registerOsn] imUrl:");
            System.out.println("[registerOsn] imUrl:" + imUrl);
            String imPwd = mImPwd;//cfgMap.get("imPwd");
            System.out.println("[registerOsn] imPwd:");
            System.out.println("[registerOsn] imPwd:" + imPwd);
            URL url = new URL(imUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("password", password);
            String body = aesEncrypt(json.toString(),imPwd);
            json.clear();
            json.put("command", "register");
            json.put("data", body);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(json.toString().getBytes());
            outputStream.flush();
            outputStream.close();

            if(httpURLConnection.getResponseCode() == 200){
                InputStream responseStream = httpURLConnection.getInputStream();
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead = 0;
                while ((bytesRead = responseStream.read(buffer)) > 0) {
                    byteStream.write(buffer, 0, bytesRead);
                }
                String responseBody = byteStream.toString("utf-8");
                //  System.out.println(responseBody);
                JSONObject jsonObject = JSONObject.parseObject(responseBody);
                if(!("0:success".equals(jsonObject.getString("errCode")))){
                    return "";
                }
                json = JSON.parseObject(responseBody);
                body = json.getString("data");
                if(body != null){

                    body = aesDecrypt(body,imPwd);
                    //System.out.println(body);
                    jsonObject = JSONObject.parseObject(body);
                    osnID = jsonObject.getString("osnID");
                    System.out.println(jsonObject.getString("osnID"));
                }
            } else {
                System.out.println("error: "+httpURLConnection.getResponseCode());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return osnID;
    }

    private String registerOsn(String username, String password, String nickName){
        String osnID = "" ;
        try {
            String imUrl = mImUrl;
            System.out.println("[registerOsn] imUrl:");
            System.out.println("[registerOsn] imUrl:" + imUrl);
            String imPwd = mImPwd;//cfgMap.get("imPwd");
            System.out.println("[registerOsn] imPwd:");
            System.out.println("[registerOsn] imPwd:" + imPwd);
            URL url = new URL(imUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("password", password);
            json.put("nickName", nickName);
            String body = aesEncrypt(json.toString(),imPwd);
            json.clear();
            json.put("command", "register");
            json.put("data", body);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(json.toString().getBytes());
            outputStream.flush();
            outputStream.close();

            if(httpURLConnection.getResponseCode() == 200){
                InputStream responseStream = httpURLConnection.getInputStream();
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead = 0;
                while ((bytesRead = responseStream.read(buffer)) > 0) {
                    byteStream.write(buffer, 0, bytesRead);
                }
                String responseBody = byteStream.toString("utf-8");
                //  System.out.println(responseBody);
                JSONObject jsonObject = JSONObject.parseObject(responseBody);
                if(!("0:success".equals(jsonObject.getString("errCode")))){
                    return "";
                }
                json = JSON.parseObject(responseBody);
                body = json.getString("data");
                if(body != null){

                    body = aesDecrypt(body,imPwd);
                    //System.out.println(body);
                    jsonObject = JSONObject.parseObject(body);
                    osnID = jsonObject.getString("osnID");
                    System.out.println(jsonObject.getString("osnID"));
                }
            } else {
                System.out.println("error: "+httpURLConnection.getResponseCode());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return osnID;
    }

    private String register2im(String osnId, String owner, String name) {
        String osnID = "" ;
        try {
            String imUrl = mImUrl;
            System.out.println("[registerOsn] imUrl:");
            System.out.println("[registerOsn] imUrl:" + imUrl);
            String imPwd = mImPwd;
            System.out.println("[registerOsn] imPwd:");
            System.out.println("[registerOsn] imPwd:" + imPwd);
            URL url = new URL(imUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            JSONObject json = new JSONObject();
            json.put("osnID", osnId);
            json.put("name", name);
            json.put("owner2", owner);
            String body = aesEncrypt(json.toString(),imPwd);
            json.clear();
            json.put("command", "register2");
            json.put("data", body);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(json.toString().getBytes());
            outputStream.flush();
            outputStream.close();

            if(httpURLConnection.getResponseCode() == 200){
                InputStream responseStream = httpURLConnection.getInputStream();
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead = 0;
                while ((bytesRead = responseStream.read(buffer)) > 0) {
                    byteStream.write(buffer, 0, bytesRead);
                }
                String responseBody = byteStream.toString("utf-8");
                System.out.println("[registerOsn] register result:");
                System.out.println("[registerOsn] register result:"+responseBody);
                JSONObject jsonObject = JSONObject.parseObject(responseBody);
                if(!("0:success".equals(jsonObject.getString("errCode")))){
                    return "";
                }
                json = JSON.parseObject(responseBody);
                body = json.getString("data");
                if(body != null){

                    body = aesDecrypt(body,imPwd);
                    //System.out.println(body);
                    jsonObject = JSONObject.parseObject(body);
                    osnID = jsonObject.getString("osnID");
                    System.out.println(jsonObject.getString("osnID"));
                }
            } else {
                System.out.println("error: "+httpURLConnection.getResponseCode());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return osnID;
    }

    public byte[] sha256(byte[] data){
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(data);
            data = messageDigest.digest();
        } catch (Exception e){
            e.printStackTrace();
        }
        return data;
    }

    public String aesEncrypt(String data, String key){
        try {
            byte[] iv = new byte[16];
            Arrays.fill(iv,(byte)0);
            byte[] pwdHash = sha256(key.getBytes());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(pwdHash, "AES"), new IvParameterSpec(iv));
            byte[] encData = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encData);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String aesDecrypt(String data, String key){
        try {
            byte[] iv = new byte[16];
            Arrays.fill(iv,(byte)0);
            byte[] rawData = Base64.getDecoder().decode(data);
            byte[] pwdHash = sha256(key.getBytes());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(pwdHash, "AES"), new IvParameterSpec(iv));
            rawData = cipher.doFinal(rawData);
            return new String(rawData);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
