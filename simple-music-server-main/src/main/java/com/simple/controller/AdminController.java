package com.simple.controller;
import com.simple.model.dto.*;
import com.simple.model.entity.Artist;
import com.simple.model.entity.Playlist;
import com.simple.model.vo.ArtistNameVO;
import com.simple.model.vo.SongAdminVO;
import com.simple.model.vo.UserManagementVO;
import com.simple.result.PageResult;
import com.simple.result.Result;
import com.simple.service.*;
import com.simple.service.impl.AdminServiceImpl;
import com.simple.util.BindingResultUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    AdminServiceImpl adminService;

    @Autowired
    IUserService userService;

    @Autowired
    IArtistService artistService;

    @Autowired
    ISongService songService;

    @Autowired
    IPlaylistService playlistService;

    @Autowired
    private MinioService minioService;
    /**
     * 登录管理员
     *
     * @param adminDTO 管理员信息
     *                 //     * @param bindingResult 绑定结果
     * @return 结果
     */
    //@Value 启用数据校验：告诉 Spring 在接收数据前先验证 AdminDTO 对象
    //BindingResult 存储校验结果：如果 @Valid 发现数据不符合规则，错误信息会存入这里
    @PostMapping("/login")
    public Result login(@RequestBody @Valid AdminDTO adminDTO, BindingResult bindingResult) {
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if(errorMessage != null){
            return Result.error(errorMessage);
        }
        return adminService.login(adminDTO);
    }
    /**
     * 登出管理员
     *
     * @param token 认证token
     * @return 登出结果
     */
    @PostMapping("/logout")
        public Result logout(@RequestHeader("Authorization") String token) {
        System.out.println("用户登出了");
        return adminService.logout(token);
    }

    /**********************************************************************************************/
    /**
     * 获取所有用户数量
     * @return
     */
    @GetMapping("/getAllUsersCount")
    public Result<Long> getAllUsersCount(){
        return userService.getAllUsersCount();
    }

    /**
     * 获取所有用户
     * @return
     */
    @PostMapping("/getAllUsers") //定义了一个PageResult用来专门返回分页结果对象
    public Result<PageResult<UserManagementVO>> getAllUsers(@RequestBody UserSearchDTO userSearchDTO){
        return userService.getAllUsers(userSearchDTO);
    }

    /**
     * 新增用户
     * @return
     */
    @PostMapping("/addUser") //定义了一个 PageResult 用来专门返回分页结果对象
    public Result addUser(@RequestBody @Valid UserAddDTO userAddDTO,BindingResult bindingResult){
        /**
         * 处理参数校验失败的情况，提取错误信息并返回
         */
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }
        return userService.addUser(userAddDTO);
    }

    /**
     * 更新用户
     * @return
     */
    @PutMapping("/updateUser") //定义了一个 PageResult 用来专门返回分页结果对象
    public Result updateUser(@RequestBody @Valid UserDTO userDTO, BindingResult bindingResult){
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }
        return userService.updateUser(userDTO);
    }

    /**
     * 更新用户状态
     * @return
     */
    @PatchMapping("/updateUserStatus/{id}/{status}") //定义了一个 PageResult 用来专门返回分页结果对象
    public Result updateUserStatus(@PathVariable("id") Long userId, @PathVariable("status") Integer userStatus){
        return userService.updateUserStatus(userId,userStatus);
    }

    /**
     * 删除用户
     * @return
     */
    @DeleteMapping("/deleteUser/{id}")
    public Result deleteUser(@PathVariable("id") Long userId){
        return userService.deleteUser(userId);
    }

    /**
     * 批量删除用户
     * @return
     */
    @DeleteMapping("/deleteUsers")
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result deleteUsers(@RequestBody List<Long> userIds){
        return userService.deleteUsers(userIds);
    }

    /**********************************************************************************************/
    /**
     * 获取所有歌手数量
     * @return
     */
    @GetMapping("/getAllArtistsCount")
    public Result<Long> getAllArtistsCount(@RequestParam(required = false) Integer gender, @RequestParam(required = false) String area){
        return artistService.getAllArtistsCount(gender,area);
    }

    /**
     * 获取歌手列表
     * @return
     */
    @PostMapping("/getAllArtists")
    public Result<PageResult<Artist>> getAllArtists(@RequestBody ArtistDTO artistDTO){
        return artistService.getAllArtistsAndDetail(artistDTO);
    }

    /**
     * 新增歌手
     * @return
     */
    @PostMapping("/addArtist")
    public Result addArtist(@RequestBody ArtistDTO artistDTO){
        return artistService.addArtist(artistDTO);
    }

    /**
     * 编辑歌手
     * @return
     */
    @PutMapping("/updateArtist")
    public Result updateArtist(@RequestBody ArtistUpdateDTO artistUpdateDTO){
        return artistService.updateArtist(artistUpdateDTO);
    }

    /**
     * 上传歌手头像
     * @return
     */
    @PatchMapping("/updateArtistAvatar/{id}")
    public Result updateArtistAvatar(@PathVariable Long id,@RequestParam("avatar") MultipartFile avatar){
        String avatarUrl = minioService.uploadFile(avatar, "artists");  // 上传到 artists 目录
        return artistService.updateArtistAvatar(id,avatarUrl);
    }

    /**
     * 删除歌手
     * @return
     */
    @DeleteMapping("/deleteArtist/{id}")
    public Result deleteArtist(@PathVariable("id") Long id){
        return artistService.deleteArtist(id);
    }

    /**
     * 批量删除歌手
     * @return
     */
    @DeleteMapping("/deleteArtists")
    public Result deleteArtists(@RequestBody List<Long> artistIds){
        return artistService.deleteArtists(artistIds);
    }


    /**********************************************************************************************/
    /**
     * 获取所有歌曲数量
     * @return
     */
    @GetMapping("/getAllSongsCount")
    public Result<Long> getAllSongsCount(@RequestParam(required = false) String style){
        return songService.getAllSongsCount(style);
    }

    /**
     * 获取所有歌手名称列表
     * @return
     */
    @GetMapping("/getAllArtistNames")
    public Result<List<ArtistNameVO>> getAllArtistNames(){
        return songService.getAllArtistNames();
    }

    /**
     * 获取歌曲列表
     * @return
     */
    @PostMapping("/getAllSongsByArtist")
    public Result<PageResult<SongAdminVO>> getAllSongsByArtist(@RequestBody SongAndArtistDTO songAndArtistDTO){
        return songService.getAllSongsByArtist(songAndArtistDTO);
    }

    /**
     * 新增歌曲
     * @return
     */
    @PostMapping("/addSong")
    public Result addSong(@RequestBody SongAddDTO songAddDTO){
        return songService.addSong(songAddDTO);
    }

    /**
     * 编辑歌曲
     * @return
     */
    @PutMapping("/updateSong")
    public Result updateSong(@RequestBody SongUpdateDTO songUpdateDTO){
        return songService.updateSong(songUpdateDTO);
    }

    /**
     * 更新歌曲封面
     * @return
     */
    @PatchMapping("/updateSongCover/{id}")
    public Result updateSongCover(@PathVariable("id") Long songId,@RequestParam("cover") MultipartFile cover){
        String coverUrl = minioService.uploadFile(cover, "songCovers");  // 上传到 songs 目录
        return songService.updateSongCover(songId,coverUrl);
    }

    /**
     * 更新歌曲音频
     * @return
     */
    @PatchMapping("/updateSongAudio/{id}")
    public Result updateSongAudio(@PathVariable("id") Long id,@RequestParam("audio") MultipartFile audio){
        String audioUrl = minioService.uploadFile(audio, "songs");  // 上传到 songs 目录
        return songService.updateSongAudio(id,audioUrl);
    }

    /**
     * 删除歌曲
     * @return
     */
    @DeleteMapping("/deleteSong/{id}")
    public Result deleteSong(@PathVariable("id") Long id){
        return songService.deleteSong(id);
    }

    /**
     * 批量删除歌曲
     * @return
     */
    @DeleteMapping("/deleteSongs")
    public Result deleteSongs(@RequestBody List<Long> songIds){
        return songService.deleteSongs(songIds);
    }


    /**********************************************************************************************/
    /**
     * 获取所有歌单数量
     *
     * @param style 歌单风格
     * @return 歌单数量
     */
    @GetMapping("/getAllPlaylistsCount")
    public Result<Long> getAllPlaylistsCount(@RequestParam(required = false) String style) {
        return playlistService.getAllPlaylistsCount(style);
    }

    /**
     * 获取歌单列表
     *
     * @param
     * @return 歌单数量
     */
    @PostMapping("/getAllPlaylists")
    public Result<PageResult<Playlist>> getAllPlaylists(@RequestBody PlaylistDTO playlistDTO) {
        return playlistService.getAllPlaylists(playlistDTO);
    }

    /**
     * 新增歌单
     *
     * @param
     * @return 歌单数量
     */
    @PostMapping("/addPlaylist")
    public Result addPlaylist(@RequestBody PlaylistAddDTO playlistAddDTO) {
        return playlistService.addPlaylist(playlistAddDTO);
    }

    /**
     * 编辑歌单
     *
     * @param
     * @return 歌单数量
     */
    @PutMapping("/updatePlaylist")
    public Result updatePlaylist(@RequestBody PlaylistUpdateDTO playlistUpdateDTO) {
        return playlistService.updatePlaylist(playlistUpdateDTO);
    }

    /**
     * 上传歌单封面
     *
     * @param
     * @return 歌单数量
     */
    @PatchMapping("/updatePlaylistCover/{id}")
    public Result updatePlaylistCover(@PathVariable Long id,@RequestParam("cover") MultipartFile cover) {
        String coverUrl = minioService.uploadFile(cover, "playlists");  // 上传到 playlists 目录
        return playlistService.updatePlaylistCover(id,coverUrl);
    }

    /**
     * 删除歌单
     *
     * @param
     * @return 歌单数量
     */
    @DeleteMapping("/deletePlaylist/{id}")
    public Result deletePlaylist(@PathVariable Long id) {
        return playlistService.deletePlaylist(id);
    }

    /**
     * 批量删除歌单
     *
     * @param
     * @return 歌单数量
     */
    @DeleteMapping("/deletePlaylists")
    public Result deletePlaylists(@RequestBody List<Long> ids) {
        return playlistService.deletePlaylists(ids);
    }



    /**********************************************************************************************/

    //测试用
    @GetMapping("/get")
    public Result get() {
        return Result.success();
    }


}
