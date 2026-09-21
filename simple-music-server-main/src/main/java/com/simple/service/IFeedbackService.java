package com.simple.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simple.model.dto.FeedbackDTO;
import com.simple.model.entity.Feedback;
import com.simple.result.PageResult;
import com.simple.result.Result;

import java.util.List;

public interface IFeedbackService extends IService<Feedback>  {
    Result<PageResult<Feedback>> getAllFeedbacks(FeedbackDTO feedbackDTO);

    Result deleteFeedback(Long id);

    Result deleteFeedbacks(List<Long> feedbackIds);
}
