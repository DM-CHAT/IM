/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.app.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wildfire.chat.app.SPUtils;
import cn.wildfire.chat.app.login.KTDemo;
import cn.wildfire.chat.app.setting.PrivaryPolicyActivity;
import cn.wildfire.chat.app.setting.SettingActivity;
import cn.wildfire.chat.app.setting.UserAgreementActivity;
import cn.wildfire.chat.kit.WfcScheme;
import cn.wildfire.chat.kit.common.FeedbackActivity;
import cn.wildfire.chat.kit.common.SingleInfoActivity;
import cn.wildfire.chat.kit.favorite.FavoriteListActivity;
import cn.wildfire.chat.kit.qrcode.QRCodeActivity;
import cn.wildfire.chat.kit.settings.MessageNotifySettingActivity;
import cn.wildfire.chat.kit.sharehome.ShareHomeActivity;
import cn.wildfire.chat.kit.third.utils.UIUtils;
import cn.wildfire.chat.kit.user.UserInfoActivity;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfire.chat.kit.web.WebViewActivity;
import cn.wildfire.chat.kit.web.WebViewActivity1;
import cn.wildfire.chat.kit.widget.OptionItemView;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.model.ModifyMyInfoEntry;
import cn.wildfirechat.model.ModifyMyInfoType;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.model.WalletsInfo;
import cn.wildfirechat.remote.ChatManager;

public class MeFragment extends Fragment {

    @BindView(R.id.meLinearLayout)
    LinearLayout meLinearLayout;
    @BindView(R.id.portraitImageView)
    ImageView portraitImageView;
    @BindView(R.id.nameTextView)
    TextView nameTextView;
    @BindView(R.id.accountTextView)
    TextView accountTextView;
    @BindView(R.id.nftFlag)
    ImageView nftImageView;

    @BindView(R.id.notificationOptionItemView)
    OptionItemView notificationOptionItem;

    @BindView(R.id.settintOptionItemView)
    OptionItemView settingOptionItem;
    @BindView(R.id.helpOptionItemView)
    OptionItemView helpOptionItem;
    @BindView(R.id.nftOptionItemView)
    OptionItemView nftOptionItem;
    @BindView(R.id.assetsOptionItemView)
    OptionItemView assetsOptionItemView;
    @BindView(R.id.walletItemView)
    OptionItemView walletItemView;
    @BindView(R.id.collectItemView)
    OptionItemView collectItemView;
    @BindView(R.id.privacyPolicy)
    OptionItemView privacyPolicy;
    @BindView(R.id.uesrAgreement)
    OptionItemView uesrAgreement;
    @BindView(R.id.lifeService)
    OptionItemView lifeService;
    @BindView(R.id.proMotion)
    OptionItemView proMotion;
    @BindView(R.id.vpnOptionItemView)
    OptionItemView vpnOptionItemView;
    @BindView(R.id.notice)
    OptionItemView notice;

    private UserViewModel userViewModel;
    private UserInfo userInfo;

