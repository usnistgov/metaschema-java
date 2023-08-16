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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.model.util.JsonUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.databind.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlWritingContext;

import java.io.IOException;
import java.util.Collection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import edu.umd.cs.findbugs.annotations.NonNull;

class ClassDataTypeHandler implements IDataTypeHandler {
  @NonNull
  private final IBoundNamedModelInstance property;
  @NonNull
  private final IClassBinding classBinding;

  public ClassDataTypeHandler(@NonNull IClassBinding classBinding, @NonNull IBoundNamedModelInstance property) {
    this.classBinding = ObjectUtils.requireNonNull(classBinding, "classBinding");
    this.property = ObjectUtils.requireNonNull(property, "property");
  }

  @Override
  public IBoundNamedModelInstance getProperty() {
    return property;
  }

  @Override
  public IDataTypeAdapter<?> getJavaTypeAdapter() {
    // this is always null
    return null;
  }

  @Override
  @NonNull
  public IClassBinding getClassBinding() {
    return classBinding;
  }

  @Override
  public boolean isUnwrappedValueAllowedInXml() {
    // classes are always wrapped
    return false;
  }

  // REFACTOR: rename to read
  @SuppressWarnings("resource") // not owned
  @Override
  public Object read(Object parentInstance, boolean requiresJsonKey, IJsonParsingContext context)
      throws IOException {
    JsonParser parser = context.getReader(); // NOPMD - intentional
    boolean objectWrapper = JsonToken.START_OBJECT.equals(parser.currentToken());
    if (objectWrapper) {
      JsonUtil.assertAndAdvance(parser, JsonToken.START_OBJECT);
    }

    Object retval = context.readDefinitionValue(getClassBinding(), parentInstance, requiresJsonKey);

    if (objectWrapper) {
      JsonUtil.assertAndAdvance(parser, JsonToken.END_OBJECT);
    }
    return retval;
  }

  // REFACTOR: rename to read
  @Override
  public Object read(Object parentInstance, StartElement start, IXmlParsingContext context)
      throws IOException, XMLStreamException {
    return context.readDefinitionValue(getClassBinding(), parentInstance, start);
  }

  @Override
  public void write(Object item, QName currentParentName, IXmlWritingContext context)
      throws IOException, XMLStreamException {
    context.writeDefinitionValue(classBinding, item, currentParentName);
  }

  @Override
  public void writeItems(Collection<? extends Object> items, boolean writeObjectWrapper,
      IJsonWritingContext context)
      throws IOException {
    context.writeDefinitionValues(classBinding, items, writeObjectWrapper);
  }

  @Override
  public Object copyItem(@NonNull Object fromItem, Object parentInstance) throws BindingException {
    return classBinding.copyBoundObject(fromItem, parentInstance);
  }
}
