package com.tanhua.dubbo.api.db;

import com.tanhua.domain.db.Question;

/**
 * 问题服务接口
 */
public interface QuestionApi {
    /**
     * 根据用户id查询问题记录
     * @param userId
     * @return
     */
    Question queryByUserId(Long userId);

    /**
     *保存问题表记录
     * @param question
     */
    void saveQuestion(Question question);

    /**
     *更新问题表
     * @param question
     */
    void updateQuestion(Question question);
}
