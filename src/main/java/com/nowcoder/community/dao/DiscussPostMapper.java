package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);
//    后两个参数，初始行号，每页最多数据行数

    int selectDiscussPostRows(@Param("userId") int userId);
//    该参数注解，用于给参数取别名
//    若只有一个参数，且在<if> 中使用，则必须加别名，否则会报错

    int insertDiscussPost(DiscussPost discussPost);

}
