/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.model;

import cn.wildfirechat.client.ClientService;
import cn.wildfirechat.client.R;

/**
 * null pattern
 * <p>
 * 当本地不存在改用户信息时，返回这个类型的实例，避免上层不断的做null check
 */
public class NullUserInfo extends UserInfo {
    public NullUserInfo(String uid) {
        this.uid = uid;
        this.name = "<" + uid + ">";
        //this.name = activity.getString(R.string.user);
        this.displayName = name;
    }
}
