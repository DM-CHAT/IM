// IRemoteClient.aidl
package cn.wildfirechat.client;

import cn.wildfirechat.client.ISendMessageCallback;
import cn.wildfirechat.client.ISearchUserCallback;
import cn.wildfirechat.client.IGeneralCallback;
import cn.wildfirechat.client.IGeneralCallback2;
import cn.wildfirechat.client.IGeneralCallback3;
import cn.wildfirechat.client.IUploadMediaCallback;
import cn.wildfirechat.client.IOnReceiveMessageListener;
import cn.wildfirechat.client.IOnConnectionStatusChangeListener;
import cn.wildfirechat.client.IGetChatRoomInfoCallback;
import cn.wildfirechat.client.IGetChatRoomMembersInfoCallback;
import cn.wildfirechat.client.IGetGroupInfoCallback;
import cn.wildfirechat.client.ICreateChannelCallback;
import cn.wildfirechat.client.ISearchChannelCallback;
import cn.wildfirechat.client.IGetRemoteMessageCallback;
import cn.wildfirechat.client.IGetFileRecordCallback;

import cn.wildfirechat.client.IGetMessageCallback;
import cn.wildfirechat.client.IGetUserCallback;
import cn.wildfirechat.client.IGetGroupCallback;
import cn.wildfirechat.client.IGetGroupMemberCallback;
import cn.wildfirechat.client.IGetLitappCallback;

import cn.wildfirechat.client.IOnFriendUpdateListener;
import cn.wildfirechat.client.IOnGroupInfoUpdateListener;
import cn.wildfirechat.client.IOnGroupMembersUpdateListener;
import cn.wildfirechat.client.IOnSettingUpdateListener;
import cn.wildfirechat.client.IGetGroupsCallback;
import cn.wildfirechat.client.IOnUserInfoUpdateListener;
import cn.wildfirechat.client.IOnChannelInfoUpdateListener;
import cn.wildfirechat.client.IOnConferenceEventListener;
import cn.wildfirechat.client.IOnLitappInfoUpdateListener;
import cn.wildfirechat.client.IOnLitappInfoResultListener;

import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.model.UnreadCount;

import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationSearchResult;
import cn.wildfirechat.model.GroupSearchResult;
import cn.wildfirechat.model.ModifyMyInfoEntry;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.FriendRequest;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.ChannelInfo;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.model.RedPacketInfo;
import cn.wildfirechat.model.UnpackInfo;

import java.util.List;
import java.util.Map;



// Declare any non-default types here with import statements

interface IRemoteClient {

    void insertWallets(String wallet);
    List getWalletsInfo();
    String getWallets(String walletId);
    void findObject(String text, IGeneralCallback callback);
    void createTempAccount();
    String[] createOsnIdFromMnemonic(String b64Seed, String password);

    String getShadowKeyPubId(String decPwd);
    String signByShadowKey(String hash, String decPwd);

    void setHideEnable1(String str);
    void setHideEnable2(String str);
    void setHideEnable3(String str);
    void setEnable(String key, String value);
    void setRole(String key, String value, IGeneralCallback callback);
    boolean getEnable(String key);
    boolean getHideEnable();
    boolean getHideEnable3();
    void deleteDBConfig();

    //void spSet(in String key, in Object object);
    //Object spGet(in String key, in Object defaultObject);



    void setLanguage(in String language);
    void simpleLogin(in String target, in String url, IGeneralCallback2 callback);
    void allowGroupAddFriend(in String value, in String groupId, IGeneralCallback2 callback);
    void isGroupForward(in String value, in String groupId, IGeneralCallback2 callback);
    void isGroupCopy(in String value, in String groupId, IGeneralCallback2 callback);
    void isClearChats(in String value, in String groupId, IGeneralCallback2 callback);
    void deleteTimeMessage(in String time, in String target, IGeneralCallback callback);
    void topMessage(in String time, in String value, in String groupId, IGeneralCallback2 callback);
    void deleteTopMessage(in List<String> time, in String groupId, IGeneralCallback2 callback);

