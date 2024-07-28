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

import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelFieldComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldValue;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelFieldComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelFieldScalar;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedField;

import java.io.IOException;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IItemWriteHandler {
  /**
   * Write an item.
   *
   * @param item
   *          the Java object representing the item to write
   * @param instance
   *          the flag instance
   * @throws IOException
   *           if an error occurred while parsing
   */
  void writeItemFlag(
      @NonNull Object item,
      @NonNull IBoundInstanceFlag instance) throws IOException;

  /**
   * Write an item.
   *
   * @param item
   *          the Java object representing the item to write
   * @param instance
   *          the field instance
   * @throws IOException
   *           if an error occurred while parsing
   */
  void writeItemField(
      @NonNull Object item,
      @NonNull IBoundInstanceModelFieldScalar instance) throws IOException;

  /**
   * Write an item.
   *
   * @param item
   *          the Java object representing the item to write
   * @param instance
   *          the field instance
   * @throws IOException
   *           if an error occurred while parsing
   */
  void writeItemField(
      @NonNull IBoundObject item,
      @NonNull IBoundInstanceModelFieldComplex instance) throws IOException;

  /**
   * Write an item.
   *
   * @param item
   *          the Java object representing the item to write
   * @param instance
   *          the field instance
   * @throws IOException
   *           if an error occurred while parsing
   */
  void writeItemField(
      @NonNull IBoundObject item,
      @NonNull IBoundInstanceModelGroupedField instance) throws IOException;

  /**
   * Write an item.
   *
   * @param item
   *          the Java object representing the item to write
   * @param definition
   *          the field instance
   * @throws IOException
   *           if an error occurred while parsing
   */
  void writeItemField(
      @NonNull IBoundObject item,
      @NonNull IBoundDefinitionModelFieldComplex definition) throws IOException;

  /**
   * Write an item.
   *
   * @param item
   *          the Java object representing the item to write
   * @param fieldValue
   *          the field value instance
   * @throws IOException
   *           if an error occurred while parsing
   */
  void writeItemFieldValue(
      @NonNull Object item,
      @NonNull IBoundFieldValue fieldValue) throws IOException;

  /**
   * Write an item.
   *
   * @param item
   *          the Java object representing the item to write
   * @param instance
   *          the assembly instance
   * @throws IOException
   *           if an error occurred while parsing
   */
  void writeItemAssembly(
      @NonNull IBoundObject item,
      @NonNull IBoundInstanceModelAssembly instance) throws IOException;

  /**
   * Write an item.
   *
   * @param item
   *          the Java object representing the item to write
   * @param instance
   *          the assembly instance
   * @throws IOException
   *           if an error occurred while parsing
   */
  void writeItemAssembly(
      @NonNull IBoundObject item,
      @NonNull IBoundInstanceModelGroupedAssembly instance) throws IOException;

  /**
   * Write an item.
   *
   * @param item
   *          the Java object representing the item to write
   * @param definition
   *          the assembly instance
   * @throws IOException
   *           if an error occurred while parsing
   */
  void writeItemAssembly(
      @NonNull IBoundObject item,
      @NonNull IBoundDefinitionModelAssembly definition) throws IOException;

  /**
   * Write an item.
   *
   * @param item
   *          the Java object representing the item to write
   * @param instance
   *          the choice group instance
   * @throws IOException
   *           if an error occurred while parsing
   */
  void writeChoiceGroupItem(
      @NonNull IBoundObject item,
      @NonNull IBoundInstanceModelChoiceGroup instance) throws IOException;

}
