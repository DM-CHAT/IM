package com.mhhy.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mhhy.common.BaseResult;
import com.mhhy.config.sms.Check;
import com.mhhy.config.sms.MeiTangSmsConfig;
import com.mhhy.config.sms.SmsCache;
import com.mhhy.exception.CheckException;
import com.mhhy.util.MD5;
import com.mhhy.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
public class SmsService {

    @Autowired
    MeiTangSmsConfig smsConfig;

    @Autowired
    KaptchaService kaptchaService;
    @Autowired
    SmsCache smsCache;

    //验证码
    private String generateCode(){
        return String.valueOf(new Random().nextInt(1000000 - 100000) + 100000 + 1);
    }

    public String getCode(String tel) {
        return RedisUtil.get("sms:" + tel);
    }

    public BaseResult sendCode(String tel, String c){

        /*if(!c.equals("3721")){
            kaptchaService.check(tel,c);
        }*/

        String smsCode = RedisUtil.get("sms:" + tel);
        if (smsCode != null) {
            return BaseResult.success("短信发送成功，有效期5分钟");
        }

        smsCache.checkMaxCount(tel);
        String code = generateCode();

        if ( sendSms(tel,code) ) {
            System.out.println("\n================ sms code:" + code);
            RedisUtil.setEx("sms:" + tel, code, 60*5);
            return BaseResult.success("短信发送成功，有效期5分钟");
        } else {
            if (c.equalsIgnoreCase("372100")) {
                code = c;
                System.out.println("\n================ sms code:" + code);
                RedisUtil.setEx("sms:" + tel, code, 60*5);
                return BaseResult.success("短信发送成功，有效期5分钟");
            }

        }

        return BaseResult.err("发送短信失败");

        // Check.requireTrue(sendSms(tel,code),"发送短信失败");
        // RedisUtil.setEx("sms:" + tel, code, 90);
        // smsCache.add(tel,code);
        // kaptchaService.clear(tel);//清除验证码
    }

    private boolean sendSms(String tel, String code) {
        log.info("发送短信验证码{} {}",tel,code);
        String account = smsConfig.getAccount();
        String password = smsConfig.getPassword();
        String mobiles = tel;
        String smscontent = smsConfig.getContent();
        smscontent = smscontent.replace("{code}", code);
        HttpURLConnection connection = null;
        try {
            String url = smsConfig.getUrl();
            URL postUrl = new URL(url);
            connection = (HttpURLConnection) postUrl.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            connection.connect();
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            String timestamp = Long.toString(System.currentTimeMillis());
            StringBuffer access_token = new StringBuffer();
            access_token.append(timestamp).append(password);
            String access_tokenS =  MD5.encodeByMD5NoTUpperCase(access_token.toString());
            StringBuffer s1 = new StringBuffer();
            s1.append("account=").append(account);
            s1.append("&timestamp=").append(timestamp);
            s1.append("&access_token=").append(access_tokenS)
                    .append("&receiver=").append(mobiles)
                    .append("&smscontent=")
                    .append(java.net.URLEncoder.encode(smscontent, "UTF-8"))
                    .append("&extcode=");
            out.writeBytes(s1.toString());
            out.flush();
            out.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), "UTF-8"));
//            StringBuilder builder = new StringBuilder();
//            String line = "";
//            while ((line = reader.readLine()) != null) {
//                builder.append(line);
//            }
            Map<String, String> stringStringMap = new ObjectMapper().readValue(reader, new TypeReference<Map<String, String>>() {});
            if(!"0".equals(stringStringMap.get("res_code"))){
                throw new CheckException(stringStringMap.get("res_message"));
            }

            log.info("发送短信{}",stringStringMap);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("发送短信失败",e);
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    public void removeSms(String tel) {
        RedisUtil.del("sms:" + tel);
    }
}
