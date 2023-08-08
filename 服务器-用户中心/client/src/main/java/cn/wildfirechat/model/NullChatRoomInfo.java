/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.model;

import cn.wildfirechat.client.ClientService;
import cn.wildfirechat.client.R;

public class NullChatRoomInfo extends ChatRoomInfo {
    public NullChatRoomInfo(String chatRoomId) {
        this.chatRoomId = chatRoomId;
        this.title = "<" + chatRoomId + ">";
        //this.title = activity.getString(R.string.room);
    }
}