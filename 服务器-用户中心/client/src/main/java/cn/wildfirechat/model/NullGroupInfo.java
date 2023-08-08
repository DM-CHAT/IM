/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.model;

import cn.wildfirechat.client.ClientService;
import cn.wildfirechat.client.R;

public class NullGroupInfo extends GroupInfo {
    public NullGroupInfo(String groupId) {
        this.target = groupId;
        this.name = "<" + groupId + ">";
        //this.name = activity.getString(R.string.group);
    }
}
