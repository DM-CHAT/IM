package cn.wildfirechat.client;

import static cn.wildfirechat.model.GroupMember.GroupMemberType.Normal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ospn.osnsdk.data.OsnGroupInfo;

import java.util.Collections;

import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.UserInfo;

public class ClientSvcApi {

    private static GroupInfo toClientGroup(OsnGroupInfo groupInfo) {
        GroupInfo g = new GroupInfo();
        g.target = groupInfo.groupID;
        g.name = groupInfo.name;
        g.portrait = groupInfo.portrait;
        g.owner = groupInfo.owner;
        g.type = GroupInfo.GroupType.type(groupInfo.type);
        g.joinType = groupInfo.joinType;
        g.passType = groupInfo.passType;
        g.mute = groupInfo.mute;
        g.memberCount = groupInfo.memberCount;
        g.notice = groupInfo.billboard == null ? "" : groupInfo.billboard;
        g.attribute = groupInfo.attribute;
        g.extra = groupInfo.extra;
        if(groupInfo.attribute != null){
            JSONObject json = JSON.parseObject(groupInfo.attribute);
            if(json != null){
                String type = json.getString("type");
                if (type != null) {
                    if (type.equalsIgnoreCase("bomb"))
                        g.redPacket = 1;
                }

            }
        }
        return g;
    }

    public static void UpdateGroupInfo(OsnGroupInfo osnGroupInfo, String mUserId) {
        try {

            if (osnGroupInfo == null) {
                return;
            }

            //System.out.println("@@@@@ UpdateGroupInfo group id:" + osnGroupInfo.groupID);
            // query db
            GroupInfo groupInfoOld = SqliteUtils.queryGroup(osnGroupInfo.groupID);
            // update group
            if (groupInfoOld != null) {
                // 更新
                GroupInfo groupInfo = toClientGroup(osnGroupInfo);
                groupInfo.fav = groupInfoOld.fav;
                groupInfo.showAlias = groupInfoOld.showAlias;
                SqliteUtils.insertGroup(groupInfo);
            } else {
                // 新建
                GroupInfo groupInfo = toClientGroup(osnGroupInfo);
                groupInfo.fav = 0;
                groupInfo.showAlias = 0;
                SqliteUtils.insertGroup(groupInfo);
            }

            // 更新一下isMember
            ConversationInfo conv = SqliteUtils.queryConversation(1, osnGroupInfo.groupID, 0);
            if (conv != null) {
                int isMember = osnGroupInfo.isMember();
                if (conv.isMember != isMember) {
                    conv.isMember = isMember;
                    SqliteUtils.updateConversation(conv, Collections.singletonList("isMember"));
                }
            }

            // 处理一下single mute
            GroupMember groupMember = SqliteUtils.queryMember(osnGroupInfo.groupID, mUserId);
            if (groupMember != null) {
                if (osnGroupInfo.singleMute != groupMember.mute) {
                    groupMember.mute = osnGroupInfo.singleMute;
                    SqliteUtils.updateMember(groupMember, Collections.singletonList("mute"));
                }
            } else {
                // 暂时还没有group member
                // if (osnGroupInfo.singleMute != 0)
                {
                    // 被禁言才管，没有被禁言管他个鸟呀？
                    groupMember = new GroupMember();
                    groupMember.groupId = osnGroupInfo.groupID;
                    groupMember.memberId = mUserId;
                    UserInfo userInfo = SqliteUtils.queryUser(mUserId);
                    groupMember.alias = userInfo.displayName;
                    groupMember.type = Normal;
                    groupMember.updateDt = System.currentTimeMillis();
                    groupMember.createDt = System.currentTimeMillis();
                    groupMember.mute = osnGroupInfo.singleMute;
                    groupMember.index = 999999;
                    //List<GroupMember> gmList = new ArrayList<>();
                    SqliteUtils.insertMembersNoIndex(Collections.singletonList(groupMember));
                }
            }


        } catch (Exception e) {
            System.out.println("@@@@@ UpdateGroupInfo Exception");
        }
        System.out.println("@@@@@ UpdateGroupInfo end.");
    }

    public static void UpdateGroupInfo(OsnGroupInfo osnGroupInfo, String mUserId, int favChange) {
        try {

            if (osnGroupInfo == null) {
                return;
            }

            System.out.println("@@@@@ UpdateGroupInfo 2 group id:" + osnGroupInfo.groupID);
            // query db
            GroupInfo groupInfoOld = SqliteUtils.queryGroup(osnGroupInfo.groupID);
            // update group
            if (groupInfoOld != null) {
                System.out.println("@@@@@@ old fav : " + groupInfoOld.fav);
                // 更新
                GroupInfo groupInfo = toClientGroup(osnGroupInfo);
                if (favChange == 1) {
                    groupInfo.fav = favChange;
                } else {
                    groupInfo.fav = groupInfo.fav;
                }
                groupInfo.showAlias = groupInfoOld.showAlias;
                SqliteUtils.insertGroup(groupInfo);
            } else {
                // 新建
                GroupInfo groupInfo = toClientGroup(osnGroupInfo);
                groupInfo.fav = favChange;
                groupInfo.showAlias = 0;
                SqliteUtils.insertGroup(groupInfo);
            }

            // 更新一下isMember
            ConversationInfo conv = SqliteUtils.queryConversation(1, osnGroupInfo.groupID, 0);
            if (conv != null) {
                int isMember = osnGroupInfo.isMember();
                if (conv.isMember != isMember) {
                    conv.isMember = isMember;
                    SqliteUtils.updateConversation(conv, Collections.singletonList("isMember"));
                }
            }

            // 处理一下single mute
            GroupMember groupMember = SqliteUtils.queryMember(osnGroupInfo.groupID, mUserId);
            if (groupMember != null) {
                if (osnGroupInfo.singleMute != groupMember.mute) {
                    groupMember.mute = osnGroupInfo.singleMute;
                    SqliteUtils.updateMember(groupMember, Collections.singletonList("mute"));
                }
            } else {
                // 暂时还没有group member
                // if (osnGroupInfo.singleMute != 0)
                {
                    // 被禁言才管，没有被禁言管他个鸟呀？
                    groupMember = new GroupMember();
                    groupMember.groupId = osnGroupInfo.groupID;
                    groupMember.memberId = mUserId;
                    UserInfo userInfo = SqliteUtils.queryUser(mUserId);
                    groupMember.alias = userInfo.displayName;
                    groupMember.type = Normal;
                    groupMember.updateDt = System.currentTimeMillis();
                    groupMember.createDt = System.currentTimeMillis();
                    groupMember.mute = osnGroupInfo.singleMute;
                    groupMember.index = 999999;
                    //List<GroupMember> gmList = new ArrayList<>();
                    SqliteUtils.insertMembersNoIndex(Collections.singletonList(groupMember));
                }
            }


        } catch (Exception e) {
            System.out.println("@@@@@ UpdateGroupInfo Exception");
        }
        System.out.println("@@@@@ UpdateGroupInfo end.");
    }
}
