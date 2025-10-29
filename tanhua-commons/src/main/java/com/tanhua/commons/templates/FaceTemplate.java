package com.tanhua.commons.templates;

import com.baidu.aip.face.AipFace;
import com.baidu.aip.util.Base64Util;
import com.tanhua.commons.properties.FaceProperties;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.util.HashMap;
@Slf4j
public class FaceTemplate {

    private FaceProperties faceProperties;

    public FaceTemplate(FaceProperties faceProperties){
        this.faceProperties = faceProperties;
    }

    /**
     * 人脸识别
     * @param bytes
     * @return
     */
    public boolean detect(byte[] bytes){
        // 初始化一个AipFace
        AipFace client = new AipFace(faceProperties.getAppId(), faceProperties.getApiKey(), faceProperties.getSecretKey());

        HashMap<String, String> options = new HashMap<String, String>();
        options.put("face_field", "age");
        options.put("max_face_num", "2");
        options.put("face_type", "LIVE");
        options.put("liveness_control", "LOW");

        // 调用接口
        String image = Base64Util.encode(bytes);
        String imageType = "BASE64";

        // 人脸检测
        JSONObject res = client.detect(image, imageType, options);
        // 没检测到人脸时，error_code值不为0
        Integer error_code = (Integer)res.get("error_code");
        log.debug("************人脸识别************"+error_code);
        return error_code == 0;
    }
}