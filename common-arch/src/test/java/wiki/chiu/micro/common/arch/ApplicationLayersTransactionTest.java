package wiki.chiu.micro.common.arch;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tngtech.archunit.core.importer.ClassFileImporter;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.common.arch.fixture.transaction.adapter.out.persistence.TransactionFixtures;

class ApplicationLayersTransactionTest {

    private static final String ROOT = "wiki.chiu.micro.common.arch.fixture.transaction";

    @Test
    void checksAdaptersWhoseNamesDoNotEndInWrapper() {
        rejects(TransactionFixtures.AuthoringAdapter.class, "findById");
    }

    @Test
    void checksClassLevelTransactions() {
        rejects(TransactionFixtures.ClassTransaction.class, "findById");
    }

    @Test
    void checksInheritedClassTransactions() {
        rejects(TransactionFixtures.InheritedTransaction.class, "findById", TransactionFixtures.ClassTransaction.class);
    }

    @Test
    void followsDefaultRepositoryHelpers() {
        rejects(TransactionFixtures.DefaultRepositoryTransaction.class, "findById", TransactionFixtures.Store.class);
    }

    @Test
    void checksInheritedTransactionalDefaultMethods() {
        rejects(TransactionFixtures.InterfaceTransaction.class, "findById", TransactionFixtures.DefaultWriter.class);
    }

    @Test
    void followsPrivateHelpers() {
        rejects(TransactionFixtures.HelperTransaction.class, "findById");
    }

    @Test
    void recognizesQueriesWithoutAReadPrefix() {
        rejects(TransactionFixtures.QueryTransaction.class, "active");
    }

    @Test
    void checksRepositoryMethodReferences() {
        rejects(TransactionFixtures.ReferenceTransaction.class, "findById");
    }

    @Test
    void followsPersistenceHelperConstructors() {
        rejects(TransactionFixtures.ConstructorTransaction.class, "findById", TransactionFixtures.Lookup.class);
    }

    @Test
    void writeMethodOverridesTheReadOnlyClassDefault() {
        rejects(TransactionFixtures.WriteOverride.class, "findById");
    }

    @Test
    void allowsReadOnlyTransactionsAndNonTransactionalReaders() {
        var classes = new ClassFileImporter().importClasses(
            TransactionFixtures.Reader.class, TransactionFixtures.ReadOverride.class, TransactionFixtures.MixedAdapter.class);
        ApplicationLayers.verify(classes, ROOT);
    }

    private void rejects(Class<?> fixture, String readMethod, Class<?>... helpers) {
        var types = Stream.concat(Stream.of(fixture), Arrays.stream(helpers)).toArray(Class<?>[]::new);
        var classes = new ClassFileImporter().importClasses(types);
        assertThatThrownBy(() -> ApplicationLayers.verify(classes, ROOT))
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("Transactional persistence writers must not read repositories")
            .hasMessageContaining(readMethod);
    }
}
