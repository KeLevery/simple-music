package com.simple.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.constant.JwtClaimsConstant;
import com.simple.mapper.ArtistMapper;
import com.simple.mapper.PlaylistMapper;
import com.simple.mapper.SongMapper;
import com.simple.mapper.UserFavoriteMapper;
import com.simple.model.entity.Artist;
import com.simple.model.entity.Playlist;
import com.simple.model.entity.Song;
import com.simple.model.entity.UserFavorite;
import com.simple.model.vo.SongVO;
import com.simple.result.PageResult;
import com.simple.result.Result;
import com.simple.util.ThreadLocalUtil;
import com.simple.util.TypeConversionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private PlaylistMapper playlistMapper;

    @Autowired
    private ArtistMapper artistMapper;

    private Long getCurrentUserId() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims != null && claims.get(JwtClaimsConstant.USER_ID) != null) {
            return TypeConversionUtil.toLong(claims.get(JwtClaimsConstant.USER_ID));
        }
        return null;
    }

    /**
     * 获取用户收藏的歌单列表
     */
    @PostMapping("/getFavoritePlaylists")
    public Result<PageResult<Playlist>> getFavoritePlaylists(@RequestBody(required = false) Map<String, Object> params, HttpServletRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.success(new PageResult<>(0L, Collections.emptyList()));
        }

        int pageNum = 1;
        int pageSize = 20;
        if (params != null) {
            if (params.get("pageNum") != null) pageNum = Integer.parseInt(params.get("pageNum").toString());
            if (params.get("pageSize") != null) pageSize = Integer.parseInt(params.get("pageSize").toString());
        }

        // 查询用户收藏的歌单 ID
        QueryWrapper<UserFavorite> favQw = new QueryWrapper<>();
        favQw.eq("user_id", userId).eq("type", 1).orderByDesc("create_time");
        List<UserFavorite> favList = userFavoriteMapper.selectList(favQw);

        if (favList == null || favList.isEmpty()) {
            return Result.success(new PageResult<>(0L, Collections.emptyList()));
        }

        List<Long> playlistIds = favList.stream()
                .map(UserFavorite::getPlaylistId)
                .filter(Objects::nonNull)
                .toList();

        if (playlistIds.isEmpty()) {
            return Result.success(new PageResult<>(0L, Collections.emptyList()));
        }

        Page<Playlist> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Playlist> qw = new QueryWrapper<>();
        qw.in("id", playlistIds);
        Page<Playlist> playlistPage = playlistMapper.selectPage(page, qw);

        return Result.success(new PageResult<>(playlistPage.getTotal(), playlistPage.getRecords()));
    }

    /**
     * 获取用户收藏的歌曲列表
     */
    @PostMapping("/getFavoriteSongs")
    public Result<PageResult<SongVO>> getFavoriteSongs(@RequestBody(required = false) Map<String, Object> params, HttpServletRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.success(new PageResult<>(0L, Collections.emptyList()));
        }

        int pageNum = 1;
        int pageSize = 20;
        String songName = null;
        if (params != null) {
            if (params.get("pageNum") != null) pageNum = Integer.parseInt(params.get("pageNum").toString());
            if (params.get("pageSize") != null) pageSize = Integer.parseInt(params.get("pageSize").toString());
            if (params.get("songName") != null) songName = params.get("songName").toString().trim();
        }

        // 查询用户收藏的歌曲 ID
        QueryWrapper<UserFavorite> favQw = new QueryWrapper<>();
        favQw.eq("user_id", userId).eq("type", 0).orderByDesc("create_time");
        List<UserFavorite> favList = userFavoriteMapper.selectList(favQw);

        if (favList == null || favList.isEmpty()) {
            return Result.success(new PageResult<>(0L, Collections.emptyList()));
        }

        List<Long> songIds = favList.stream()
                .map(UserFavorite::getSongId)
                .filter(Objects::nonNull)
                .toList();

        if (songIds.isEmpty()) {
            return Result.success(new PageResult<>(0L, Collections.emptyList()));
        }

        Page<Song> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Song> qw = new QueryWrapper<>();
        qw.in("id", songIds);
        if (songName != null && !songName.isEmpty()) {
            qw.like("name", songName);
        }
        Page<Song> songPage = songMapper.selectPage(page, qw);

        // 组装歌手名称并转换为 SongVO
        List<Artist> allArtists = artistMapper.selectList(null);
        Map<Long, String> artistMap = allArtists != null ? allArtists.stream()
                .collect(Collectors.toMap(Artist::getArtistId, Artist::getArtistName, (k1, k2) -> k1))
                : Collections.emptyMap();

        List<SongVO> voList = songPage.getRecords().stream().map(song -> {
            SongVO vo = new SongVO();
            vo.setSongId(song.getSongId());
            vo.setSongName(song.getSongName());
            vo.setAlbum(song.getAlbum());
            vo.setDuration(song.getDuration());
            vo.setCoverUrl(song.getCoverUrl());
            vo.setAudioUrl(song.getAudioUrl());
            vo.setReleaseTime(song.getReleaseTime());
            vo.setLikeStatus(1); // 收藏列表中所有歌曲的喜欢状态均为 1
            vo.setArtistName(artistMap.getOrDefault(song.getArtistId(), "群星"));
            return vo;
        }).toList();

        return Result.success(new PageResult<>(songPage.getTotal(), voList));
    }

    /**
     * 收藏歌曲
     */
    @PostMapping("/collectSong")
    public Result<String> collectSong(@RequestParam("songId") Long songId, HttpServletRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }

        QueryWrapper<UserFavorite> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("type", 0).eq("song_id", songId);
        if (userFavoriteMapper.selectCount(qw) == 0) {
            UserFavorite fav = new UserFavorite();
            fav.setUserId(userId);
            fav.setType(0);
            fav.setSongId(songId);
            fav.setCreateTime(LocalDateTime.now());
            userFavoriteMapper.insert(fav);
        }
        return Result.success("收藏成功", null);
    }

    /**
     * 取消收藏歌曲
     */
    @DeleteMapping("/cancelCollectSong")
    public Result<String> cancelCollectSong(@RequestParam("songId") Long songId, HttpServletRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }

        QueryWrapper<UserFavorite> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("type", 0).eq("song_id", songId);
        userFavoriteMapper.delete(qw);
        return Result.success("已取消收藏", null);
    }

    /**
     * 收藏歌单
     */
    @PostMapping("/collectPlaylist")
    public Result<String> collectPlaylist(@RequestParam("playlistId") Long playlistId, HttpServletRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }

        QueryWrapper<UserFavorite> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("type", 1).eq("playlist_id", playlistId);
        if (userFavoriteMapper.selectCount(qw) == 0) {
            UserFavorite fav = new UserFavorite();
            fav.setUserId(userId);
            fav.setType(1);
            fav.setPlaylistId(playlistId);
            fav.setCreateTime(LocalDateTime.now());
            userFavoriteMapper.insert(fav);
        }
        return Result.success("收藏成功", null);
    }

    /**
     * 取消收藏歌单
     */
    @DeleteMapping("/cancelCollectPlaylist")
    public Result<String> cancelCollectPlaylist(@RequestParam("playlistId") Long playlistId, HttpServletRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }

        QueryWrapper<UserFavorite> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("type", 1).eq("playlist_id", playlistId);
        userFavoriteMapper.delete(qw);
        return Result.success("已取消收藏", null);
    }
}
