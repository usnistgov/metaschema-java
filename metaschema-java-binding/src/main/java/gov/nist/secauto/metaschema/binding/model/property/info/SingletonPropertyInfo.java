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

package gov.nist.secauto.metaschema.binding.model.property.info;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.JsonUtil;
import gov.nist.secauto.metaschema.binding.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.property.IBoundNamedModelInstance;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

public class SingletonPropertyInfo
    extends AbstractModelPropertyInfo<Type>
    implements IModelPropertyInfo {

  public SingletonPropertyInfo(IBoundNamedModelInstance property) {
    super(property);
  }

  @Override
  public List<?> getItemsFromValue(Object value) {
    return value == null ? List.of() : List.of(value);
  }

  @Override
  public void readValue(IPropertyCollector collector, Object parentInstance, IJsonParsingContext context)
      throws IOException, BindingException {
    IBoundNamedModelInstance property = getProperty();

    JsonParser parser = context.getReader();

    boolean isObject = JsonToken.START_OBJECT.equals(parser.currentToken());
    if (isObject) {
      // read the object's START_OBJECT
      JsonUtil.assertAndAdvance(parser, JsonToken.START_OBJECT);
    }

    List<Object> values = property.readItem(parentInstance, context);
    collector.addAll(values);

    if (isObject) {
      // read the object's END_OBJECT
      JsonUtil.assertAndAdvance(context.getReader(), JsonToken.END_OBJECT);
    }
  }

  @Override
  public boolean readValue(IPropertyCollector collector, Object parentInstance, StartElement start,
      IXmlParsingContext context) throws IOException, BindingException, XMLStreamException {
    boolean handled = true;
    Object value = getProperty().readItem(parentInstance, start, context);
    if (value != null) {
      collector.add(value);
      handled = true;
    }
    return handled;
  }

  @Override
  public Class<?> getItemType() {
    return (Class<?>) getType();
  }

  @Override
  public IPropertyCollector newPropertyCollector() {
    return new SingletonPropertyCollector();
  }

  @Override
  public boolean writeValue(Object parentInstance, QName parentName, IXmlWritingContext context)
      throws XMLStreamException, IOException {
    IBoundNamedModelInstance property = getProperty();
    return property.writeItem(property.getValue(parentInstance), parentName, context);
  }

  @Override
  public void writeValue(Object parentInstance, IJsonWritingContext context) throws IOException {
    IBoundNamedModelInstance property = getProperty();
    getProperty().getDataTypeHandler().writeItems(Collections.singleton(property.getValue(parentInstance)), true,
        context);
  }

  @Override
  public boolean isValueSet(Object parentInstance) throws IOException {
    return getProperty().getValue(parentInstance) != null;
  }

  @Override
  public void copy(@NotNull Object fromInstance, @NotNull Object toInstance, @NotNull IPropertyCollector collector)
      throws BindingException {
    IBoundNamedModelInstance property = getProperty();

    Object value = property.getValue(fromInstance);

    Object copiedValue = property.copyItem(value, toInstance);

    collector.add(copiedValue);
  }

}
