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

import gov.nist.secauto.metaschema.core.metapath.MetapathException;
import gov.nist.secauto.metaschema.core.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.core.model.constraint.impl.DefaultLet;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

@SuppressWarnings("PMD.ShortClassName")
public interface ILet {
  /**
   * Create a new Let expression by compiling the provided Metapath expression
   * string.
   *
   * @param name
   *          the let expression variable name
   * @param valueExpression
   *          a Metapath expression string representing the variable value
   * @param source
   *          the source descriptor for the resource containing the constraint
   * @return the original let statement with the same name or {@code null}
   */
  @SuppressWarnings("PMD.ShortMethodName")
  @NonNull
  static ILet of(
      @NonNull QName name,
      @NonNull String valueExpression,
      @NonNull ISource source) {
    try {
      return of(name, MetapathExpression.compile(valueExpression), source);
    } catch (MetapathException ex) {
      throw new MetapathException(
          String.format("Unable to compile the let expression '%s=%s'%s. %s",
              name,
              valueExpression,
              source.getSource() == null ? "" : " in " + source.getSource(),
              ex.getMessage()),
          ex);
    }
  }

  /**
   * Create a new Let expression.
   *
   * @param name
   *          the let expression variable name
   * @param valueExpression
   *          a Metapath expression representing the variable value
   * @param source
   *          the source descriptor for the resource containing the constraint
   * @return the original let statement with the same name or {@code null}
   */
  @SuppressWarnings("PMD.ShortMethodName")
  @NonNull
  static ILet of(
      @NonNull QName name,
      @NonNull MetapathExpression valueExpression,
      @NonNull ISource source) {
    return new DefaultLet(name, valueExpression, source);
  }

  /**
   * Get the name of the let variable.
   *
   * @return the name
   */
  @NonNull
  QName getName();

  /**
   * Get the Metapath expression to use to query the value.
   *
   * @return the Metapath expression to use to query the value
   */
  @NonNull
  MetapathExpression getValueExpression();

  /**
   * Information about the source resource containing the let statement.
   *
   * @return the source information
   */
  @NonNull
  ISource getSource();
}
