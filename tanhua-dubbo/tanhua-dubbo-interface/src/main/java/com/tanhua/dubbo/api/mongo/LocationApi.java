package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.vo.UserLocationVo;

import java.util.List;

/**
 * 搜附近服务接口
 */
public interface LocationApi {
    /**
     * 上报地理位置
     * latitude//纬度
     * longitude//经度
     */
    void saveLocation(Long userId, Double latitude, Double longitude, String addrStr);

    /**
     * 搜附近用户列表
     * @param userId
     * @param distance
     * @return
     */
    List<UserLocationVo> searchNearUser(Long userId, Long distance);
}
