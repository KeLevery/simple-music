package com.simple.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simple.mapper.ArtistMapper;
import com.simple.mapper.SongMapper;
import com.simple.model.dto.ArtistDTO;
import com.simple.model.entity.Artist;
import com.simple.model.entity.Song;
import com.simple.model.vo.ArtistVO;
import com.simple.result.PageResult;
import com.simple.result.Result;
import com.simple.service.IArtistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/artist")
public class ArtistController {

    @Autowired
    private IArtistService artistService;

    @Autowired
    private ArtistMapper artistMapper;

    @Autowired
    private SongMapper songMapper;

    /**
     * 获取所有歌手列表
     *
     * @param artistDTO artistDTO
     * @return 歌手列表
     */
    @PostMapping("/getAllArtists")
    public Result<PageResult<ArtistVO>> getAllArtists(@RequestBody @Valid ArtistDTO artistDTO) {
        return artistService.getAllArtists(artistDTO);
    }

    /**
     * 获取歌手详情
     */
    @GetMapping("/getArtistDetail/{id}")
    public Result<Map<String, Object>> getArtistDetail(@PathVariable("id") Long id) {
        Artist artist = artistMapper.selectById(id);
        if (artist == null) {
            return Result.error("歌手不存在");
        }

        // 查询该歌手的名下歌曲
        List<Song> songs = songMapper.selectList(new QueryWrapper<Song>().eq("artist_id", id));
        Map<String, Object> map = new HashMap<>();
        map.put("artistId", artist.getArtistId());
        map.put("artistName", artist.getArtistName());
        map.put("avatar", artist.getAvatar());
        map.put("birth", artist.getBirth());
        map.put("area", artist.getArea());
        map.put("introduction", artist.getIntroduction() != null ? artist.getIntroduction() : "暂无简介");
        map.put("songs", songs);

        return Result.success(map);
    }
}
