package org.chiu.micro.auth.user;

import org.chiu.micro.auth.dto.UserEntityDto;
import org.chiu.micro.auth.rpc.wrapper.UserHttpServiceWrapper;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public final class UserDetailsServiceImpl implements UserDetailsService {

	private final UserHttpServiceWrapper userHttpServiceWrapper;

	public UserDetailsServiceImpl(UserHttpServiceWrapper userHttpServiceWrapper) {
		this.userHttpServiceWrapper = userHttpServiceWrapper;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		UserEntityDto user = userHttpServiceWrapper.findByUsernameOrEmailOrPhone(username);

		Long userId = user.getId();
		List<String> roleCodes = userHttpServiceWrapper.findRoleCodesByUserId(userId);

		//通过User去自动比较用户名和密码
		return new LoginUser(username,
				user.getPassword(),
				true,
				true,
				true,
				user.getStatus() == 0,
				AuthorityUtils.createAuthorityList(roleCodes),
				userId);
	}

}
