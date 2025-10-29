package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * 评论服务接口实现类
 */
@Service
public class VideoApiImpl implements VideoApi{

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void saveSmallVideos(Video video) {
        video.setId(ObjectId.get());//设置id
        video.setCreated(System.currentTimeMillis());//当前发布时间
        mongoTemplate.save(video);
    }

    /**
     * 小视频列表
     */
    @Override
    public PageResult<Video> querySmallVideos(int page, int pagesize) {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC,"created"));//降序
        query.limit(pagesize).skip((page-1)*pagesize);//分页
        //1.查询总记录数
        long counts = mongoTemplate.count(query, Video.class);
        //2.分页查询当前页面数据
        List<Video> videoList = mongoTemplate.find(query, Video.class);
        long pages = counts/pagesize + counts%pagesize > 0 ? 1:0;//总页码
        return new PageResult<>(counts,(long)pagesize,pages,(long)page,videoList);
    }

    /**
     * 视频用户关注
     * @param followUser
     */
    @Override
    public void saveFollowUser(FollowUser followUser) {
        followUser.setId(ObjectId.get());
        followUser.setCreated(System.currentTimeMillis());
        mongoTemplate.save(followUser);
    }

    /**
     * 视频用户关注
     * @param followUser
     */
    @Override
    public void removeFollowUser(FollowUser followUser) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(followUser.getUserId()).and("followUserId").is(followUser.getFollowUserId()));
        mongoTemplate.remove(query,FollowUser.class);
    }
}
