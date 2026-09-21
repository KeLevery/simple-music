package com.simple.controller;

import com.simple.model.vo.PlaylistVO;
import com.simple.result.Result;
import com.simple.service.IPlaylistService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/playlist")
public class PlaylistController {

    @Autowired
    private IPlaylistService playlistService;

    @GetMapping("/getRecommendedPlaylists")
    public Result<List<PlaylistVO>> getRecommendedPlaylists(HttpServletRequest request){
        return playlistService.getRecommendedPlaylists(request);
    }
}
