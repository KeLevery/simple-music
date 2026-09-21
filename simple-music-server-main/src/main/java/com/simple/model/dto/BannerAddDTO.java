package com.simple.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BannerAddDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private MultipartFile image;

    private String url;

    private Integer status;
}
