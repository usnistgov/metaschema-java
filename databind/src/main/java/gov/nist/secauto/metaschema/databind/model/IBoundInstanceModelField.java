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

package gov.nist.secauto.metaschema.databind.model;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.model.IFieldInstance;
import gov.nist.secauto.metaschema.core.model.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.model.impl.DefinitionField;
import gov.nist.secauto.metaschema.databind.model.impl.InstanceModelFieldComplex;
import gov.nist.secauto.metaschema.databind.model.impl.InstanceModelFieldScalar;

import java.lang.reflect.Field;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IBoundInstanceModelField extends IBoundInstanceModelNamed, IFieldInstance {

  @Override
  IBoundDefinitionField getDefinition();

  /**
   * Create a new bound field instance.
   *
   * @param field
   *          the Java field the instance is bound to
   * @param containingDefinition
   *          the definition containing the instance
   * @return the new instance
   */
  @NonNull
  static IBoundInstanceModelField newInstance(
      @NonNull Field field,
      @NonNull IBoundDefinitionAssembly containingDefinition) {
    Class<?> itemType = IBoundInstanceModel.getItemType(field);
    IBindingContext bindingContext = containingDefinition.getBindingContext();
    IBoundDefinitionModel definition = bindingContext.getBoundDefinitionForClass(itemType);

    IBoundInstanceModelField retval;
    if (definition == null) {
      retval = new InstanceModelFieldScalar(field, containingDefinition);
    } else if (definition instanceof DefinitionField) {
      retval = new InstanceModelFieldComplex(field, (DefinitionField) definition, containingDefinition);
    } else {
      throw new IllegalStateException(String.format(
          "The field '%s' on class '%s' is not bound to a Metaschema field",
          field.toString(),
          field.getDeclaringClass().getName()));
    }
    return retval;
  }

  @Override
  default boolean canHandleXmlQName(QName qname) {
    boolean retval;
    if (XmlGroupAsBehavior.GROUPED.equals(getXmlGroupAsBehavior())) {
      retval = qname.equals(getXmlGroupAsQName());
    } else if (isValueWrappedInXml()) {
      retval = qname.equals(getXmlQName());
    } else {
      IDataTypeAdapter<?> adapter = getDefinition().getJavaTypeAdapter();
      // we are to parse the data type
      retval = adapter.canHandleQName(qname);
    }
    return retval;
  }
}
