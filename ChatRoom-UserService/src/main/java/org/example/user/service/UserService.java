package org.example.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.pojo.dto.UserAuthority;
import org.example.pojo.vo.Result;
import org.example.user.entity.dto.UserLoginDTO;
import org.example.user.entity.dto.UserRegisterDTO;
import org.example.user.entity.po.User;
import org.example.user.entity.vo.UserLoginInfoVo;

/**
 * @author yinjunbiao
 * @version 1.0
 * @date 2024/4/30
 */
public interface UserService extends IService<User> {

    UserAuthority getUserAuthorityByPhone(String phone);

    Boolean register(UserRegisterDTO userRegisterDTO);

    Result<UserLoginInfoVo> login(UserLoginDTO userRegisterDTO);
}
