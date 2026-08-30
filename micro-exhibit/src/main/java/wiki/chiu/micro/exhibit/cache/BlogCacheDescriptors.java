package wiki.chiu.micro.exhibit.cache;

import wiki.chiu.micro.cache.key.CacheDescriptor;

public final class BlogCacheDescriptors {

  public static final int VERSION = 2;
  public static final String DETAIL_NAMESPACE = "blog-detail";
  public static final String PAGE_NAMESPACE = "blog-page";
  public static final String SENSITIVE_NAMESPACE = "blog-sensitive";

  public static final CacheDescriptor DETAIL = new CacheDescriptor(DETAIL_NAMESPACE, VERSION);
  public static final CacheDescriptor PAGE = new CacheDescriptor(PAGE_NAMESPACE, VERSION);
  public static final CacheDescriptor SENSITIVE =
      new CacheDescriptor(SENSITIVE_NAMESPACE, VERSION);

  private BlogCacheDescriptors() {}
}
