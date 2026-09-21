package com.simple.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.model.entity.Song;
import com.simple.model.vo.SongAdminVO;
import com.simple.model.vo.SongDetailVO;
import com.simple.model.vo.SongVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SongMapper extends BaseMapper<Song> {
    /**
     * 获取歌曲列表
     *
     * @param page 分页参数
     * @param artistId 歌手ID
     * @param songName 歌曲名称
     * @param album 专辑名称
     * @return 歌曲列表
     */
    @Select("""
                 SELECT 
                            s.id AS songId, 
                            s.name AS songName, 
                            s.artist_id AS artistId, 
                            s.album, 
                            s.lyric, 
                            s.duration, 
                            s.style, 
                            s.cover_url AS coverUrl, 
                            s.audio_url AS audioUrl, 
                            s.release_time AS releaseTime, 
                            a.name AS artistName
                 from tb_song s
                 left join tb_artist a on s.artist_id = a.id
                 where 
                    (#{artistId} IS NULL OR s.artist_id = #{artistId})
                    and (#{songName} IS NULL OR s.name LIKE CONCAT('%', #{songName}, '%'))
                    and (#{album} IS NULL OR s.album LIKE CONCAT('%', #{album}, '%'))
                    ORDER BY s.release_time DESC
                 """
    )
    IPage<SongAdminVO> getSongsWithArtistName(Page<SongAdminVO> page, Long artistId, String songName, String album);

    // 获取歌曲列表
    @Select("""
                SELECT 
                    s.id AS songId, 
                    s.name AS songName, 
                    s.album, 
                    s.duration, 
                    s.cover_url AS coverUrl, 
                    s.audio_url AS audioUrl, 
                    s.release_time AS releaseTime, 
                    a.name AS artistName
                FROM tb_song s
                LEFT JOIN tb_artist a ON s.artist_id = a.id
                WHERE 
                    (#{songName} IS NULL OR s.name LIKE CONCAT('%', #{songName}, '%'))
                    AND (#{artistName} IS NULL OR a.name LIKE CONCAT('%', #{artistName}, '%'))
                    AND (#{album} IS NULL OR s.album LIKE CONCAT('%', #{album}, '%'))
            """)
    IPage<SongVO> getSongsWithArtist(Page<SongVO> page,
                                     @Param("songName") String songName,
                                     @Param("artistName") String artistName,
                                     @Param("album") String album);

    // 获取随机歌曲列表
    @Select("""
                SELECT 
                    s.id AS songId, 
                    s.name AS songName, 
                    s.album, 
                    s.duration, 
                    s.cover_url AS coverUrl, 
                    s.audio_url AS audioUrl, 
                    s.release_time AS releaseTime, 
                    a.name AS artistName
                FROM tb_song s
                LEFT JOIN tb_artist a ON s.artist_id = a.id
                ORDER BY RAND() LIMIT 20
            """)
    List<SongVO> getRandomSongsWithArtist();


    List<Long> getFavoriteSongStyles(List<Long> favoriteSongIds);

    List<SongVO> getRecommendedSongsByStyles(List<Long> sortedStyleIds, List<Long> favoriteSongIds);

    // 根据id获取歌曲详情
    SongDetailVO getSongDetailById(Long songId);
}
