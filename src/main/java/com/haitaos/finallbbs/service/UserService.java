package com.haitaos.finallbbs.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haitaos.finallbbs.dto.PaginationDTO;
import com.haitaos.finallbbs.dto.ResultDTO;
import com.haitaos.finallbbs.dto.UserDTO;
import com.haitaos.finallbbs.dto.UserQueryDTO;
import com.haitaos.finallbbs.exception.CustomizeErrorCode;
import com.haitaos.finallbbs.mapper.UserAccountMapper;
import com.haitaos.finallbbs.mapper.UserExtMapper;
import com.haitaos.finallbbs.mapper.UserInfoMapper;
import com.haitaos.finallbbs.mapper.UserMapper;
import com.haitaos.finallbbs.model.*;

import com.haitaos.finallbbs.utils.TimeUtils;
import com.haitaos.finallbbs.utils.TokenUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private Environment env;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserExtMapper userExtMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private TimeUtils timeUtils;
    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private TokenUtils tokenUtils;
    @Value("${site.password.salt}")
    private String salt;

    public User selectUserByExample(UserExample userExample){
        List<User> users = userMapper.selectByExample(userExample);
        if(users.size()!=0) return users.get(0);
        return null;
    }

    public ResultDTO repass(Long user_id, String nowpass, String pass){
        User user = userMapper.selectByPrimaryKey(user_id);
        if((StringUtils.isBlank(nowpass)&&StringUtils.isBlank(user.getPassword()))||DigestUtils.sha256Hex(nowpass+salt).equals(user.getPassword())){
            user.setPassword(DigestUtils.sha256Hex(pass+salt));
            int i = userMapper.updateByPrimaryKeySelective(user);
            if(i>0) return ResultDTO.okOf("修改成功");
        }

        return ResultDTO.errorOf("当前密码错误");
    }

    public User createOrUpdate(User user) {

        UserExample userExample = new UserExample();
        userExample.createCriteria()
                .andAccountIdEqualTo(user.getAccountId());
        List<User> users = userMapper.selectByExample(userExample);
        //User user2 = new User();
        //User dbUser = userMapper.findByAccountId(user.getAccountId());
        if (users.size() == 0) {
            // 插入
            if(user.getName()==null|| StringUtils.isBlank(user.getName()))
                user.setName(getUserName("Github"));
            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            userMapper.insert(user);
            UserExample userExample2 = new UserExample();
            userExample2.createCriteria()
                    .andAccountIdEqualTo(user.getAccountId());
            List<User> users2 = userMapper.selectByExample(userExample2);
            if(users2.size() != 0){//表示user表已创建
                User dbUser = users2.get(0);
                BeanUtils.copyProperties(dbUser,user);
                initUserTable(dbUser,new UserInfo());
            }
        } else {
            //更新
            User dbUser = users.get(0);
            User updateUser = new User();
            BeanUtils.copyProperties(dbUser,user);
            if(dbUser.getName()==null&&(user.getName()==null|| StringUtils.isBlank(user.getName())))//数据库为空，当前为空
                updateUser.setName(getUserName("Github"));
          /* if(user.getName()!=null)//当前不空
               updateUser.setName(user.getName());*/
            updateUser.setGmtModified(System.currentTimeMillis());
            //updateUser.setAvatarUrl(user.getAvatarUrl());
            // updateUser.setName(user.getName());
            updateUser.setToken(user.getToken());
            UserExample example = new UserExample();
            example.createCriteria()
                    .andIdEqualTo(dbUser.getId());
            userMapper.updateByExampleSelective(updateUser, example);
        }

        return user;
    }

    public int createOrUpdateWeibo(User user, UserDTO loginuser, UserInfo userInfo) {
        UserExample userExample = new UserExample();
        userExample.createCriteria()
                .andWeiboAccountIdEqualTo(user.getWeiboAccountId());
        List<User> users = userMapper.selectByExample(userExample);
        User updateUser = new User();
        if (users.size() == 0) {  // 插入
            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            if(user.getName()==null|| StringUtils.isBlank(user.getName()))
                user.setName(getUserName("微博"));
            if (loginuser == null) {//创建
                userMapper.insert(user);
                UserExample userExample2 = new UserExample();
                userExample2.createCriteria()
                        .andWeiboAccountIdEqualTo(user.getWeiboAccountId());
                List<User> users2 = userMapper.selectByExample(userExample2);
                if(users2.size() != 0){//表示user表已创建
                    User dbUser = users2.get(0);
                    initUserTable(dbUser,userInfo);
                   /* userInfo.setUserId(dbUser.getId());
                    userInfoMapper.insert(userInfo);*/
                }
                return 1;
            }
            if (loginuser != null) {//绑定或者换绑
                updateUser.setWeiboAccountId(user.getWeiboAccountId());
                updateUserInfo(user,updateUser,loginuser,userInfo);
                return 2;
            }



            // user.setName(getUserName("Github"));

        } else {
            //登录更新
            User dbUser = users.get(0);
            if(dbUser.getName()==null&&(user.getName()==null|| StringUtils.isBlank(user.getName())))//数据库为空，当前为空
                updateUser.setName(getUserName("微博"));
            updateUser.setGmtModified(System.currentTimeMillis());
            updateUser.setToken(user.getToken());
            //updateUser.setAvatarUrl(user.getAvatarUrl());
            UserExample example = new UserExample();
            example.createCriteria()
                    .andIdEqualTo(dbUser.getId());
            userMapper.updateByExampleSelective(updateUser, example);
            return 3;
        }
        return 0;
    }


    public int createOrUpdateBaidu(User user, UserDTO loginuser, UserInfo userInfo) {
        UserExample userExample = new UserExample();
        userExample.createCriteria()
                .andBaiduAccountIdEqualTo(user.getBaiduAccountId());
        List<User> users = userMapper.selectByExample(userExample);
        User updateUser = new User();
        if (users.size() == 0) {  // 插入

            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            if(user.getName()==null|| StringUtils.isBlank(user.getName()))
                user.setName(getUserName("Baidu"));
            if (loginuser == null) {//创建
                userMapper.insert(user);
                UserExample userExample2 = new UserExample();
                userExample2.createCriteria()
                        .andBaiduAccountIdEqualTo(user.getBaiduAccountId());
                List<User> users2 = userMapper.selectByExample(userExample2);
                if(users2.size() != 0){//表示user表已创建
                    User dbUser = users2.get(0);
                    initUserTable(dbUser,userInfo);
                   /* userInfo.setUserId(dbUser.getId());
                    userInfoMapper.insert(userInfo);*/
                }
                return 1;
                //  userMapper.insert(user);

            }
            if (loginuser != null) {//绑定或者换绑
                updateUser.setBaiduAccountId(user.getBaiduAccountId());
                updateUserInfo(user,updateUser,loginuser,userInfo);
                return 2;
            }

        } else {
            //登录更新
            User dbUser = users.get(0);
            if(dbUser.getName()==null&&(user.getName()==null|| StringUtils.isBlank(user.getName())))//数据库为空，当前为空
                updateUser.setName(getUserName("Baidu"));
            updateUser.setGmtModified(System.currentTimeMillis());
            updateUser.setToken(user.getToken());
            // updateUser.setAvatarUrl(user.getAvatarUrl());
            UserExample example = new UserExample();
            example.createCriteria()
                    .andIdEqualTo(dbUser.getId());
            userMapper.updateByExampleSelective(updateUser, example);
            return 3;
        }
        return 0;
    }

    public void createNewBaidu(User user,UserInfo userInfo) {
        user.setGmtCreate(System.currentTimeMillis());
        user.setGmtModified(user.getGmtCreate());
        if(user.getName()==null|| StringUtils.isBlank(user.getName()))
            user.setName(getUserName("Baidu"));
        userMapper.insert(user);

        UserExample userExample = new UserExample();
        userExample.createCriteria()
                .andBaiduAccountIdEqualTo(user.getBaiduAccountId());
        List<User> users = userMapper.selectByExample(userExample);
        if(users.size() != 0){
            User dbUser = users.get(0);
            userInfo.setUserId(dbUser.getId());
            userInfoMapper.insert(userInfo);
        }


    }

    public String getUserName(String authorizeSize) {
        String str = RandomStringUtils.random(5,
                "abcdefghijklmnopqrstuvwxyz1234567890");
        String name = authorizeSize+"用户_"+str;
        return name;
    }

    public User selectUserByUserId(String userId) {
        Long id = Long.parseLong(userId);
        User user = userMapper.selectByPrimaryKey(id);
        return user;
    }

    public User selectUserByUserId(Long userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        return user;
    }

    public Object updateUserMailById(String userId,String mail) {
        UserExample userExample = new UserExample();
        userExample.createCriteria()
                .andEmailEqualTo(mail);
        List<User> users = userMapper.selectByExample(userExample);
        if(users.size() != 0){
            return ResultDTO.errorOf(CustomizeErrorCode.SUBMIT_MAIL_FAILED);
        }
        User updateUser = new User();
        updateUser.setEmail(mail);
        UserExample example = new UserExample();
        example.createCriteria()
                .andIdEqualTo(Long.parseLong(userId));
        try{
            userMapper.updateByExampleSelective(updateUser, example);
            return ResultDTO.okOf("邮箱绑定/更新成功！！！");
        }catch (Exception e){
            return ResultDTO.errorOf(CustomizeErrorCode.SUBMIT_MAIL_FAILED);
        }

    }

    public int updateAvatarById(Long userId,String url){
        User user = userMapper.selectByPrimaryKey(userId);
        user.setAvatarUrl(url);
        return userMapper.updateByPrimaryKey(user);
    }

    public int updateUsernameById(Long userId,String username){
        User user = userMapper.selectByPrimaryKey(userId);
        if(user.getName().equals(username)) return 1;
        else user.setName(username);
        return userMapper.updateByPrimaryKey(user);
    }

    public UserDTO getUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user,userDTO);
        UserAccount userAccount = userAccountService.selectUserAccountByUserId(user.getId());
        userDTO.setGroupId(userAccount.getGroupId());
        userDTO.setVipRank(userAccount.getVipRank());
