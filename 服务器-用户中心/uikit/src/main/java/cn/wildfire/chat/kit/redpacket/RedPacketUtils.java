package cn.wildfire.chat.kit.redpacket;

import android.content.Context;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.wildfire.chat.kit.R;

public class RedPacketUtils {
    public static JSONObject getData(Context context, String data){
        try{
            JSONObject json = JSON.parseObject(data);
            int code = json.getIntValue("code");
            if(code != 200) {
                if(code == 40000004){
                    json = json.getJSONObject("msg");
                    //Toast.makeText(context, context.getString(R.string.pwd_error_hit)+json.getString("error_count"), Toast.LENGTH_SHORT).show();
                    Toast.makeText(context, json.getString("error_count"), Toast.LENGTH_LONG).show();
                }else{
                    //Toast.makeText(context, context.getString(R.string.error_hit) + json.getString("msg"), Toast.LENGTH_SHORT).show();
                    Toast.makeText(context, json.getString("msg"), Toast.LENGTH_LONG).show();
                }
                return null;
            }
            return json.getJSONObject("data");
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
