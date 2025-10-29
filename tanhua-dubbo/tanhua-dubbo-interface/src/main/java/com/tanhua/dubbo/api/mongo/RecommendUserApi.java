package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;

/**
 * 佳人服务接口
 */
public interface RecommendUserApi {
    /**
     * 根据当前用户查询佳人
     * @param userId
     * @return
     */
    RecommendUser queryMaxScore(Long userId);

    /**
     * 根据当前用户id分页查询用户列表
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    PageResult<RecommendUser> queryPageByUserId(Integer page, Integer pagesize, Long userId);

    /**
     * 根据佳人用户id 与 推荐的用户id 查询缘分值
     * @param personUserId
     * @param currentUserId
     * @return
     */
    RecommendUser queryScoreByPersonUserId(Long personUserId, Long currentUserId);
}
