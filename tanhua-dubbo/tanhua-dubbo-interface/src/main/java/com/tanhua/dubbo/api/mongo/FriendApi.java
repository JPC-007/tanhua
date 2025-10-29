package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;

/**
 * 好友服务接口
 */
public interface FriendApi {

    /**
     * 添加好友关系
     * @param friend
     */
    void saveFriend(Friend friend);

    /**
     * 联系人列表查询
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    PageResult<Friend> queryContacts(Integer page, Integer pagesize, Long userId);

    /**
     * 互相喜欢统计
     * @param currentUserId
     * @return
     */
    Long eachLoveCount(Long currentUserId);

    /**
     * 互相关注分页查询
     * @param page
     * @param pagesize
     * @param currentUserId
     * @return
     */
    PageResult<RecommendUser> queryMyLikeByPage(int page, int pagesize, Long currentUserId);
}
