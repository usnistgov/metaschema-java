
package gov.nist.secauto.metaschema.model.common.metapath.item.node;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

import gov.nist.secauto.metaschema.core.testing.MockedModelTestSupport;
import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldInstance;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

class DefaultNodeItemFactoryTest
    extends MockedModelTestSupport {
  @Test
  void testGenerateFlags() {
    DefaultNodeItemFactory nodeFactory = DefaultNodeItemFactory.instance();

    IAssemblyDefinition parent = assembly()
        .name("assembly1")
        .toDefinition();

    IFieldInstance fieldInstance = field()
        .name("field1")
        .flags(List.of(
            flag().name("flag1")))
        .toInstance(parent);

    Object fieldValue = "test value";

    // setup the value calls
    getContext().checking(new Expectations() {
      { // NOPMD - intentional
        allowing(fieldInstance.getDefinition().getFlagInstanceByName("flag1")).getValue(fieldValue);
        will(returnValue("flag1 value"));
      }
    });

    IAssemblyNodeItem parentItem = INodeItemFactory.instance().newAssemblyNodeItem(parent);
    IFieldNodeItem field = new FieldInstanceNodeItemImpl(fieldInstance, parentItem, 2, fieldValue, nodeFactory);

    Collection<? extends IFlagNodeItem> flagItems = field.getFlags();
    assertThat(flagItems, containsInAnyOrder(
        allOf(
            match("name", flag -> flag.getName(), equalTo("flag1")),
            match("value", flag -> flag.getValue(), equalTo("flag1 value"))))); // NOPMD
  }

  @Test
  void testGenerateModelItems() {
    DefaultNodeItemFactory nodeFactory = DefaultNodeItemFactory.instance();

    IAssemblyDefinition assembly = assembly()
        .name("assembly1")
        .flags(List.of(
            flag().name("flag1")))
        .modelInstances(List.of(
            field().name("field1")))
        .toDefinition();

    Object assemblyValue = "assembly value";
    Object flagValue = "flag1 value";
    Object fieldValue = "field1 value";

    // Setup the value calls
    getContext().checking(new Expectations() {
      { // NOPMD - intentional
        allowing(assembly.getFlagInstanceByName("flag1")).getValue(assemblyValue);
        will(returnValue(flagValue));
        allowing(assembly.getModelInstanceByName("field1")).getValue(assemblyValue);
        will(returnValue(fieldValue));
        allowing(assembly.getModelInstanceByName("field1")).getItemValues(fieldValue);
        will(returnValue(List.of(fieldValue)));
      }
    });

    IAssemblyNodeItem parentItem = INodeItemFactory.instance().newAssemblyNodeItem(assembly, null, assemblyValue);

    Collection<? extends IFlagNodeItem> flagItems = parentItem.getFlags();
    Collection<? extends IModelNodeItem<?, ?>> modelItems = parentItem.modelItems()
        .collect(Collectors.toUnmodifiableList());
    assertAll(
        () -> assertThat(flagItems, containsInAnyOrder(
            allOf(
                match("name", flag -> flag.getName(), equalTo("flag1")),
                match("value", flag -> flag.getValue(), equalTo("flag1 value"))))),
        () -> assertThat(modelItems, containsInAnyOrder(
            allOf(
                match("name", model -> model.getName(), equalTo("field1")),
                match("value", model -> model.getValue(), equalTo("field1 value"))))));
  }

  private static <T, R> FeatureMatcher<T, R> match(
      @NonNull String label,
      @NonNull Function<T, R> lambda,
      Matcher<R> matcher) {
    return new FeatureMatcher<T, R>(matcher, label, label) {
      @Override
      protected R featureValueOf(T actual) {
        return lambda.apply(actual);
      }
    };
  }
}
