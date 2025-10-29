package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.*;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 圈子服务接口实现类
 */
@Service
public class PublishApiImpl implements PublishApi{

    @Autowired
    private MongoTemplate mongoTemplate;
    /**
     * 发布动态
     * @param publishVo
     */
    @Override
    public String savePublish(PublishVo publishVo) {
        Long currentUserId = publishVo.getUserId();//当前用户id
        //发布动态时间
        long nowTime = System.currentTimeMillis();
        //a.往动态表插入动态数据quanzi_publish
        Publish publish = new Publish();
        BeanUtils.copyProperties(publishVo,publish);
        publish.setId(ObjectId.get());//设置发布表主键id
        publish.setPid(666l);//推荐系统 没有用到
        publish.setLocationName(publishVo.getLocation());//位置名称
        publish.setSeeType(1);//默认公开
        publish.setCreated(nowTime); //发布时间
        //发布动态默认状态为 待审核
        publish.setState(0);//状态0：待审核，1：已审核，2：已驳回
        mongoTemplate.save(publish);
        //b.往相册表插入我的动态记录（动态id 或 发布id） quanzi_album_当前用户id
        Album album = new Album();
        album.setId(ObjectId.get());//主键id
        album.setPublishId(publish.getId());
        album.setCreated(nowTime);
        mongoTemplate.save(album,"quanzi_album_"+currentUserId);
        //c.先查询好友表获取好友ids
        Query friendQuery = new Query();
        friendQuery.addCriteria(Criteria.where("userId").is(currentUserId));
        List<Friend> friendList = mongoTemplate.find(friendQuery, Friend.class);
        if(!CollectionUtils.isEmpty(friendList)) {
            //d.再往好友时间线表插入动态记录（动态id 或 发布id） quanzi_time_line_好友id
            for (Friend friend : friendList) {
                Long friendId = friend.getFriendId();//好友id
                TimeLine timeLine = new TimeLine();
                timeLine.setId(ObjectId.get());//主键id
                timeLine.setUserId(currentUserId);//发布动态用户id
                timeLine.setPublishId(publish.getId());//发布id
                timeLine.setCreated(nowTime);
                mongoTemplate.save(timeLine,"quanzi_time_line_"+friendId);
            }
        }

        return publish.getId().toHexString();
    }
    /**
     * 分页查询动态发布表（先查询时间线表 再查询动态发布表）
     * @param page
     * @param pagesize
     * @param currentUserId
     * @return
     */
    @Override
    public PageResult<Publish> queryPublishByTimeLine(int page, int pagesize, Long currentUserId) {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC,"created"));
        query.limit(pagesize).skip((page-1)*pagesize);
        //a.根据当前用户id 分页查询自己的时间线表 quanzi_time_line_当前用户id
        long counts = mongoTemplate.count(query, "quanzi_time_line_" + currentUserId);
        //b.查询时间线表 （条件：根据created 降序排序）
        List<TimeLine> timeLineList = mongoTemplate.find(query, TimeLine.class, "quanzi_time_line_" + currentUserId);

        //将List<TimeLine> 循环遍历 查询 List<Publish>
        List<Publish> publishList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(timeLineList)){
            for (TimeLine timeLine : timeLineList) {
                ObjectId publishId = timeLine.getPublishId();//动态发布id
                if(!StringUtils.isEmpty(publishId)){
                    Publish pb = mongoTemplate.findById(publishId, Publish.class);
                    if(!StringUtils.isEmpty(pb)) {
                        publishList.add(pb);
                    }
                }
            }
        }
        //b.根据发布id查询发布表
        long pages = counts/pagesize + counts%pagesize > 0 ? 1:0;//总页码
        return new PageResult<>(counts,(long)pagesize,pages,(long)page,publishList);
    }
    /**
     * 推荐动态
     */
    @Override
    public PageResult<Publish> queryPublishByReQuanzi(int page, int pagesize, Long currentUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(currentUserId));
        query.with(Sort.by(Sort.Direction.DESC,"created"));
        query.limit(pagesize).skip((page-1)*pagesize);
        //a.根据当前用户id 分页查询自己的时间线表 quanzi_time_line_当前用户id
        long counts = mongoTemplate.count(query, RecommendQuanzi.class);
        //b.查询时间线表 （条件：根据created 降序排序）
        List<RecommendQuanzi> recommendQuanziList = mongoTemplate.find(query, RecommendQuanzi.class);
        //将List<TimeLine> 循环遍历 查询 List<Publish>
        List<Publish> publishList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(recommendQuanziList)){
            for (RecommendQuanzi recommendQuanzi : recommendQuanziList) {
                ObjectId publishId = recommendQuanzi.getPublishId();//动态发布id
                if(!StringUtils.isEmpty(publishId)){
                    Publish pb = mongoTemplate.findById(publishId, Publish.class);
                    if(!StringUtils.isEmpty(pb)) {
                        publishList.add(pb);
                    }
                }
            }
        }
        //b.根据发布id查询发布表
        long pages = counts/pagesize + counts%pagesize > 0 ? 1:0;//总页码
        return new PageResult<>(counts,(long)pagesize,pages,(long)page,publishList);
    }

    /**
     * 用户动态
     * @param page
     * @param pagesize
     * @param currentUserId
     * @return
     */
    @Override
    public PageResult<Publish> queryPublishByAlbum(int page, int pagesize, Long currentUserId) {

        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC,"created"));
        query.limit(pagesize).skip((page-1)*pagesize);
        //a.根据当前用户id拼接表名 分页查询自己的相册表
        long counts = mongoTemplate.count(query, "quanzi_album_"+currentUserId);
        //b.查询相册表 （条件：根据created 降序排序）
        List<Album> albumList = mongoTemplate.find(query, Album.class,"quanzi_album_"+currentUserId);
        //将List<Album> 循环遍历 查询 List<Publish>
        List<Publish> publishList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(albumList)){
            for (Album album : albumList) {
                ObjectId publishId = album.getPublishId();//动态发布id
                if(!StringUtils.isEmpty(publishId)){
                    Publish pb = mongoTemplate.findById(publishId, Publish.class);
                    if(!StringUtils.isEmpty(pb)) {
                        publishList.add(pb);
                    }
                }
            }
        }
        //b.根据发布id查询发布表
        long pages = counts/pagesize + counts%pagesize > 0 ? 1:0;//总页码
        return new PageResult<>(counts,(long)pagesize,pages,(long)page,publishList);
    }

    /**
     * 单条动态
     */
    @Override
    public Publish queryPublish(String publishId) {
        return mongoTemplate.findById(new ObjectId(publishId),Publish.class);
    }

    /**
     * 根据发布id更新状态
     * @param publishId
     * @param state
     */
    @Override
    public void updatePublishState(String publishId, Integer state) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(new ObjectId(publishId)));
        Update update = new Update();
        update.set("state",state);
        mongoTemplate.updateFirst(query,update,Publish.class);
    }
}
