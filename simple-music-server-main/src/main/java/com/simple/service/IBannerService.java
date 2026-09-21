package com.simple.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simple.model.dto.BannerAddDTO;
import com.simple.model.dto.BannerDTO;
import com.simple.model.entity.Banner;
import com.simple.result.PageResult;
import com.simple.result.Result;

import java.util.List;

public interface IBannerService extends IService<Banner> {
    Result<PageResult<Banner>> getAllBanners(BannerDTO bannerDTO);

    Result addBanner(String cover);

    Result updateBanner(Long id,String banner);

    Result updateBannerStatus(Long id, Integer bannerStatus);

    Result deleteBanner(Long id);

    Result deleteBanners(List<Long> ids);

    Result getBannerList();
}