    void upAttribute(in String key, in String value, in String groupId, IGeneralCallback2 callback);
    void upDescribes(in String key, in String value, in String user,IGeneralCallback2 callback);
    void upDescribe(in String key, in String value, in String group,IGeneralCallback2 callback);
    void upPrivateInfo(in String key, in String value, in String group,IGeneralCallback2 callback);
    void removeDescribes(String key, IGeneralCallback2 callback);
    void litappLogin(in LitappInfo litappInfo, in String url, IGeneralCallback2 callback);
    void register(in String userName, in String password, in String serviceID, IGeneralCallback callback);
    boolean connect(in String userId, in String token);
    boolean connect2(in String userId, in String token, in String password2);
    void disconnect(in boolean disablePush, in boolean clearSession);
    void setForeground(in int isForeground);
    void onNetworkChange();
    void setServerAddress(in String host);

    oneway void setOnReceiveMessageListener(in IOnReceiveMessageListener listener);
    oneway void setOnConnectionStatusChangeListener(in IOnConnectionStatusChangeListener listener);

    oneway void setOnUserInfoUpdateListener(in IOnUserInfoUpdateListener listener);
    oneway void setOnGroupInfoUpdateListener(in IOnGroupInfoUpdateListener listener);
    oneway void setOnGroupMembersUpdateListener(in IOnGroupMembersUpdateListener listener);
    oneway void setOnFriendUpdateListener(in IOnFriendUpdateListener listener);
    oneway void setOnSettingUpdateListener(in IOnSettingUpdateListener listener);
    oneway void setOnChannelInfoUpdateListener(in IOnChannelInfoUpdateListener listener);
    oneway void setOnConferenceEventListener(in IOnConferenceEventListener listener);
    oneway void setOnLitappInfoUpdateListener(in IOnLitappInfoUpdateListener listener);
    oneway void setOnLitappInfoResultListener(in IOnLitappInfoResultListener listener);

    oneway void registerMessageContent(in String msgContentCls);

    oneway void send(in Message msg, in ISendMessageCallback callback, in int expireDuration);
    oneway void updateRedPaket(in RedPacketInfo redPacketInfo);
    oneway void sendSavedMessage(in Message msg, in int expireDuration, in ISendMessageCallback callback);
    oneway void recall(in long messageUid, IGeneralCallback callback);
    oneway void recall2(in long messageUid, IGeneralCallback callback);
    oneway void deleteMessageTo(in long messageUid, IGeneralCallback callback);
    oneway void sendNeedShare(in List<String> userList, in String groupName, in String portrait, in String groupId);
    oneway void sendCallMessage(in Message msg, in IGeneralCallback callback);

    oneway void broadcast(in String json, in IGeneralCallback callback);
    oneway void dynamic(in String data, in IGeneralCallback2 callback);
    oneway void orderPay(in String data, in String code, in IGeneralCallback2 callback);
    oneway void getOwnerSign(in String groupID, IGeneralCallback2 callback);
    oneway void getGroupSign(in String groupID, String data, IGeneralCallback2 callback);
    oneway void setGroupOwner(in String groupID, String owner, IGeneralCallback2 callback);
    oneway void addGroupManager(in String groupID, in List<String> memberIds, IGeneralCallback2 callback);
    oneway void delGroupManager(in String groupID, in List<String> memberIds, IGeneralCallback2 callback);
    oneway void fetchRedPacket(in String packetID, String unpackID, String userID, in IGeneralCallback2 callback);
    oneway void openRedPacket(in String packetID, String unpackID, long messageID, in IGeneralCallback2 callback);
    RedPacketInfo getRedPacket(in String packetID);
    List<UnpackInfo> getUnpackList(String unpackID);

    long getServerDeltaTime();
    List<ConversationInfo> getConversationList(in int[] conversationTypes, in int[] lines, in int tagId);
    ConversationInfo getConversation(in int conversationType, in String target, in int line);
    long getFirstUnreadMessageId(in int conversationType, in String target, in int line);
    List<Message> getMessages(in Conversation conversation, in long fromIndex, in boolean before, in int count, in String withUser);
    List<Message> getMessagesEx(in int[] conversationTypes, in int[] lines, in int[] contentTypes, in long fromIndex, in boolean before, in int count, in String withUser);
    List<Message> getMessagesEx2(in int[] conversationTypes, in int[] lines, in int[] messageStatus, in long fromIndex, in boolean before, in int count, in String withUser);

