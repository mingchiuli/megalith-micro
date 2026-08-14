package wiki.chiu.micro.outbox;

import wiki.chiu.micro.outbox.config.OutboxProperties;

final class OutboxLockNames {

  private OutboxLockNames() {}

  static String publisher(OutboxProperties properties) {
    return "megalith:"
        + properties.getEnvironment()
        + ":outbox:publisher:"
        + properties.getProducer().name();
  }
}
