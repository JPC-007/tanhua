package com.tanhua.commons.templates;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tanhua.commons.properties.HuaWeiUGCProperties;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;

/**
 * 华为 内容审核 工具模板
 */
public class HuaWeiUGCTemplate {

    private HuaWeiUGCProperties properties;

    private String token;

    private long expire = 0L;

    public HuaWeiUGCTemplate(HuaWeiUGCProperties properties) {
        this.properties = properties;
    }

    /**
     * 文本审核
     * 参考：https://support.huaweicloud.com/api-moderation/moderation_03_0018.html
     * @param textModeration
     * @return
     */
    public boolean textContentCheck(String textModeration) {
        String url = properties.getTextApiUrl();
        String reqBody = JSONUtil.createObj()
            .set("categories", StrUtil.split(properties.getCategoriesText(), ','))
            .set("items", JSONUtil.createArray()
                .set(JSONUtil.createObj()
                    .set("text", textModeration)
                    .set("type", "content")
                )
            ).toString();

        String resBody = HttpRequest.post(url)
            .header("X-Auth-Token", this.getToken())
            .contentType("application/json;charset=utf8")
            .setConnectionTimeout(3000)
            .setReadTimeout(2000)
            .body(reqBody)
            .execute()
            .body();

        JSONObject jsonObject = JSONUtil.parseObj(resBody);
        if (jsonObject.containsKey("result") && jsonObject.getJSONObject("result").containsKey("suggestion")) {
            String suggestion = jsonObject.getJSONObject("result").getStr("suggestion").toUpperCase();
            if ("PASS".equals(suggestion)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 图片审核
     * 参数：https://support.huaweicloud.com/api-moderation/moderation_03_0036.html
     * @param urls 多个图片的完整地址
     * @return
     */
    public boolean imageContentCheck(String[] urls) {
        String url = properties.getImageApiUrl();
        String reqBody = JSONUtil.createObj()
            .set("categories", properties.getCategoriesImage().split(","))
            .set("urls", urls)
            .toString();

        String resBody = HttpRequest.post(url)
            .header("X-Auth-Token", this.getToken())
            .contentType("application/json;charset=utf8")
            .setConnectionTimeout(5000)
            .setReadTimeout(3000)
            .body(reqBody)
            .execute()
            .body();

        System.out.println("resBody=" + resBody);
        JSONObject jsonObject = JSONUtil.parseObj(resBody);
        if (jsonObject.containsKey("result")) {
            //审核结果中如果出现一个block或review，整体结果就是不通过，如果全部为PASS就是通过
            if (StrUtil.contains(resBody, "\"suggestion\":\"block\"")) {
                return false;
            } else if (StrUtil.contains(resBody, "\"suggestion\":\"review\"")) {
                return false;
            } else {
                return true;
            }
        }

        //默认人工审核
        return false;
    }

    /**
     * 获取授权Token
     * 参考 https://support.huaweicloud.com/api-iam/iam_30_0001.html
     * @return
     */
    public synchronized String getToken() {
        // 获取当前系统时间
        Long now = System.currentTimeMillis();
        // 判断token是否超时，超时需要重新获取
        if (now > expire) {
            // token的url
            String url = "https://iam.myhuaweicloud.com/v3/auth/tokens";
            // 构建请求体内容
            String reqBody = JSONUtil.createObj().set("auth", JSONUtil.createObj()
                .set("identity", JSONUtil.createObj()
                    .set("methods", JSONUtil.createArray().set("password"))
                    .set("password", JSONUtil.createObj()
                        .set("user", JSONUtil.createObj()
                            .set("domain", JSONUtil.createObj().set("name", properties.getDomain()))
                            .set("name", properties.getUsername())
                            .set("password", properties.getPassword())
                        )
                    )
                )
                .set("scope", JSONUtil.createObj()
                    .set("project", JSONUtil.createObj()
                        .set("name", properties.getProject())
                    )
                )
            ).toString();
            // 执行请求获取响应结果
            HttpResponse response = HttpRequest.post(url)
                .contentType("application/json;charset=utf8")
                .setConnectionTimeout(3000).setReadTimeout(5000)
                .body(reqBody).execute();
            // 获取返回的token
            token = response.header("X-Subject-Token");
            //设置Token有效时长 避免频繁获取
            setExpireTime(response.body());
        }
        return token;
    }

    /**
     * 设置Token有效时长，如果api有返回，则要提前5分钟获取新的Token
     * 默认有效时长2小时
     * @param jsonString
     */
    private void setExpireTime(String jsonString) {
        try {
            JSONObject jsonObject = JSONUtil.parseObj(jsonString);
            if (jsonObject.containsKey("token") && jsonObject.getJSONObject("token").containsKey("expires_at")) {
                String str = jsonObject.getJSONObject("token").getStr("expires_at");
                str = str.replace("T", " ");
                Date expireAt = DateUtils.parseDate(str.substring(0, 16), "yyyy-MM-dd HH:mm");
                expire = expireAt.getTime()-5*60*1000; // 提前5分钟
            }
        } catch (Exception e) {
        }
        // 没获取到有效期，则1小时后过期
        expire = System.currentTimeMillis() + 60*60*1000;
    }
}