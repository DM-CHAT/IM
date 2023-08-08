package com.mhhy.model.req;

import lombok.Data;

@Data
public class CreateGroupReq {
    String user;
    String phoneOrEmail;
}
