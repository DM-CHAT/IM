package com.mhhy.service;

import com.alibaba.fastjson.JSONObject;
import com.ospn.command.CmdDappLogin;
import com.ospn.command.CmdReDappLogin;
import com.ospn.common.OsnServer;
import com.ospn.core.ILTPEvent;
import com.ospn.core.LTPData;
import com.ospn.utils.CryptUtils;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.ospn.utils.CryptUtils.makeMessage;
import static com.ospn.utils.CryptUtils.takeMessage;

@Service
public class LtpServer extends OsnServer implements ILTPEvent {

    //@Autowired
    //public IDeviceService deviceService;
    //@Autowired
    //private MyJPushClient myJPushClient;


    public static LTPData ltpData;

    public static String custormer;

    static RunnerThread runner;

    private ConcurrentHashMap<String, List<String>> pushDataMap = new ConcurrentHashMap<>();

    static String password="password123";

    //public static String robotId = "OSNU6ng2NDGxynjQPvLfuRKcTYzkveSZaCSErj8zWcsDPaKEKYJ";
    //public static String robotKey = "VK0MHcCAQEEIC9fPiYrq8A7oGMMCTSkJvrmRvXz0RxVjJp4Nw5ZAskYoAoGCCqGSM49AwEHoUQDQgAEk09f4tJagEIn6Jp7Fxmrvaw4MBPU2I7jwZWijbHeD+fN4l7mBIR6NTFNzCndIgBXnJmyg4CA6Nmyzb/Vw4clPA==";

    public LtpServer(){
        //wait_commandService = svc;
        init();
    }

