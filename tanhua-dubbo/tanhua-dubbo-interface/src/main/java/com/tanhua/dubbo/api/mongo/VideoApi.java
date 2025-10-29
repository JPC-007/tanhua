package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;

/**
 * 小视频服务接口
 */
public interface VideoApi {
    /**
     * 发布小视频
     * @param video
     */
    void saveSmallVideos(Video video);

    /**
     * 小视频列表
     */
    PageResult<Video> querySmallVideos(int page, int pagesize);

    /**
     *视频用户关注
     * @param followUser
     */
    void saveFollowUser(FollowUser followUser);

    /**
     * 视频用户关注
     * @param followUser
     */
    void removeFollowUser(FollowUser followUser);
}
