package cn.wildfire.chat.kit.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import cn.wildfire.chat.kit.R;

import static cn.wildfire.chat.kit.third.utils.UIUtils.dip2Px;

public class LoadingDialog {
    static Dialog dialog = null;
    public static void showLoading(Context context, String msg){
        if(dialog != null)
            return;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.dialog_loading, null);
        LinearLayout linearLayout = view.findViewById(R.id.loading_view);
        dialog = new Dialog(context, R.style.DialogLoading);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(linearLayout);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = dip2Px(120);
        layoutParams.height = dip2Px(100);
        window.setAttributes(layoutParams);
        dialog.show();
    }
    public static void hideLoading(){
        if(dialog != null){
            dialog.dismiss();
            dialog = null;
        }
    }
}
