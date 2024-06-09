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

import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModel;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelNamed;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractModelInstanceReadHandler implements IModelInstanceReadHandler {
  @NonNull
  private final IBoundInstanceModel instance;
  @NonNull
  private final Object parentObject;

  protected AbstractModelInstanceReadHandler(
      @NonNull IBoundInstanceModel instance,
      @NonNull Object parentObject) {
    this.instance = instance;
    this.parentObject = parentObject;
  }

  /**
   * Get the model instance associated with this handler.
   *
   * @return the collection information
   */
  @NonNull
  public IBoundInstanceModel getInstance() {
    return instance;
  }

  /**
   * Get the collection Java type information associated with this handler.
   *
   * @return the collection information
   */
  @NonNull
  public IModelInstanceCollectionInfo getCollectionInfo() {
    return getInstance().getCollectionInfo();
  }

  /**
   * Get the object onto which parsed data will be stored.
   *
   * @return the parentObject
   */
  @NonNull
  public Object getParentObject() {
    return parentObject;
  }

  @Override
  public String getJsonKeyFlagName() {
    IBoundInstanceModel instance = getInstance();
    String retval = null;
    if (instance instanceof IBoundInstanceModelNamed) {
      IBoundInstanceFlag jsonKey = ((IBoundInstanceModelNamed) instance).getEffectiveJsonKey();
      if (jsonKey != null) {
        retval = jsonKey.getEffectiveName();
      }
    }
    return retval;
  }
}
