package com.tanhua.server.service;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.UserLocation;
import com.tanhua.domain.vo.NearUserVo;
import com.tanhua.domain.vo.UserLocationVo;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.dubbo.api.mongo.LocationApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜附近业务处理层
 */
@Service
@Slf4j
public class LocationService {
    @Reference
    private LocationApi locationApi;
    
    @Reference
    private UserInfoApi userInfoApi;

    /**
     * 上报地理位置
     * latitude//纬度
     * longitude//经度
     */
    public void saveLocation(Double latitude, Double longitude, String addrStr) {
        Long userId = UserHolder.getUserId();
        locationApi.saveLocation(userId,latitude,longitude,addrStr);
    }

    /**
     * 搜附近
     * required=false：性别非必须的
     */
    public List<NearUserVo> searchNearUser(String gender, String distance) {
        Long userId = UserHolder.getUserId();
        List<NearUserVo> nearUserVoList = new ArrayList<>();
        //a.调用搜附近服务方法 返回List<T>
        List<UserLocationVo> userLocationList = locationApi.searchNearUser(userId,Long.parseLong(distance));
        if(CollectionUtils.isEmpty(userLocationList)){
            return null;
        }
        //b.将自己的用户id过滤
        for (UserLocationVo userLocationVo : userLocationList) {
            NearUserVo nearUserVo = new NearUserVo();

            if(userLocationVo.getUserId().equals(userId)){
                //当前用户id 进行过滤
                continue;
            }
            Long nearUserId = userLocationVo.getUserId();//附近用户id
            //根据附近的用户id查询userInfo（返回Vo 过滤）
            UserInfo userInfo = userInfoApi.queryUserInfo(nearUserId);
            if(!userInfo.getGender().equals(gender)){
                //将性别不符合要求过滤
                continue;
            }
            //c.根据userId查询UserInfo
            BeanUtils.copyProperties(userInfo,nearUserVo);//头像 昵称
            nearUserVo.setUserId(nearUserId);//附近用户的id
            nearUserVoList.add(nearUserVo);
        }
        //d.封装VO返回
        return nearUserVoList;
    }
}