//        userDTO.setUnread(notificationService.unreadCount(user.getId()).toString());
        return userDTO;
    }

    public UserDTO getUserDTO(Long id) {
        User user = userMapper.selectByPrimaryKey(id);
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user,userDTO);
        UserAccount userAccount = userAccountService.selectUserAccountByUserId(user.getId());
        userDTO.setGroupId(userAccount.getGroupId());
        userDTO.setVipRank(userAccount.getVipRank());
        userDTO.setGroupStr(env.getProperty("user.group.r"+userAccount.getGroupId()));
        return userDTO;
    }

    public Object registerOrLoginWithMail(String mail,String password) {

        UserExample userExample = new UserExample();
        userExample.createCriteria()
                .andEmailEqualTo(mail);
        List<User> users = userMapper.selectByExample(userExample);
        User updateUser = new User();
        if(password!=null)
            updateUser.setPassword(DigestUtils.sha256Hex(password+salt));
        if(users.size() != 0){//登录
            User dbUser = users.get(0);
            UserDTO userDTO = getUserDTO(dbUser);
            if(dbUser.getName()==null|| StringUtils.isBlank(dbUser.getName()))//数据库为空，当前为空
                updateUser.setName(getUserName("邮箱"));
            updateUser.setGmtModified(System.currentTimeMillis());

            UserExample example = new UserExample();
            example.createCriteria()
                    .andIdEqualTo(dbUser.getId());
            userMapper.updateByExampleSelective(updateUser, example);
            return ResultDTO.okOf(tokenUtils.getToken(userDTO));
        }else {
            //注册
            updateUser.setEmail(mail);
            updateUser.setName(getUserName("邮箱"));
            updateUser.setAvatarUrl("/images/avatar/"+(int)(Math.random()*11)+".jpg");
            updateUser.setGmtCreate(System.currentTimeMillis());
            updateUser.setGmtModified(updateUser.getGmtCreate());
            userMapper.insert(updateUser);
            UserExample example = new UserExample();
            example.createCriteria()
                    .andEmailEqualTo(mail);
            List<User> insertUsers = userMapper.selectByExample(example);
            //System.out.println("size:"+insertUsers.size());
            if(insertUsers.size() != 0){
                User insertUser = insertUsers.get(0);
                initUserTable(insertUser,new UserInfo());
                UserDTO userDTO = getUserDTO(insertUser);
                return ResultDTO.okOf(tokenUtils.getToken(userDTO));
            }

        }

        return ResultDTO.errorOf("未知错误");


    }

    public Object updateUserPhoneById(Long userId, String phone) {
        UserExample userExample = new UserExample();
        userExample.createCriteria()
                .andPhoneEqualTo(phone);
        List<User> users = userMapper.selectByExample(userExample);
        if(users.size() != 0){
            return ResultDTO.errorOf(CustomizeErrorCode.SUBMIT_PHONE_FAILED);
        }
        User updateUser = new User();
        updateUser.setPhone(phone);
        UserExample example = new UserExample();
        example.createCriteria()
                .andIdEqualTo(userId);
        try{
            userMapper.updateByExampleSelective(updateUser, example);
            return ResultDTO.okOf("手机绑定/更新成功！！！");
        }catch (Exception e){
            return ResultDTO.errorOf(CustomizeErrorCode.SUBMIT_PHONE_FAILED);
        }
    }

    public Object registerOrLoginWithPhone(String phone,String password) {
        UserExample userExample = new UserExample();
        userExample.createCriteria()
                .andPhoneEqualTo(phone);
        List<User> users = userMapper.selectByExample(userExample);
        User updateUser = new User();
        if(password!=null)
            updateUser.setPassword(DigestUtils.sha256Hex(password+salt));
        if(users.size() != 0){//登录
            User dbUser = users.get(0);
            UserDTO userDTO = getUserDTO(dbUser);
            if(dbUser.getName()==null|| StringUtils.isBlank(dbUser.getName()))//数据库为空，当前为空
                updateUser.setName(getUserName("手机"));

            updateUser.setGmtModified(System.currentTimeMillis());

            UserExample example = new UserExample();
            example.createCriteria()
                    .andIdEqualTo(dbUser.getId());
            userMapper.updateByExampleSelective(updateUser, example);
            return ResultDTO.okOf(tokenUtils.getToken(userDTO));
        }else {
            //注册
            updateUser.setPhone(phone);
            updateUser.setName(getUserName("手机"));
            updateUser.setAvatarUrl("/images/avatar/"+(int)(Math.random()*11)+".jpg");
            updateUser.setGmtCreate(System.currentTimeMillis());
            updateUser.setGmtModified(updateUser.getGmtCreate());
            userMapper.insert(updateUser);
            UserExample example = new UserExample();
            example.createCriteria()
                    .andPhoneEqualTo(phone);
            List<User> insertUsers = userMapper.selectByExample(example);
            User insertUser = insertUsers.get(0);
            initUserTable(insertUser,new UserInfo());
            UserDTO userDTO = getUserDTO(insertUser);
            return ResultDTO.okOf(tokenUtils.getToken(userDTO));
        }

    }

    public int createOrUpdateQq(User user, UserDTO loginuser, UserInfo userInfo) {
        UserExample userExample = new UserExample();
        userExample.createCriteria()
                .andQqAccountIdEqualTo(user.getQqAccountId());
        List<User> users = userMapper.selectByExample(userExample);
        User updateUser = new User();
        if (users.size() == 0) {  // 插入
            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            if(user.getName()==null|| StringUtils.isBlank(user.getName()))
                user.setName(getUserName("QQ"));
            if (loginuser == null) {//创建
                userMapper.insert(user);
                UserExample userExample2 = new UserExample();
                userExample2.createCriteria()
                        .andQqAccountIdEqualTo(user.getQqAccountId());
                List<User> users2 = userMapper.selectByExample(userExample2);
                if(users2.size() != 0){//表示user表已创建
                    User dbUser = users2.get(0);
                    initUserTable(dbUser,userInfo);
                   /* userInfo.setUserId(dbUser.getId());
                    userInfoMapper.insert(userInfo);
                    UserAccount userAccount = new UserAccount();
                    userAccount = initUserAccount(userAccount);
                    userAccount.setUserId(dbUser.getId());
                    userAccount.setGroupId(1);
                    userAccount.setVipRank(0);
                    userAccount.setScore(0);
                    userAccount.setScore1(0);
                    userAccount.setScore2(0);
                    userAccount.setScore3(0);
                    userAccountMapper.insert(userAccount);*/

                }
                return 1;
            }
            if (loginuser != null) {//绑定或者换绑
                updateUser.setQqAccountId(user.getQqAccountId());
                updateUserInfo(user,updateUser,loginuser,userInfo);
              /*  updateUser.setGmtModified(System.currentTimeMillis());
                updateUser.setToken(user.getToken());
                updateUser.setAvatarUrl(user.getAvatarUrl());
                UserExample example = new UserExample();
                example.createCriteria()
                        .andIdEqualTo(loginuser.getId());
                userMapper.updateByExampleSelective(updateUser, example);

                UserInfoExample userInfoExample = new UserInfoExample();
                userInfoExample.createCriteria()
                        .andUserIdEqualTo(loginuser.getId());
                List<UserInfo> dbUserInfos = userInfoMapper.selectByExample(userInfoExample);
                if(dbUserInfos.size() == 0){//信息为空插入
                    userInfo.setUserId(loginuser.getId());
                    userInfoMapper.insert(userInfo);
                }
                else{//信息不空更新
                    UserInfoExample userInfEexample = new UserInfoExample();
                    userInfEexample.createCriteria()
                            .andUserIdEqualTo(loginuser.getId());
                    userInfoMapper.updateByExampleSelective(userInfo, userInfEexample);
                }*/
                return 2;
            }



            // user.setName(getUserName("Github"));

        } else {
            //登录更新
            User dbUser = users.get(0);
            if(dbUser.getName()==null&&(user.getName()==null|| StringUtils.isBlank(user.getName())))//数据库为空，当前为空
                updateUser.setName(getUserName("QQ"));
            updateUser.setGmtModified(System.currentTimeMillis());
            updateUser.setToken(user.getToken());
            //updateUser.setAvatarUrl(user.getAvatarUrl());
            UserExample example = new UserExample();
            example.createCriteria()
                    .andIdEqualTo(dbUser.getId());
            userMapper.updateByExampleSelective(updateUser, example);
            return 3;
        }
        return 0;
    }

    public UserAccount initUserAccount(UserAccount userAccount){
        userAccount.setGroupId(1);
        userAccount.setVipRank(0);
        userAccount.setScore(0);
        userAccount.setScore1(0);
        userAccount.setScore2(0);
        userAccount.setScore3(0);
        return userAccount;
    }

    public void initUserTable(User user,UserInfo userInfo){
        // UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfoMapper.insert(userInfo);
        UserAccount userAccount = new UserAccount();
        userAccount = initUserAccount(userAccount);
        userAccount.setUserId(user.getId());
        userAccountMapper.insert(userAccount);
        userInfo=null;
        userAccount=null;
    }

    public void updateUserInfo(User user,User updateUser,UserDTO loginuser,UserInfo userInfo){
        updateUser.setGmtModified(System.currentTimeMillis());
        updateUser.setToken(user.getToken());
        //updateUser.setAvatarUrl(user.getAvatarUrl());
        UserExample example = new UserExample();
        example.createCriteria()
                .andIdEqualTo(loginuser.getId());
        userMapper.updateByExampleSelective(updateUser, example);

        UserInfoExample userInfoExample = new UserInfoExample();
        userInfoExample.createCriteria()
                .andUserIdEqualTo(loginuser.getId());
        List<UserInfo> dbUserInfos = userInfoMapper.selectByExample(userInfoExample);
        if(dbUserInfos.size() == 0){//信息为空插入
            userInfo.setUserId(loginuser.getId());
            userInfoMapper.insert(userInfo);
        }
        else{//信息不空更新
            UserInfoExample userInfEexample = new UserInfoExample();
            userInfEexample.createCriteria()
                    .andUserIdEqualTo(loginuser.getId());
            userInfoMapper.updateByExampleSelective(userInfo, userInfEexample);
        }

    }

    public Object login(Integer type, String name, String password) {
        UserExample userExample = new UserExample();
        System.out.println("pw:"+password);
        System.out.println("salt:"+salt);
        System.out.println("pw+salt:"+DigestUtils.sha256Hex(password+salt));
        if(type==1){//手机号登录
            userExample.createCriteria().andPhoneEqualTo(name).andPasswordEqualTo(DigestUtils.sha256Hex(password+salt));
        }else if(type==2){//邮箱登录
            userExample.createCriteria().andEmailEqualTo(name).andPasswordEqualTo(DigestUtils.sha256Hex(password+salt));
        }else {
            return ResultDTO.errorOf("不支持此登录类型");
        }
        List<User> users = userMapper.selectByExample(userExample);
        User updateUser = new User();
        if(users.size()!=0){
            User dbUser = users.get(0);
            UserDTO userDTO=getUserDTO(dbUser);
            updateUser.setGmtModified(System.currentTimeMillis());
            UserExample example= new UserExample();
            example.createCriteria()
                    .andIdEqualTo(dbUser.getId());
            userMapper.updateByExampleSelective(updateUser,example);
            ResultDTO resultDTO = ResultDTO.okOf("登录成功");
            resultDTO.setData(tokenUtils.getToken(userDTO));
            return resultDTO;
        }
        else return ResultDTO.errorOf(CustomizeErrorCode.LOGIN_FAILED);
    }

    public Object register(Integer type, String name, String password) {
        if(1==type){
            return registerOrLoginWithPhone(name,password);
        }else if(2==type){
            //updateUser.setEmail(name);
            // updateUser.setName(getUserName("邮箱"));
            return registerOrLoginWithMail(name,password);
        }
        return ResultDTO.errorOf("未知错误，请联系管理员");
    }

    public PaginationDTO list(UserQueryDTO userQueryDTO) {
        Integer totalPage;
        Integer totalCount = userExtMapper.count(userQueryDTO);
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        if(userQueryDTO.getId()!=null&&(userQueryDTO.getId().longValue()!=0L))
            criteria.andIdEqualTo(userQueryDTO.getId());
        if(StringUtils.isNotBlank(userQueryDTO.getName()))
            criteria.andNameEqualTo(userQueryDTO.getName());
        if(StringUtils.isNotBlank(userQueryDTO.getPhone()))
            criteria.andPhoneEqualTo(userQueryDTO.getPhone());
        if(StringUtils.isNotBlank(userQueryDTO.getEmail()))
            criteria.andEmailEqualTo(userQueryDTO.getEmail());


        if (totalCount % userQueryDTO.getSize() == 0) {
            totalPage = totalCount / userQueryDTO.getSize();
        } else {
            totalPage = totalCount / userQueryDTO.getSize() + 1;
        }

        if (userQueryDTO.getPage() > totalPage) {
            userQueryDTO.setPage(totalPage);
        }

        Integer offset = userQueryDTO.getPage() < 1 ? 0 : userQueryDTO.getSize() * (userQueryDTO.getPage() - 1);
        userQueryDTO.setOffset(offset);
        userExample.setOrderByClause(userQueryDTO.getSort()+" "+userQueryDTO.getOrder());

        List<User> users = userMapper.selectByExampleWithRowbounds(userExample,new RowBounds(userQueryDTO.getSize()*(userQueryDTO.getPage()-1), userQueryDTO.getSize()));
        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setTotalCount(totalCount);
        if (users.size() == 0) {
            paginationDTO.setPage(0);
            paginationDTO.setTotalPage(0);
            paginationDTO.setData(new ArrayList<>());
            return paginationDTO;
        }
        List<UserDTO> userDTOs = new ArrayList<>();
        for (User user : users) {
            userDTOs.add(getUserDTO(user));
        }
        paginationDTO.setData(userDTOs);
        paginationDTO.setPagination(totalPage,userQueryDTO.getPage());
        return paginationDTO;
        //return userDTOs;
    }

    public PaginationDTO listwithColumn(String search,  Integer page, Integer size,String start, String end,UserDTO loingUser){
        if (StringUtils.isNotBlank(search)) {
            String[] tags = StringUtils.split(search, " ");
            search = Arrays
                    .stream(tags)
                    .filter(StringUtils::isNotBlank)
                    .map(t -> t.replace("+", "").replace("*", "").replace("?", ""))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining("|"));
        }
        Integer totalPage;
        UserQueryDTO userQueryDTO= new UserQueryDTO();
        userQueryDTO.setSearch(search);
        if(start !="" && start !=null  ){userQueryDTO.setStart(timeUtils.farmatTime2(start));}

        if(end !="" && end !=null ){userQueryDTO.setEnd(timeUtils.farmatTime2(end));}
        Integer totalCount = userExtMapper.countBySearch(userQueryDTO);

        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }
        if (page < 1) {
            page = 1;
        }
        if (page > totalPage) {
            page = totalPage;
        }

        Integer offset = page < 1 ? 0 : size * (page - 1);
        userQueryDTO.setSize(size);
        userQueryDTO.setOffset(offset);


        List<User> users=userExtMapper.selectBySearch(userQueryDTO);
        List<UserDTO> userDTOS = new ArrayList<>();
        PaginationDTO paginationDTO=new PaginationDTO();

        for(User user : users){
            UserDTO userDTO=new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setName(user.getName());
            userDTO.setAvatarUrl(user.getAvatarUrl());
            userDTO.setGmtCreateStr(timeUtils.getTime2(user.getGmtCreate(),"yyyy-MM-dd HH:mm:ss"));
            userDTO.setGmtModifiedStr(timeUtils.getTime2(user.getGmtModified(),"yyyy-MM-dd HH:mm:ss"));
            userDTO.setEmail(user.getEmail());
            userDTO.setAccountId(user.getAccountId());
            UserAccountExample userAccountExample=new UserAccountExample();
            userAccountExample.createCriteria()
                    .andUserIdEqualTo(user.getId());
            List<UserAccount> userAccount= userAccountMapper.selectByExample(userAccountExample);
            UserInfoExample userInfoExample=new UserInfoExample();
            userInfoExample.createCriteria()
                    .andUserIdEqualTo(user.getId());
            List<UserInfo> userInfo= userInfoMapper.selectByExample(userInfoExample);

            userDTO.setGroupStr(env.getProperty("user.group.r"+userAccount.get(0).getGroupId()));
            userDTO.setUserAccount(userAccount.get(0));
            userDTO.setUserInfo(userInfo.get(0));
            if(userDTO.getUserAccount().getGroupId()>=loingUser.getGroupId() && loingUser.getGroupId()!=4) continue;
            userDTOS.add(userDTO);
        }
        paginationDTO.setData(userDTOS);
        paginationDTO.setPagination(totalPage,page);
        return paginationDTO;
    }

    public UserDTO selectUserInfo(Long userid){
        UserDTO userDTO=new UserDTO();
        User user =userMapper.selectByPrimaryKey(userid);
        UserAccountExample userAccountExample=new UserAccountExample();
        userAccountExample.createCriteria()
                .andUserIdEqualTo(userid);
        List<UserAccount> userAccount= userAccountMapper.selectByExample(userAccountExample);
        UserInfoExample userInfoExample=new UserInfoExample();
        userInfoExample.createCriteria()
                .andUserIdEqualTo(userid);
        List<UserInfo> userInfo= userInfoMapper.selectByExample(userInfoExample);
        userDTO.setUserAccount(userAccount.get(0));
        userDTO.setUserInfo(userInfo.get(0));
        userDTO.setId(userid);
        userDTO.setName(user.getName());
        userDTO.setGroupStr(env.getProperty("user.group.r"+userAccount.get(0).getGroupId()));
        return userDTO;
    }

    public void  delUser(Long userid){
        UserExample userExample=new UserExample();
        userExample.createCriteria()
                .andIdEqualTo(userid);
        userMapper.deleteByExample(userExample);
        UserAccountExample userAccountExample=new UserAccountExample();
        userAccountExample.createCriteria()
                .andUserIdEqualTo(userid);
        userAccountMapper.deleteByExample(userAccountExample);
        UserInfoExample userInfoExample=new UserInfoExample();
        userInfoExample.createCriteria()
                .andUserIdEqualTo(userid);
        userInfoMapper.deleteByExample(userInfoExample);
    }

    public ArrayList<Long> countSex(){
        UserInfoExample userInfoExample=new UserInfoExample();
        userInfoExample.createCriteria()
                .andSexEqualTo("男");
        Long male=userInfoMapper.countByExample(userInfoExample);
        UserInfoExample userInfoExample2=new UserInfoExample();
        userInfoExample2.createCriteria()
                .andSexEqualTo("女");
        Long female=userInfoMapper.countByExample(userInfoExample2);
        ArrayList<Long> res=new ArrayList<Long>();
        res.add(male);
        res.add(female);
        return res;

    }


    public ArrayList<Long>  countEducation(){
        UserInfoExample userInfoExample=new UserInfoExample();
        userInfoExample.createCriteria()
                .andEducationEqualTo("小学");
        Long edu1=userInfoMapper.countByExample(userInfoExample);
        UserInfoExample userInfoExample2=new UserInfoExample();
        userInfoExample2.createCriteria()
                .andEducationEqualTo("初中");
        Long edu2=userInfoMapper.countByExample(userInfoExample2);
        UserInfoExample userInfoExample3=new UserInfoExample();
        userInfoExample3.createCriteria()
                .andEducationEqualTo("高中");
        Long edu3=userInfoMapper.countByExample(userInfoExample3);
        UserInfoExample userInfoExample4=new UserInfoExample();
        userInfoExample4.createCriteria()
                .andEducationEqualTo("大学");
        Long edu4=userInfoMapper.countByExample(userInfoExample4);
        ArrayList<Long> res=new ArrayList<Long>();
        res.add(edu1);
        res.add(edu2);
        res.add(edu3);
        res.add(edu4);
        return res;

    }

    public ArrayList<Long> countRegister(){
        ArrayList<Long> res=new ArrayList<Long>();
        try {
            List<Date> dates=timeUtils.getZhou();
            for(Date date :dates){
                UserQueryDTO userQueryDTO=new UserQueryDTO();
                userQueryDTO.setStart(date.getTime());
                userQueryDTO.setEnd(date.getTime()+3600*1000*24);
                res.add( Long.valueOf(userExtMapper.countBySearch(userQueryDTO)));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return res;
    }

}
