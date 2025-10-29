package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.mongo.UserLike;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 喜欢服务接口实现类
 */
@Service
public class UserLikeApiImpl implements UserLikeApi{

    @Autowired
    private MongoTemplate mongoTemplate;
    /**
     *2喜欢统计
     * @param currentUserId
     * @return
     */
    @Override
    public Long loveCount(Long currentUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(currentUserId));
        return mongoTemplate.count(query, UserLike.class);
    }
    /**
     * 3粉丝统计
     * @param currentUserId
     * @return
     */
    @Override
    public Long fanCount(Long currentUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("likeUserId").is(currentUserId));
        return mongoTemplate.count(query, UserLike.class);
    }
    /**
     * 我关注分页查询
     * @param page
     * @param pagesize
     * @param currentUserId
     * @param flag true:我关注 false:粉丝
     * @return
     */
    @Override
    public PageResult<RecommendUser> queryMyLikeByPage(int page, int pagesize, Long currentUserId, boolean flag) {
        Query query = new Query();
        if(flag){
            query.addCriteria(Criteria.where("userId").is(currentUserId));
        }
        else
        {
            query.addCriteria(Criteria.where("likeUserId").is(currentUserId));
        }
        query.limit(pagesize).skip((page-1)*pagesize);//分页
        //1.查询总记录数
        long counts = mongoTemplate.count(query, UserLike.class);
        //2.分页查询当前页面数据
        List<UserLike> userLikeList = mongoTemplate.find(query, UserLike.class);
        if(CollectionUtils.isEmpty(userLikeList)){
            return  null;
        }
        List<RecommendUser> recommendUserList = new ArrayList<>();
        for (UserLike userLike : userLikeList) {
            Long friendId ;
            if(flag) {
                 friendId = userLike.getLikeUserId();//好友id
            }
            else
            {
                friendId = userLike.getUserId();//好友id
            }
            Query recommQuery = new Query();
            recommQuery.addCriteria(Criteria.where("userId").is(friendId).and("toUserId").is(currentUserId));
            RecommendUser recommendUser = mongoTemplate.findOne(recommQuery, RecommendUser.class);
            if(recommendUser == null){
                recommendUser = new RecommendUser();
                recommendUser.setScore(99d);
                recommendUser.setToUserId(currentUserId);
                recommendUser.setUserId(friendId);
            }
            //将recommendUser放入recommendUserList
            recommendUserList.add(recommendUser);
        }
        long pages = counts/pagesize + counts%pagesize > 0 ? 1:0;//总页码
        return new PageResult<>(counts,(long)pagesize,pages,(long)page,recommendUserList);
    }

    /**
     * 根据登录用户id 和 粉丝id 删除userLike关注记录
     * @param fansUserId
     * @param currentUserId
     */
    @Override
    public void delete(Long fansUserId, Long currentUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(fansUserId).and("likeUserId").is(currentUserId));
        mongoTemplate.remove(query,UserLike.class);
    }
}
