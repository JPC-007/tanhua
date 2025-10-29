package com.tanhua.commons.templates;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.tanhua.commons.properties.SmsProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SmsTemplate {
    /** SMSRESPONSE_CODE SMSRESPONSE_MESSAGE 用来返回给调用者，短信发送结果的key */
    public static final String SMSRESPONSE_CODE="Code";
    public static final String SMSRESPONSE_MESSAGE="Message";
    private SmsProperties smsProperties;
    private IAcsClient acsClient;

    public SmsTemplate(SmsProperties smsProperties){
        this.smsProperties = smsProperties;
    }

    public void init(){
        // 设置超时时间-可自行调整
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        // 初始化ascClient需要的几个参数
        final String product = "Dysmsapi";// 短信API产品名称（短信产品名固定，无需修改）
        final String domain = "dysmsapi.aliyuncs.com";// 短信API产品域名（接口地址固定，无需修改）
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", smsProperties.getAccessKeyId(), smsProperties.getAccessKeySecret());
        try {
            DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
            acsClient = new DefaultAcsClient(profile);

        } catch (ClientException e) {
            log.error("初始化阿里云短信失败",e);
            //e.printStackTrace();
        }
    }

    /**
     * 发送验证码
     * @param phoneNumber
     * @param validateCode
     * @return
     */
    public Map<String,String> sendValidateCode(String phoneNumber, String validateCode){
        return sendSms(smsProperties.getValidateCodeTemplateCode(),phoneNumber,validateCode);
    }

    /**
     * 发送短信
     * @param templateCode 验证码的模板code
     * @param phoneNumber 接收的手机号码
     * @param param        短信模板中参数对应的值
     * @return Map<String,String>    code=OK代表发送成功
     */
    public Map<String,String> sendSms(String templateCode, String phoneNumber, String param){
        // 组装请求对象
        SendSmsRequest request = new SendSmsRequest();
        // 使用post提交
        request.setMethod(MethodType.POST);
        // 必填:待发送手机号。支持以逗号分隔的形式进行批量调用，批量上限为1000个手机号码,批量调用相对于单条调用及时性稍有延迟,验证码类型的短信推荐使用单条调用的方式
        request.setPhoneNumbers(phoneNumber);
        // 必填:短信签名-可在短信控制台中找到
        request.setSignName(smsProperties.getSignName());
        // 必填:短信模板-可在短信控制台中找到
        request.setTemplateCode(templateCode);
        // 可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
        request.setTemplateParam(String.format("{\"%s\":\"%s\"}",smsProperties.getParameterName(),param));

        //将发送的结果封装到map里
        Map<String,String> result = new HashMap<String,String>();
        try {
            SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
            if (("OK").equals(sendSmsResponse.getCode())) {
            	//System.out.println("请求成功");
            	return null;
            }
            result.put(SmsTemplate.SMSRESPONSE_CODE,sendSmsResponse.getCode());
            result.put(SmsTemplate.SMSRESPONSE_MESSAGE,sendSmsResponse.getMessage());
        } catch (Exception e) {
            //e.printStackTrace();
            log.error("发送短信失败!",e);
            result.put(SmsTemplate.SMSRESPONSE_CODE,"FAIL");
        }
        return result;
    }
}
