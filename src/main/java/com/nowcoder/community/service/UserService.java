package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    //注入，包括bean的注入，和值的注入
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Value("${community.path.domain}")
    private String domin;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private LoginTicketMapper loginTicketMapper;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    // 注册业务
    public Map<String, Object> register(User user){
        Map<String,Object> map = new HashMap<>();

        //空值处理
        if(user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        //验证账号，重复
        User u = userMapper.selectByName(user.getUsername());
        if(u != null){
            map.put("usernameMsg","该账号存在");
        }

        //验证邮箱，重复
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null){
            map.put("emailMsg","该邮箱已被注册");
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5)); // 得到随机字符串， 5位
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt())); // 密码加密并覆盖
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/userid/code
        //激活路径如上
        String url = domin + contextPath +"/activation/" + user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活邮件", content);

        return map;
    }

    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String,Object> login(String username,String password, int expiredSecond){
        Map<String,Object> map = new HashMap<>();

        //空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能够为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能够为空");
            return map;
        }

        //验证账号
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg","账号不存在");
            return map;
        }

        //验证账户状态
        if(user.getStatus()==0){
            map.put("usernameMsg","账号未激活");
            return map;
        }

        //验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg","密码不正确");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSecond * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    //退出登录
    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket,1);//1 表示无效
    }

    //忘记密码之后给邮箱发送验证码
    public Map<String, Object> getCode(String email) {
        Map<String,Object> map = new HashMap<>();
        //空值判断
        if (StringUtils.isBlank(email)){
            map.put("emailMsg","请输入邮箱！");
            return map;
        }
        //邮箱是否正确
        User user = userMapper.selectByEmail(email);
        if (user == null){
            map.put("emailMsg","该邮箱还未注册过，请注册后再使用！");
            return map;
        }
        //该用户还未激活
        if (user.getStatus() == 0){
            map.put("emailMsg","该邮箱还未激活，请到邮箱中激活后再使用！");
            return map;
        }
        //邮箱正确的情况下，发送验证码到邮箱
        Context context = new Context();
        context.setVariable("email",email);
        String code = CommunityUtil.generateUUID().substring(0,5);
        context.setVariable("code",code);
        String content = templateEngine.process("mail/forget", context);
        mailClient.sendMail(email, "牛客验证码", content);

        map.put("code",code);//map中存放一份，为了之后和用户输入的验证码进行对比
        map.put("expirationTime", LocalDateTime.now().plusMinutes(5L));//过期时间
        return map;
    }


    //忘记密码
    public Map<String, Object> forget(String email, String verifycode, String password, HttpSession session){
        Map<String,Object> map = new HashMap<>();
        //空值处理
        if (StringUtils.isBlank(email)){
            map.put("emailMsg","请输入邮箱！");
            return map;
        }
        if (StringUtils.isBlank(verifycode)){
            map.put("codeMsg","请输入验证码！");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","请输入新密码！");
            return map;
        }
    /*
    //验证验证码是否正确
    if (!verifycode.equals(session.getAttribute("code"))){
        map.put("codeMsg","输入的验证码不正确！");
        return map;
    }
    //验证码是否过期
    if (LocalDateTime.now().isAfter((LocalDateTime)session.getAttribute("expirationTime"))){
        map.put("codeMsg","输入的验证码已过期，请重新获取验证码！");
        return map;
    }
    */
        //邮箱在获取验证码那一步已经验证过了，是有效的邮箱，且验证码也有效
        User user = userMapper.selectByEmail(email);
        password = CommunityUtil.md5(password + user.getSalt());
        userMapper.updatePassword(user.getId(),password);

        return map;
    }

    public LoginTicket findLoginTicket(String ticket){
        return loginTicketMapper.selectByTicket(ticket);
    }

    public int updateHeader(int userId, String headerUrl){
        return userMapper.updateHeader(userId,headerUrl);
    }

    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    //重置密码
    public Map<String,Object> resetPassword(String email, String password){
        Map<String,Object> map = new HashMap<>();

        //空值处理
        if(StringUtils.isBlank(email)){
            map.put("emailMsg","请填写邮箱");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        //验证邮箱
        User user = userMapper.selectByEmail(email);
        if(user == null){
            map.put("emailMsg","该邮箱尚未注册");
            return map;
        }

        //重置密码
        password = CommunityUtil.md5(password+user.getSalt());
        userMapper.updatePassword(user.getId(),password);

        map.put("user",user);
        return map;
    }

    //修改密码
    public Map<String, Object> updatePassword(int userId, String oldPassword, String newPassword, String confirmPassword) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(oldPassword)) {
            map.put("oldPasswordMsg", "原密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "新密码不能为空!");
            return map;
        }
        if (confirmPassword == null) {
            map.put("confirmPasswordMsg", "请确认密码");
            return map;
        }

        // 验证原始密码
        User user = userMapper.selectById(userId);
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)) {
            map.put("oldPasswordMsg", "原密码输入有误!");
            return map;
        }
        if (!confirmPassword.equals(newPassword)) {
            map.put("confirmPasswordMsg", "两次输入的密码不一致");
            return map;
        }

        // 更新密码
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(userId, newPassword);

        return map;
    }
}
