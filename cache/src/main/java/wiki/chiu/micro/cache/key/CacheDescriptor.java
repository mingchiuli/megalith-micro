package wiki.chiu.micro.cache.key;

import java.util.regex.Pattern;

/**
 * Identifies a cache contract independently of its declaring Java method.
 *
 * @param namespace stable lowercase namespace
 * @param version positive contract version
 */
public record CacheDescriptor(String namespace, int version) {

  private static final Pattern NAMESPACE =
      Pattern.compile("[a-z][a-z0-9]*(?:[.-][a-z0-9]+)*");

  /** Validates the namespace and version. */
  public CacheDescriptor {
    if (namespace == null || !NAMESPACE.matcher(namespace).matches()) {
      throw new IllegalArgumentException(
          "Cache namespace must use lowercase letters, digits, dots, and hyphens");
    }
    if (version <= 0) {
      throw new IllegalArgumentException("Cache version must be greater than zero");
    }
  }
}
