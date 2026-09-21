package com.simple.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simple.constant.MessageConstant;
import com.simple.enumeration.BannerStatusEnum;
import com.simple.mapper.BannerMapper;
import com.simple.model.dto.BannerAddDTO;
import com.simple.model.dto.BannerDTO;
import com.simple.model.entity.Banner;
import com.simple.model.vo.BannerVO;
import com.simple.result.PageResult;
import com.simple.result.Result;
import com.simple.service.IBannerService;
import com.simple.service.MinioService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements IBannerService {

    @Autowired
    private BannerMapper bannerMapper;

    @Autowired
    private MinioService minioService;

    /**
     * 获取轮播图列表
     *
     * @return 轮播图列表
     */
    @Override
    public Result<PageResult<Banner>> getAllBanners(BannerDTO bannerDTO) {
        Page<Banner> page = new Page<>(bannerDTO.getPageNum(), bannerDTO.getPageSize());
        QueryWrapper<Banner> queryWrapper = new QueryWrapper<>();
        if (bannerDTO.getBannerStatus() != null) {
            queryWrapper.eq("status", bannerDTO.getBannerStatus().getId());
        }
        // 倒序排序
        queryWrapper.orderByDesc("id");

        IPage<Banner> bannerIpage = bannerMapper.selectPage(page, queryWrapper);
        if (bannerIpage.getRecords().size() == 0) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }
        return Result.success(new PageResult<>(bannerIpage.getTotal(), bannerIpage.getRecords()));
    }

    /**
     * 新增轮播图
     *
     * @return 轮播图列表
     */
    @Override
    public Result addBanner(String bannerUrl) {
        Banner banner = new Banner();
        banner.setBannerUrl(bannerUrl)
                .setBannerStatus(BannerStatusEnum.ENABLE);

        if (bannerMapper.insert(banner) == 0) {
            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    /**
     * 编辑轮播图
     *
     * @return 轮播图列表
     */
    @Override
    public Result updateBanner(Long id,String bannerUrl) {
        // 删除原图片
        Banner banner = bannerMapper.selectById(id);

        if(banner == null){
            return Result.error(MessageConstant.DATA_NOT_FOUND);
        }

        if(banner.getBannerUrl() != null && !banner.getBannerUrl().isEmpty()){
            minioService.deleteFile(banner.getBannerUrl());
        }
        // 更新图片
        banner.setBannerUrl(bannerUrl);
        if (bannerMapper.updateById(banner) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 更新轮播图状态
     *
     * @return 轮播图列表
     */
    @Override
    public Result updateBannerStatus(Long id, Integer bannerStatus) {
        BannerStatusEnum statusEnum;
        if(bannerStatus == 0){
            statusEnum = BannerStatusEnum.ENABLE;
        }else if(bannerStatus == 1){
            statusEnum = BannerStatusEnum.DISABLE;
        }else{
            return Result.error(MessageConstant.BANNER_STATUS_INVALID);
        }

        // 更新轮播图状态
        Banner banner = new Banner();
        banner.setBannerId(id).setBannerStatus(statusEnum);

        if(bannerMapper.updateById(banner) == 0){
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 删除轮播图
     *
     * @return 轮播图列表
     */
    @Override
    public Result deleteBanner(Long id) {
        Banner banner = bannerMapper.selectById(id);
        if(banner == null){
            return Result.error(MessageConstant.DATA_NOT_FOUND);
        }
        // 删除minio里面的图片
        String bannerUrl = banner.getBannerUrl();
        if(bannerUrl != null && !bannerUrl.isEmpty()){
            minioService.deleteFile(bannerUrl);
        }

        if(bannerMapper.deleteById(id) == 0){
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 批量删除轮播图
     *
     * @return 轮播图列表
     */
    @Override
    public Result deleteBanners(List<Long> ids) {
        List<Banner> banners = bannerMapper.selectByIds(ids);
        for (Banner banner : banners) {
            if(banner.getBannerUrl() != null && !banner.getBannerUrl().isEmpty()){
                minioService.deleteFile(banner.getBannerUrl());
            }
            if(bannerMapper.deleteById(banner.getBannerId()) == 0){
                return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
            }
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 获取轮播图列表
     *
     * @return 轮播图列表
     */
    @Override
    public Result<List<BannerVO>> getBannerList() {
        // 获取最后九个有效的轮播图
        List<Banner> banners = bannerMapper.selectList(new QueryWrapper<Banner>()
                .eq("status", BannerStatusEnum.ENABLE.getId())
                .orderByDesc("id")
                .last("limit 9"));

        List<BannerVO> bannerVOList = banners.stream()
                .map(banner -> {
                    BannerVO bannerVO = new BannerVO();
                    BeanUtils.copyProperties(banner, bannerVO);
                    return bannerVO;
                }).toList();
        return Result.success(bannerVOList);
    }
}
