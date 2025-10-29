package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * 佳人服务接口实现类
 */
@Service
public class RecommendUserApiImpl implements RecommendUserApi{

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 根据当前用户查询佳人
     * @param userId
     * @return
     */
    @Override
    public RecommendUser queryMaxScore(Long userId) {
        //db.getCollection("recommend_user").find({toUserId:1}).sort({"score":-1}).limit(1)
        //理论情况应该再加一个条件date
        Query query = new Query();
        query.addCriteria(Criteria.where("toUserId").is(userId));//根据当前用户id查询
        query.with(Sort.by(Sort.Direction.DESC,"score"));//降序
        query.limit(1);
        return mongoTemplate.findOne(query,RecommendUser.class);
    }

    /**
     * 根据当前用户id分页查询用户列表
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public PageResult<RecommendUser> queryPageByUserId(Integer page, Integer pagesize, Long userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("toUserId").is(userId));
        query.with(Sort.by(Sort.Direction.DESC,"score"));//降序
        query.limit(pagesize).skip((page-1)*pagesize);//分页
        //1.查询总记录数
        long counts = mongoTemplate.count(query, RecommendUser.class);
        //2.分页查询当前页面数据
        List<RecommendUser> recommendUserList = mongoTemplate.find(query, RecommendUser.class);
        long pages = counts/pagesize + counts%pagesize > 0 ? 1:0;//总页码
        return new PageResult<>(counts,(long)pagesize,pages,(long)page,recommendUserList);
    }

    /**
     * 根据佳人用户id 与 推荐的用户id 查询缘分值
     * @param personUserId
     * @param currentUserId
     * @return
     */
    @Override
    public RecommendUser queryScoreByPersonUserId(Long personUserId, Long currentUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(personUserId).and("toUserId").is(currentUserId));
        query.with(Sort.by(Sort.Direction.DESC,"date"));
        query.limit(1);
        return mongoTemplate.findOne(query,RecommendUser.class);
    }
}
