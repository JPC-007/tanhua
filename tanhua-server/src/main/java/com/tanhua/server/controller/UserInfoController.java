package com.tanhua.server.controller;

import com.tanhua.domain.vo.CountsVo;
import com.tanhua.domain.vo.FriendVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.server.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息控制层
 */
@RestController
@RequestMapping("/users")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;


    /**
     * 查询用户信息
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity queryUserInfo(Long userID, Long huanxinID) {
        UserInfoVo userInfoVo = userInfoService.queryUserInfo(userID, huanxinID);
        return ResponseEntity.ok(userInfoVo);
    }


    /**
     * 更新用户信息
     */
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity updateUserInfo(@RequestBody UserInfoVo userInfoVo) {
        userInfoService.updateUserInfo(userInfoVo);
        return ResponseEntity.ok(null);
    }


    /**
     * 互相喜欢，喜欢，粉丝 - 统计
     */
    @RequestMapping(value = "/counts", method = RequestMethod.GET)
    public ResponseEntity queryCounts() {
        CountsVo countsVo = userInfoService.queryCounts();
        return ResponseEntity.ok(countsVo);
    }

    /**
     * 互相喜欢，喜欢，粉丝 谁看过我 分页查询
     * type:
     * 1 互相关注
     * 2 我关注
     * 3 粉丝
     * 4 谁看过我
     */
    @RequestMapping(value = "/friends/{type}", method = RequestMethod.GET)
    public ResponseEntity queryMyLikeByPage(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize, @PathVariable("type") String type) {
        PageResult<FriendVo> friendVoPageResult = userInfoService.queryMyLikeByPage(page, pagesize, type);
        return ResponseEntity.ok(friendVoPageResult);
    }


    /**
     * 粉丝 - 喜欢
     */
    @RequestMapping(value = "/fans/{uid}", method = RequestMethod.POST)
    public ResponseEntity fansLike(@PathVariable("uid") Long fansUserId) {
        userInfoService.fansLike(fansUserId);
        return ResponseEntity.ok(null);
    }
}
