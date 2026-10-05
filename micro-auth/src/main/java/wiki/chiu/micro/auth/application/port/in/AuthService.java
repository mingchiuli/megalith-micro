package wiki.chiu.micro.auth.application.port.in;

import java.util.List;

import wiki.chiu.micro.auth.application.model.MenuDisplay;
import wiki.chiu.micro.auth.application.model.RouteDecision;
import wiki.chiu.micro.auth.application.model.RouteQuery;

public interface AuthService {

    List<MenuDisplay> getCurrentUserNav(List<String> roles);

    RouteDecision authorizeRoute(RouteQuery query, String token);
}
