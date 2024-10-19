package org.chiu.micro.user.service;

import org.chiu.micro.common.page.PageAdapter;
import org.chiu.micro.user.req.UserEntityRegisterReq;
import org.chiu.micro.user.req.UserEntityReq;
import org.chiu.micro.user.vo.UserEntityVo;

import java.util.List;

/**
 * @Author limingjiu
 * @Date 2024/5/29 22:12
 **/
public interface UserRoleService {

    void saveOrUpdate(UserEntityReq userEntityReq);

    void saveRegisterPage(String token, UserEntityRegisterReq userEntityRegisterReq);

    List<String> findRoleCodesByUserId(Long userId);

    UserEntityVo findById(Long userId);

    PageAdapter<UserEntityVo> listPage(Integer currentPage, Integer size);

    void deleteUsers(List<Long> ids);
}
