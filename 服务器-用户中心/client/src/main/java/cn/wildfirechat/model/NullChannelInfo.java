/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.model;

import cn.wildfirechat.client.ClientService;
import cn.wildfirechat.client.R;

public class NullChannelInfo extends ChannelInfo {
    public NullChannelInfo(String channelId) {
        this.channelId = channelId;
        this.name = "<" + channelId + ">";
        //this.name = activity.getString(R.string.channel);
        this.owner = "";
    }
}
