package com.tanhua.server.service;

import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.VideoVo;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.dubbo.api.mongo.VideoApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 小视频业务处理类
 */
@Service
@Slf4j
public class VideoService {

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private FastFileStorageClient client;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Reference
    private VideoApi videoApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;


    /**
     * 发布小视频 smallVideos
     * videoThumbnail:视频封面
     * videoFile：视频文件
     */
    @CacheEvict(value = "Video",allEntries = true)
    public void saveSmallVideos(MultipartFile videoThumbnail, MultipartFile videoFile) {
        try {
            //a.视频封面图片文件（oss存储）、视频文件（fastdfs存储）
            String filename = videoThumbnail.getOriginalFilename();//原始文件名
            //图片地址
            String picUrl = ossTemplate.upload(filename, videoThumbnail.getInputStream());
            //b.分别得到图片地址、视频地址
            String videoFileName = videoFile.getOriginalFilename();
            String videoSuffix = videoFileName.substring(videoFileName.lastIndexOf(".") + 1);
            StorePath storePath = client.uploadFile(videoFile.getInputStream(), videoFile.getSize(), videoSuffix, null);
            //视频地址
            String videoUrl = fdfsWebServer.getWebServerUrl() + storePath.getFullPath();//目录+文件名
            //c.封装数据调用服务提供者保存视频记录
            Video video = new Video();
            video.setUserId(UserHolder.getUserId());//当前用户id
            video.setText("探花交友");//文字
            video.setPicUrl(picUrl);//图片地址
            video.setVideoUrl(videoUrl);//视频地址
            log.debug("发布小视频" + video.toString());
            videoApi.saveSmallVideos(video);
        } catch (IOException e) {
            log.debug("上传小视频异常。。。");
            throw new TanHuaException(ErrorResult.error());
        }
    }

    /**
     * 小视频列表
     * key:video::1_10 value:PageResult<VideoVo>
     */
    @Cacheable(value = "Video",key = "#page+'_'+#pagesize")
    public PageResult<VideoVo> querySmallVideos(int page, int pagesize) {
        log.debug("查询小视频列表********************");
        Long currentUserId = UserHolder.getUserId();
        PageResult<VideoVo> voPageResult = new PageResult<>();

        //a.调用小视频列表分页查询服务方法
        PageResult<Video> pageResult = videoApi.querySmallVideos(page, pagesize);
        if (StringUtils.isEmpty(pageResult) || CollectionUtils.isEmpty(pageResult.getItems())) {
            //前端没有处理的很好 返回空对象设置值
            voPageResult = new PageResult<>(0l, 10l, 0l, 1l, null);
            return voPageResult;
        }
        List<VideoVo> videoVoList = new ArrayList<>();
        //b.根据视频记录中发布小视频的用户id查询UserInfo信息
        for (Video video : pageResult.getItems()) {
            VideoVo videoVo = new VideoVo();

            Long userId = video.getUserId();//发布小视频的用户id
            UserInfo userInfo = userInfoApi.queryUserInfo(userId);

            BeanUtils.copyProperties(userInfo, videoVo);//头像 昵称
            BeanUtils.copyProperties(video, videoVo);//视频地址 点赞数量 评论数量

            if (StringUtils.isEmpty(video.getText())) {
                videoVo.setSignature("小花");//签名
            } else {
                videoVo.setSignature(video.getText());//签名
            }
            videoVo.setHasLiked(0);//是否已赞（1是，0否）
            videoVo.setCover(video.getPicUrl());//视频封面
            String key = "follower_user_"+currentUserId+"_"+userId;
            if(StringUtils.isEmpty(redisTemplate.opsForValue().get(key))){
                videoVo.setHasFocus(0);//是否关注 （1是，0否）
            }else {
                videoVo.setHasFocus(1);//是否关注 （1是，0否）
            }
            videoVo.setUserId(userId);//发布小视频的用户id
            videoVo.setId(video.getId().toHexString());//视频主键id
            videoVoList.add(videoVo);
        }
        //c.将小视频数据与用户信息封装返回Vo
        BeanUtils.copyProperties(pageResult, voPageResult);
        voPageResult.setItems(videoVoList);
        return voPageResult;
    }

    /**
     * 视频用户关注
     */
    public void userFocus(Long followUserId) {
        Long currentUserId = UserHolder.getUserId();
        //1.保存数据关注表
        FollowUser followUser = new FollowUser();
        followUser.setUserId(currentUserId);
        followUser.setFollowUserId(followUserId);
        videoApi.saveFollowUser(followUser);
        //2.将关注记录写入redis
        String key = "follower_user_"+currentUserId+"_"+followUserId;
        redisTemplate.opsForValue().set(key,"true");
    }

    /**
     * 视频用户关注
     */
    public void userUnFocus(Long followUserId) {
        Long currentUserId = UserHolder.getUserId();
        //1.从关注表删除记录
        FollowUser followUser = new FollowUser();
        followUser.setUserId(currentUserId);
        followUser.setFollowUserId(followUserId);
        videoApi.removeFollowUser(followUser);
        //2.将关注记录从redis删除
        String key = "follower_user_"+currentUserId+"_"+followUserId;
        redisTemplate.delete(key);
    }
}
