package com.tanhua.domain.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 为什么使用vo?
 * 1.让请求响应对象（app对应）  跟  数据库操作对象 解耦
 * 2.通过vo可以严格按照前端需要的数据封装
 */
@Data
public class UserInfoVo implements Serializable {
    private Long id; //用户id
    private String nickname; //昵称
    private String avatar; //用户头像
    private String birthday; //生日
    private String gender; //性别
    private String age; //年龄
    private String city; //城市
    private String income; //收入
    private String education; //学历
    private String profession; //行业
    private Integer marriage; //婚姻状态
}