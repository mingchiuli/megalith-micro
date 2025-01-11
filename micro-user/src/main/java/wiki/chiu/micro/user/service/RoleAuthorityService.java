package wiki.chiu.micro.user.service;



import java.util.Set;

public interface RoleAuthorityService {

    Set<String> getAuthoritiesByRoleCodes(String roleCode);
}
