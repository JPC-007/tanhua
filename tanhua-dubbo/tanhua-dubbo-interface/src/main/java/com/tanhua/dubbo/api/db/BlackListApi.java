package com.tanhua.dubbo.api.db;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.PageResult;

/**
 * 黑名单管理服务接口
 */
public interface BlackListApi {
    /**
     * 黑名单 - 翻页列表
     */
    PageResult<UserInfo> queryBlacklist(int page, int pagesize, Long userId);
    /**
     * 黑名单 - 移除
     */
    void delBlacklist(Long blackUserId, Long userId);
}
