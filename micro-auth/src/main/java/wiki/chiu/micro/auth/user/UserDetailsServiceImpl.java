package wiki.chiu.micro.auth.user;

import wiki.chiu.micro.auth.rpc.UserHttpServiceWrapper;

import wiki.chiu.micro.common.dto.UserEntityRpcDto;
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

		UserEntityRpcDto user = userHttpServiceWrapper.findByUsernameOrEmailOrPhone(username);

		Long userId = user.id();
		List<String> roleCodes = userHttpServiceWrapper.findRoleCodesByUserId(userId);

		//通过User去自动比较用户名和密码
		return new LoginUser(username,
				user.password(),
				true,
				true,
				true,
				user.status() == 0,
				AuthorityUtils.createAuthorityList(roleCodes),
				userId);
	}

}