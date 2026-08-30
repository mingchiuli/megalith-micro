package wiki.chiu.micro.blog.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class EntityEqualityTest {

  @Test
  void persistedEntitiesUseDatabaseIdentity() {
    BlogEntity first = BlogEntity.builder().id(7L).title("before").build();
    BlogEntity second = BlogEntity.builder().id(7L).title("different").build();

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void transientEntitiesAreOnlyEqualToThemselvesAndHashIsStableAcrossMutation() {
    BlogEntity first = BlogEntity.builder().title("before").build();
    BlogEntity second = BlogEntity.builder().title("before").build();
    int hash = first.hashCode();

    first.setTitle("after");

    assertNotEquals(first, second);
    assertEquals(hash, first.hashCode());
  }
}
