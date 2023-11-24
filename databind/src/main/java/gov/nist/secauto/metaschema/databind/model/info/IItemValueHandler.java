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

package gov.nist.secauto.metaschema.databind.model.info;

import gov.nist.secauto.metaschema.databind.io.BindingException;

import java.io.IOException;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IItemValueHandler {
  /**
   * Parse and return an item.
   *
   * @param parent
   *          the parent Java object to use for serialization callbacks, or
   *          {@code null} if there is no parent
   * @param handler
   *          the item parsing handler
   * @return the Java object representing the parsed item
   * @throws IOException
   *           if an error occurred while parsing
   */
  Object readItem(Object parent, IItemReadHandler handler)
      throws IOException;

  /**
   * Write the provided item.
   *
   * @param item
   *          the data to write
   * @param handler
   *          the item writing handler
   * @throws IOException
   *           if an error occurred while writing
   */
  void writeItem(
      @NonNull Object item,
      @NonNull IItemWriteHandler handler) throws IOException;

  /**
   * Create and return a deep copy of the provided item.
   *
   * @param item
   *          the item to copy
   * @param parentInstance
   *          an optional parent object to use for serialization callbacks
   * @return the new deep copy
   * @throws BindingException
   *           if an error occurred while analyzing the bound objects
   */
  @NonNull
  Object deepCopyItem(@NonNull Object item, @Nullable Object parentInstance) throws BindingException;
}