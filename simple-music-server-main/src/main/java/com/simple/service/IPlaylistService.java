package com.simple.service;

import com.simple.model.dto.PlaylistAddDTO;
import com.simple.model.dto.PlaylistDTO;
import com.simple.model.dto.PlaylistUpdateDTO;
import com.simple.model.entity.Playlist;
import com.simple.model.vo.PlaylistVO;
import com.simple.result.PageResult;
import com.simple.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IPlaylistService {
    Result<Long> getAllPlaylistsCount(String style);

    Result<PageResult<Playlist>> getAllPlaylists(PlaylistDTO playlistDTO);

    Result addPlaylist(PlaylistAddDTO playlistAddDTO);

    Result updatePlaylist(PlaylistUpdateDTO playlistUpdateDTO);

    Result updatePlaylistCover(Long id, String coverUrl);

    Result deletePlaylist(Long id);

    Result deletePlaylists(List<Long> ids);

    Result<List<PlaylistVO>> getRecommendedPlaylists(HttpServletRequest request);
}
