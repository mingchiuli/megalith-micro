package wiki.chiu.micro.user.application.service;

import java.util.List;

import wiki.chiu.micro.user.application.model.AuthorityDraft;
import wiki.chiu.micro.user.application.model.MenuAuthorityView;
import wiki.chiu.micro.user.application.model.MenuDraft;
import wiki.chiu.micro.user.application.model.MenuNode;
import wiki.chiu.micro.user.application.model.MenuSelection;
import wiki.chiu.micro.user.application.model.RegistrationDraft;
import wiki.chiu.micro.user.application.model.RoleDraft;
import wiki.chiu.micro.user.application.model.UserDraft;
import wiki.chiu.micro.user.domain.Authority;
import wiki.chiu.micro.user.domain.Menu;
import wiki.chiu.micro.user.domain.Role;
import wiki.chiu.micro.user.domain.User;

public final class UserModelConvertor {

    private UserModelConvertor() {
    }

    public static User mergeInto(UserDraft draft, User dealUser) {
        return new User(
            draft.id(),
            draft.username(),
            draft.nickname(),
            draft.avatar(),
            draft.email(),
            draft.phone(),
            draft.password(),
            draft.status(),
            null,
            dealUser.created(),
            dealUser.updated(),
            dealUser.lastLogin());
    }

    public static Role mergeInto(RoleDraft draft, Role dealRole) {
        return new Role(
            draft.id(), draft.name(), draft.code(), draft.remark(), dealRole.created(), dealRole.updated(), draft.status());
    }

    public static Menu mergeInto(MenuDraft draft, Menu dealMenu) {
        return new Menu(
            draft.id(),
            draft.parentId(),
            draft.title(),
            draft.name(),
            draft.url(),
            draft.component(),
            draft.type(),
            draft.icon(),
            draft.orderNum(),
            draft.status(),
            dealMenu.created(),
            dealMenu.updated());
    }

    public static Authority mergeInto(AuthorityDraft draft, Authority dealAuthority) {
        return new Authority(
            draft.id(),
            draft.code(),
            draft.remark(),
            draft.protocol(),
            draft.methodType(),
            draft.routePattern(),
            draft.serviceHost(),
            draft.servicePort(),
            dealAuthority.created(),
            dealAuthority.updated(),
            draft.type(),
            draft.status());
    }

    public static UserDraft toUserDraft(
        RegistrationDraft request,
        Long existingId,
        Integer status,
        List<String> roles) {
        return new UserDraft(
            existingId, request.username(), request.nickname(), request.avatar(), request.password(), request.email(), request.phone(), status, roles);
    }

    public static MenuAuthorityView toView(Authority authority, List<Long> ids) {
        return new MenuAuthorityView(
            authority.id(), authority.code(), ids.contains(authority.id()));
    }

    public static List<MenuSelection> toSelections(
        List<MenuNode> nodes, List<Long> menuIdsByRole) {
        return nodes.stream()
            .map(
                node ->
                    new MenuSelection(
                        node.menu().id(),
                        node.menu().title(),
                        menuIdsByRole.contains(node.menu().id()),
                        toSelections(node.children(), menuIdsByRole)))
            .toList();
    }
}
