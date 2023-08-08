/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.ext;

import static cn.wildfire.chat.kit.third.utils.FileUtils.getFileNameFromPath;
import static cn.wildfire.chat.kit.utils.FileUtils.*;
import static cn.wildfire.chat.kit.utils.OssHelper.TempDirectory;
import static cn.wildfirechat.model.Conversation.ConversationType.Group;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.afollestad.materialdialogs.BuildConfig;
import com.molihuan.pathselector.PathSelector;
import com.molihuan.pathselector.adapters.FileListAdapter;
import com.molihuan.pathselector.adapters.TabbarFileListAdapter;
import com.molihuan.pathselector.dao.SelectOptions;
import com.molihuan.pathselector.entities.FileBean;
import com.molihuan.pathselector.utils.Constants;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.annotation.ExtContextMenuItem;
import cn.wildfire.chat.kit.conversation.ext.core.ConversationExt;
import cn.wildfire.chat.kit.third.utils.ImageUtils;
import cn.wildfire.chat.kit.third.utils.UIUtils;
import cn.wildfire.chat.kit.utils.CompressUtil;
import cn.wildfire.chat.kit.utils.FileChooseUtil;
import cn.wildfire.chat.kit.utils.FileOpen;
import cn.wildfire.chat.kit.utils.FileUtil;
import cn.wildfire.chat.kit.utils.FileUtils;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.utils.OssHelper;
import cn.wildfire.chat.kit.utils.PathUtils;
import cn.wildfire.chat.kit.utils.ProgresDialog;
import cn.wildfirechat.message.TypingMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.remote.ChatManager;

public class FileExt extends ConversationExt {


