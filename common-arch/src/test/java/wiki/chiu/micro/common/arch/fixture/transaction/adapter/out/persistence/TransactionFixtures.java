package wiki.chiu.micro.common.arch.fixture.transaction.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

public final class TransactionFixtures {

    private TransactionFixtures() {
    }

    public interface Store extends Repository<Object, Long> {
        Object findById(Long id);
        void save(Object value);

        @Query("SELECT item FROM Item item WHERE item.active = true")
        Object active();

        @Modifying
        @Query("UPDATE Item item SET item.active = false")
        void deactivate();

        default Object current() {
            return findById(1L);
        }
    }

    public static class AuthoringAdapter {
        private Store store;

        @Transactional
        public void persist() {
            store.findById(1L);
        }
    }

    @Transactional
    public static class ClassTransaction {
        private Store store;

        public void persist() {
            store.findById(1L);
        }
    }

    public static class InheritedTransaction extends ClassTransaction {
    }

    public static class DefaultRepositoryTransaction {
        private Store store;

        @Transactional
        public void persist() {
            store.current();
        }
    }

    public interface DefaultWriter {
        Store store();

        @Transactional
        default void persist() {
            store().findById(1L);
        }
    }

    public static class InterfaceTransaction implements DefaultWriter {
        private Store store;

        @Override
        public Store store() {
            return store;
        }
    }

    public static class HelperTransaction {
        private Store store;

        @Transactional
        public void persist() {
            load();
        }

        private Object load() {
            return store.findById(1L);
        }
    }

    public static class QueryTransaction {
        private Store store;

        @Transactional
        public void persist() {
            store.active();
        }
    }

    public static class ReferenceTransaction {
        private Store store;

        @Transactional
        public List<Object> persist(List<Long> ids) {
            return ids.stream().map(store::findById).toList();
        }
    }

    public static class ConstructorTransaction {
        private Store store;

        @Transactional
        public void persist() {
            new Lookup(store);
        }
    }

    public static class Lookup {
        public Lookup(Store store) {
            store.findById(1L);
        }
    }

    @Transactional(readOnly = true)
    public static class Reader {
        private Store store;

        public Object load() {
            return store.findById(1L);
        }
    }

    @Transactional(readOnly = true)
    public static class WriteOverride extends Reader {
        private Store store;

        @Transactional
        public void persist() {
            store.findById(1L);
        }
    }

    @Transactional
    public static class ReadOverride {
        private Store store;

        @Transactional(readOnly = true)
        public Object load() {
            return store.findById(1L);
        }

        public void persist() {
            store.save(new Object());
            store.deactivate();
        }
    }

    public static class MixedAdapter {
        private Store store;

        public Object load() {
            return store.findById(1L);
        }

        @Transactional
        public void persist() {
            store.save(new Object());
        }
    }
}
