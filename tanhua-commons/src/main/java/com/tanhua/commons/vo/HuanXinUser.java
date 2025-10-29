package com.tanhua.commons.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class HuanXinUser implements Serializable {

    private String username;
    private String password;
    private String nickname;//没用到 随便写
}