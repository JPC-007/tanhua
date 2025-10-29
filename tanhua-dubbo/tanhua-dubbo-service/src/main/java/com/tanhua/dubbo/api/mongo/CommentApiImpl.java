package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

/**
 * 评论服务接口实现类
 */
@Service
public class CommentApiImpl implements CommentApi{

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 动态点赞 点赞数量+1 返回点赞总数
     * 动态喜欢 喜欢数量+1 返回喜欢总数
     * @param comment
     * @return
     */
    @Override
    public long saveComment(Comment comment) {
        //a.将动态点赞、喜欢记录保存评论表
        comment.setId(ObjectId.get());//主键id
        comment.setCreated(System.currentTimeMillis());//点赞时间
        //对动态点赞 评论 喜欢 被评论的用户设置
        if(comment.getPubType() == 1){//动态
            ObjectId publishId = comment.getPublishId();
            Publish publish = mongoTemplate.findById(publishId, Publish.class);
            if(publish != null){
                comment.setPublishUserId(publish.getUserId());//被评论的用户id(发布表的中userId)
            }
        }
        mongoTemplate.save(comment);
        //b.根据发布id更新发布表点赞数量+1
        updateComment(comment,1);
        //c.根据发布id查询发布表点赞数量 返回
        long count = queryCount(comment);
        return count;
    }

    /**
     * 根据发布id对点赞数量进行更新
     * @param comment
     * @param num
     */
    private void updateComment(Comment comment, int num) {
        //方式一：
        /*if(comment.getPubType() == 1){//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
            Query query = new Query();//更新点赞数量条件
            query.addCriteria(Criteria.where("_id").is(comment.getPublishId()));//根据发布id条件
            if(comment.getCommentType() == 1){//评论类型，1-点赞，2-评论，3-喜欢
                Update update = new Update();
                update.inc("likeCount",num);//inc：针对某个字段值 增加或减少值
                mongoTemplate.updateFirst(query,update, Publish.class);
            }
            if(comment.getCommentType() == 2){//评论类型，1-点赞，2-评论，3-喜欢
                Update update = new Update();
                update.inc("commentCount",num);//inc：针对某个字段值 增加或减少值
                mongoTemplate.updateFirst(query,update, Publish.class);
            }
            if(comment.getCommentType() == 3){//评论类型，1-点赞，2-评论，3-喜欢
                Update update = new Update();
                update.inc("loveCount",num);//inc：针对某个字段值 增加或减少值
                mongoTemplate.updateFirst(query,update, Publish.class);
            }
        }*/

        //方式二：
        if(comment.getPubType() == 1 || comment.getPubType() == 3) {//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
            Query query = new Query();//更新点赞数量条件
            query.addCriteria(Criteria.where("_id").is(comment.getPublishId()));//根据发布id条件
            Update update = new Update();
            update.inc(comment.getCol(),num);//inc：针对某个字段值 增加或减少值
            Class<?> myClass = Publish.class;
            if(comment.getPubType() == 3){
                myClass=Comment.class;
            }
            mongoTemplate.updateFirst(query,update, myClass);
        }
    }

    /**
     * 动态取消点赞 点赞数量-1 返回点赞总数
     * @param comment
     * @return
     */
    @Override
    public long removeComment(Comment comment) {
        ///a.将动态点赞记录从评论表删除
        Query query = new Query();
        query.addCriteria(
                Criteria.where("publishId").is(comment.getPublishId()) //发布id
                        .and("commentType").is(comment.getCommentType())//评论类型
                        .and("pubType").is(comment.getPubType())//评论内容类型
                        .and("userId").is(comment.getUserId())//评论人
        );
        mongoTemplate.remove(query,Comment.class);
        //b.根据发布id更新发布表点赞数量-1
        updateComment(comment,-1);
        //c.根据发布id查询发布表点赞数量 返回
        long count = queryCount(comment);
        return count;
    }

    /**
     * 评论列表分页查询
     * @param publishId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult<Comment> queryCommentsByPage(String publishId, int page, int pagesize) {
        Query query = new Query();
        //publishId：动态id  commentType=2 pubType=1
        query.addCriteria(
                Criteria.where("publishId").is(new ObjectId(publishId))
                .and("commentType").is(2)
                .and("pubType").is(1)
        );
        query.with(Sort.by(Sort.Direction.ASC,"created"));//降序
        query.limit(pagesize).skip((page-1)*pagesize);//分页
        //1.查询总记录数
        long counts = mongoTemplate.count(query, Comment.class);
        //2.分页查询当前页面数据
        List<Comment> commentList = mongoTemplate.find(query, Comment.class);
        long pages = counts/pagesize + counts%pagesize > 0 ? 1:0;//总页码
        return new PageResult<>(counts,(long)pagesize,pages,(long)page,commentList);
    }

    /**
     * 点赞 喜欢 评论 列表
     * 评论类型，1-点赞，2-评论，3-喜欢
     */
    @Override
    public PageResult<Comment> queryCommentPage(Integer page, Integer pagesize, int num, Long currentUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("commentType").is(num).and("publishUserId").is(currentUserId));
        query.with(Sort.by(Sort.Direction.DESC,"created"));//降序
        query.limit(pagesize).skip((page-1)*pagesize);//分页
        //1.查询总记录数
        long counts = mongoTemplate.count(query, Comment.class);
        //2.分页查询当前页面数据
        List<Comment> commentList = mongoTemplate.find(query, Comment.class);
        long pages = counts/pagesize + counts%pagesize > 0 ? 1:0;//总页码
        return new PageResult<>(counts,(long)pagesize,pages,(long)page,commentList);
    }

    /**
     * 查询点赞 喜欢 评论 数量
     * @param comment
     * @return
     */
    private long queryCount(Comment comment) {
        if(comment.getPubType() == 1){//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(comment.getPublishId()));//发布表主键id
            Publish publish = mongoTemplate.findOne(query, Publish.class);
            if(comment.getCommentType() == 1){//评论类型，1-点赞，2-评论，3-喜欢
                return publish.getLikeCount();
            }
            if(comment.getCommentType() == 2){//评论类型，1-点赞，2-评论，3-喜欢
                return publish.getCommentCount();
            }
            if(comment.getCommentType() == 3){//评论类型，1-点赞，2-评论，3-喜欢
                return publish.getLoveCount();
            }
        }
        if(comment.getPubType() == 3){
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(comment.getPublishId()));//被点赞记录主键id
            Comment cm = mongoTemplate.findOne(query, Comment.class);
            if(comment.getCommentType() == 1){//评论类型，1-点赞，2-评论，3-喜欢
                return cm.getLikeCount();
            }
        }
        return 0;
    }
}
