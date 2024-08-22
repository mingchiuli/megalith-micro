package org.chiu.micro.user.service;


import org.chiu.micro.user.req.RoleEntityReq;
import org.chiu.micro.user.page.PageAdapter;
import org.chiu.micro.user.vo.RoleEntityRpcVo;
import org.chiu.micro.user.vo.RoleEntityVo;

import java.util.List;

/**
 * @author mingchiuli
 * @create 2022-12-04 2:25 am
 */
public interface RoleService {

    RoleEntityVo info(Long id);

    PageAdapter<RoleEntityVo> getPage(Integer current, Integer size);

    void saveOrUpdate(RoleEntityReq role);

    void delete(List<Long> ids);

    byte[] download();

    List<RoleEntityVo> getValidAll();

    List<RoleEntityRpcVo> findByRoleCodeInAndStatus(List<String> roles, Integer status);
}
