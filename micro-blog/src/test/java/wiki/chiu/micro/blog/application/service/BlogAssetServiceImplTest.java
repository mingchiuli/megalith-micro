package wiki.chiu.micro.blog.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import wiki.chiu.micro.blog.application.model.UploadObject;
import wiki.chiu.micro.blog.application.port.out.BlogAssetStorage;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.domain.BlogEntity;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.DataPermissionEnum;

class BlogAssetServiceImplTest {

  private final BlogAssetStorage storage = mock(BlogAssetStorage.class);
  private final BlogQueryStore blogs = mock(BlogQueryStore.class);
  private final BlogAssetServiceImpl service = new BlogAssetServiceImpl(storage, blogs);

  @Test
  void storesImageUnderImmutableUserOwner() {
    when(storage.storeImage(anyString(), any())).thenReturn("https://cdn/image.png");

    service.upload(new UploadObject(png()), null, 42L, java.util.List.of());

    ArgumentCaptor<String> objectName = ArgumentCaptor.forClass(String.class);
    verify(storage).storeImage(objectName.capture(), any());
    assertTrue(objectName.getValue().matches("blog/42/[0-9a-f-]+"));
  }

  @Test
  void rejectsDeletionOutsideAuthenticatedUserPrefix() {
    when(storage.objectName("https://cdn/image.png")).thenReturn("blog/41/image.png");

    assertThrows(
        MissException.class,
        () -> service.delete("https://cdn/image.png", null, 42L, java.util.List.of()));

    verify(storage, never()).delete(anyString());
  }

  @Test
  void allEditPermissionStoresAndDeletesAssetsUnderBlogOwner() {
    BlogEntity blog = BlogEntity.builder().id(7L).userId(41L).link("https://cdn/old.png").build();
    when(blogs.findById(7L)).thenReturn(java.util.Optional.of(blog));
    when(storage.storeImage(anyString(), any())).thenReturn("https://cdn/new.png");
    when(storage.objectName("https://cdn/old.png")).thenReturn("blog/41/old.png");

    service.upload(
        new UploadObject(png()), 7L, 42L, java.util.List.of(DataPermissionEnum.BLOG_EDIT_ALL));
    service.delete(
        "https://cdn/old.png", 7L, 42L, java.util.List.of(DataPermissionEnum.BLOG_EDIT_ALL));

    ArgumentCaptor<String> objectName = ArgumentCaptor.forClass(String.class);
    verify(storage).storeImage(objectName.capture(), any());
    assertTrue(objectName.getValue().startsWith("blog/41/7/"));
    verify(storage).delete("blog/41/old.png");
  }

  private static byte[] png() {
    return new byte[] {1, 2, 3};
  }
}
