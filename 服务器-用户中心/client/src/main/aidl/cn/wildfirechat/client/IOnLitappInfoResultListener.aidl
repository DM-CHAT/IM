// IOnReceiveMessage.aidl
package cn.wildfirechat.client;

// Declare any non-default types here with import statements
import cn.wildfirechat.model.LitappInfo;

interface IOnLitappInfoResultListener {
    void onLitappInfoResult(in List<LitappInfo> litappInfos);
}