    oneway void getMessagesAsync(in Conversation conversation, in long fromIndex, in boolean before, in int count, in String withUser, in IGetMessageCallback callback);
    oneway void getMessagesInTypesAsync(in Conversation conversation, in int[] contentTypes, in long fromIndex, in boolean before, in int count, in String withUser, in IGetMessageCallback callback);
    oneway void getMessagesInStatusAsync(in Conversation conversation, in int[] messageStatus, in long fromIndex, in boolean before, in int count, in String withUser, in IGetMessageCallback callback);
    oneway void getMessagesExAsync(in int[] conversationTypes, in int[] lines, in int[] contentTypes, in long fromIndex, in boolean before, in int count, in String withUser, in IGetMessageCallback callback);
    oneway void getMessagesEx2Async(in int[] conversationTypes, in int[] lines, in int[] messageStatus, in long fromIndex, in boolean before, in int count, in String withUser, in IGetMessageCallback callback);

    oneway void getUserMessages(in String userId, in Conversation conversation, in long fromIndex, in boolean before, in int count, in IGetMessageCallback callback);
    oneway void getUserMessagesEx(in String userId, in int[] conversationTypes, in int[] lines, in int[] contentTypes, in long fromIndex, in boolean before, in int count, in IGetMessageCallback callback);

    oneway void getRemoteMessages(in Conversation conversation, in long beforeMessageUid, in int count, in IGetRemoteMessageCallback callback);
    oneway void getConversationFileRecords(in Conversation conversation, in String fromUser, in long beforeMessageUid, in int count, in IGetFileRecordCallback callback);
    oneway void getMyFileRecords(in long beforeMessageUid, in int count, in IGetFileRecordCallback callback);
    oneway void deleteFileRecord(in long messageUid, in IGeneralCallback callback);
    oneway void searchFileRecords(in String keyword, in Conversation conversation, in String fromUser, in long beforeMessageUid, in int count, in IGetFileRecordCallback callback);
    oneway void searchMyFileRecords(in String keyword, in long beforeMessageUid, in int count, in IGetFileRecordCallback callback);

    Message getMessage(in long messageId);
    Message getMessageByUid(in String msgHash0);
    Message getMessageByHash(in String msgHash);

    Message insertMessage(in Message message, in boolean notify);
    boolean updateMessageContent(in Message message);
    boolean updateMessageStatus(in long messageId, in int messageStatus);
    boolean syncFriend();
    boolean syncGroup();

    UnreadCount getUnreadCount(in int conversationType, in String target, in int line);
    UnreadCount getUnreadCountEx(in int[] conversationTypes, in int[] lines);
    boolean clearUnreadStatus(in int conversationType, in String target, in int line);
    boolean clearUnreadStatusEx(in int[] conversationTypes, in int[] lines);
    void clearAllUnreadStatus();
    void clearMessages(in int conversationType, in String target, in int line);
    void clearMessagesEx(in int conversationType, in String target, in int line, in long before);
    void setMediaMessagePlayed(in long messageId);
 //   void callMessage(in Message message);
    void saveCallMessage(in Message message);
    void clearConversation(in boolean clearMsg);
    void removeConversation(in int conversationType, in String target, in int line, in boolean clearMsg);
    oneway void setConversationTop(in int conversationType, in String target, in int line, in boolean top, in IGeneralCallback callback);
    void setConversationDraft(in int conversationType, in String target, in int line, in String draft);
    oneway void setConversationSilent(in int conversationType, in String target, in int line, in boolean silent,  in IGeneralCallback callback);
    void setConversationTimestamp(in int conversationType, in String target, in int line, in long timestamp);

    Map getConversationRead(in int conversationType, in String target, in int line);
    Map getMessageDelivery(in int conversationType, in String target);
    oneway void searchUser(in String keyword, in int searchType, in int page, in ISearchUserCallback callback);

    boolean isMyFriend(in String userId);
    List<String> getMyFriendList(in boolean refresh);
    List<UserInfo> getMyFriendListInfo(in boolean refresh);
    oneway void loadFriendRequestFromRemote();

    String getUserSetting(in int scope, in String key);
    Map getUserSettings(in int scope);
    oneway void setUserSetting(in int scope, in String key, in String value, in IGeneralCallback callback);
    oneway void startLog();
    oneway void stopLog();
    oneway void setDeviceToken(in String token, in int pushType);

    List<FriendRequest> getFriendRequest(in boolean incomming);
    FriendRequest getOneFriendRequest(in String userId, in boolean incomming);
    String getFriendAlias(in String userId);
    oneway void setFriendAlias(in String userId, in String alias, in IGeneralCallback callback);
    String getFriendExtra(in String userId);

