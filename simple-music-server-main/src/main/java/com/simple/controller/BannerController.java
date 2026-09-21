package com.simple.controller;


import com.simple.model.dto.BannerAddDTO;
import com.simple.model.dto.BannerDTO;
import com.simple.model.entity.Banner;
import com.simple.model.vo.BannerVO;
import com.simple.result.PageResult;
import com.simple.result.Result;
import com.simple.service.IBannerService;
import com.simple.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class BannerController {
    @Autowired
    IBannerService bannerService;

    @Autowired
    MinioService minioService;
    /**
     * 获取轮播图列表
     *
     * @return 轮播图列表
     */
    @PostMapping("/admin/getAllBanners")
    public Result<PageResult<Banner>> getAllBanners(@RequestBody BannerDTO bannerDTO){
        return bannerService.getAllBanners(bannerDTO);
    }

    /**
     * 新增轮播图
     *
     * @return 轮播图列表
     */
    @PostMapping("/admin/addBanner")
    public Result addBanner(@RequestParam("banner") MultipartFile banner){
        String bannerUrl = minioService.uploadFile(banner, "banners");
        return bannerService.addBanner(bannerUrl);
    }

    /**
     * 编辑轮播图
     *
     * @return 轮播图列表
     */
    @PostMapping("/admin/updateBanner/{id}")
    public Result updateBanner(@PathVariable Long id,@RequestParam("banner") MultipartFile banner){
        String banners = minioService.uploadFile(banner, "banners");
        return bannerService.updateBanner(id, banners);
    }

    /**
     * 更新轮播图状态
     *
     * @return 轮播图列表
     */
    @PatchMapping("/admin/updateBannerStatus/{id}")
    public Result updateBannerStatus(@PathVariable Long id,@RequestParam("status") Integer bannerStatus){
        return bannerService.updateBannerStatus(id,bannerStatus);
    }

    /**
     * 删除轮播图
     *
     * @return 轮播图列表
     */
    @DeleteMapping("/admin/deleteBanner/{id}")
    public Result deleteBanner(@PathVariable Long id){
        return bannerService.deleteBanner(id);
    }

    /**
     * 批量删除轮播图
     *
     * @return 轮播图列表
     */
    @DeleteMapping("/admin/deleteBanners")
    public Result deleteBanners(@RequestBody List<Long> ids){
        return bannerService.deleteBanners(ids);
    }

    /**
     * 批量删除轮播图
     *
     * @return 轮播图列表
     */
    @GetMapping("/banner/getBannerList")
    public Result<List<BannerVO>> getBannerList(){
        return bannerService.getBannerList();
    }
}
