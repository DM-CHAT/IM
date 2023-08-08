/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.ext;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.Random;

import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.WfcBaseActivity1;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.annotation.ExtContextMenuItem;
import cn.wildfire.chat.kit.conversation.ext.core.ConversationExt;
import cn.wildfire.chat.kit.mm.TakePhotoActivity;
import cn.wildfire.chat.kit.third.utils.ImageUtils;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.utils.CompressUtil;
import cn.wildfire.chat.kit.utils.FileUtil;
import cn.wildfire.chat.kit.utils.OssHelper;
import cn.wildfire.chat.kit.utils.ProgresDialog;
import cn.wildfirechat.message.TypingMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.remote.ChatManager;

import static android.app.Activity.RESULT_OK;
import static cn.wildfire.chat.kit.utils.OssHelper.TempDirectory;
import static cn.wildfirechat.model.Conversation.ConversationType.Group;

public class ShootExt extends ConversationExt {

    /**
     * @param containerView 扩展view的container
     * @param conversation
     */
    @ExtContextMenuItem
    public void shoot(View containerView, Conversation conversation) {
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

        String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!((WfcBaseActivity1) activity).checkPermission(permissions)) {
                activity.requestPermissions(permissions, 100);
                return;
            }
        }
        Intent intent = new Intent(activity, TakePhotoActivity.class);
        startActivityForResult(intent, 100);

        /*Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);// 照相机拍照
        // 需要说明一下，以下操作使用照相机拍照，
        // 拍照后的图片会存放在相册中的,这里使用的这种方式有一个好处就是获取的图片是拍照后的原图，
        // 如果不实用ContentValues存放照片路径的话，拍照后获取的图片为缩略图不清晰
        ContentValues values = new ContentValues();
        Uri photoUri = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, 100);*/

        TypingMessageContent content = new TypingMessageContent(TypingMessageContent.TYPING_CAMERA);
        messageViewModel.sendMessage(conversation, content);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String path = data.getStringExtra("path");
            if (TextUtils.isEmpty(path)) {
                Toast.makeText(activity, "Photo error, please give us feedback", Toast.LENGTH_SHORT).show();
                return;
            }

            if (data.getBooleanExtra("take_photo", true)) {
                //随机文件名
                Random random = new Random();
                int number = random.nextInt(899999);
                number = number + 100000;
                String fileType = OssHelper.getFileType(path);
                String fileName = String.valueOf(System.currentTimeMillis()) + number + fileType;

                String paths = FileUtil.getTempDir(this.activity);
                /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    paths = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
                }*/
                String pwd = CompressUtil.getPsw(8);
                conversation.decKey = pwd;
                String zipPath = CompressUtil.zip(path,paths+"/", pwd);
                String remotePath = OssHelper.getInstance(activity).uploadImage(activity,zipPath,fileName,OssHelper.TempDirectory);

                FileUtil.deleteSingleFile(zipPath);

                System.out.println("[ShootExt] source path:"+ path);
                System.out.println("[ShootExt] temp path:"+ path);
                System.out.println("[ShootExt] zip Path:"+zipPath);

       //         OssHelper.getInstance(activity).uploadImage(activity,path,fileName, TempDirectory);
                //照片
                if (remotePath != null) {
                    messageViewModel.sendImgMsg(conversation, ImageUtils.genThumbImgFile(path), new File(path),
                            remotePath);
                } else {
                    Toast.makeText(activity,"发送失败",Toast.LENGTH_SHORT).show();
                }
            } else {

                sendVideoByEncrypt(path);
                //小视频
                /*String paths = FileUtil.getTempDir(this.activity);
                String pwd = CompressUtil.getPsw(8);
                conversation.decKey = pwd;
                String zipPath = CompressUtil.zip(path,paths+"/", pwd);
                OssHelper.getInstance(activity).uploadFileDeleteTempFile(zipPath, TempDirectory, new OssHelper.CallBack() {
                    @Override
                    public void success() {
                    }

                    @Override
                    public void success(String remote, String filename) {

                        messageViewModel.sendVideoMsg(conversation, new File(path), remote);
                    }

                    @Override
                    public void fail() {

                    }
                });*/
            }
        }
    }

    private void sendVideoByEncrypt(String path) {
        ProgresDialog progresDialog = new ProgresDialog(this.activity);
        progresDialog.show();

        ChatManager.Instance().getWorkHandler().post(() -> {
            //小视频
            String tempDir = FileUtil.getTempDir(this.activity);

            String pwd = CompressUtil.getPsw(8);
            conversation.decKey = pwd;
            String zipPath = CompressUtil.zip(path,tempDir+"/", pwd);
            OssHelper.getInstance(activity).uploadFileDeleteTempFile(zipPath, TempDirectory, new OssHelper.CallBack() {
                @Override
                public void success() {
                }

                @Override
                public void success(String remote, String filename) {
                    progresDialog.dismiss();
                    messageViewModel.sendVideoMsg(conversation, new File(path), remote);
                }

                @Override
                public void fail() {
                    progresDialog.dismiss();
                    Toast.makeText(activity,"Upload file failed.",Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_func_shot;
    }

    @Override
    public String title(Context context) {
        return WfcUIKit.getString(R.string.shot);
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        return title(context);
    }
}