    String getVoiceBaseUrl();
    void setVoiceBaseUrl(in String voiceBaseUrl);
    String getVoiceHostUrl();
    void setVoiceHostUrl(in String voiceHostUrl);
    String getMainDapp();
    void setMainDapp(in String mainDapp);

    void removeVoiceSetup();
    void clearOutGroupMembers(String groupId, int min);

    void clearUnreadFriendRequestStatus();
    int getUnreadFriendRequestStatus();
    oneway void removeFriend(in String userId, in IGeneralCallback callback);
    oneway void sendFriendRequest(in String userId, in String reason, in IGeneralCallback callback);
    oneway void handleGroupRequest(in String userId, in String groupID, in boolean accept, in IGeneralCallback callback);
    oneway void handleFriendRequest(in String userId, in boolean accept, in String extra, in IGeneralCallback callback);
    oneway void deleteFriend(in String userId, in IGeneralCallback callback);
    oneway void clearFriend(in IGeneralCallback callback);

    boolean updateTagUser(String osnId, int tagId);

    boolean isBlackListed(in String userId);
    List<String> getBlackList(in boolean refresh);
    oneway void setBlackList(in String userId, in boolean isBlacked, in IGeneralCallback callback);

    oneway void joinChatRoom(in String chatRoomId, in IGeneralCallback callback);
    oneway void quitChatRoom(in String chatRoomId, in IGeneralCallback callback);

    oneway void getChatRoomInfo(in String chatRoomId, in long updateDt, in IGetChatRoomInfoCallback callback);
    oneway void getChatRoomMembersInfo(in String chatRoomId, in int maxCount, in IGetChatRoomMembersInfoCallback callback);
    oneway void getGroupDetail(in String groupId, in String type, IGeneralCallback2 callback);
    GroupInfo getGroupInfo(in String groupId, in boolean refresh);
    GroupInfo getGroupInfoFromDB(in String groupId);

    boolean insertTag(String name, int tagId);
    List listTag();
    List<String> listTagUser(int tagId);
    boolean deleteTagUser(String osnID);
    boolean deleteTag(int tagId);
    String QueryTagUser(String osnId);
    boolean insertTagUser(String osnId, int tagId);
    oneway void getGroupInfoEx(in String groupId, in boolean refresh, in IGetGroupCallback callback);
    UserInfo getUserInfo(in String userId, in String groupId, in boolean refresh);
    List<UserInfo> getUserInfos(in List<String> userIds, in String groupId);
    oneway void getUserInfoEx(in String userId, in boolean refresh, in IGetUserCallback callback);

    oneway void uploadLogger(in IUploadMediaCallback callback);
    oneway void uploadMedia(in String fileName, in byte[] data, int mediaType, in IUploadMediaCallback callback);
    oneway void uploadMediaFile(in String mediaPath, int mediaType, in IUploadMediaCallback callback);
    oneway void modifyMyInfo(in List<ModifyMyInfoEntry> values, in IGeneralCallback callback);
    boolean deleteMessage(in long messageId);
    List<ConversationSearchResult> searchConversation(in String keyword, in int[] conversationTypes, in int[] lines);
    List<Message> searchMessage(in Conversation conversation, in String keyword, in boolean desc, in int limit, in int offset);
    oneway void searchMessagesEx(in int[] conversationTypes, in int[] lines, in int[] contentTypes, in String keyword, in long fromIndex, in boolean before, in int count, in IGetMessageCallback callback);

    List<GroupSearchResult> searchGroups(in String keyword);
    List<UserInfo> searchFriends(in String keyworkd);

    String getEncodedClientId();