    private Observer<List<UserInfo>> userInfoLiveDataObserver = new Observer<List<UserInfo>>() {
        @Override
        public void onChanged(@Nullable List<UserInfo> userInfos) {
            if (userInfos == null) {
                return;
            }
            for (UserInfo info : userInfos) {
                if(info.uid == null){
                    continue;
                }
                if (info.uid.equals(userViewModel.getUserId())) {
                    userInfo = info;
                    updateUserInfo(userInfo);
                    break;
                }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment_me, container, false);
        ButterKnife.bind(this, view);

        init();
        return view;
    }

    private void updateUserInfo(UserInfo userInfo) {
        RequestOptions options = new RequestOptions()
            .placeholder(R.mipmap.avatar_def)
            .transforms(new CenterCrop(), new RoundedCorners(UIUtils.dip2Px(getContext(), 10)));
        Glide.with(this)
            .load(userInfo.portrait)
            .apply(options)
            .into(portraitImageView);
        nameTextView.setText(userInfo.displayName);
        accountTextView.setText(getString(R.string.account)+": " + userInfo.name);
        if(userInfo.getNft() != null)
            nftImageView.setVisibility(View.VISIBLE);
        else
            nftImageView.setVisibility(View.GONE);
    }

    private boolean isEnable() {
        boolean enable1 = (boolean) SPUtils.get(getActivity(),"enable1",false);
  //      boolean enable2 = (boolean) SPUtils.get(getActivity(),"enable2",false);
        boolean enable2 = ChatManager.Instance().getHideEnable();
        return enable1 || enable2;
    }
    private boolean isHideEnable3() {

        List<WalletsInfo> list = ChatManager.Instance().getWalletsInfo();
        if(list.size() > 0){
            return true;
        }
        return false;
    }
    private boolean isTakeTaxi(){
        boolean takeTaxi = (boolean) SPUtils.get(getActivity(),"takeTaxi",false);
        return takeTaxi;
    }

    private void init() {
        boolean enable2 = (boolean) SPUtils.get(getContext(),"enable2",false);
        System.out.println("@@@ me frag enable2 : " +enable2);
        if (isEnable() || enable2) {

        }else{
        }
        //是否显示Crossgo
        boolean openCrossgo = (boolean) SPUtils.get(getContext(),"openCrossgo",false);
        if(openCrossgo){
            helpOptionItem.setVisibility(View.VISIBLE);
        }else{
            helpOptionItem.setVisibility(View.GONE);
        }
     //   vpnOptionItemView.setVisibility(View.VISIBLE);
        boolean openVPN = (boolean) SPUtils.get(getContext(),"openVPN",false);
        if(openVPN){
            vpnOptionItemView.setVisibility(View.VISIBLE);
        }else{
            vpnOptionItemView.setVisibility(View.GONE);
        }

        if(isHideEnable3()){
            walletItemView.setVisibility(View.VISIBLE);
        }else{
            walletItemView.setVisibility(View.GONE);
        }
        if(isTakeTaxi()){
            lifeService.setVisibility(View.VISIBLE);
        }else{
            lifeService.setVisibility(View.GONE);
        }

        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        userViewModel.getUserInfoAsync(userViewModel.getUserId(), true)
            .observe(getViewLifecycleOwner(), info -> {
                userInfo = info;
                if (userInfo != null) {
                    updateUserInfo(userInfo);
                }
            });
        userViewModel.userInfoLiveData().observeForever(userInfoLiveDataObserver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        userViewModel.userInfoLiveData().removeObserver(userInfoLiveDataObserver);
    }

    @OnClick(R.id.notice)
    void notice(){
        Intent intent = new Intent(getActivity(),AnnouncementActivity.class);
        getActivity().startActivity(intent);
    }
    void setupVpn(){
        /*mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.init(getContext());
        mainViewModel.isRunning().observe(getViewLifecycleOwner(), aBoolean -> {
            System.out.println("vpn isRunning: "+aBoolean);
        });
        mainViewModel.startListenBroadcast(getContext());*/
    }
    @OnClick(R.id.vpnOptionItemView)
    void VPN(){
        /*if(mainViewModel == null){
            setupVpn();
        }
        if (mainViewModel.isRunning().getValue() == true) {
            Utils.INSTANCE.stopVService(getContext());
        }else{
            Intent intent = VpnService.prepare(getContext());
            if (intent == null) {
                V2RayServiceManager.INSTANCE.startV2Ray(getContext());
            } else {
                startActivityForResult(intent, 0x123456);
            }
        }*/
    }

    @OnClick(R.id.googleOptionItemView)
    void google(){
        Intent intent = new Intent(getActivity(), WebViewActivity1.class);
        intent.putExtra("url", "https://www.google.com/");
        startActivity(intent);
    }

    @OnClick({R.id.meLinearLayout,R.id.ib_set_up})
    void showMyInfo() {
        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
        intent.putExtra("userInfo", userInfo);
        startActivity(intent);
    }
    @OnClick(R.id.walletItemView)
    void walletOnClick(){
        String Wallet = (String) SPUtils.get(getActivity(), "Wallet", "");
        if (!Wallet.isEmpty()) {
            try {
                ChatManager.Instance().insertWallets(Wallet);
                SPUtils.remove(getActivity(), "Wallet");
            } catch (Exception e) {

            }
        }
        Intent intent = new Intent(getActivity(),WalletListActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.collectItemView)
    void collectItemViewOnClick(){
        Intent intent = new Intent(getActivity(),CollectLitappListActivity.class);
        startActivity(intent);
    }
    @OnClick(R.id.lifeService)
    void lifeService(){
        Intent intent = new Intent(getActivity(),LifeServiceActivity.class);
        startActivity(intent);
    }
    @OnClick(R.id.proMotion)
    void proMotion(){
        //推广
        Intent intent = new Intent(getActivity(),ProMationActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.favOptionItemView)
    void fav() {
        Intent intent = new Intent(getActivity(), FavoriteListActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.ib_qr_code)
    void showMyQRCode() {
        String qrCodeValue = WfcScheme.QR_CODE_PREFIX_USER + userInfo.uid;
        startActivity(QRCodeActivity.buildQRCodeIntent(getActivity(), getString(cn.wildfire.chat.kit.R.string.qrcode), userInfo.portrait, qrCodeValue, userInfo.uid,userInfo.displayName,userInfo.uid));
    }


    @OnClick(R.id.settintOptionItemView)
    void setting() {
        Intent intent = new Intent(getActivity(), SettingActivity.class);
        intent.putExtra("userInfo",userInfo);
        startActivity(intent);
    }
    @OnClick(R.id.helpOptionItemView)
    void helpNetwork() {
        Intent intent = new Intent(getActivity(), HelpActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.privacyPolicy)
    void pricacyPolicy(){
        //0中文,1越南语,2英语
        SharedPreferences sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        String language = sp.getString("language", "0");

     //   http://streemfinancialg.xyz/JTalking/J-privacy-zh.html
    //    http://streemfinancialg.xyz/JTalking/J-privacy-vn.html
    //    http://streemfinancialg.xyz/JTalking/J-privacy-en.html

        Intent intent = new Intent(getActivity(), PrivaryPolicyActivity.class);
        if(language.equals("0")){
            intent.putExtra("url","http://streemfinancialg.xyz/DM-CHAT/J-privacy-zh.html");
        }else if(language.equals("1")){
            intent.putExtra("url","http://streemfinancialg.xyz/DM-CHAT/J-privacy-vn.html");
        }else if(language.equals("2")){
            intent.putExtra("url","http://streemfinancialg.xyz/DM-CHAT/J-privacy-en.html");
        }
        startActivity(intent);
    }

    @OnClick(R.id.uesrAgreement)
    void uesrAgreement(){
        //0中文,1越南语,2英语
        SharedPreferences sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        String language = sp.getString("language", "0");

    //    http://streemfinancialg.xyz/JTalking/J-user-zh.html
    //    http://streemfinancialg.xyz/JTalking/J-user-vn.html
   //     http://streemfinancialg.xyz/JTalking/J-user-en.html

        Intent intent = new Intent(getActivity(), UserAgreementActivity.class);
        if(language.equals("0")){
            intent.putExtra("url","http://streemfinancialg.xyz/DM-CHAT/J-user-zh.html");
        }else if(language.equals("1")){
            intent.putExtra("url","http://streemfinancialg.xyz/DM-CHAT/J-user-vn.html");
        }else if(language.equals("2")){
            intent.putExtra("url","http://streemfinancialg.xyz/DM-CHAT/J-user-en.html");
        }
        startActivity(intent);
    }

    @OnClick(R.id.notificationOptionItemView)
    void msgNotifySetting() {
        Intent intent = new Intent(getActivity(), MessageNotifySettingActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.userFeedback)
    void userFeedback() {
        Intent intent = new Intent(getActivity(), FeedbackActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.teamOptionItemView)
    void teamCircle() {
        Intent intent = new Intent(getActivity(), ShareHomeActivity.class);
        startActivity(intent);
    }
	@OnClick(R.id.nftOptionItemView)
    void setNftInfo() {
        Intent intent = new Intent(getActivity(), SingleInfoActivity.class);
        startActivityForResult(intent, 0x1000);
    }
    @OnClick(R.id.assetsOptionItemView)
    void openAssets(){
        String url_WALLET_URL = (String) SPUtils.get(getActivity(),"WALLET_URL","");
        Intent intent = new Intent(getActivity(), WebViewActivity.class);
        intent.putExtra("url", url_WALLET_URL);
        startActivity(intent);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0x1000){
            if(data == null || !data.hasExtra("info"))
                return;
            String info = data.getExtras().getString("info");
            ModifyMyInfoEntry entry = new ModifyMyInfoEntry();
            entry.type = ModifyMyInfoType.Modify_Nft;
            entry.value = info;
            userViewModel.modifyMyInfo(Collections.singletonList(entry)).observe(this, booleanOperateResult -> {
                if (booleanOperateResult.isSuccess()) {
                    Toast.makeText(getContext(), getString(R.string.modify_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.modify_failure), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }
}
