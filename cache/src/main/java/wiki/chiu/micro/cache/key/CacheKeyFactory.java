package wiki.chiu.micro.cache.key;

/** Generates opaque Redis keys from stable cache descriptors and typed arguments. */
public interface CacheKeyFactory {

  /**
   * Generates a key in {@code <namespace>:v<version>:<sha256>} form.
   *
   * @param descriptor stable cache contract identity
   * @param args method arguments in declaration order
   * @return the exact L1 and L2 key
   */
  String generate(CacheDescriptor descriptor, Object... args);
}