    oneway void createGroup(in String groupId, in String groupName, in String groupPortrait,in String owner2, in int groupType, in List<String> memberIds, in int[] notifyLines, in MessagePayload notifyMsg, in IGeneralCallback2 callback);
    oneway void joinGroup(in String groupId, in String reason, in IGeneralCallback callback);
    oneway void addGroupMembers(in String groupId, in List<String> memberIds, in int[] notifyLines, in MessagePayload notifyMsg, in IGeneralCallback callback);
    oneway void removeGroupMembers(in String groupId, in List<String> memberIds, in int[] notifyLines, in MessagePayload notifyMsg, in IGeneralCallback callback);
    oneway void quitGroup(in String groupId, in int[] notifyLines, in MessagePayload notifyMsg, in IGeneralCallback callback);
    oneway void dismissGroup(in String groupId, in int[] notifyLines, in MessagePayload notifyMsg, in IGeneralCallback callback);
    oneway void modifyGroupInfo(in String groupId, in int modifyType, in String newValue, in int[] notifyLines, in MessagePayload notifyMsg, in IGeneralCallback callback);
    oneway void modifyGroupAlias(in String groupId, in String newAlias, in int[] notifyLines, in MessagePayload notifyMsg, in IGeneralCallback callback);
    oneway void modifyGroupMemberAlias(in String groupId, in String memberId, in String newAlias, in int[] notifyLines, in MessagePayload notifyMsg, in IGeneralCallback callback);
    List<GroupMember> getGroupMembers(in String groupId, in boolean forceUpdate);
    List<GroupMember> getGroupMemberAll(in String groupId);
    List<GroupMember> getGroupManagers(in String groupId);
    List<GroupMember> getGroupMembersByType(in String groupId, in int type);
    GroupMember getGroupMember(in String groupId, in String memberId);
    oneway void getGroupMemberZone(in String groupId, in int start, in int count, in IGetGroupMemberCallback callback);
    oneway void getGroupMemberFromDB(in String groupId, in int begin, in int end, in IGetGroupMemberCallback callback);
    oneway void getGroupMemberEx(in String groupId, in boolean forceUpdate, in IGetGroupMemberCallback callback);
    oneway void transferGroup(in String groupId, in String newOwner, in int[] notifyLines, in MessagePayload notifyMsg, in IGeneralCallback callback);
    oneway void setGroupManager(in String groupId, in boolean isSet, in List<String> memberIds, in int[] notifyLines, in MessagePayload notifyMsg, in IGeneralCallback callback);
    oneway void muteOrAllowGroupMember(in String groupId, in boolean isSet, in int mode, in List<String> memberIds, in boolean isAllow, in int[] notifyLines, in MessagePayload notifyMsg, in IGeneralCallback callback);
    oneway void muteGroup(in String groupId, in boolean isSet, in int mode, in List<String> memberIds, in boolean isAllow, in int[] notifyLines, in MessagePayload notifyMsg, in IGeneralCallback callback);

    byte[] encodeData(in byte[] data);
    byte[] decodeData(in byte[] data);

    String getHost();
    void setHost(in String host);
    oneway void createChannel(in String channelId, in String channelName, in String channelPortrait, in String desc, in String extra, in ICreateChannelCallback callback);
    oneway void modifyChannelInfo(in String channelId, in int modifyType, in String newValue, in IGeneralCallback callback);
    ChannelInfo getChannelInfo(in String channelId, in boolean refresh);
    oneway void searchChannel(in String keyword, in ISearchChannelCallback callback);
    boolean isListenedChannel(in String channelId);
    oneway void listenChannel(in String channelId, in boolean listen, in IGeneralCallback callback);
    oneway void destoryChannel(in String channelId, in IGeneralCallback callback);
    List<String> getMyChannels();
    List<String> getListenedChannels();

    List<LitappInfo> getLitappList();
    List<LitappInfo> getCollectLitappList();

    void deleteLitapp(in String target);
    void deleteWallets(in String osnID);
    void deleteCollectLitapp(in String target);
    void addLitapp(in LitappInfo litapp);
    void addCollectLitapp(in LitappInfo litapp);
    LitappInfo getCollectLitapp(in String target);
    LitappInfo getLitappInfo(in String target, boolean refresh);
    oneway void getLitappInfoEx(in String target, boolean refresh, in IGetLitappCallback callback);
    String hashData(in byte[] data);
    String signData(in byte[] data);
    boolean verifyData(in String osnID, in byte[] data, in String sign);
    String encryptData(in String osnID, in byte[] data);

    String getImageThumbPara();

    void kickoffPCClient(in String pcClientId, in IGeneralCallback callback);
    void getApplicationId(in String applicationId, in IGeneralCallback2 callback);
    oneway void getAuthorizedMediaUrl(in long messageUid, in int mediaType, in String mediaPath, in IGeneralCallback2 callback);

    int getMessageCount(in Conversation conversation);
    boolean begainTransaction();
    void commitTransaction();

    boolean isCommercialServer();
    boolean isReceiptEnabled();
    void sendConferenceRequest(in long sessionId, in String roomId, in String request, in String data, in IGeneralCallback2 callback);

    boolean isGroupMember(in String groupId, in String userId);
    oneway void setIconId(in int id);
}
