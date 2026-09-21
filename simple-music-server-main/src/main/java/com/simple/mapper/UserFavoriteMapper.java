package com.simple.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.model.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {

    // 查询用户收藏的所有歌单ID
    @Select("SELECT playlist_id FROM tb_user_favorite WHERE user_id = #{userId} AND type = 1")
    List<Long> getFavoritePlaylistIdsByUserId(@Param("userId") Long userId);

    List<Long> getFavoriteIdsByStyle(List<String> favoriteStyles);
}
