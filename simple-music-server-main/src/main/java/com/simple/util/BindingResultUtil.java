package com.simple.util;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

//此类用于使用BindingResult的错误校验
public class BindingResultUtil {
    /**
     * 处理参数校验错误，将 BindingResult 中的错误信息拼接成字符串
     *
     * @param bindingResult Spring 参数校验结果对象，包含所有校验错误的详细信息
     * @return 如果存在校验错误，返回拼接后的错误提示字符串；如果没有错误，返回 null
     */
    public static String handleBindingResultErrors(BindingResult bindingResult) {
        // 检查是否存在参数校验错误
        if(bindingResult.hasErrors()){
            // 构建错误提示信息
            StringBuilder errorMessage = new StringBuilder("输入参数校验失败");
            // 遍历所有错误，将每个错误的提示信息拼接到结果中
            for(ObjectError error : bindingResult.getAllErrors()){
                errorMessage.append(error.getDefaultMessage()).append("; ");
            }
            return errorMessage.toString();
        }
        return null;
    }
}
