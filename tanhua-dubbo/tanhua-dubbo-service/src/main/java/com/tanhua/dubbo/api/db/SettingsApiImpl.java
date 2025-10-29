package com.tanhua.dubbo.api.db;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.Settings;
import com.tanhua.dubbo.mapper.SettingsMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 通用设置服务接口实现类
 */
@Service
public class SettingsApiImpl implements SettingsApi{

    @Autowired
    private SettingsMapper settingsMapper;
    /**
     * 根据用户id查询通用设置信息
     * @param userId
     * @return
     */
    @Override
    public Settings queryByUserId(Long userId) {
        QueryWrapper<Settings> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id",userId);
        return settingsMapper.selectOne(queryWrapper);
    }
    /**
     * 保存通知设置记录
     * @param settings
     */
    @Override
    public void saveSettings(Settings settings) {
        settingsMapper.insert(settings);
    }
    /**
     * 更新通知设置
     * @param settings
     */
    @Override
    public void updateSettings(Settings settings) {
        settingsMapper.updateById(settings);
    }
}
