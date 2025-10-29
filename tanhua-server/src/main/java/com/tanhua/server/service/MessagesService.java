package com.tanhua.server.service;

import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.vo.ContactVo;
import com.tanhua.domain.vo.MessageVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.dubbo.api.mongo.FriendApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 消息业务处理层
 */
@Service
@Slf4j
public class MessagesService {

    @Reference
    private FriendApi friendApi;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private CommentApi commentApi;

    /**
     * 添加联系人
     */
    public void saveContacts(Long personUserId) {
        Long currentUserId = UserHolder.getUserId();
        //1.调用服务提供者 添加好友关系 tanhua_users表中
        Friend friend = new Friend();
        friend.setUserId(currentUserId);
        friend.setFriendId(personUserId);
        friendApi.saveFriend(friend);
        //2.调用环信云 makeFriends方法
        huanXinTemplate.makeFriends(personUserId,currentUserId);
        log.debug("添加联系人成功了,当前用户id{}，好友用户id{}",currentUserId,personUserId);
    }

    /**
     * 联系人列表
     * required = false:keyword 此参数非必须
     */
    public PageResult<ContactVo> queryContacts(Integer page, Integer pagesize, String keyword) {
        PageResult<ContactVo> voPageResult = new PageResult<>();
        Long userId = UserHolder.getUserId();
        //1.根据分页参数查询联系人列表数据(得到好友ids)
        PageResult<Friend> pageResult = friendApi.queryContacts(page,pagesize,userId);
        if(pageResult == null || CollectionUtils.isEmpty(pageResult.getItems())){
            voPageResult = new PageResult<>(0l,10l,0l,1l,null);
            return voPageResult;
        }
        //将List<Friend> 转为 List<ContactVo>
        List<ContactVo> contactVoList = new ArrayList<>();

        //2.根据好友ids查询userInfo
        Long myid = 1l;
        for (Friend friend : pageResult.getItems()) {
            ContactVo contactVo = new ContactVo();
            Long friendId = friend.getFriendId();//好友id
            UserInfo userInfo = userInfoApi.queryUserInfo(friendId);
            BeanUtils.copyProperties(userInfo,contactVo);
            contactVo.setId(myid);//序号
            contactVo.setUserId(friendId.toString());//好友id
            contactVoList.add(contactVo);
            myid++;
        }
        //3.封装数据返回
        BeanUtils.copyProperties(pageResult,voPageResult);
        voPageResult.setItems(contactVoList);
        return voPageResult;
    }

    /**
     * 点赞 喜欢 评论 列表
     * 评论类型，1-点赞，2-评论，3-喜欢
     */
    public PageResult<MessageVo> queryCommentPage(Integer page, Integer pagesize, int num) {
        PageResult<MessageVo> voPageResult = new PageResult<>();
        Long currentUserId = UserHolder.getUserId();
        //1.根据分页参数 评论类型1-点赞，2-评论，3-喜欢  当前用户id(被评论的用户id)publishUserId
        PageResult<Comment> pageResult = commentApi.queryCommentPage(page,pagesize,num,currentUserId);
        if(pageResult == null || CollectionUtils.isEmpty(pageResult.getItems())){
            voPageResult = new PageResult<>(0l,10l,0l,1l,null);
            return voPageResult;
        }
        List<MessageVo> messageVoList = new ArrayList<>();
        //2.根据评论记录中userId查询userInfo
        for (Comment comment : pageResult.getItems()) {
            MessageVo messageVo = new MessageVo();
            Long userId = comment.getUserId();//评论人用户id
            UserInfo userInfo = userInfoApi.queryUserInfo(userId);
            BeanUtils.copyProperties(userInfo,messageVo);//头像 昵称
            messageVo.setId(userId.toString());//设置评论人的用户id
            Long created = comment.getCreated(); // 2019-09-08 10:07
            messageVo.setCreateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(created)));
            messageVoList.add(messageVo);
        }
        //3.将userInfo 跟 评论时间封装vo返回
        BeanUtils.copyProperties(pageResult,voPageResult);
        voPageResult.setItems(messageVoList);
        return voPageResult;
    }
}
