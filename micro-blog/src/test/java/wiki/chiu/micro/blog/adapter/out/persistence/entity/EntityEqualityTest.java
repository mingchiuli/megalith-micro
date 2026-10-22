package wiki.chiu.micro.blog.adapter.out.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class EntityEqualityTest {

    @Test
    void persistedEntitiesUseDatabaseIdentity() {
        BlogEntity first = entity(7L);
        BlogEntity second = entity(7L);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void transientEntitiesAreOnlyEqualToThemselvesAndHashIsStableAcrossMutation() {
        BlogEntity first = entity(null);
        BlogEntity second = entity(null);
        int hash = first.hashCode();

        first.setAccessMode(1);

        assertNotEquals(first, second);
        assertEquals(hash, first.hashCode());
    }

    private BlogEntity entity(Long id) {
        return new BlogEntity(id, 42L, null, 0L, null, null, null, 0, 0L, 0L);
    }
}
