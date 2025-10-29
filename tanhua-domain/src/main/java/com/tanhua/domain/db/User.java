package com.tanhua.domain.db;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class User extends BasePojo {
    private Long id;
    private String mobile; //手机号
    private String password; //密码，json序列化时忽略
    //private Date created;
    //private Date updated;
}