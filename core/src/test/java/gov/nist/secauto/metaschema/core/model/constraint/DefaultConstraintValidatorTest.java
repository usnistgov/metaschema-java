/*
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.core.model.constraint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.StaticContext;
import gov.nist.secauto.metaschema.core.metapath.format.IPathFormatter;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IStringItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IFlagNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.MockNodeItemFactory;
import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.action.CustomAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

@SuppressWarnings("PMD.TooManyStaticImports")
class DefaultConstraintValidatorTest {
  @RegisterExtension
  Mockery context = new JUnit5Mockery();

  @SuppressWarnings("null")
  @Test
  void testAllowedValuesAllowOther() {
    MockNodeItemFactory itemFactory = new MockNodeItemFactory(context);

    IFlagNodeItem flag = itemFactory.flag("value", IStringItem.valueOf("value"));

    IFlagDefinition flagDefinition = context.mock(IFlagDefinition.class);

    IAllowedValuesConstraint allowedValues = IAllowedValuesConstraint.builder()
        .source(ISource.modelSource())
        .allowedValue(IAllowedValue.of("other", MarkupLine.fromMarkdown("some documentation")))
        .allowedOther(true)
        .build();

    context.checking(new Expectations() {
      { // NOPMD - intentional
        allowing(flag).getDefinition();
        will(returnValue(flagDefinition));
        allowing(flag).accept(with(any(DefaultConstraintValidator.Visitor.class)), with(aNull(Void.class)));
        will(new FlagVisitorAction());
        allowing(flag).toPath(with(any(IPathFormatter.class)));
        will(returnValue("flag/path"));

        allowing(flagDefinition).getAllowedValuesConstraints();
        will(returnValue(CollectionUtil.singletonList(allowedValues)));
        allowing(flagDefinition).getExpectConstraints();
        will(returnValue(CollectionUtil.emptyList()));
        allowing(flagDefinition).getMatchesConstraints();
        will(returnValue(CollectionUtil.emptyList()));
        allowing(flagDefinition).getIndexHasKeyConstraints();
        will(returnValue(CollectionUtil.emptyList()));
      }
    });

    DynamicContext dynamicContext = StaticContext.builder()
        .build().newDynamicContext();
    FindingCollectingConstraintValidationHandler handler = new FindingCollectingConstraintValidationHandler();
    DefaultConstraintValidator validator = new DefaultConstraintValidator(dynamicContext, handler);
    validator.validate(flag);
    validator.finalizeValidation();

    assertTrue(handler.isPassing(), "doesn't pass");
  }

  @SuppressWarnings("null")
  @Test
  void testAllowedValuesMultipleAllowOther() {
    MockNodeItemFactory itemFactory = new MockNodeItemFactory(context);

    IFlagNodeItem flag = itemFactory.flag("value", IStringItem.valueOf("value"));

    IFlagDefinition flagDefinition = context.mock(IFlagDefinition.class);

    IAllowedValuesConstraint allowedValues1 = IAllowedValuesConstraint.builder()
        .source(ISource.modelSource())
        .allowedValue(IAllowedValue.of("other", MarkupLine.fromMarkdown("some documentation")))
        .allowedOther(true)
        .build();
    IAllowedValuesConstraint allowedValues2 = IAllowedValuesConstraint.builder()
        .source(ISource.modelSource())
        .allowedValue(IAllowedValue.of("other2", MarkupLine.fromMarkdown("some documentation")))
        .allowedOther(true)
        .build();

    List<? extends IAllowedValuesConstraint> allowedValuesConstraints
        = List.of(allowedValues1, allowedValues2);

    context.checking(new Expectations() {
      { // NOPMD - intentional
        allowing(flag).getDefinition();
        will(returnValue(flagDefinition));
        allowing(flag).accept(with(any(DefaultConstraintValidator.Visitor.class)), with(aNull(Void.class)));
        will(new FlagVisitorAction());
        allowing(flag).toPath(with(any(IPathFormatter.class)));
        will(returnValue("flag/path"));

        allowing(flagDefinition).getAllowedValuesConstraints();
        will(returnValue(allowedValuesConstraints));
        allowing(flagDefinition).getExpectConstraints();
        will(returnValue(CollectionUtil.emptyList()));
        allowing(flagDefinition).getMatchesConstraints();
        will(returnValue(CollectionUtil.emptyList()));
        allowing(flagDefinition).getIndexHasKeyConstraints();
        will(returnValue(CollectionUtil.emptyList()));
      }
    });

    DynamicContext dynamicContext = StaticContext.builder()
        .build().newDynamicContext();
    FindingCollectingConstraintValidationHandler handler = new FindingCollectingConstraintValidationHandler();
    DefaultConstraintValidator validator = new DefaultConstraintValidator(dynamicContext, handler);
    validator.validate(flag);
    validator.finalizeValidation();

    assertTrue(handler.isPassing(), "doesn't pass");
  }

  @SuppressWarnings("null")
  @Test
  void testMultipleAllowedValuesConflictingAllowOther() {
    MockNodeItemFactory itemFactory = new MockNodeItemFactory(context);

    IFlagNodeItem flag1 = itemFactory.flag("value", IStringItem.valueOf("value"));
    IFlagNodeItem flag2 = itemFactory.flag("other2", IStringItem.valueOf("other2"));

    IFlagDefinition flagDefinition = context.mock(IFlagDefinition.class);

    IAllowedValuesConstraint allowedValues1 = IAllowedValuesConstraint.builder()
        .source(ISource.modelSource())
        .allowedValue(IAllowedValue.of("other", MarkupLine.fromMarkdown("some documentation")))
        .allowedOther(true)
        .build();
    IAllowedValuesConstraint allowedValues2 = IAllowedValuesConstraint.builder()
        .source(ISource.modelSource())
        .allowedValue(IAllowedValue.of("other2", MarkupLine.fromMarkdown("some documentation")))
        .allowedOther(false)
        .build();

    List<? extends IAllowedValuesConstraint> allowedValuesConstraints
        = List.of(allowedValues1, allowedValues2);

    context.checking(new Expectations() {
      { // NOPMD - intentional
        allowing(flag1).getDefinition();
        will(returnValue(flagDefinition));
        allowing(flag1).accept(with(any(DefaultConstraintValidator.Visitor.class)), with(aNull(Void.class)));
        will(new FlagVisitorAction());
        allowing(flag1).toPath(with(any(IPathFormatter.class)));
        will(returnValue("flag1/path"));

        allowing(flag2).getDefinition();
        will(returnValue(flagDefinition));
        allowing(flag2).accept(with(any(DefaultConstraintValidator.Visitor.class)), with(aNull(Void.class)));
        will(new FlagVisitorAction());
        allowing(flag2).toPath(with(any(IPathFormatter.class)));
        will(returnValue("flag2/path"));

        allowing(flagDefinition).getAllowedValuesConstraints();
        will(returnValue(allowedValuesConstraints));
        allowing(flagDefinition).getExpectConstraints();
        will(returnValue(CollectionUtil.emptyList()));
        allowing(flagDefinition).getMatchesConstraints();
        will(returnValue(CollectionUtil.emptyList()));
        allowing(flagDefinition).getIndexHasKeyConstraints();
        will(returnValue(CollectionUtil.emptyList()));
      }
    });

    DynamicContext dynamicContext = StaticContext.builder()
        .build().newDynamicContext();
    FindingCollectingConstraintValidationHandler handler = new FindingCollectingConstraintValidationHandler();
    DefaultConstraintValidator validator = new DefaultConstraintValidator(dynamicContext, handler);
    validator.validate(flag1);
    validator.validate(flag2);
    validator.finalizeValidation();
    assertAll(
        () -> assertFalse(handler.isPassing(), "must pass"),
        () -> assertThat("only 1 finding", handler.getFindings(), hasSize(1)),
        () -> assertThat("finding is for a flag node", handler.getFindings(), hasItem(hasProperty("node", is(flag1)))));
  }

  private static class FlagVisitorAction
      extends CustomAction {

    public FlagVisitorAction() {
      super("return the flag");
    }

    @Override
    public Object invoke(Invocation invocation) {
      IFlagNodeItem thisFlag = (IFlagNodeItem) invocation.getInvokedObject();
      assert thisFlag != null;
      DefaultConstraintValidator.Visitor visitor = (DefaultConstraintValidator.Visitor) invocation.getParameter(0);
      return visitor.visitFlag(thisFlag, null);
    }
  }
}
