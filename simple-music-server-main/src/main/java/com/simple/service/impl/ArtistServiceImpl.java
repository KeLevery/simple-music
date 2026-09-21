package com.simple.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.constant.MessageConstant;
import com.simple.mapper.ArtistMapper;
import com.simple.model.dto.ArtistDTO;
import com.simple.model.dto.ArtistUpdateDTO;
import com.simple.model.entity.Artist;
import com.simple.model.vo.ArtistVO;
import com.simple.result.PageResult;
import com.simple.result.Result;
import com.simple.service.IArtistService;
import com.simple.service.MinioService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArtistServiceImpl implements IArtistService {

    @Autowired
    ArtistMapper artistMapper;

    @Autowired
    MinioService minioService;

    /**
     * 获取所有歌手数量
     *
     * @param gender 性别
     * @param area   地区
     * @return 歌手数量
     */
    @Override
    public Result<Long> getAllArtistsCount(Integer gender, String area) {
        QueryWrapper<Artist> queryWrapper = new QueryWrapper<>();
        if (gender != null) {
            queryWrapper.eq("gender", gender);
        }
        if (area != null) {
            queryWrapper.eq("area", area);
        }

        return Result.success(artistMapper.selectCount(queryWrapper));
    }


    /**
     * 获取歌手列表
     *
     * @param artistDTO
     * @return
     */
    @Override
    public Result<PageResult<Artist>> getAllArtistsAndDetail(ArtistDTO artistDTO) {
        //分页查询
        Page<Artist> page = new Page<>(artistDTO.getPageNum(), artistDTO.getPageSize());
        //设置查询条件
        QueryWrapper<Artist> queryWrapper = new QueryWrapper<>();
        if (artistDTO.getArtistName() != null) {
            queryWrapper.like(artistDTO.getArtistName() != null, "name", artistDTO.getArtistName());
        }
        if (artistDTO.getGender() != null) {
            queryWrapper.eq(artistDTO.getGender() != null, "gender", artistDTO.getGender());
        }
        if (artistDTO.getArea() != null) {
            queryWrapper.eq(artistDTO.getArea() != null, "area", artistDTO.getArea());
        }

        // 倒序排序
        queryWrapper.orderByDesc("id");

        Page<Artist> artistPage = artistMapper.selectPage(page, queryWrapper);
        if (artistPage.getRecords().size() == 0) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }

        return Result.success(new PageResult<>(artistPage.getTotal(), artistPage.getRecords()));
    }

    /**
     * 添加歌手
     *
     * @param artistDTO
     * @return
     */
    @Override
    public Result addArtist(ArtistDTO artistDTO) {
        QueryWrapper<Artist> artistQueryWrapper = new QueryWrapper<>();
        if (artistDTO.getArtistName() != null) {
            artistQueryWrapper.eq("name", artistDTO.getArtistName());
        }
        if (artistMapper.selectCount(artistQueryWrapper) > 0) {
            return Result.error(MessageConstant.ARTIST + MessageConstant.ALREADY_EXISTS);
        }

        Artist artist = new Artist();
        BeanUtils.copyProperties(artistDTO, artist);
        artistMapper.insert(artist);

        return Result.success(MessageConstant.ARTIST + MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    /**
     * 修改歌手信息
     *
     * @param artistUpdateDTO 歌手更新数据传输对象，包含以下字段：
     *                        - artistId: 歌手 ID（必填，用于标识要更新的歌手）
     *                        - artistName: 歌手名（可选）
     *                        - gender: 性别，0-男歌手，1-女歌手，2-组合/乐队（可选）
     *                        - birth: 出生日期，格式 yyyy-MM-dd（可选）
     *                        - area: 国籍/地区（可选）
     *                        - introduction: 歌手简介（可选）
     * @return Result 操作结果：
     *         - 成功：返回包含成功消息的 Result 对象
     *         - 失败：返回包含错误消息的 Result 对象，包括歌手不存在、歌手名重复、更新失败等情况
     */
    @Override
    public Result updateArtist(ArtistUpdateDTO artistUpdateDTO) {
        Long artistId = artistUpdateDTO.getArtistId();
        QueryWrapper<Artist> artistQueryWrapper = new QueryWrapper<>();
        if (artistUpdateDTO.getArtistName() != null) {
            artistQueryWrapper.eq("name", artistUpdateDTO.getArtistName());
        }

        // 检查歌手名是否已被其他歌手使用
        Artist artistByArtistName = artistMapper.selectOne(new QueryWrapper<Artist>().eq("name", artistUpdateDTO.getArtistName()));
        if (artistByArtistName != null && !artistByArtistName.getArtistId().equals(artistId)) {
            return Result.error(MessageConstant.ARTIST + MessageConstant.ALREADY_EXISTS);
        }
        
        // 复制属性并执行更新操作
        Artist artist = new Artist();
        BeanUtils.copyProperties(artistUpdateDTO, artist);
        if (artistMapper.updateById(artist) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.ARTIST + MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 修改歌手头像
     *
     * @param id
     * @param avatar
     * @return
     */
    @Override
    public Result updateArtistAvatar(Long id, String avatar) {
        Artist artist = artistMapper.selectById(id);
        String avatarUrl = artist.getAvatar();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            minioService.deleteFile(avatarUrl);
        }

        artist.setAvatar(avatar);
        if (artistMapper.updateById(artist) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.ARTIST + MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 删除歌手
     *
     * @param id
     * @return
     */
    @Override
    public Result deleteArtist(Long id) {
        // 1.查询歌手信息，获取歌手头像
        Artist artist = artistMapper.selectById(id);
        if (artist == null) {
            return Result.error(MessageConstant.ARTIST + MessageConstant.NOT_FOUND);
        }
        String avatarUrl = artist.getAvatar();
        // 2. 先删除 MinIO 里的头像文件
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            minioService.deleteFile(avatarUrl);
        }
        // 3. 删除歌手
        if (artistMapper.deleteById(id) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 批量删除歌手
     *
     * @param artistIds
     * @return
     */
    @Override
    public Result deleteArtists(List<Long> artistIds) {
        // 1.查询歌手信息，获取歌手头像
        List<Artist> artists = artistMapper.selectByIds(artistIds);
        List<String> avatarUrlList = artists.stream()
                .map(Artist::getAvatar)
                .filter(avatarUrl -> avatarUrl != null && !avatarUrl.isEmpty())
                .toList();

        // 2. 先删除 MinIO 里的头像文件
        for (String avatarUrl : avatarUrlList) {
            minioService.deleteFile(avatarUrl);
        }

        if (artistMapper.deleteBatchIds(artistIds) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 获取所有歌手列表
     *
     * @param artistDTO
     * @return
     */
    @Override
    public Result<PageResult<ArtistVO>> getAllArtists(ArtistDTO artistDTO) {
        // 分页查询
        Page<Artist> page = new Page<>(artistDTO.getPageNum(), artistDTO.getPageSize());
        QueryWrapper<Artist> queryWrapper = new QueryWrapper<>();
        // 根据 artistDTO 的条件构建查询条件
        if (artistDTO.getArtistName() != null) {
            queryWrapper.like("name", artistDTO.getArtistName());
        }
        if (artistDTO.getGender() != null) {
            queryWrapper.eq("gender", artistDTO.getGender());
        }
        if (artistDTO.getArea() != null) {
            queryWrapper.like("area", artistDTO.getArea());
        }

        IPage<Artist> artistPage = artistMapper.selectPage(page, queryWrapper);
        if (artistPage.getRecords().size() == 0) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }

        // 转换成 ArtistVO
        List<ArtistVO> artistVOList = artistPage.getRecords().stream()
                .map(artist -> {
                    ArtistVO artistVO = new ArtistVO();
                    BeanUtils.copyProperties(artist, artistVO);
                    return artistVO;
                }).toList();

        return Result.success(new PageResult<>(artistPage.getTotal(), artistVOList));
    }
}
