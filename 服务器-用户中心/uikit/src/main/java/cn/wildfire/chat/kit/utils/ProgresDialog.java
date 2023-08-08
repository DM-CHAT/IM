package cn.wildfire.chat.kit.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;

import cn.wildfire.chat.kit.R;


/**
*
* @describe:弹窗
*/


public class ProgresDialog extends Dialog {

    public ProgresDialog(@NonNull Context context) {
        super(context, R.style.dialog_progress);
        setContentView(R.layout.dialog_porgress);

        Animation rotateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate_anim);
        LinearInterpolator lin = new LinearInterpolator();
        rotateAnimation.setInterpolator(lin);
        findViewById(R.id.iv).startAnimation(rotateAnimation);
        setCanceledOnTouchOutside(false);
    }

    @Override
    public void dismiss() {
        findViewById(R.id.iv).clearAnimation();
        super.dismiss();
    }

}
