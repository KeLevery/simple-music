package com.simple.service.impl;

import com.baomidou.mybatisplus.core.conditions.interfaces.Func;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.constant.JwtClaimsConstant;
import com.simple.constant.MessageConstant;
import com.simple.enumeration.RoleEnum;
import com.simple.mapper.PlaylistMapper;
import com.simple.mapper.UserFavoriteMapper;
import com.simple.model.dto.PlaylistAddDTO;
import com.simple.model.dto.PlaylistDTO;
import com.simple.model.dto.PlaylistUpdateDTO;
import com.simple.model.entity.Playlist;
import com.simple.model.vo.PlaylistVO;
import com.simple.result.PageResult;
import com.simple.result.Result;
import com.simple.service.IPlaylistService;
import com.simple.service.MinioService;
import com.simple.util.JwtUtil;
import com.simple.util.TypeConversionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PlaylistServiceImpl implements IPlaylistService {

    @Autowired
    PlaylistMapper playlistMapper;

    @Autowired
    MinioService minioService;

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Override
    public Result<Long> getAllPlaylistsCount(String style) {
        QueryWrapper<Playlist> queryWrapper = new QueryWrapper<>();
        if (style != null) {
            queryWrapper.like("style", style);
        }
        return Result.success(playlistMapper.selectCount(queryWrapper));
    }

    /**
     * 获取所有歌单
     *
     * @param playlistDTO
     * @return
     */
    @Override
    public Result<PageResult<Playlist>> getAllPlaylists(PlaylistDTO playlistDTO) {
        Page<Playlist> page = new Page<>(playlistDTO.getPageNum(), playlistDTO.getPageSize());
        QueryWrapper<Playlist> queryWrapper = new QueryWrapper<>();
        if (playlistDTO.getStyle() != null) {
            queryWrapper.like("style", playlistDTO.getStyle());
        }
        if (playlistDTO.getTitle() != null) {
            queryWrapper.like("title", playlistDTO.getTitle());
        }
        //倒叙排列
        queryWrapper.orderByDesc("id");

        IPage<Playlist> playlistPage = playlistMapper.selectPage(page, queryWrapper);
        if (playlistPage.getRecords().size() == 0) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }
        return Result.success(new PageResult<>(playlistPage.getTotal(), playlistPage.getRecords()));
    }

    /**
     * 添加歌单
     *
     * @param playlistAddDTO
     * @return
     */
    @Override
    @CacheEvict(cacheNames = "playlistCache", allEntries = true)
    public Result addPlaylist(PlaylistAddDTO playlistAddDTO) {
        QueryWrapper<Playlist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("title", playlistAddDTO.getTitle());
        if (playlistMapper.selectCount(queryWrapper) > 0) {
            return Result.error(MessageConstant.PLAYLIST + MessageConstant.ALREADY_EXISTS);
        }

        Playlist playlist = new Playlist();
        BeanUtils.copyProperties(playlistAddDTO, playlist);
        playlistMapper.insert(playlist);

        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    /**
     * 修改歌单
     *
     * @param playlistUpdateDTO
     * @return
     */
    @Override
    public Result updatePlaylist(PlaylistUpdateDTO playlistUpdateDTO) {
        Long playlistId = playlistUpdateDTO.getPlaylistId();

        Playlist playlistByTitle = playlistMapper.selectOne(new QueryWrapper<Playlist>().eq("title", playlistUpdateDTO.getTitle()));
        if (playlistByTitle != null && !playlistByTitle.getPlaylistId().equals(playlistId)) {
            return Result.error(MessageConstant.PLAYLIST + MessageConstant.ALREADY_EXISTS);
        }

        Playlist playlist = new Playlist();
        BeanUtils.copyProperties(playlistUpdateDTO, playlist);
        if (playlistMapper.updateById(playlist) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 修改歌单封面
     *
     * @param id
     * @param coverUrl
     * @return
     */
    @Override
    public Result updatePlaylistCover(Long id, String coverUrl) {
        Playlist playlist = playlistMapper.selectById(id);
        String cover = playlist.getCoverUrl();
        // 删除旧封面
        if (cover != null && !cover.isEmpty()) {
            minioService.deleteFile(cover);
        }

        playlist.setCoverUrl(coverUrl);
        if (playlistMapper.updateById(playlist) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 删除歌单
     *
     * @param id
     * @return
     */
    @Override
    public Result deletePlaylist(Long id) {
        Playlist playlist = playlistMapper.selectById(id);
        if (playlist == null) {
            return Result.error(MessageConstant.PLAYLIST + MessageConstant.NOT_FOUND);
        }
        // 删除minio里的数据
        if (playlist.getCoverUrl() != null) {
            minioService.deleteFile(playlist.getCoverUrl());
        }

        if (playlistMapper.deleteById(id) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 批量删除歌单
     *
     * @param ids
     * @return
     */
    @Override
    public Result deletePlaylists(List<Long> ids) {
        List<Playlist> playlists = playlistMapper.selectBatchIds(ids);
        List<String> coverUrls = playlists.stream()
                .map(Playlist::getCoverUrl)
                .filter(coverUrl -> coverUrl != null && !coverUrl.isEmpty())
                .toList();

        for (String coverUrl : coverUrls) {
            minioService.deleteFile(coverUrl);
        }

        // 批量删除
        if (playlistMapper.deleteBatchIds(ids) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 获取推荐歌单
     *
     * @return
     */
    @Override
    public Result<List<PlaylistVO>> getRecommendedPlaylists(HttpServletRequest request) {
        // 1.先判断有没有带token
        //2.如果有就解析token
        //3.从token里面解析角色权限
        //4.用户为登录没用token，返回随机歌单
        //5.用户登录了，获取用户收藏的歌单
        //6.查询用户收藏的歌单风格并统计频率
        //7.按风格出现次数降序排序
        //8.根据排序后的风格推荐歌单
        //9.如果推荐不足10个，则用随机歌单填充
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 移除 Bearer 前缀
        }

        Map<String, Object> map = null;
        if (token != null && !token.isEmpty()) {
            map = JwtUtil.parseToken(token);
        }

        Long userId = null;                    // 先默认设为 null
        if (map != null) {                     // 防止 map 为空（map 通常是 JWT 解析后的 Claims Map）
            String role = (String) map.get(JwtClaimsConstant.ROLE);   // 取出角色字段
            // 判断当前用户是否是【普通用户】
            if (role.equals(RoleEnum.USER.getRole())) {
                Object userIdObj = map.get(JwtClaimsConstant.USER_ID);   // 取出用户ID
                userId = TypeConversionUtil.toLong(userIdObj);   // 转成 Long 类型
            }
            // 如果是管理员（ADMIN）或其他角色，这里不会进入，所以 userId 依然是 null
        }

        // 用户未登录返回随机歌单
        if (userId == null) {
            return Result.success(playlistMapper.getRandomPlaylists(10));
        }

        // 获取用户收藏的歌单ID
        List<Long> favoritePlaylistIds = userFavoriteMapper.getFavoritePlaylistIdsByUserId(userId);
        if(favoritePlaylistIds.isEmpty()){
            return Result.success(playlistMapper.getRandomPlaylists(10));
        }

        // 查询用户收藏的歌单风格并统计频率
        //据用户收藏的歌单ID列表，查询这些歌单关联的所有风格名称
        List<String> favoriteStyles = playlistMapper.getFavoritePlaylistStyles(favoritePlaylistIds);
        //根据这些风格名称，查询具有相同风格的其他歌单ID
        List<Long> favoriteStyleIds = userFavoriteMapper.getFavoriteIdsByStyle(favoriteStyles);
        //对查询到的歌单ID按风格进行分组计数
        Map<Long, Long> styleFrequency = favoriteStyleIds.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // 按风格出现次数降序排序
        List<Long> sortedStyleIds = styleFrequency.entrySet().stream()
                // (a, b) 是 Lambda 表达式的参数,代表要比较的两个 Map.Entry 对象。
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                //提取 key
                .map(Map.Entry::getKey)
                //collect() → 收集为 List
                .collect(Collectors.toList());

        // 根据排序后的风格推荐歌单（排除已收藏歌单）
        List<PlaylistVO> recommendedPlaylists = playlistMapper.getRecommendedPlaylistsByStyles(sortedStyleIds, favoritePlaylistIds, 10);

        // 如果推荐的歌单不足 10 个，则用随机歌单填充
        if (recommendedPlaylists.size() < 10) {
            List<PlaylistVO> randomPlaylists = playlistMapper.getRandomPlaylists(10);
            Set<Long> addedPlaylistIds = recommendedPlaylists.stream().map(PlaylistVO::getPlaylistId).collect(Collectors.toSet());

            for (PlaylistVO playlist : randomPlaylists) {
                if (recommendedPlaylists.size() >= 10) break;
                if (!addedPlaylistIds.contains(playlist.getPlaylistId())) {
                    recommendedPlaylists.add(playlist);
                }
            }
        }

        return Result.success(recommendedPlaylists);
    }
}
