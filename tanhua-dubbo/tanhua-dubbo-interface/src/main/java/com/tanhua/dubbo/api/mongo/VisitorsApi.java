package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.mongo.Visitor;
import com.tanhua.domain.vo.PageResult;

import java.util.List;

/**
 * 访客服务接口
 */
public interface VisitorsApi {
    /**
     * 根据当前用户id查询访客列表记录(前5条)
     * @param currentUserId
     * @return
     */
    List<Visitor> queryVisitorsByUserId(Long currentUserId);

    /**
     * 根据当前用户id  与 date>当前用户上次登录时间  查询访客列表记录(前5条)
     * @param currentUserId
     * @param lastTime
     * @return
     */
    List<Visitor> queryVisitorsByUserIdLastTime(Long currentUserId, Long lastTime);



    /**
     * 保存访客记录
     */
    void save(Visitor visitor);

    /**
     * 谁看过我分页查询
     * @param page
     * @param pagesize
     * @param currentUserId
     * @return
     */
    PageResult<RecommendUser> queryMyLikeByPage(int page, int pagesize, Long currentUserId);
}