    void init(){

        if (ltpData != null){
            return;
        }
        ltpData = new LTPData();

        try {

            InputStream in = new FileInputStream("ospn.properties");
            Properties prop = new Properties();
            prop.load(in);

            custormer = prop.getProperty("customer");
            System.out.println("[custormer]" + custormer);

            ltpData.init(prop, password,this);
            in.close();

            prop.put("appID", ltpData.apps.osnID);
            prop.put("appKey", ltpData.apps.osnKey);

            FileOutputStream oFile = new FileOutputStream("ospn.properties", false);
            prop.store(oFile, "Comments");

            oFile.close();

        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    public String getAppId(){
        if (ltpData == null){
            init();
        }
        if (ltpData == null){
            return null;
        }
        return ltpData.apps.osnID;
    }

    public JSONObject makeResult(String to, JSONObject data){

        try {
            JSONObject json = CryptUtils.makeMessage(
                    "Message",
                    ltpData.apps.osnID,
                    to,
                    data,
                    ltpData.apps.osnKey,
                    (JSONObject)null);
            return json;
        } catch (Exception e) {

        }
        return new JSONObject();
    }

    public void sendCommand(String command, String groupID, JSONObject json){

        // 先扔队列里
        if (command.equalsIgnoreCase("SystemNotify")) {
            SendCommandInfo info = new SendCommandInfo(command, groupID, json);
            push(info);
        } else {
            if (ltpData != null){
                ltpData.sendJson(command, groupID, json);
            }
        }

    }

    public void sendMessage(String to, JSONObject json){
        if (ltpData != null){
            ltpData.sendMessage(to, json);
        }
    }

    public void sendNotice2UserByAddBalance(String to, long balance, String txid, JSONObject json){

        JSONObject content = new JSONObject();
        content.put("type", "text");
        float bal1 = balance;
        bal1 = bal1 / 100;

        String coin = "";
        if (json != null) {
            if (json.containsKey("coinType")) {
                coin = json.getString("coinType") + " ";
            }
        }

        String text = "[DM wallet test system notice]\r\n" +
                "Account balance changes, receive " + coin +
                String.format("%.2f.\r\n", bal1) +
                "txid:" + txid;
        content.put("data", text);

        JSONObject data = new JSONObject();
        data.put("content", content);

        // 扔队列里去
        SendCommandInfo info = new SendCommandInfo("Message", to, data);
        push(info);

        /*if (ltpData != null){
            ltpData.sendMessage(to, data);
        }*/
    }

    public void sendNotice2UserBySubBalance(String to, long balance, String txid, JSONObject json){

        JSONObject content = new JSONObject();
        content.put("type", "text");
        float bal1 = balance;
        bal1 = bal1 / 100;

        String coin = "";
        if (json != null) {
            if (json.containsKey("coinType")) {
                coin = json.getString("coinType") + " ";
            }
        }

        String text = "[DM wallet test system notice]\r\n" +
                "Account balance changes, send " + coin +
                String.format("%.2f.\r\n", bal1) +
                "txid:" + txid;
        content.put("data", text);

        JSONObject data = new JSONObject();
        data.put("content", content);

        // 扔队列里去
        SendCommandInfo info = new SendCommandInfo("Message", to, data);
        push(info);

        /*if (ltpData != null){
            ltpData.sendMessage(to, data);
        }*/
    }

    public void sendNotice2UserByWithdrawal(String to){

        JSONObject content = new JSONObject();
        content.put("type", "text");
        String text = "[J-Talking system notice]\r\n" +
                "Withdrawal success.";
        content.put("data", text);

        JSONObject data = new JSONObject();
        data.put("content", content);

        if (ltpData != null){
            ltpData.sendMessage(to, data);
        }
    }

    public boolean login(CmdDappLogin cmd){
        CmdReDappLogin cmdRe = ltpData.login(cmd);

        if (cmdRe.errCode == null){
            return false;
        }
        if (!cmdRe.errCode.equalsIgnoreCase("0:success")){
            return false;
        }

        return true;
    }

    public String getDappInfo(){
        JSONObject content = ltpData.dappInfo.toClientJson(ltpData.apps.osnKey);
        if (content == null){
            return "";
        }
        return content.toString();
    }

    public void push(SendCommandInfo data){
        if (runner == null) {
            runner = new RunnerThread();
            runner.start();
        }
        runner.push(data);
    }

    void sendNotice(JSONObject json){
        System.out.println("[sendNotice] send notice : " + json);
        String groupId = json.getString("groupID");
        if (groupId == null){
            return;
        }
        System.out.println("[sendNotice] sendCommand");
        sendCommand("SystemNotify", groupId, json);

    }


    @Override
    public void handleMessage(ChannelHandlerContext channelHandlerContext, JSONObject jsonObject) {

        //System.out.println("[LtpServer::handleMessage] begin. " + jsonObject);

        System.out.println("----------------handleMessage---------------------:"+jsonObject);
        // 处理收到的消息
        if (jsonObject == null){
            return;
        }


        String command = jsonObject.getString("command");
        String to = jsonObject.getString("to");
        if (command == null || to == null){
            return;
        }

        if (!to.equalsIgnoreCase(ltpData.apps.osnID)){
            System.out.println("[LtpServer::handleMessage] error, to is " + to);
            return;
        }


        //System.out.println("[handleMessage] begin!!!!!!");


        if(command.equalsIgnoreCase("Message")){

            JSONObject data = takeMessage(jsonObject);
            if(data == null) {
                return;
            }
            String command2 = data.getString("command");
            if (command2 != null) {
                if (command2.equalsIgnoreCase("PushInfo")) {

                    String user = data.getString("user");
                    String type = data.getString("type");

                    List<String> cmdList = pushDataMap.get(user);
                    if (cmdList == null) {
                        cmdList = new ArrayList<>();
                        pushDataMap.put(user, cmdList);
                    }
                    cmdList.add(type);



                    return;
                }
            }


            String commandID = data.getString("commandID");
            if (commandID == null){
                return;
            }
            System.out.println("commandID : " + commandID);
            String result = data.getString("result");
            if (result == null){
                return;
            }



        } else if (command.equalsIgnoreCase("GetServiceInfo")){

            String from = jsonObject.getString("from");
            if (from == null){
                return;
            }

            JSONObject content = ltpData.dappInfo.toClientJson(ltpData.apps.osnKey);

            System.out.println("[LtpServer::HandleMessage] dapp content : " + content);

            JSONObject original = new JSONObject();
            original.put("id", jsonObject.getString("id"));

            System.out.println("[LtpServer::HandleMessage] original id : " + original);

            JSONObject returnData = makeMessage(
                    "ServiceInfo",
                    ltpData.apps.osnID,
                    from,
                    content,
                    ltpData.apps.osnKey,
                    original);

            ltpData.sendJson(returnData);

        }


    }

    public void pushWorker(){

        ConcurrentHashMap<String, List<String>> tempPushMap = pushDataMap;
        System.out.println("push data map size : " + tempPushMap.size());

        pushDataMap = new ConcurrentHashMap<>();

        System.out.println("push data map size : " + tempPushMap.size());
        Iterator<ConcurrentHashMap.Entry<String, List<String>>> iter = tempPushMap.entrySet().iterator();

        while (iter.hasNext()) {
            ConcurrentHashMap.Entry<String, List<String>> entry = iter.next();
           // pushMessage(entry.getKey(), entry.getValue());
        }

    }
    //
    //public void pushMessage(String user, List<String> cmdList){
    //
    //    // 根据device表中的vendor决定使用什么推送
    //    // iphone
    //    // Xiaomi 使用小米推送
    //    // huawei
    //    // OPPO
    //    // vivo
    //    // FCM 使用极光
    //
    //    System.out.println("推送开始------");
    //
    //    String content = "You have " +cmdList.size()+ " new message.";
    //    String alias = genAlias(user);
    //
    //    Device device = deviceService.getByOsnId(user);
    //    if (device != null) {
    //        String vendor = device.getVendor();
    //        switch (vendor) {
    //
    //            case "Xiaomi":
    //                pushToXiaomi(alias, "new message", content);
    //                break;
    //
    //            case "apple":
    //                pushJiguangToId(device.getDevice(), "new message", content);
    //                break;
    //
    //            //default:
    //            //    pushJiguang(alias, "new message", content);
    //            //    break;
    //            default:
    //                firebase(device.getDevice(), "new message", content);
    //                break;
    //        }
    //
    //    }
    //
    //
    //
    //}

    //void firebase(String device, String title, String content){
    //    myJPushClient.sendMsgFirebase(device, title, content);
    //}
    //
    //void pushToXiaomi(String alias, String title, String content){
    //
    //    Constants.useOfficial();
    //    Sender sender = new Sender("6jzbNMq0oMzsXQFCuSIeYA==");
    //    String messagePayload = "This is a message";
    //    String description = content;
    //    com.xiaomi.xmpush.server.Message message = new com.xiaomi.xmpush.server.Message.Builder()
    //            .title(title)
    //            .description(description).payload(messagePayload)
    //            .restrictedPackageName("cn.net.shuting.sns.weiliao")
    //            .notifyType(2)     // 使用默认提示音提示
    //            .extra(Constants.EXTRA_PARAM_NOTIFY_EFFECT, Constants.NOTIFY_LAUNCHER_ACTIVITY)
    //            .build();
    //
    //
    //    try {
    //        Result result = sender.sendToAlias(message, alias, 3);
    //        /*Log.v("Server response: ", "MessageId: " + result.getMessageId()
    //            + " ErrorCode: " + result.getErrorCode().toString()
    //            + " Reason: " + result.getReason());*/
    //    } catch (Exception e) {
    //
    //    }
    //
    //
    //}
    //
    //void pushJiguang(String alias, String title, String content){
    //    // 推送数据
    //    Message message = new Message();
    //
    //    System.out.println("push alias : " + alias);
    //
    //    message.setUserId(alias);
    //
    //    message.setMessageContent(content);
    //
    //    myJPushClient.sendMsg(message);
    //}
    //
    //void pushJiguangToId(String registerId, String title, String content){
    //    // 推送数据
    //    Message message = new Message();
    //
    //    System.out.println("push alias : " + registerId);
    //
    //    message.setUserId(registerId);
    //
    //    message.setMessageContent(content);
    //
    //    myJPushClient.sendMsgToId(message);
    //}

    static byte[] ToMD5(String str) {
        // 加密后的16进制字符串
        String hexStr = "";
        try {
            // 此 MessageDigest 类为应用程序提供信息摘要算法的功能
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            // 转换为MD5码
            byte[] digest = md5.digest(str.getBytes("utf-8"));
            return digest;
            //hexStr = ByteUtils.toHexString(digest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static String genAlias(String user) {
        String alias = Base64.getUrlEncoder().encodeToString(ToMD5(user));
        return alias;
    }




    @Data
    class SendCommandInfo{
        String command;
        String groupID;
        JSONObject json;
        long createTime;

        public SendCommandInfo(String command, String to, JSONObject json){
            this.command = command;
            this.groupID = to;
            this.json = json;
            createTime = System.currentTimeMillis();
        }

    }

    class RunnerThread extends Thread {

        final ConcurrentLinkedQueue<SendCommandInfo> dataList = new ConcurrentLinkedQueue<>();

        public RunnerThread(){
        }

        public void push(SendCommandInfo data) {

            dataList.offer(data);
            synchronized (dataList){
                dataList.notify();
            }
        }

        @Override
        public void run(){

            while(true){
                try{
                    if(dataList.isEmpty()){
                        synchronized (dataList){
                            dataList.wait();
                        }
                    }
                    SendCommandInfo data = dataList.poll();

                    try {
                        long createTime = data.getCreateTime();
                        if (createTime + 5000 > System.currentTimeMillis()) {
                            Thread.sleep(5000);
                        }

                        if (ltpData != null){
                            ltpData.sendJson(data.getCommand(), data.getGroupID(), data.getJson());
                        }

                    } catch (Exception e) {

                    }

                }catch (Exception e){
                    //logError(e);
                }
            }
        }

    }

}
