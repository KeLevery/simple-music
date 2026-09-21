package com.simple.service;

import com.simple.model.dto.ArtistDTO;
import com.simple.model.dto.ArtistUpdateDTO;
import com.simple.model.entity.Artist;
import com.simple.model.vo.ArtistVO;
import com.simple.result.PageResult;
import com.simple.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IArtistService {
    Result<Long> getAllArtistsCount(Integer gender, String area);

    Result<PageResult<Artist>> getAllArtistsAndDetail(ArtistDTO artistDTO);

    Result addArtist(ArtistDTO artistDTO);

    Result updateArtist(ArtistUpdateDTO artistUpdateDTO);

    Result updateArtistAvatar(Long id, String avatar);

    Result deleteArtist(Long id);

    Result deleteArtists(List<Long> artistIds);

    Result<PageResult<ArtistVO>> getAllArtists(ArtistDTO artistDTO);
}
