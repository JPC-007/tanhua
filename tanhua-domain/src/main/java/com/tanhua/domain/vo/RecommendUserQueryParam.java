package com.tanhua.domain.vo;
import lombok.Data;
import java.io.Serializable;

/**
 * 推荐用户列表 vo对象 用于接收参数
 */
@Data
public class RecommendUserQueryParam implements Serializable {

    private Integer page;
    private Integer pagesize;
    private String gender;
    private String lastLogin;
    private Integer age;
    private String city;
    private String education;
}