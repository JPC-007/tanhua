package com.tanhua.dubbo.api.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.Settings;
import com.tanhua.dubbo.mapper.QuestionMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 问题服务接口实现类
 */
@Service
public class QuestionApiImpl implements QuestionApi{

    @Autowired
    private QuestionMapper questionMapper;
    /**
     * 根据用户id查询问题记录
     * @param userId
     * @return
     */
    @Override
    public Question queryByUserId(Long userId) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id",userId);
        return questionMapper.selectOne(queryWrapper);
    }
    /**
     *保存问题表记录
     * @param question
     */
    @Override
    public void saveQuestion(Question question) {
        questionMapper.insert(question);
    }
    /**
     *更新问题表
     * @param question
     */
    @Override
    public void updateQuestion(Question question) {
        questionMapper.updateById(question);
    }
}
