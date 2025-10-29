package com.tanhua.dubbo.api.db;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.BlackList;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.mapper.BlackListMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 黑名单管理服务接口实现类
 */
@Service
public class BlackListApiImpl implements BlackListApi{

    @Autowired
    private BlackListMapper blackListMapper;

    /**
     * 黑名单 - 翻页列表
     */
    @Override
    public PageResult<UserInfo> queryBlacklist(int page, int pagesize, Long userId) {
        //封装分页对象
        Page pageRequest = new Page(page,pagesize);
        //黑名单列表分页查询
        IPage<UserInfo> userInfoIPage = blackListMapper.queryBlacklist(pageRequest,userId);
        return new PageResult<>(userInfoIPage.getTotal(),
                (long)pagesize,userInfoIPage.getPages(),(long)page,userInfoIPage.getRecords());
    }
    /**
     * 黑名单 - 移除
     */
    @Override
    public void delBlacklist(Long blackUserId, Long userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("black_user_id",blackUserId);
        queryWrapper.eq("user_id",userId);
        blackListMapper.delete(queryWrapper);
    }
}
