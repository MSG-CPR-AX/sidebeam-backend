package com.sidebeam.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private JavaClasses imported() {
        return new ClassFileImporter().importPackages("com.sidebeam");
    }

    @Test
    void domain_should_not_depend_on_controller_or_external() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage("com.sidebeam.bookmark.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.sidebeam.bookmark.controller..",
                        "com.sidebeam.external.."
                );
        rule.check(imported());
    }

    @Test
    void services_should_not_depend_on_controllers() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage("com.sidebeam.bookmark.service..")
                .should().dependOnClassesThat().resideInAnyPackage("com.sidebeam.bookmark.controller..");
        rule.check(imported());
    }

    @Test
    void external_should_not_depend_on_controllers() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage("com.sidebeam.external..")
                .should().dependOnClassesThat().resideInAnyPackage("com.sidebeam.bookmark.controller..");
        rule.check(imported());
    }
}
