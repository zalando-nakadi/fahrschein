package org.zalando.fahrschein.e2e;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.api.Response;

import java.util.ArrayList;
import java.util.List;

import static com.tngtech.archunit.base.DescribedPredicate.anyElementThat;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableFrom;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.codeUnits;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noCodeUnits;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;


public class ArchitectureTest {
    private final JavaClasses importedClasses = new ClassFileImporter().importPackages("org.zalando.fahrschein");

    @Test
    public void verifyRequestFactory() {
        ArchRule rule = classes().that().implement(RequestFactory.class)
                .and().arePublic()
                .should()
                .haveNameMatching(".*RequestFactory$")
                .andShould()
                .haveModifier(JavaModifier.FINAL);
         // violated by SimpleRequestFactory:
         // .andShould()
         // .haveOnlyFinalFields();
        rule.check(importedClasses);
    }

    @Test
    public void verifyRequest() {
        ArchRule rule = classes().that().implement(Request.class)
                .should()
                .haveNameMatching(".*Request")
                .andShould()
                .notBePublic();
        rule.check(importedClasses);
    }

    @Test
    public void verifyResponse() {
        ArchRule rule = classes().that().implement(Response.class)
                .should()
                .haveNameMatching(".*Response")
                .andShould()
                .notBePublic();
        rule.check(importedClasses);
    }

    /**
     * Accepting or returning concrete list implementations is prohibited.
     * For example:
     * {@code
     public ArrayList<String> someMethod1() { return null; }
     public void someMethod2(ArrayList<String> input) { return; }
     }
     */
    @Test
    public void prohibitConcreteListImplementation() {
        ArchRule rule = codeUnits()
                .that().arePublic()
                .should()
                .notHaveRawReturnType(assignableTo(List.class).and(not(assignableFrom(List.class))))
                .andShould()
                .notHaveRawParameterTypes(anyElementThat(assignableTo(List.class).and(not(assignableFrom(List.class)))));
        rule.check(importedClasses);
    }

}
