/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.user;

import static cn.wildfirechat.message.CardMessageContent.CardType_User;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.lqr.imagepicker.ImagePicker;
import com.lqr.imagepicker.bean.ImageItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wildfire.chat.kit.Config;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcIntent;
import cn.wildfire.chat.kit.WfcScheme;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.WfcWebViewActivity;
import cn.wildfire.chat.kit.common.ComplaintActivity;
import cn.wildfire.chat.kit.common.OperateResult;
import cn.wildfire.chat.kit.contact.ContactListActivity;
import cn.wildfire.chat.kit.contact.ContactViewModel;
import cn.wildfire.chat.kit.contact.newfriend.InviteFriendActivity;
import cn.wildfire.chat.kit.conversation.ConversationActivity;
import cn.wildfire.chat.kit.conversation.forward.ForwardActivity;
import cn.wildfire.chat.kit.group.GroupListActivity;
import cn.wildfire.chat.kit.litapp.LitappActivity;
import cn.wildfire.chat.kit.qrcode.QRCodeActivity;
import cn.wildfire.chat.kit.tag.DeleteTagActivity;
import cn.wildfire.chat.kit.tag.TagActivity;
import cn.wildfire.chat.kit.tag.TagSelectionActivity;
import cn.wildfire.chat.kit.third.utils.FileUtils;
import cn.wildfire.chat.kit.third.utils.ImageUtils;
import cn.wildfire.chat.kit.third.utils.UIUtils;
import cn.wildfire.chat.kit.utils.OssHelper;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfire.chat.kit.widget.OptionItemView;
import cn.wildfirechat.message.CardMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.model.OrgTag;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class UserInfoFragment extends Fragment {
    @BindView(R2.id.portraitImageView)
    ImageView portraitImageView;
    @BindView(R2.id.nameTextView)
    TextView nameTextView;
    @BindView(R2.id.accountTextView)
    TextView accountTextView;
    @BindView(R2.id.tv_qianzui)
    TextView tv_qianzui;
    @BindView(R2.id.nftFlag)
    ImageView nftImageView;
    @BindView(R2.id.chatButton)
    View chatButton;
    @BindView(R2.id.voipChatButton)
    View voipChatButton;
    @BindView(R2.id.reportButton)
    View reportButton;
    @BindView(R2.id.inviteButton)
    Button inviteButton;
    @BindView(R2.id.aliasOptionItemView)
    OptionItemView aliasOptionItemView;

    @BindView(R2.id.qrCodeOptionItemView)
    OptionItemView qrCodeOptionItemView;

    @BindView(R2.id.momentButton)
    View momentButton;

    @BindView(R2.id.favContactTextView)
    TextView favContactTextView;

    @BindView(R2.id.shareCardInfoItemView)
    OptionItemView shareCardInfoItemView;

    private UserInfo userInfo;
    private UserViewModel userViewModel;
    private ContactViewModel contactViewModel;

    public static UserInfoFragment newInstance(UserInfo userInfo) {
        UserInfoFragment fragment = new UserInfoFragment();
        Bundle args = new Bundle();
        args.putParcelable("userInfo", userInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        assert args != null;
        userInfo = args.getParcelable("userInfo");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_info_fragment, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init() {
        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
        String selfUid = userViewModel.getUserId();
        if (selfUid.equals(userInfo.uid)) {
            // self
            chatButton.setVisibility(View.GONE);
            voipChatButton.setVisibility(View.GONE);
            inviteButton.setVisibility(View.GONE);
            qrCodeOptionItemView.setVisibility(View.VISIBLE);
            aliasOptionItemView.setVisibility(View.VISIBLE);
        } else if (contactViewModel.isFriend(userInfo.uid)) {
            // friend
            chatButton.setVisibility(View.VISIBLE);
            voipChatButton.setVisibility(View.GONE);
            inviteButton.setVisibility(View.GONE);
            qrCodeOptionItemView.setVisibility(View.VISIBLE);
        } else {
            // stranger
            momentButton.setVisibility(View.GONE);
            chatButton.setVisibility(View.GONE);
            voipChatButton.setVisibility(View.GONE);
            inviteButton.setVisibility(View.VISIBLE);
            aliasOptionItemView.setVisibility(View.GONE);
            reportButton.setVisibility(View.GONE);
        }
        if (userInfo.type == 1) {
            voipChatButton.setVisibility(View.GONE);
        }
        if (userInfo.uid.equals(Config.FILE_TRANSFER_ID)) {
            chatButton.setVisibility(View.VISIBLE);
            inviteButton.setVisibility(View.GONE);
        }
        if(userInfo.urlSpace != null && !userInfo.urlSpace.isEmpty())
            momentButton.setVisibility(View.VISIBLE);
        else
            momentButton.setVisibility(View.GONE);
        setNftTextView();
        setUserInfo(userInfo);
        userViewModel.userInfoLiveData().observe(getViewLifecycleOwner(), userInfos -> {
            for (UserInfo info : userInfos) {
                if (userInfo.uid.equals(info.uid)) {
                    userInfo = info;
                    setUserInfo(info);
                    break;
                }
            }
        });
        userViewModel.getUserInfo(userInfo.uid, true);
        favContactTextView.setVisibility(contactViewModel.isFav(userInfo.uid) ? View.VISIBLE : View.GONE);

//        if (!WfcUIKit.getWfcUIKit().isSupportMoment()) {
//            momentButton.setVisibility(View.GONE);
//        }
    }

    private void setNftTextView(){
        if(userInfo.getNft() != null)
            nftImageView.setVisibility(View.VISIBLE);
        else
            nftImageView.setVisibility(View.GONE);

    }
    private void setUserInfo(UserInfo userInfo) {
        RequestOptions requestOptions = new RequestOptions()
            .placeholder(R.mipmap.avatar_def)
            .transforms(new CenterCrop(), new RoundedCorners(UIUtils.dip2Px(getContext(), 10)));
        Glide.with(this)
            .load(userInfo.portrait)
            .apply(requestOptions)
            .into(portraitImageView);
        nameTextView.setText(userViewModel.getUserDisplayName(userInfo));
        tv_qianzui.setText(getString(R.string.imid)+":");
        accountTextView.setText(userInfo.uid);
        setNftTextView();
    }

    @OnClick(R2.id.chatButton)
    void chat() {
        Intent intent = new Intent(getActivity(), ConversationActivity.class);
        Conversation conversation = new Conversation(Conversation.ConversationType.Single, userInfo.uid, 0);
        intent.putExtra("conversation", conversation);
        startActivity(intent);
        getActivity().finish();
    }
    @OnClick(R2.id.tagItemView)
    void tagOption(){

        List<OrgTag> list = ChatManager.Instance().listTag();
        if(list.size() > 0){
            Intent intent = new Intent(getActivity(), TagSelectionActivity.class);
            intent.putExtra("uid",userInfo.uid);
            startActivity(intent);
        }else{
            new android.app.AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.no_tag))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getActivity(), TagActivity.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setCancelable(true)
                    .create().show();
        }


    }

    @OnClick(R2.id.momentButton)
    void moment() {
//        Intent intent = new Intent(WfcIntent.ACTION_MOMENT);
//        intent.putExtra("userInfo", userInfo);
//        startActivity(intent);
        LitappInfo litappInfo = ChatManager.Instance().getLitappInfo(userInfo.urlSpace, true);
        if(litappInfo != null){
            litappInfo.url += "?userID="+userInfo.uid;
            Intent intent = new Intent(getContext(), LitappActivity.class);
            intent.putExtra("litappInfo", litappInfo);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @OnClick(R2.id.voipChatButton)
    void voipChat() {
        WfcUIKit.singleCall(getActivity(), userInfo.uid, false);
    }

    @OnClick(R2.id.reportButton)
    void report() {
        Intent intent = new Intent(getContext(), ComplaintActivity.class);
        startActivity(intent);
    }

    @OnClick(R2.id.aliasOptionItemView)
    void alias() {
        String selfUid = userViewModel.getUserId();
        if (selfUid.equals(userInfo.uid)) {
            Intent intent = new Intent(getActivity(), ChangeMyNameActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getActivity(), SetAliasActivity.class);
            intent.putExtra("userId", userInfo.uid);
            startActivity(intent);
        }
    }

    private static final int REQUEST_CODE_PICK_IMAGE = 100;

    @OnClick(R2.id.portraitImageView)
    void portrait() {
        if (userInfo.uid.equals(userViewModel.getUserId())) {
            updatePortrait();
        } else {
            if(userInfo.getNft() != null){
              //  WfcWebViewActivity.loadUrl(getContext(), userInfo.displayName, userInfo.nft);
                Intent intent = new Intent(getActivity(),NftActivity.class);
                intent.putExtra("url",userInfo.getNft());
                startActivity(intent);
            }else{
                Intent intent = new Intent(getActivity(),ImageActivity.class);
                intent.putExtra("img_url",userInfo.portrait);
                startActivity(intent);
            }
        }
    }

    @OnClick(R2.id.shareCardInfoItemView)
    void shareCardInfo(){



        Message msg = new Message();
        msg.content = new CardMessageContent(
                CardType_User,
                userInfo.uid,
                userInfo.displayName,
                userInfo.displayName,
                userInfo.portrait);
        msg.sender = ChatManager.Instance().getUserId();
        msg.conversation = new Conversation(0, msg.sender);
        msg.direction = MessageDirection.Send;
        msg.status = MessageStatus.Sending;
        msg.serverTime = System.currentTimeMillis();

        Intent intent = new Intent(getActivity(), ForwardActivity.class);
        intent.putExtra("message", msg);
        startActivity(intent);




/*
        Intent intent = new Intent(getActivity(), ContactListActivity.class);
        intent.putExtra("userinfo", userInfo);
        intent.putExtra("pick",true);
        startActivity(intent);
*/
    }

    private void updatePortrait() {
        //设置1是单选 并且可以裁剪
        ImagePicker.picker().enableMultiMode(1).pick(this, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
            if (images == null || images.isEmpty()) {
                Toast.makeText(getActivity(), R.string.update_error_select_file, Toast.LENGTH_SHORT).show();
                return;
            }
            /*File thumbImgFile = ImageUtils.genThumbImgFile(images.get(0).path);
            if (thumbImgFile == null) {
                Toast.makeText(getActivity(), R.string.update_error_thumbnail, Toast.LENGTH_SHORT).show();
                return;
            }
            String imagePath = thumbImgFile.getAbsolutePath();*/

            String imagePath = images.get(0).path;
//            String key = FileUtils.getFileNameFromPath(imagePath);
            //上传
            OssHelper.getInstance(getActivity()).uploadFile(imagePath, OssHelper.UserPortraitDirectory, new OssHelper.CallBack() {
                @Override
                public void success() {

                }

                @Override
                public void success(String remote, String filename) {
                    MutableLiveData<OperateResult<Boolean>> result = userViewModel.updateUserPortrait2(remote);
                    result.observe(getActivity(), booleanOperateResult -> {
                        if (booleanOperateResult.isSuccess()) {
                            Toast.makeText(getActivity(), R.string.update_ok, Toast.LENGTH_SHORT).show();
                            ChatManager.Instance().removeDescribes("nft",null);
                            init();

                        } else {
                            Toast.makeText(getActivity(), getString(R.string.update_no) + booleanOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void fail() {

                }
            });
            //下载显示
//            OssHelper.getInstance(getActivity()).setImageBackground(getActivity(),"3512MagazinePic-05-2.3.001-bigpicture_05_15.jpg",portraitImageView);
/*
            MutableLiveData<OperateResult<Boolean>> result = userViewModel.updateUserPortrait(imagePath);
            result.observe(this, booleanOperateResult -> {
                if (booleanOperateResult.isSuccess()) {
                    Toast.makeText(getActivity(), R.string.update_ok, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.update_no) + booleanOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                }
            });*/
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @OnClick(R2.id.inviteButton)
    void invite() {
        Intent intent = new Intent(getActivity(), InviteFriendActivity.class);
        intent.putExtra("userInfo", userInfo);
        startActivity(intent);
        getActivity().finish();
    }

    @OnClick(R2.id.qrCodeOptionItemView)
    void showMyQRCode() {
        //UserInfo userInfo = userViewModel.getUserInfo(userViewModel.getUserId(), false);
        String qrCodeValue = WfcScheme.QR_CODE_PREFIX_USER + userInfo.uid;
        startActivity(QRCodeActivity.buildQRCodeIntent(getActivity(), getString(R.string.qrcode), userInfo.portrait, qrCodeValue, userInfo.uid,userInfo.displayName,userInfo.uid));
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }
}
