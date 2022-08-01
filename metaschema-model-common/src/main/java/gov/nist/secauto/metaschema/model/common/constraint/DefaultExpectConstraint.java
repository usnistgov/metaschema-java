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

package gov.nist.secauto.metaschema.model.common.constraint;

import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.item.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.util.ReplacementScanner;

import java.util.Objects;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class DefaultExpectConstraint
    extends AbstractConstraint
    implements IExpectConstraint {
  @SuppressWarnings("null")
  @NonNull
  private static final Pattern METAPATH_VALUE_TEMPLATE_PATTERN
      = Pattern.compile("(?<!\\\\)(\\{\\s*((?:(?:\\\\})|[^}])*)\\s*\\})");
  @NonNull
  private final MetapathExpression test;
  private final String message;

  /**
   * Construct a new expect constraint which requires that the associated test evaluates to
   * {@link IBooleanItem#TRUE} against the target.
   * 
   * @param id
   *          the optional identifier for the constraint
   * @param source
   *          information about the constraint source
   * @param level
   *          the significance of a violation of this constraint
   * @param message
   *          an optional message to emit when the constraint is violated
   * @param target
   *          the Metapath expression identifying the nodes the constraint targets
   * @param test
   *          a Metapath expression that is evaluated against the target node to determine if the
   *          constraint passes
   * @param remarks
   *          optional remarks describing the intent of the constraint
   */
  @SuppressWarnings("null")
  public DefaultExpectConstraint(
      @Nullable String id,
      @NonNull ISource source,
      @NonNull Level level,
      String message,
      @NonNull MetapathExpression target,
      @NonNull MetapathExpression test,
      MarkupMultiline remarks) {
    super(id, source, level, target, remarks);
    this.test = Objects.requireNonNull(test);
    this.message = message;
  }

  @Override
  public MetapathExpression getTest() {
    return test;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public CharSequence generateMessage(@NonNull INodeItem item, @NonNull DynamicContext context) {
    String message = getMessage();

    return message == null ? null
        : ReplacementScanner.replaceTokens(message, METAPATH_VALUE_TEMPLATE_PATTERN, match -> {
          @SuppressWarnings("null")
          @NonNull
          String metapath = match.group(2);
          MetapathExpression expr = MetapathExpression.compile(metapath);
          return expr.evaluateAs(item, MetapathExpression.ResultType.STRING, context);
        });
  }
}