    /**
     * @param containerView 扩展view的container
     * @param conversation
     */
    @ExtContextMenuItem
    public void pickFile(View containerView, Conversation conversation) {
        if(conversation.type == Group) {
            GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(conversation.target,false);
            int timeInterval = groupInfo.getTimeInterval();
            String time1 = groupInfo.timeInterval;
            long timeNow = System.currentTimeMillis();
            long timeLast = 0;
            if(time1 != null){
                timeLast = Long.valueOf(time1);
            }

            if (timeLast + (long)timeInterval * 1000 > timeNow) {
                Toast.makeText(activity, R.string.input_speed_too_fast, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);   //最近文件
        //   Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);//新建intent对象，参数为调用系统的文件管理系统。
        intent.setType("*/*");//无类型限制
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 100);
        TypingMessageContent content = new TypingMessageContent(TypingMessageContent.TYPING_FILE);
        messageViewModel.sendMessage(conversation, content);

        //如果没有权限会自动申请权限
        /*PathSelector.build(activity, Constants.BUILD_ACTIVITY)//跳转Activity方式
                .requestCode(10011)//请求码
                //toolbar选项
                .setMoreOPtions(new String[]{"选择"},
                        new boolean[]{true},//选择后结束掉Activity结果会给到onActivityResult()
                        new SelectOptions.onToolbarOptionsListener() {
                            @Override
                            public void onOptionClick(View view, String currentPath, List<FileBean> fileBeanList, List<String> callBackData, TabbarFileListAdapter tabbarAdapter, FileListAdapter fileAdapter, List<FileBean> callBackFileBeanList) {
                                //for (String callBackDatum : callBackData) {
                                //Mtools.toast(getBaseContext(),callBackDatum);//也可一在这里拿到选择的结果
                                //}
                            }
                        }
                )
                .start();//开始构建*/

    }
    private String getFileName(String path) {
        int pos = path.lastIndexOf('/');
        if (pos > -1) {
            String name = path.substring(pos + 1);
            return name;
        }

        return null;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {


            Uri uri = data.getData();
            String pathSrc = getPath(activity, uri);


            if (!FileUtil.isFileExist(pathSrc) ){
                Toast.makeText(activity, "file note exist", Toast.LENGTH_SHORT).show();
                return;
            }


            long fileSize = FileUtil.getFileSize(pathSrc);
            if (fileSize == 0) {
                Toast.makeText(activity, "文件损坏", Toast.LENGTH_SHORT).show();
                return;
            }
            if(fileSize >= 51200000 ){
                Toast.makeText(activity, "文件不允许超过50M", Toast.LENGTH_SHORT).show();
                return;
            }





            //int pos = path.lastIndexOf(".");
            //String type = pos > 0 ? path.substring(pos) : ".";

            String type = FileUtil.getFileType(pathSrc);

            //随机文件名
            //Random random = new Random();
            //int number = random.nextInt(899999);
            //number = number + 100000;
            //String fileType = OssHelper.getFileType(path);
            //String fileName = String.valueOf(System.currentTimeMillis()) + number + fileType;
            String path;

            switch (type) {
                case ".png":
                case ".jpg":
                case ".jpeg":
                case ".gif":

                    /*String paths = FileUtil.getTempDir(this.activity);

                    String pwd = CompressUtil.getPsw(8);
                    conversation.decKey = pwd;
                    String zipPath = CompressUtil.zip(path,paths+"/", pwd);
                    if (zipPath == null) {
                        conversation.decKey = null;
                        zipPath = path;
                    }
                    String remotePath = OssHelper.getInstance(activity).uploadImage(activity,zipPath,fileName,OssHelper.TempDirectory);
                    FileUtil.deleteSingleFile(zipPath);

                    System.out.println("[FileExt] source path:"+path2);
                    System.out.println("[FileExt] temp path:"+path);
                    System.out.println("[FileExt] zip Path:"+zipPath);

             //       OssHelper.getInstance(activity).uploadImage(activity,path,fileName, TempDirectory);
                    //照片
                    if (remotePath != null) {
                        //照片
                        UIUtils.postTaskSafely(() ->messageViewModel.sendImgMsg(conversation,
                                ImageUtils.genThumbImgFile(path),
                                new File(path),
                                remotePath));
                    } else {
                        Toast.makeText(activity,"发送失败",Toast.LENGTH_SHORT).show();
                    }*/

                    path = FileUtil.copy(activity, pathSrc);
                    sendPictureByEncrypt(path);

                    break;

                case ".3gp":
                case ".mpg":
                case ".mpeg":
                case ".mpe":
                case ".mp4":
                case ".avi":


                    // 创建 加密key 随机字符串
                    // zip

                    /*String paths = FileUtil.getTempDir(this.activity);
                    String pwd = CompressUtil.getPsw(8);
                    conversation.decKey = pwd;
                    String zipPath = CompressUtil.zip(path,paths+"/", pwd);
                    if (zipPath == null) {
                        conversation.decKey = null;
                        zipPath = path;
                    }

                    OssHelper.getInstance(activity).uploadFile(path, OssHelper.TempDirectory, new OssHelper.CallBack() {
                        @Override
                        public void success() {
                            // 把password传到下层
                            //messageViewModel.sendVideoMsg(conversation, file,OssHelper.getInstance(activity).getMediaRemoteUrl(path, OssHelper.TempDirectory));
                        }

                        @Override
                        public void success(String remoteUrl, String fileName) {
                            messageViewModel.sendVideoMsg(conversation, file,remoteUrl);
                        }

                        @Override
                        public void fail() {

                        }
                    });*/
                    path = FileUtil.copy(activity, pathSrc);
                    sendVideoByEncrypt(path, new File(path));
                    break;


                default:

                    String pathSrcReal = PathUtils.getFilePathForN(activity,uri);
                    String name = getFileName(pathSrcReal);
                    if (TextUtils.isEmpty(pathSrc)) {
                        Toast.makeText(activity, WfcUIKit.getString(R.string.select_file_error), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    System.out.println("[FileExt] send file:" + pathSrc);
                    sendFileByEncrypt(pathSrc, new File(pathSrc), name);
                    /*String name = getFileName(pathSrc);
                    File file = new File(pathSrc);
                    OssHelper.getInstance(activity).uploadFile(pathSrc, OssHelper.TempDirectory, new OssHelper.CallBack() {
                        @Override
                        public void success() {
                            messageViewModel.sendFileMsg(conversation, file,OssHelper.getInstance(activity).getMediaRemoteUrl(pathSrc, OssHelper.TempDirectory),name);
                        }

                        @Override
                        public void success(String remote, String fileName) {
                            messageViewModel.sendFileMsg(conversation, file, remote, name);
                        }

                        @Override
                        public void fail() {
                        }
                    });*/
                    break;
            }
        }
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_func_file;
    }

    @Override
    public String title(Context context) {
        return WfcUIKit.getString(R.string.file);
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        return title(context);
    }

    private void sendPictureByEncrypt(String path) {

        ProgresDialog progresDialog = new ProgresDialog(this.activity);
        progresDialog.show();

        ChatManager.Instance().getWorkHandler().post(() -> {

            String tempDir = FileUtil.getTempDir(this.activity);

            String pwd = CompressUtil.getPsw(8);
            conversation.decKey = pwd;
            String zipPath = CompressUtil.zip(path, tempDir + "/", pwd);
            if (zipPath == null) {
                conversation.decKey = null;
                zipPath = path;
            }
            String remotePath = OssHelper.getInstance(activity).uploadImage(activity, zipPath, null, OssHelper.TempDirectory);
            FileUtil.deleteSingleFile(zipPath);

            //System.out.println("[FileExt] source path:"+path2);
            System.out.println("[FileExt] temp path:" + path);
            System.out.println("[FileExt] zip Path:" + zipPath);

            progresDialog.dismiss();

            //照片
            if (remotePath != null) {
                //照片
                UIUtils.postTaskSafely(() -> messageViewModel.sendImgMsg(conversation,
                        ImageUtils.genThumbImgFile(path),
                        new File(path),
                        remotePath));
            } else {
                Toast.makeText(activity, "发送失败", Toast.LENGTH_SHORT).show();
            }

        });

    }

    private void sendVideoByEncrypt(String path, File file) {

        ProgresDialog progresDialog = new ProgresDialog(this.activity);
        progresDialog.show();

        ChatManager.Instance().getWorkHandler().post(() -> {
            //Toast.makeText(activity,"sendVideoByEncrypt begin.",Toast.LENGTH_SHORT).show();
            String tempDir = FileUtil.getTempDir(this.activity);
            String pwd = CompressUtil.getPsw(8);
            conversation.decKey = pwd;
            String zipPath = CompressUtil.zip(path,tempDir+"/", pwd);
            if (zipPath == null) {
                conversation.decKey = null;
                zipPath = path;
            }

            OssHelper.getInstance(activity).uploadFileDeleteTempFile(zipPath, OssHelper.TempDirectory, new OssHelper.CallBack() {
                @Override
                public void success() {
                    // 把password传到下层
                    //messageViewModel.sendVideoMsg(conversation, file,OssHelper.getInstance(activity).getMediaRemoteUrl(path, OssHelper.TempDirectory));
                }

                @Override
                public void success(String remoteUrl, String fileName) {
                    progresDialog.dismiss();
                    messageViewModel.sendVideoMsg(conversation, file, remoteUrl);
                    Toast.makeText(activity,"Upload file complete.",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void fail() {
                    progresDialog.dismiss();
                    Toast.makeText(activity,"Upload file failed.",Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendFileByEncrypt(String path, File file, String name) {

        ProgresDialog progresDialog = new ProgresDialog(this.activity);
        progresDialog.show();

        ChatManager.Instance().getWorkHandler().post(() -> {

            String tempDir = FileUtil.getTempDir(this.activity);

            String pwd = CompressUtil.getPsw(8);
            conversation.decKey = pwd;


            String tempFilePath = FileUtil.copy(this.activity, path, name);
            String zipPath = CompressUtil.zip(tempFilePath, tempDir + "/", pwd);
            FileUtil.deleteSingleFile(tempFilePath);
            if (zipPath == null) {
                System.out.println("[FileExt] compress failed.");
                conversation.decKey = null;
                zipPath = path;
            } else {
                System.out.println("[FileExt] compress success.");
            }
            String remotePath = OssHelper.getInstance(activity).uploadImage(activity, zipPath, null, OssHelper.TempDirectory);
            FileUtil.deleteSingleFile(zipPath);

            //System.out.println("[FileExt] source path:"+path2);
            System.out.println("[FileExt] temp path:" + path);
            System.out.println("[FileExt] zip Path:" + zipPath);

            progresDialog.dismiss();

            //照片
            if (remotePath != null) {
                //照片
                messageViewModel.sendFileMsg(conversation, file, remotePath, name);
            } else {
                Toast.makeText(activity, "发送失败", Toast.LENGTH_SHORT).show();
            }

        });



    }
}
