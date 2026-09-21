package com.simple.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simple.constant.MessageConstant;
import com.simple.mapper.FeedbackMapper;
import com.simple.model.dto.FeedbackDTO;
import com.simple.model.entity.Feedback;
import com.simple.result.PageResult;
import com.simple.result.Result;
import com.simple.service.IFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements IFeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Override
    public Result<PageResult<Feedback>> getAllFeedbacks(FeedbackDTO feedbackDTO) {
        Page<Feedback> page = new Page<>(feedbackDTO.getPageNum(), feedbackDTO.getPageSize());
        QueryWrapper<Feedback> queryWrapper = new QueryWrapper<>();
        if (feedbackDTO.getKeyword() != null) {
            queryWrapper.like("feedback", feedbackDTO.getKeyword());
        }
        // 倒序排序
        queryWrapper.orderByDesc("create_time");
        IPage<Feedback> feedbackPage = feedbackMapper.selectPage(page,queryWrapper);
        if (feedbackPage.getRecords().size() == 0) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }
        return Result.success(new PageResult<>(feedbackPage.getTotal(), feedbackPage.getRecords()));
    }

    /**
     * 删除反馈
     *
     * @param feedbackId
     * @return
     */
    @Override
    @CacheEvict(cacheNames = "feedbackCache", allEntries = true)
    public Result deleteFeedback(Long feedbackId) {
        if (feedbackMapper.deleteById(feedbackId) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 批量删除反馈
     *
     * @param feedbackIds
     * @return
     */
    @Override
    @CacheEvict(cacheNames = "feedbackCache", allEntries = true)
    public Result deleteFeedbacks(List<Long> feedbackIds) {
        if (feedbackMapper.deleteByIds(feedbackIds) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }


}
