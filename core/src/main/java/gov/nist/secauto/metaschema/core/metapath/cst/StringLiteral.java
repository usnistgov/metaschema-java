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

package gov.nist.secauto.metaschema.core.metapath.cst;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IStringItem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * An implementation of the
 * <a href="https://www.w3.org/TR/xpath-31/#id-literals">String Literal
 * Expression</a> supporting the creation of a Metapath constant literal
 * {@link IStringItem}.
 */
public class StringLiteral
    extends AbstractLiteralExpression<IStringItem, String> {

  private static final Pattern QUOTE_PATTERN = Pattern.compile("^'(.*)'$|^\"(.*)\"$");

  /**
   * Construct a new expression that always returns the same string value.
   *
   * @param value
   *          the literal value
   */
  public StringLiteral(@NonNull String value) {
    super(removeQuotes(value));
  }

  @Override
  public Class<IStringItem> getBaseResultType() {
    return IStringItem.class;
  }

  @SuppressWarnings("null")
  @NonNull
  private static String removeQuotes(@NonNull String value) {
    Matcher matcher = QUOTE_PATTERN.matcher(value);

    String retval = value;
    if (matcher.matches()) {
      if (matcher.group(1) != null) {
        retval = matcher.group(1);
      } else if (matcher.group(2) != null) {
        retval = matcher.group(2);
      }
    }
    return retval;
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitStringLiteral(this, context);
  }

  @Override
  public ISequence<? extends IStringItem> accept(DynamicContext dynamicContext, ISequence<?> focus) {
    return ISequence.of(IStringItem.valueOf(getValue()));
  }
}
