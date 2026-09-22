package com.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simple.model.dto.ArtistDTO;
import com.simple.model.dto.PlaylistDTO;
import com.simple.model.dto.SongDTO;
import com.simple.model.dto.UserLoginDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SimpleMusicServerMainApplication.class)
@AutoConfigureMockMvc
public class ApiControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("测试轮播图列表接口 /banner/getBannerList")
    void testGetBannerList() throws Exception {
        mockMvc.perform(get("/banner/getBannerList"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("测试推荐歌单接口 /playlist/getRecommendedPlaylists")
    void testGetRecommendedPlaylists() throws Exception {
        mockMvc.perform(get("/playlist/getRecommendedPlaylists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("测试歌单广场分页查询 /playlist/getAllPlaylists")
    void testGetAllPlaylists() throws Exception {
        PlaylistDTO dto = new PlaylistDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);

        mockMvc.perform(post("/playlist/getAllPlaylists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("测试推荐歌曲接口 /song/getRecommendedSongs")
    void testGetRecommendedSongs() throws Exception {
        mockMvc.perform(get("/song/getRecommendedSongs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("测试全部歌曲分页查询 /song/getAllSongs")
    void testGetAllSongs() throws Exception {
        SongDTO dto = new SongDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);

        mockMvc.perform(post("/song/getAllSongs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("测试歌手列表分页查询 /artist/getAllArtists")
    void testGetAllArtists() throws Exception {
        ArtistDTO dto = new ArtistDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);

        mockMvc.perform(post("/artist/getAllArtists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("测试用户登录接口 /user/login")
    void testUserLogin() throws Exception {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setEmail("user651@example.com");
        dto.setPassword("123456abc");

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @DisplayName("测试未登录访问受保护接口应拦截为401 /favorite/getFavoriteSongs")
    void testUnauthorizedFavorite() throws Exception {
        mockMvc.perform(post("/favorite/getFavoriteSongs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pageNum\":1,\"pageSize\":10}"))
                .andExpect(status().isUnauthorized());
    }
}
