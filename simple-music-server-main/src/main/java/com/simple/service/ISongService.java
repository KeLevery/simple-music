package com.simple.service;

import com.simple.model.dto.SongAddDTO;
import com.simple.model.dto.SongAndArtistDTO;
import com.simple.model.dto.SongDTO;
import com.simple.model.dto.SongUpdateDTO;
import com.simple.model.vo.ArtistNameVO;
import com.simple.model.vo.SongAdminVO;
import com.simple.model.vo.SongDetailVO;
import com.simple.model.vo.SongVO;
import com.simple.result.PageResult;
import com.simple.result.Result;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface ISongService{
    Result<Long> getAllSongsCount(String style);

    Result<List<ArtistNameVO>> getAllArtistNames();

    Result<PageResult<SongAdminVO>> getAllSongsByArtist(SongAndArtistDTO songAndArtistDTO);

    Result addSong(SongAddDTO songAddDTO);

    Result updateSong(SongUpdateDTO songUpdateDTO);

    Result updateSongAudio(Long id,String audioUrl);

    Result updateSongCover(Long songId, String coverUrl);

    Result deleteSong(Long id);

    Result deleteSongs(List<Long> songIds);

    Result<PageResult<SongVO>> getAllSongs(SongDTO songDTO, HttpServletRequest request);

    Result<List<SongVO>> getRecommendedSongs(HttpServletRequest request);

    Result<SongDetailVO> getSongDetail(Long songId, HttpServletRequest request);
}
