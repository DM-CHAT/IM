/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.LayoutRes;
import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class WfcBaseActivity3 extends AppCompatActivity{

    @BindView(R2.id.toolbar)
    Toolbar toolbar;
    Menu menu;
    public static String baseLanguage = null;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        beforeViews();
        setContentView(contentLayout());
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        if (sp.getBoolean("darkTheme", false)) {
            // dark
            toolbar.getContext().setTheme(R.style.AppTheme_DarkAppbar);
            customToolbarAndStatusBarBackgroundColor(true);
        } else {
            // light
            toolbar.getContext().setTheme(R.style.AppTheme_LightAppbar);
            customToolbarAndStatusBarBackgroundColor(false);
        }
        afterViews();
        try {
            ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), 0);
            if(activityInfo.labelRes != 0)
                setTitle(activityInfo.labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        WfcUIKit.getWfcUIKit().setActivity(this);
    }

    /**
     * @param darkTheme 和toolbar.xml里面的 app:theme="@style/AppTheme.DarkAppbar" 相关
     */
    private void customToolbarAndStatusBarBackgroundColor(boolean darkTheme) {
        int toolbarBackgroundColorResId = darkTheme ? R.color.white : R.color.white;
        Drawable drawable = getResources().getDrawable(R.mipmap.ic_back1);
        if (darkTheme) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable.setTint(Color.WHITE);
            }
            toolbar.setTitleTextColor(Color.WHITE);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable.setTintList(null);
            }
        }
        getSupportActionBar().setHomeAsUpIndicator(drawable);
        if (showHomeMenuItem()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitleBackgroundResource(toolbarBackgroundColorResId, darkTheme);
    }

    /**
     * 设置状态栏和标题栏的颜色
     *
     * @param resId 颜色资源id
     */
    protected void setTitleBackgroundResource(int resId, boolean dark) {
        toolbar.setBackgroundResource(resId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, resId));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//实现状态栏图标和文字颜色为暗色
        }
        setStatusBarTheme(this, dark);
    }

    protected void yincangToolbar(){
        toolbar.setVisibility(View.GONE);
    }
    protected void showToolbar(){
        toolbar.setVisibility(View.VISIBLE);
    }

    protected boolean isDarkTheme() {
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        return sp.getBoolean("darkTheme", false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu() != 0) {
            getMenuInflater().inflate(menu(), menu);
        }
        afterMenus(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            hideInputMethod();
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void hideInputMethod() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * @return 布局文件
     */
    protected abstract @LayoutRes
    int contentLayout();

    /**
     * @return menu
     */
    protected @MenuRes
    int menu() {
        return 0;
    }

    /**
     * {@link AppCompatActivity#setContentView(int)}之前调用
     */
    protected void beforeViews() {

    }

    /**
     * {@link AppCompatActivity#setContentView(int)}之后调用
     * <p>
     * 此时已经调用了{@link ButterKnife#bind(Activity)}, 子类里面不需要再次调用
     */
    protected void afterViews() {

    }

    /**
     * {@code getMenuInflater().inflate(menu(), menu);}之后调用
     *
     * @param menu
     */
    protected void afterMenus(Menu menu) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        hideInputMethod();
    }

    protected boolean showHomeMenuItem() {
        return true;
    }

    public boolean checkPermission(String permission) {
        return checkPermission(new String[]{permission});
    }

    public boolean checkPermission(String[] permissions) {
        boolean granted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                granted = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
                if (!granted) {
                    break;
                }
            }
        }
        return granted;
    }

    /**
     * Changes the System Bar Theme.
     */
    public static void setStatusBarTheme(final Activity pActivity, final boolean pIsDark) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            // Fetch the current flags.
//            final int lFlags = pActivity.getWindow().getDecorView().getSystemUiVisibility();
//            // Update the SystemUiVisibility dependening on whether we want a Light or Dark theme.
//            pActivity.getWindow().getDecorView().setSystemUiVisibility(pIsDark ? (lFlags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) : (lFlags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));
//        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        if(baseLanguage == null){
            SharedPreferences sp = base.getSharedPreferences("config", Context.MODE_PRIVATE);
            baseLanguage = sp.getString("language", "0");
        }
        String language = WfcBaseActivity3.toLanguage(baseLanguage);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            super.attachBaseContext(updateResources(base, language));
        }else{
            super.attachBaseContext(updateResourcesLegacy(base, language));
        }
    }
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }
    private static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale);
        }

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return context;
    }
    public static String toLanguage(String language){
        if(language.equalsIgnoreCase("0")){
            language = "zh";
        }else if(language.equalsIgnoreCase("1")){
            language = "vn";
        }else if(language.equalsIgnoreCase("2")){
            language = "en";
        }else if(language.equalsIgnoreCase("3")){
            language = "jpr";
        }else if(language.equalsIgnoreCase("4")){
            language = "kr";
        }else if(language.equalsIgnoreCase("5")){
            language = "es";
        }else if(language.equalsIgnoreCase("6")){
            language = "de";
        }else if(language.equalsIgnoreCase("7")){
            language = "tr";
        }else if(language.equalsIgnoreCase("8")){
            language = "tw";
        }else if(language.equalsIgnoreCase("9")){
            language = "id";
        }
        else{
            language = "zh";
        }
        return language;
    }
    public static void setBaseLanguage(String language){
        baseLanguage = language;
    }
    public static String getBaseLanguage(){
        return baseLanguage;
    }
    public static String getLanguage(){
        return toLanguage(baseLanguage);
    }
//    public static String getBaseToken(Activity activity){
//        SharedPreferences sp = activity.getSharedPreferences("config", Context.MODE_PRIVATE);
//        return sp.getString("token", "");
//    }

}
