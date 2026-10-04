package org.example.aispingboot.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.aispingboot.DTO.command.UserLoginCommandDTO;
import org.example.aispingboot.DTO.command.UserRegisterCommandDTO;
import org.example.aispingboot.DTO.command.UserUpdateCommandDTO;
import org.example.aispingboot.DTO.response.UserLoginResponseDTO;
import org.example.aispingboot.common.Result;
import org.example.aispingboot.entity.User;
import org.example.aispingboot.enumClass.UserType;
import org.example.aispingboot.exception.BusinessException;
import org.example.aispingboot.mapper.UserMapper;
import org.example.aispingboot.service.convert.UserConvert;
import org.example.aispingboot.util.JwtTokenUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class UserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private UserCacheService userCacheService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserLoginResponseDTO login(UserLoginCommandDTO commandDTO) {
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper =  new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, commandDTO.getUsername())
                .or()
                .eq(User::getEmail, commandDTO.getUsername());
        // 调用MP API查询
        // 用user对象来接收查询结果
        User user = userMapper.selectOne(queryWrapper);
        System.out.println(user);

        // 判断用户是否存在
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 验证密码
        String inputPassword = commandDTO.getPassword().trim();  //trim()方法用于移除字符串首尾的空格
        if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 检查用户的状态
        if (!user.isActive()) {
            throw new BusinessException("用户已被禁用，请联系管理员");
        }

        // 生成JWT token
        String token = JwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());
        System.out.println(token);
        UserLoginResponseDTO.UserDetailResponseDTO userInfo = UserConvert.entityToDetailResponse(user);
        return UserConvert.entityToLoginResponse(token, userInfo);  //返回登录响应DTO
    }

    public UserLoginResponseDTO.UserDetailResponseDTO register(UserRegisterCommandDTO commandDTO) {
        System.out.println(JSONUtil.parseObj(commandDTO));  // 打印commandDTO，用于调试，方便开发者查看前端传来的数据
        // 验证密码是否一致
        if (!commandDTO.getPassword().equals(commandDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入密码不一致");
        }

        // 检查用户名是否存在,构建查询条件,然后调用MP API查询,判断查询结果是否为空,如果为空,则用户名不存在
        LambdaQueryWrapper<User> userNameQuery =  new LambdaQueryWrapper<>();   //首先构建一个空的查询条件对象
        userNameQuery.eq(User::getUsername, commandDTO.getUsername());   //添加查询条件,查询用户表中用户名是否存在
        if (userMapper.selectCount(userNameQuery) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否存在
        LambdaQueryWrapper<User> emailQuery =  new LambdaQueryWrapper<>();
        emailQuery.eq(User::getEmail, commandDTO.getEmail());
        if (userMapper.selectCount(emailQuery) > 0) {
            throw new BusinessException("邮箱已存在");
        }

        // 用户类型
        if (!UserType.isValidCode(commandDTO.getUserType())) {
            throw new BusinessException("无效的用户类型");
        }

        // 创建用户
        String password = commandDTO.getPassword().trim();  //trim()方法用于移除字符串首尾的空格
        String encodedPassword = passwordEncoder.encode(password);  //使用BCryptPasswordEncoder加密密码
        User user = UserConvert.registerCommandToEntity(commandDTO, encodedPassword);  //将commandDTO转换为实体类User，Mapper.insert()方法会自动调用UserConvert.entityToEntity()方法，将user转换为数据库中的User实体

        // 插入数据库
        userMapper.insert(user);  //调用MP API插入用户实体到数据库

        return UserConvert.entityToDetailResponse(user);
    }

    public UserLoginResponseDTO.UserDetailResponseDTO getUserById(Long userId) {

        UserLoginResponseDTO.UserDetailResponseDTO userInfo;
        //1. 判断Redis是否存在key,如果存在,则返回用户详情
        if (userCacheService.hasUserCache(userId)) {// 查询缓存，如果缓存中存在用户详情,则返回用户详情
            //2. 判断缓存中是否存在空值,如果存在,则抛出异常
            if (userCacheService.isNullUser(userId)) {

                throw new BusinessException(
                        "用户不存在"
                );

            }
            //3. 从缓存中获取用户详情,正常返回
            userInfo = userCacheService.getUser(userId);//convertUser()方法将缓存中的用户详情转换为UserLoginResponseDTO.UserDetailResponseDTO
            // 打印缓存中的用户详情,用于调试,方便开发者查看缓存中的数据
            log.info("Redis缓存命中,userId={}", userId);
            return userInfo;

        }

            log.info(
                "Redis没有缓存，查询数据库,userId={}",
                userId
        );

        // 如果缓存中不存在用户详情,则从数据库中查询用户详情
        User user = userMapper.selectById(userId);
        if (user == null) {

            userCacheService.saveNullUser(userId);  // 缓存空用户，防止缓存穿透
            throw new BusinessException("用户不存在");
        }
        // 将查询到的用户实体user转换为UserLoginResponseDTO.UserDetailResponseDTO
        userInfo = UserConvert.entityToDetailResponse(user);
        //将user转换为实体类UserLoginResponseDTO.UserDetailResponseDTO

        // 缓存用户详情到Redis中
        userCacheService.saveUser(userId, userInfo);
        return userInfo;//返回用户详情
    }

    public UserLoginResponseDTO.UserDetailResponseDTO updateUser(
            UserUpdateCommandDTO commandDTO
    ){

        //1. 根据id查询用户
        User user = userMapper.selectById(commandDTO.getId());


        if(user == null){

            throw new BusinessException("用户不存在");

        }


        //2. DTO更新Entity
        UserConvert.updateCommandToEntity(commandDTO, user);

        //3. 更新数据库
        userMapper.updateById(user);


        //4. 删除Redis缓存
        userCacheService.deleteUser(user.getId());//Entity 才是数据库真实对象,所以需要根据Entity中的id删除缓存中的用户详情

        //5.数据库更新后重新查询用户详情
        User latestUser = userMapper.selectById(commandDTO.getId());


        return UserConvert.entityToDetailResponse(latestUser);

    }

}
