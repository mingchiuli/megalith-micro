package wiki.chiu.micro.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import org.junit.jupiter.api.Test;

class ContractPackageStructureTest {

    private static final JavaClasses CLASSES =
        new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("wiki.chiu.micro.common");

    @Test
    void legacyLangPackageIsGone() {
        assertThat(CLASSES.stream().map(JavaClass::getPackageName))
            .noneMatch(name -> name.startsWith("wiki.chiu.micro.common.lang"));
    }

    @Test
    void contractKindsLiveInDedicatedPackages() {
        assertThat(CLASSES.stream().map(JavaClass::getName).toList())
            .contains(
                "wiki.chiu.micro.common.result.Result",
                "wiki.chiu.micro.common.error.ErrorCode",
                "wiki.chiu.micro.common.error.ExceptionMessage",
                "wiki.chiu.micro.common.message.AuthCacheEvictMessage",
                "wiki.chiu.micro.common.message.BlogChangedMessage",
                "wiki.chiu.micro.common.message.UserDeletedMessage",
                "wiki.chiu.micro.common.model.BlogSnapshot",
                "wiki.chiu.micro.common.enums.DataPermissionEnum",
                "wiki.chiu.micro.common.enums.StatusEnum",
                "wiki.chiu.micro.common.constant.Const");
    }
}
