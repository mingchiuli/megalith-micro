package wiki.chiu.micro.common.arch;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import wiki.chiu.micro.common.arch.fixture.message.application.model.MessageCommand;
import wiki.chiu.micro.common.arch.fixture.snapshot.domain.SnapshotDomain;
import wiki.chiu.micro.common.arch.fixture.value.domain.SharedValue;

class ApplicationLayersTransportBoundaryTest {

    @Test
    void rejectsQueueEnvelopesInApplicationModels() {
        var classes = new ClassFileImporter().importClasses(MessageCommand.class);
        assertThatThrownBy(() -> ApplicationLayers.verify(classes, "wiki.chiu.micro.common.arch.fixture.message"))
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("wiki.chiu.micro.common.message");
    }

    @Test
    void rejectsTransportSnapshotsInDomainModels() {
        var classes = new ClassFileImporter().importClasses(SnapshotDomain.class);
        assertThatThrownBy(() -> ApplicationLayers.verify(classes, "wiki.chiu.micro.common.arch.fixture.snapshot"))
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("shared transport snapshots");
    }

    @Test
    void retainsSharedBusinessEnumsAndValueObjects() {
        var classes = new ClassFileImporter().importClasses(SharedValue.class);
        ApplicationLayers.verify(classes, "wiki.chiu.micro.common.arch.fixture.value");
    }
}
