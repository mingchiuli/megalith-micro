package wiki.chiu.micro.user.service.impl;

import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.user.entity.RoleEntity;
import wiki.chiu.micro.user.entity.UserRoleEntity;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.repository.UserRoleRepository;
import wiki.chiu.micro.user.service.UserRoleService;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @Author limingjiu
 * @Date 2024/5/29 22:12
 **/
@Service
public class UserRoleServiceImpl implements UserRoleService {

    private final RoleRepository roleRepository;

    private final UserRoleRepository userRoleRepository;

    public UserRoleServiceImpl(RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public List<String> findRoleCodesByUserId(Long userId) {
        List<Long> roleIds = userRoleRepository.findByUserId(userId).stream()
                .map(UserRoleEntity::getRoleId)
                .toList();

        return roleRepository.findAllById(roleIds).stream()
                .filter(item -> StatusEnum.NORMAL.getCode().equals(item.getStatus()))
                .map(RoleEntity::getCode)
                .toList();
    }


}
