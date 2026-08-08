package wiki.chiu.micro.user.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class EntityEqualityTest {

  @Test
  void persistedEntitiesUseDatabaseIdentityRegardlessOfMutableFields() {
    AuthorityEntity first = AuthorityEntity.builder().id(9L).code("read").type(1).build();
    AuthorityEntity second = AuthorityEntity.builder().id(9L).code("write").type(2).build();

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void transientEntitiesAreOnlyEqualToThemselvesAndHashIsStableAcrossMutation() {
    UserEntity first = UserEntity.builder().username("before").build();
    UserEntity second = UserEntity.builder().username("before").build();
    int hash = first.hashCode();

    first.setUsername("after");

    assertNotEquals(first, second);
    assertEquals(hash, first.hashCode());
  }
}
