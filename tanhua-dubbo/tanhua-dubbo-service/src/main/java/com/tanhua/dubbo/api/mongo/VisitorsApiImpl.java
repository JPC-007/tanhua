package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.mongo.Visitor;
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
 * 访客服务接口实现类
 */
@Service
public class VisitorsApiImpl implements VisitorsApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 根据当前用户id查询访客列表记录(前5条)
     * @param currentUserId
     * @return
     */
    @Override
    public List<Visitor> queryVisitorsByUserId(Long currentUserId) {
        Query query = new Query();
        //userId  date降序  limit 5
        query.addCriteria(Criteria.where("userId").is(currentUserId));
        query.with(Sort.by(Sort.Direction.DESC,"date"));
        query.limit(5);
        return mongoTemplate.find(query, Visitor.class);
    }

    /**
     * 根据当前用户id查询访客列表记录(前5条)
     * @param currentUserId
     * @return
     */
    @Override
    public List<Visitor> queryVisitorsByUserIdLastTime(Long currentUserId, Long lastTime) {
        Query query = new Query();
        //userId date>上次用户登录时间  date降序  limit 5
        query.addCriteria(Criteria.where("userId").is(currentUserId).and("date").gte(lastTime));
        query.with(Sort.by(Sort.Direction.DESC,"date"));
        query.limit(5);
        return mongoTemplate.find(query, Visitor.class);
    }

    /**
     * 保存访客记录
     */
    @Override
    public void save(Visitor visitor) {
        visitor.setId(ObjectId.get());
        visitor.setDate(System.currentTimeMillis());
        mongoTemplate.save(visitor);
    }

    /**
     * 谁看过我分页查询
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
        long counts = mongoTemplate.count(query, Visitor.class);
        //2.分页查询当前页面数据
        List<Visitor> visitorList = mongoTemplate.find(query, Visitor.class);
        if(CollectionUtils.isEmpty(visitorList)){
            return  null;
        }
        List<RecommendUser> recommendUserList = new ArrayList<>();
        for (Visitor visitor : visitorList) {
            Long friendId = visitor.getVisitorUserId();//好友id
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
