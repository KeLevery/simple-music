package com.simple.controller;
import com.simple.model.dto.ArtistDTO;
import com.simple.model.vo.ArtistVO;
import com.simple.result.PageResult;
import com.simple.result.Result;
import com.simple.service.IArtistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/artist")
public class ArtistController {

    @Autowired
    private IArtistService artistService;

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

}
