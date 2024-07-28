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

package gov.nist.secauto.metaschema.core.model;

import gov.nist.secauto.metaschema.core.model.impl.EmptyFlagContainer;

import java.util.Map;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IContainerFlagSupport<FI extends IFlagInstance> {
  /**
   * Provides an empty instance.
   *
   * @param <T>
   *          the Java type of the flag instances
   * @return an empty instance
   */
  @SuppressWarnings("unchecked")
  @NonNull
  static <T extends IFlagInstance> IContainerFlagSupport<T> empty() {
    return (IContainerFlagSupport<T>) EmptyFlagContainer.EMPTY;
  }

  /**
   * Create a new flag container without a JSON key.
   *
   * @param <T>
   *          the Java type of the flag instances
   * @return the flag container
   */
  @NonNull
  static <T extends IFlagInstance> IFlagContainerBuilder<T> builder() {
    return new FlagContainerBuilder<>(null);
  }

  /**
   * Create a new flag container using the provided flag qualified name as the
   * JSON key.
   *
   * @param <T>
   *          the Java type of the flag instances
   * @param jsonKey
   *          the qualified name of the JSON key
   * @return the flag container
   */
  @NonNull
  static <T extends IFlagInstance> IFlagContainerBuilder<T> builder(@NonNull QName jsonKey) {
    return new FlagContainerBuilder<>(jsonKey);
  }

  /**
   * Get a mapping of flag effective name to flag instance.
   *
   * @return the mapping of flag effective name to flag instance
   */
  @NonNull
  Map<QName, FI> getFlagInstanceMap();

  /**
   * Get the JSON key flag instance.
   *
   * @return the flag instance or {@code null} if no JSON key is configured
   */
  @Nullable
  FI getJsonKeyFlagInstance();
}
