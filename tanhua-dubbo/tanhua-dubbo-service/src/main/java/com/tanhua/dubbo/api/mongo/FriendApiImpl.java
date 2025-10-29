package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 好友服务接口实现类
 */
@Service
public class FriendApiImpl implements FriendApi{

    @Autowired
    private MongoTemplate mongoTemplate;
    /**
     * 添加好友关系
     * @param friend
     */
    @Override
    public void saveFriend(Friend friend) {
        Long friendId = friend.getFriendId();
        Long currentUserId = friend.getUserId();
        Query query1 = new Query();
        query1.addCriteria(Criteria.where("userId").is(friendId).and("friendId").is(currentUserId));
        //1.根据好友id 与 当前用户的id 查询记录是否存在
        List<Friend> friendList = mongoTemplate.find(query1, Friend.class);
        if(CollectionUtils.isEmpty(friendList)){
            //保存
            friend.setFriendId(currentUserId);
            friend.setUserId(friendId);
            friend.setCreated(System.currentTimeMillis());
            friend.setId(ObjectId.get());
            mongoTemplate.save(friend);
        }

        Query query2 = new Query();
        query2.addCriteria(Criteria.where("userId").is(currentUserId).and("friendId").is(friendId));
        //2.根据当前用户的id 与 好友id 查询记录是否存在
        List<Friend> friendList2 = mongoTemplate.find(query2, Friend.class);
        if(CollectionUtils.isEmpty(friendList2)){
            //保存
            friend.setFriendId(friendId);
            friend.setUserId(currentUserId);
            friend.setCreated(System.currentTimeMillis());
            friend.setId(ObjectId.get());
            mongoTemplate.save(friend);
        }
    }
    /**
     * 联系人列表查询
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public PageResult<Friend> queryContacts(Integer page, Integer pagesize, Long userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.limit(pagesize).skip((page-1)*pagesize);//分页
        //1.查询总记录数
        long counts = mongoTemplate.count(query, Friend.class);
        //2.分页查询当前页面数据
        List<Friend> friendList = mongoTemplate.find(query, Friend.class);
        long pages = counts/pagesize + counts%pagesize > 0 ? 1:0;//总页码
        return new PageResult<>(counts,(long)pagesize,pages,(long)page,friendList);
    }

    /**
     * 互相喜欢统计
     * @param currentUserId
     * @return
     */
    @Override
    public Long eachLoveCount(Long currentUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(currentUserId));
        return mongoTemplate.count(query,Friend.class);
    }
    /**
     * 互相关注分页查询
     * @param page
     * @param pagesize
     * @param currentUserId
     * @return
     */
    @Override
    public PageResult<RecommendUser> queryMyLikeByPage(int page, int pagesize, Long currentUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(currentUserId));
        query.limit(pagesize).skip((page-1)*pagesize);//分页
        //1.查询总记录数
        long counts = mongoTemplate.count(query, Friend.class);
        //2.分页查询当前页面数据
        List<Friend> friendList = mongoTemplate.find(query, Friend.class);
        if(CollectionUtils.isEmpty(friendList)){
            return  null;
        }
        List<RecommendUser> recommendUserList = new ArrayList<>();
        for (Friend friend : friendList) {
            Long friendId = friend.getFriendId();//好友id
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
}
