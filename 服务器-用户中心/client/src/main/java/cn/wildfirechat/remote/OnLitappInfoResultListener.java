/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.remote;

import java.util.List;

import cn.wildfirechat.model.LitappInfo;

public interface OnLitappInfoResultListener {
    void onLitappInfoResult(List<LitappInfo> litappInfos);
}
