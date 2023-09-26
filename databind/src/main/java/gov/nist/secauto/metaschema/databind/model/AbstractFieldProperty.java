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

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundField;

import org.codehaus.stax2.XMLStreamWriter2;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Locale;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractFieldProperty
    extends AbstractNamedModelProperty
    implements IBoundFieldInstance {
  @NonNull
  private final BoundField fieldAnnotation;

  public AbstractFieldProperty(@NonNull Field field, @NonNull IAssemblyClassBinding parentClassBinding) {
    super(field, parentClassBinding);

    BoundField fieldAnnotation = field.getAnnotation(BoundField.class);
    if (fieldAnnotation == null) {
      throw new IllegalStateException(String.format("Field '%s' on class '%s' is missing the '%s' annotation.",
          field.getName(), parentClassBinding.getBoundClass().getName(), BoundField.class.getName()));
    }
    this.fieldAnnotation = fieldAnnotation;
  }

  @NonNull
  protected BoundField getFieldAnnotation() {
    return fieldAnnotation;
  }

  //
  // @Override
  // protected IDataTypeHandler newDataTypeHandler() {
  // Class<?> itemClass = getItemType();
  //
  // IDataTypeHandler retval;
  // if (itemClass.isAnnotationPresent(MetaschemaField.class)) {
  // IClassBinding classBinding
  // =
  // getParentClassBinding().getBindingContext().getClassBinding(getPropertyInfo().getItemType());
  // retval = new ClassDataTypeHandler(classBinding, this);
  // } else {
  // retval = new JavaTypeAdapterDataTypeHandler(this);
  // }
  // return retval;
  // }

  @Override
  public String getFormalName() {
    return ModelUtil.resolveToString(getFieldAnnotation().formalName());
  }

  @Override
  public MarkupLine getDescription() {
    return ModelUtil.resolveToMarkupLine(getFieldAnnotation().description());
  }

  @Override
  public String getUseName() {
    return ModelUtil.resolveToString(getFieldAnnotation().useName());
  }

  @Override
  public boolean isInXmlWrapped() {
    return getFieldAnnotation().inXmlWrapped();
  }

  @Override
  public int getMinOccurs() {
    return getFieldAnnotation().minOccurs();
  }

  @Override
  public int getMaxOccurs() {
    return getFieldAnnotation().maxOccurs();
  }

  @Override
  public MarkupMultiline getRemarks() {
    return ModelUtil.resolveToMarkupMultiline(getFieldAnnotation().remarks());
  }

  @Override
  public void writeItem(Object item, QName parentName, IXmlWritingContext context)
      throws XMLStreamException, IOException {
    // figure out how to parse the item
    IDataTypeHandler handler = getPropertyInfo().getDataTypeHandler();

    // figure out if we need to parse the wrapper or not
    boolean writeWrapper = isInXmlWrapped() || !handler.isUnwrappedValueAllowedInXml();

    XMLStreamWriter2 writer = context.getWriter();

    QName currentParentName;
    if (writeWrapper) {
      currentParentName = getXmlQName();
      writer.writeStartElement(currentParentName.getNamespaceURI(), currentParentName.getLocalPart());
    } else {
      currentParentName = parentName;
    }

    // write the value
    handler.accept(item, currentParentName, context);

    if (writeWrapper) {
      writer.writeEndElement();
    }
  }

  @Override
  public IAssemblyClassBinding getContainingDefinition() {
    return getParentClassBinding();
  }

  @SuppressWarnings("null")
  @Override
  public String toCoordinates() {
    return String.format("%s Instance(%s): %s",
        getModelType().name().toLowerCase(Locale.ROOT),
        getParentClassBinding().getBoundClass().getName(),
        getName());
  }
}
