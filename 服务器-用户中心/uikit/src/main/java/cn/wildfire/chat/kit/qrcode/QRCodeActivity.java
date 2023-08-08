/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.qrcode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.king.zxing.util.CodeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.kit.GlideApp;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.WfcBaseActivity2;
import cn.wildfire.chat.kit.third.utils.UIUtils;

public class QRCodeActivity extends WfcBaseActivity2 {
    private String title;
    private String logoUrl;
    private String qrCodeValue;
    private String rawData;
    private String name;
    private String uid;
    private Bitmap screenShotBm;

    @BindView(R2.id.qrCodeImageView)
    ImageView qrCodeImageView;
    @BindView(R2.id.iv_user_head)
    ImageView iv_user_head;
    @BindView(R2.id.tv_user_name)
    TextView tv_user_name;
    @BindView(R2.id.copyButton)
    Button copyButton;
    @BindView(R2.id.tv_user_uid)
    TextView tv_user_uid;

    public static Intent buildQRCodeIntent(Context context, String title, String logoUrl, String qrCodeValue, String rawData) {
        Intent intent = new Intent(context, QRCodeActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("logoUrl", logoUrl);
        intent.putExtra("qrCodeValue", qrCodeValue);
        intent.putExtra("rawData", rawData);
        return intent;
    }

    public static Intent buildQRCodeIntent(Context context, String title, String logoUrl, String qrCodeValue, String rawData,String name,String uid) {
        Intent intent = new Intent(context, QRCodeActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("logoUrl", logoUrl);
        intent.putExtra("qrCodeValue", qrCodeValue);
        intent.putExtra("rawData", rawData);
        intent.putExtra("name", name);
        intent.putExtra("uid",uid);
        return intent;
    }
    @Override
    protected void beforeViews() {
        super.beforeViews();
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        qrCodeValue = intent.getStringExtra("qrCodeValue");
        logoUrl = intent.getStringExtra("logoUrl");
        rawData = intent.getStringExtra("rawData");
        name=intent.getStringExtra("name");
        uid = intent.getStringExtra("uid");
    }

    @Override
    protected int contentLayout() {
        return R.layout.qrcode_activity;
    }

    @Override
    protected void afterViews() {
        yincangToolbar();
        setTitle(title);
        genQRCode();
    }

    private void genQRCode() {
        tv_user_name.setText(name);
        tv_user_uid.setText("ID: "+uid);
        Bitmap bitmap = CodeUtils.createQRCode(qrCodeValue, 400, null);
        GlideApp.with(this).load(bitmap).into(qrCodeImageView);

        RequestOptions options = new RequestOptions()
                .placeholder(R.mipmap.avatar_def)
                .transforms(new CenterCrop(), new RoundedCorners(UIUtils.dip2Px(this, 150)));
        Glide.with(this)
                .load(logoUrl)
                .apply(options)
                .into(iv_user_head);
//        GlideApp.with(this)
//                .asBitmap()
//                .load(logoUrl)
//                .placeholder(R.mipmap.qr_code)
//                .into(new CustomViewTarget<ImageView, Bitmap>(qrCodeImageView) {
//                    @Override
//                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
//                        // the errorDrawable will always be bitmapDrawable here
//                        if (errorDrawable instanceof BitmapDrawable) {
//                            Bitmap bitmap = ((BitmapDrawable) errorDrawable).getBitmap();
//                            Bitmap qrBitmap = CodeUtils.createQRCode(qrCodeValue, 400, bitmap);
//                            qrCodeImageView.setImageBitmap(qrBitmap);
//                        }
//                    }
//
//                    @Override
//                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition transition) {
//                        //Bitmap bitmap = CodeUtils.createQRCode(qrCodeValue, 400, resource);
//                        Bitmap bitmap = CodeUtils.createQRCode(qrCodeValue, 400, null);
//                        qrCodeImageView.setImageBitmap(bitmap);
//                    }
//
//                    @Override
//                    protected void onResourceCleared(@Nullable Drawable placeholder) {
//
//                    }
//                });
    }

    @OnClick(R2.id.copyButton)
    void copyQRData() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText(getString(R.string.qrcode), rawData);
        cm.setPrimaryClip(mClipData);

        Toast.makeText(this, R.string.copy_success, Toast.LENGTH_SHORT).show();
    }
    @OnClick(R2.id.btn_share)
    void share() {
        //分享图片
        /*Bitmap bm =((BitmapDrawable) ((ImageView) qrCodeImageView).getDrawable()).getBitmap();
        if(bm!=null){
            saveImageToGallery(this,bm,System.currentTimeMillis()+"");
            shareImg(bm);
        }*/
        screenShot1(QRCodeActivity.this);


    }
    /**
     * 屏幕截图
     */
    public void screenShot1(Activity activity) {
        Bitmap bitmap = null;
        try {
            Log.e("whh0914", "111开始屏幕截图...");
            //截图
            activity.getWindow().getDecorView().setDrawingCacheEnabled(true);
            bitmap = activity.getWindow().getDecorView().getDrawingCache();
            if(bitmap != null){
                saveImageToGallery(QRCodeActivity.this,bitmap,System.currentTimeMillis()+"");
                shareImg(bitmap);
            }
        } catch (Exception e) {
            Log.e("whh0914", "111屏幕截图出现异常：" + e.toString());
        }
    }


    @OnClick(R2.id.rl_back)
    void rlBack(){
        finish();
    }
    private void shareImg(Bitmap bitmap){
        Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null,null));
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");//设置分享内容的类型
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent = Intent.createChooser(intent, getResources().getString(R.string.share));
        startActivity(intent);
    }
    /**
     * 保存图片到指定路径
     *
     * @param context
     * @param bitmap   要保存的图片
     * @param fileName 自定义图片名称
     * @return
     */
    public static boolean saveImageToGallery(Context context, Bitmap bitmap, String fileName) {
        // 保存图片至指定路径
        String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "qrcode";
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片(80代表压缩20%)
            boolean isSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();

            //发送广播通知系统图库刷新数据
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            if (isSuccess) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
