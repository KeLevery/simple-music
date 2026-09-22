package com.simple.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simple.mapper.PlaylistMapper;
import com.simple.mapper.SongMapper;
import com.simple.model.dto.PlaylistDTO;
import com.simple.model.entity.Playlist;
import com.simple.model.entity.Song;
import com.simple.model.vo.PlaylistVO;
import com.simple.result.PageResult;
import com.simple.result.Result;
import com.simple.service.IPlaylistService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/playlist")
public class PlaylistController {

    @Autowired
    private IPlaylistService playlistService;

    @Autowired
    private PlaylistMapper playlistMapper;

    @Autowired
    private SongMapper songMapper;

    /**
     * 获取推荐歌单
     */
    @GetMapping("/getRecommendedPlaylists")
    public Result<List<PlaylistVO>> getRecommendedPlaylists(HttpServletRequest request) {
        return playlistService.getRecommendedPlaylists(request);
    }

    /**
     * 获取所有歌单列表（歌单广场）
     */
    @PostMapping("/getAllPlaylists")
    public Result<PageResult<Playlist>> getAllPlaylists(@RequestBody(required = false) PlaylistDTO playlistDTO) {
        if (playlistDTO == null) {
            playlistDTO = new PlaylistDTO();
        }
        return playlistService.getAllPlaylists(playlistDTO);
    }

    /**
     * 获取歌单详情
     */
    @GetMapping("/getPlaylistDetail/{id}")
    public Result<Map<String, Object>> getPlaylistDetail(@PathVariable("id") Long id) {
        Playlist playlist = playlistMapper.selectById(id);
        if (playlist == null) {
            return Result.error("歌单不存在");
        }

        // 查询前 50 首歌曲用于歌单填充播放
        List<Song> songs = songMapper.selectList(new QueryWrapper<Song>().last("LIMIT 50"));
        Map<String, Object> detail = new HashMap<>();
        detail.put("playlistId", playlist.getPlaylistId());
        detail.put("title", playlist.getTitle());
        detail.put("coverUrl", playlist.getCoverUrl());
        detail.put("introduction", playlist.getIntroduction() != null ? playlist.getIntroduction() : "暂无歌单简介");
        detail.put("songs", songs);
        detail.put("comments", Collections.emptyList());
        detail.put("likeStatus", 0);
        detail.put("isCollected", false);

        return Result.success(detail);
    }
}
