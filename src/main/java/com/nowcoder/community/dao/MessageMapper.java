package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Woes
 * @version 1.0
 */
@Mapper
public interface MessageMapper {

    //查询当前用户的私信列表，针对每个会话返回最新的一条私信
    List<Message> selectConversations(int userId, int offset, int limit);

    //查询当前用户的私信数量
    int selectConversationCount(int userId);

    //查询某个会话包含的私信（列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    //某个会话包含的私信数量
    int selectLetterCount(String conversationId);

    //未读私信的数量
    int selectLetterUnreadCount(int userId, String conversationId);

    // 新增消息
    int insertMessage(Message message);

    //修改私信的状态status
    int updateStatus(List<Integer> ids, int status);
}
