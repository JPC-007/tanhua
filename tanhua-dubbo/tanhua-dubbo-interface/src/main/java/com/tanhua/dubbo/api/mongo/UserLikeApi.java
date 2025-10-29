package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;

/**
 * 喜欢服务接口
 */
public interface UserLikeApi {
    /**
     *2喜欢统计
     * @param currentUserId
     * @return
     */
    Long loveCount(Long currentUserId);

    /**
     * 3粉丝统计
     * @param currentUserId
     * @return
     */
    Long fanCount(Long currentUserId);

    /**
     * 我关注分页查询
     * @param page
     * @param pagesize
     * @param currentUserId
     * @param b
     * @return
     */
    PageResult<RecommendUser> queryMyLikeByPage(int page, int pagesize, Long currentUserId, boolean b);

    /**
     * 根据登录用户id 和 粉丝id 删除userLike关注记录
     * @param fansUserId
     * @param currentUserId
     */
    void delete(Long fansUserId, Long currentUserId);
}
