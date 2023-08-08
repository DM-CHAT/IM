package com.mhhy.model.struct;

import lombok.Data;
import java.util.Date;

@Data
public class ServiceInfo {
    int id;

    String iconUrl;

    String serviceName;

    String url;

    String serviceRemark;

    String appRemark;

    String appIntroduction;

    String createTime;
}
