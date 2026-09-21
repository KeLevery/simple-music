package com.simple.controller;

import com.simple.model.dto.FeedbackDTO;
import com.simple.model.entity.Feedback;
import com.simple.result.PageResult;
import com.simple.result.Result;
import com.simple.service.IFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping
public class FeedbackController {

    @Autowired
    private IFeedbackService feedbackService;

    /**
     * 获取反馈列表
     *
     * @return 反馈列表
     */
    @PostMapping("/admin/getAllFeedbacks")
    public Result<PageResult<Feedback>> getAllFeedbacks(@RequestBody FeedbackDTO feedbackDTO) {
        return feedbackService.getAllFeedbacks(feedbackDTO);
    }

    /**
     * 删除反馈
     *
     * @return 反馈列表
     */
    @DeleteMapping("/admin/deleteFeedback/{id}")
    public Result deleteFeedback(@PathVariable Long id) {
        return feedbackService.deleteFeedback(id);
    }

    /**
     * 批量删除反馈
     *
     * @return 反馈列表
     */
    @DeleteMapping("/admin/deleteFeedbacks")
    public Result deleteFeedbacks(@RequestBody List<Long> feedbackIds) {
        return feedbackService.deleteFeedbacks(feedbackIds);
    }
}
