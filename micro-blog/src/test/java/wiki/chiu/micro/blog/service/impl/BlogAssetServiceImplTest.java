package wiki.chiu.micro.blog.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.repository.BlogRepository;
import wiki.chiu.micro.blog.service.UploadObject;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.common.rpc.storage.ObjectStorageClient;

class BlogAssetServiceImplTest {

  private final ObjectStorageClient storage = mock(ObjectStorageClient.class);
  private final BlogRepository blogs = mock(BlogRepository.class);
  private final BlogAssetServiceImpl service = new BlogAssetServiceImpl(storage, blogs);

  @Test
  void storesValidatedImageUnderImmutableUserOwner() throws IOException {
    when(storage.put(anyString(), any(), eq("image/png"))).thenReturn("https://cdn/image.png");

    service.upload(new UploadObject(png()), null, 42L, java.util.List.of());

    ArgumentCaptor<String> objectName = ArgumentCaptor.forClass(String.class);
    verify(storage).put(objectName.capture(), any(), eq("image/png"));
    assertTrue(objectName.getValue().matches("blog/42/[0-9a-f-]+\\.png"));
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
  void allEditPermissionStoresAndDeletesAssetsUnderBlogOwner() throws IOException {
    BlogEntity blog = BlogEntity.builder().id(7L).userId(41L).link("https://cdn/old.png").build();
    when(blogs.findById(7L)).thenReturn(java.util.Optional.of(blog));
    when(storage.put(anyString(), any(), eq("image/png"))).thenReturn("https://cdn/new.png");
    when(storage.objectName("https://cdn/old.png")).thenReturn("blog/41/old.png");

    service.upload(
        new UploadObject(png()), 7L, 42L, java.util.List.of(DataPermissionEnum.BLOG_EDIT_ALL));
    service.delete(
        "https://cdn/old.png", 7L, 42L, java.util.List.of(DataPermissionEnum.BLOG_EDIT_ALL));

    ArgumentCaptor<String> objectName = ArgumentCaptor.forClass(String.class);
    verify(storage).put(objectName.capture(), any(), eq("image/png"));
    assertTrue(objectName.getValue().startsWith("blog/41/7/"));
    verify(storage).delete("blog/41/old.png");
  }

  private static byte[] png() throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", output);
    return output.toByteArray();
  }
}
