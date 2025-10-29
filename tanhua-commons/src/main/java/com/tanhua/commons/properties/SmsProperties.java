package com.tanhua.commons.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tanhua.sms")
public class SmsProperties {

    /**
     * 签名
     */
    private String signName;

    /**
     * 模板中的参数名
     */
    private String parameterName;

    /**
     * 验证码 短信模板code
     */
    private String validateCodeTemplateCode;

    private String accessKeyId;

    private String accessKeySecret;

}