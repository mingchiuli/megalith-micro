package wiki.chiu.micro.blog.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.BlogStatusEnum;

class BlogAccessPolicyTest {

  private final BlogAccessPolicy policy = new BlogAccessPolicy("admin");

  @Test
  void authenticatedUsersCanCollaborateOnPublicAndDraftBlogs() {
    assertTrue(policy.canCollaborate(blog(BlogStatusEnum.NORMAL, 1L), 2L, List.of("user")));
    assertTrue(policy.canCollaborate(blog(BlogStatusEnum.DRAFT, 1L), 2L, List.of("user")));
    assertFalse(policy.canCollaborate(blog(BlogStatusEnum.DRAFT, 1L), 0L, List.of()));
  }

  @Test
  void hiddenAndSensitiveBlogsRemainRestricted() {
    BlogEntity hidden = blog(BlogStatusEnum.HIDE, 1L);
    BlogEntity sensitive = blog(BlogStatusEnum.SENSITIVE_FILTER, 1L);

    assertThrows(
        MissException.class, () -> policy.requireCollaboration(hidden, 2L, List.of("user")));
    assertThrows(
        MissException.class, () -> policy.requireCollaboration(sensitive, 2L, List.of("user")));
    assertTrue(policy.canCollaborate(hidden, 1L, List.of("user")));
    assertTrue(policy.canCollaborate(sensitive, 2L, List.of("admin")));
  }

  @Test
  void collaboratorsCannotCommitOrManageMetadata() {
    var permissions = policy.permissions(blog(BlogStatusEnum.DRAFT, 1L), 2L, List.of("user"));

    assertTrue(permissions.collaborate());
    assertFalse(permissions.commit());
    assertFalse(permissions.manageMetadata());
    assertFalse(permissions.manageAssets());
  }

  @Test
  void ownerCanManageAndAnonymousUserCannotManageUnownedBlog() {
    var ownerPermissions = policy.permissions(blog(BlogStatusEnum.NORMAL, 1L), 1L, List.of("user"));
    BlogEntity unowned = blog(BlogStatusEnum.NORMAL, null);

    assertTrue(ownerPermissions.commit());
    assertTrue(ownerPermissions.manageMetadata());
    assertTrue(ownerPermissions.manageAssets());
    assertFalse(policy.canManage(unowned, null, List.of()));
  }

  private BlogEntity blog(BlogStatusEnum status, Long ownerId) {
    return BlogEntity.builder().userId(ownerId).status(status.getCode()).build();
  }
}
