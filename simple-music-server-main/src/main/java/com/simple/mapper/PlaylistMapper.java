package com.simple.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.model.entity.Playlist;
import com.simple.model.vo.PlaylistVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlaylistMapper extends BaseMapper<Playlist> {
    // 随机推荐歌单
    @Select("""
            SELECT 
                p.id AS playlistId, 
                p.title AS title, 
                p.cover_url AS coverUrl
            FROM tb_playlist p
            ORDER BY RAND() 
            LIMIT #{limit}
            """)
    List<PlaylistVO> getRandomPlaylists(int i);

    List<String> getFavoritePlaylistStyles(List<Long> favoritePlaylistIds);


    // 根据风格推荐歌单（排除已收藏歌单）
    List<PlaylistVO> getRecommendedPlaylistsByStyles(List<Long> sortedStyleIds, List<Long> favoritePlaylistIds, int limit);


}
