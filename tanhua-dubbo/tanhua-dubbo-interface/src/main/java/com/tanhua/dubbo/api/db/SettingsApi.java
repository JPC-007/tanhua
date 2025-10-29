package com.tanhua.dubbo.api.db;

import com.tanhua.domain.db.Settings;

/**
 * 通用设置服务接口
 */
public interface SettingsApi {
    /**
     * 根据用户id查询通用设置信息
     * @param userId
     * @return
     */
    Settings queryByUserId(Long userId);

    /**
     * 保存通知设置记录
     * @param settings
     */
    void saveSettings(Settings settings);

    /**
     * 更新通知设置
     * @param settings
     */
    void updateSettings(Settings settings);
}
