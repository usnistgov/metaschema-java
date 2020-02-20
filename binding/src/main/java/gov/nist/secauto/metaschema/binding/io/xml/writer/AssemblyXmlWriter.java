/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.binding.io.xml.writer;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.model.property.CollectionPropertyInfo;
import gov.nist.secauto.metaschema.binding.model.property.FieldPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.ModelItemPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyInfo;
import gov.nist.secauto.metaschema.datatype.markup.MarkupMultiline;

import org.codehaus.stax2.evt.XMLEventFactory2;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

public class AssemblyXmlWriter<CLASS> extends AbstractXmlWriter<CLASS, AssemblyClassBinding<CLASS>> {
  public AssemblyXmlWriter(AssemblyClassBinding<CLASS> classBinding) {
    super(classBinding);
  }

  @Override
  public void writeXml(Object obj, QName name, XmlWritingContext writingContext) throws BindingException {
    if (name == null && getClassBinding().isRootElement()) {
      name = getClassBinding().getRootQName();
    }
    super.writeXml(obj, name, writingContext);
  }

  @Override
  protected void writeBody(Object obj, StartElement parent, XmlWritingContext writingContext) throws BindingException {
    for (ModelItemPropertyBinding propertyBinding : getClassBinding().getModelItemPropertyBindings()) {
      PropertyInfo propertyInfo = propertyBinding.getPropertyInfo();

      XMLEventFactory2 factory = writingContext.getXMLEventFactory();
      XMLEventWriter writer = writingContext.getEventWriter();

      QName itemWrapperQName = null;
      // Need to emit the item wrapper
      if (!(propertyBinding instanceof FieldPropertyBinding)
          || ((FieldPropertyBinding) propertyBinding).isWrappedInXml()
          || !MarkupMultiline.class.isAssignableFrom((Class<?>) propertyBinding.getPropertyInfo().getItemType())) {
        itemWrapperQName = propertyBinding.getXmlQName();
      }

      Object value = propertyInfo.getValue(obj);

      if (value != null) {
        JavaTypeAdapter<?> typeAdapter
            = writingContext.getBindingContext().getJavaTypeAdapter(propertyBinding.getPropertyInfo().getItemType());

        try {
          StartElement propertyParent = parent;
          Iterable<? extends Object> iterable;
          QName groupWrapperQName = null;
          if (propertyInfo instanceof CollectionPropertyInfo) {
            CollectionPropertyInfo collectionPropertyInfo = (CollectionPropertyInfo) propertyInfo;

            if (XmlGroupAsBehavior.GROUPED.equals(collectionPropertyInfo.getXmlGroupAsBehavior())) {
              groupWrapperQName = collectionPropertyInfo.getGroupXmlQName();

              propertyParent = factory.createStartElement(groupWrapperQName, null, null);
              writer.add(propertyParent);
            }

            if (collectionPropertyInfo.isMap()) {
              @SuppressWarnings("unchecked")
              Map<String, ? extends Object> map = (Map<String, ? extends Object>) value;
              iterable = map.values();
            } else if (collectionPropertyInfo.isList()) {
              @SuppressWarnings("unchecked")
              List<? extends Object> list = (List<? extends Object>) value;
              iterable = list;
            } else {
              throw new BindingException("Unknown collection type: " + value.getClass());
            }
          } else {
            iterable = Collections.singleton(value);
          }

          for (Object child : iterable) {
            typeAdapter.writeXmlElement(child, itemWrapperQName, propertyParent, writingContext);
          }

          if (groupWrapperQName != null) {
            writer.add(factory.createEndElement(groupWrapperQName, null));
          }
        } catch (XMLStreamException ex) {
          throw new BindingException(ex);
        }
      }
    }
  }
}
