/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.ext;

import static cn.wildfire.chat.kit.utils.OssHelper.TempDirectory;
import static cn.wildfirechat.model.Conversation.ConversationType.Group;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.lqr.imagepicker.ImagePicker;
import com.lqr.imagepicker.bean.ImageItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.annotation.ExtContextMenuItem;
import cn.wildfire.chat.kit.conversation.ext.core.ConversationExt;
import cn.wildfire.chat.kit.third.utils.FileUtils;
import cn.wildfire.chat.kit.third.utils.ImageUtils;
import cn.wildfire.chat.kit.third.utils.UIUtils;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.utils.CompressOperate_zip4j;
import cn.wildfire.chat.kit.utils.CompressUtil;
import cn.wildfire.chat.kit.utils.FileUtil;
import cn.wildfire.chat.kit.utils.OssHelper;
import cn.wildfire.chat.kit.utils.ProgresDialog;
import cn.wildfirechat.message.TypingMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.remote.ChatManager;

public class ImageExt extends ConversationExt {


    /**
     * @param containerView 扩展view的container
     * @param conversation
     */
    @ExtContextMenuItem
    public void pickImage(View containerView, Conversation conversation) {
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

        Intent intent = ImagePicker.picker().showCamera(true).enableMultiMode(9).buildPickIntent(activity);
        startActivityForResult(intent, 100);
        TypingMessageContent content = new TypingMessageContent(TypingMessageContent.TYPING_CAMERA);
        messageViewModel.sendMessage(conversation, content);
    }
    private boolean isGifFile(String file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            int[] flags = new int[5];
            flags[0] = inputStream.read();
            flags[1] = inputStream.read();
            flags[2] = inputStream.read();
            flags[3] = inputStream.read();
            inputStream.skip(inputStream.available() - 1);
            flags[4] = inputStream.read();
            inputStream.close();
            return flags[0] == 71 && flags[1] == 73 && flags[2] == 70 && flags[3] == 56 && flags[4] == 0x3B;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                ProgresDialog progresDialog = new ProgresDialog(this.activity);
                progresDialog.show();

                ChatManager.Instance().getWorkHandler().post(() -> {

                    //是否发送原图
                    boolean compress = data.getBooleanExtra(ImagePicker.EXTRA_COMPRESS, true);
                    ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                    String pwd = CompressUtil.getPsw(8);

                    int i = 1;
                    for (ImageItem imageItem : images) {
                        /*boolean isGif = isGifFile(imageItem.path);
                        if (isGif) {
                            UIUtils.postTaskSafely(() -> messageViewModel.sendStickerMsg(conversation, imageItem.path, null));
                            continue;
                        }*/
                        /*File imageFileThumb;
                        File imageFileSource = null;
                        // FIXME: 2018/11/29 压缩, 不是发原图的时候，大图需要进行压缩
                        if (compress) {
                            imageFileSource = ImageUtils.compressImage(imageItem.path);
                        }
                        imageFileSource = imageFileSource == null ? new File(imageItem.path) : imageFileSource;
//                    if (isOrig) {
//                    imageFileSource = new File(imageItem.path);
                        imageFileThumb = ImageUtils.genThumbImgFile(imageItem.path);
                        if (imageFileThumb == null) {
                            Log.e("ImageExt", "gen image thumb fail");
                            return;
                        }*/
//                    } else {
//                        //压缩图片
//                        // TODO  压缩的有问题
//                        imageFileSource = ImageUtils.genThumbImgFileEx(imageItem.path);
//                        //imageFileThumb = ImageUtils.genThumbImgFile(imageFileSource.getAbsolutePath());
//                        imageFileThumb = imageFileSource;
//                    }
//                            messageViewModel.sendImgMsg(conversation, imageFileThumb, imageFileSource);

                        //File finalImageFileSource = imageFileSource;
                        Random random = new Random();
                        int number = random.nextInt(899999);
                        number = number + 100000;
                        String fileType = OssHelper.getFileType(imageItem.path);
                        String fileName = String.valueOf(System.currentTimeMillis()) + number + fileType;


                        String paths = FileUtil.getTempDir(this.activity);
                        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                            paths = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
                        }*/

                        conversation.decKey = pwd;

                        String tmpPath = FileUtil.copy(this.activity, imageItem.path);
                        String zipPath = CompressUtil.zip(tmpPath,paths+"/", pwd);
                        if (zipPath == null) {
                            conversation.decKey = null;
                            zipPath = tmpPath;
                        }

                        String remotePath = OssHelper.getInstance(activity).uploadImage(activity,zipPath,fileName,OssHelper.TempDirectory);
                        FileUtil.deleteSingleFile(zipPath);

                        System.out.println("[ImageExt] source path:"+imageItem.path);
                        System.out.println("[ImageExt] temp path:"+tmpPath);
                        System.out.println("[ImageExt] zip Path:"+zipPath);


                        if (remotePath != null) {
                            //照片
                            UIUtils.postTaskSafely(() ->messageViewModel.sendImgMsg(conversation,
                                    ImageUtils.genThumbImgFile(imageItem.path),
                                    new File(tmpPath),
                                    remotePath));
                        } else {
                            Toast.makeText(activity,"发送失败",Toast.LENGTH_SHORT).show();
                        }

                    }
                    progresDialog.dismiss();

                });

            }
        }
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_func_pic;
    }

    @Override
    public String title(Context context) {
        return WfcUIKit.getString(R.string.photo);
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        return title(context);
    }
}
