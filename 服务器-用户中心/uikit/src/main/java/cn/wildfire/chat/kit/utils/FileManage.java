package cn.wildfire.chat.kit.utils;

import android.content.Context;

import cn.wildfire.chat.kit.litapp.FileUploadSetup;

public class FileManage {

    //FileUploadSetup fileUploadSetup;

    public static void uploadFile(Context context, FileUploadSetup fileUploadSetup, String filePath, OssHelper.CallBack callBack) {
        if (fileUploadSetup != null) {
            if (fileUploadSetup.type != null) {
                if (fileUploadSetup.type.equals("aliyun")) {
                    OssHelper.upload(context, fileUploadSetup, filePath, callBack);
                }
            }
        }

    }

}
