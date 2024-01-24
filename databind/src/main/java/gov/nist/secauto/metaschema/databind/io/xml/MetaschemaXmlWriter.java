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

package gov.nist.secauto.metaschema.databind.io.xml;

import gov.nist.secauto.metaschema.databind.io.json.DefaultJsonProblemHandler;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionFieldComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModel;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldValue;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModel;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelFieldComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelFieldScalar;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedField;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedNamed;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelNamed;
import gov.nist.secauto.metaschema.databind.model.info.AbstractModelInstanceWriteHandler;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureComplexItemValueHandler;
import gov.nist.secauto.metaschema.databind.model.info.IItemWriteHandler;
import gov.nist.secauto.metaschema.databind.model.info.IModelInstanceCollectionInfo;

import org.codehaus.stax2.XMLStreamWriter2;

import java.io.IOException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;

public class MetaschemaXmlWriter implements IXmlWritingContext {
  @NonNull
  private final XMLStreamWriter2 writer;

  /**
   * Construct a new Module-aware JSON writer.
   *
   * @param writer
   *          the XML stream writer to write with
   * @see DefaultJsonProblemHandler
   */
  public MetaschemaXmlWriter(
      @NonNull XMLStreamWriter2 writer) {
    this.writer = writer;
  }

  @Override
  public XMLStreamWriter2 getWriter() {
    return writer;
  }

  @Override
  public void write(
      @NonNull IBoundDefinitionModelComplex definition,
      @NonNull Object item) throws IOException {

    QName qname = definition.getXmlQName();

    definition.writeItem(item, new InstanceWriter(qname));
  }

  private static class ModelInstanceWriteHandler
      extends AbstractModelInstanceWriteHandler {
    @NonNull
    private final ItemWriter itemWriter;

    public ModelInstanceWriteHandler(
        @NonNull IBoundInstanceModel instance,
        @NonNull ItemWriter itemWriter) {
      super(instance);
      this.itemWriter = itemWriter;
    }

    @Override
    public void writeItem(Object item) throws IOException {
      IBoundInstanceModel instance = getInstance();
      instance.writeItem(item, itemWriter);
    }
  }

  private class InstanceWriter
      extends AbstractItemWriter {

    public InstanceWriter(@NonNull QName parentQName) {
      super(parentQName);
    }

    @Override
    public void writeItemFlag(Object item, IBoundInstanceFlag instance) throws IOException {
      instance.writeItem(item, new ItemWriter(getParentQName()));
    }

    @Override
    public void writeItemField(Object value, IBoundInstanceModelFieldScalar instance) throws IOException {
      writeModelInstance(instance, value);
    }

    @Override
    public void writeItemField(Object value, IBoundInstanceModelFieldComplex instance) throws IOException {
      writeModelInstance(instance, value);
    }

    @Override
    public void writeItemField(Object item, IBoundInstanceModelGroupedField instance) throws IOException {
      throw new UnsupportedOperationException("not needed");
    }

    @Override
    public void writeItemField(Object item, IBoundDefinitionFieldComplex definition) throws IOException {
      definition.writeItem(item, new ItemWriter(getParentQName()));
    }

    @Override
    public void writeItemFieldValue(Object item, IBoundFieldValue fieldValue) throws IOException {
      throw new UnsupportedOperationException("not needed");
    }

    @Override
    public void writeItemAssembly(Object value, IBoundInstanceModelAssembly instance) throws IOException {
      writeModelInstance(instance, value);
    }

    @Override
    public void writeItemAssembly(Object item, IBoundInstanceModelGroupedAssembly instance) throws IOException {
      throw new UnsupportedOperationException("not needed");
    }

    @Override
    public void writeItemAssembly(Object item, IBoundDefinitionAssembly definition) throws IOException {
      definition.writeItem(item, new ItemWriter(getParentQName()));
    }

    @Override
    public void writeChoiceGroupItem(Object value, IBoundInstanceModelChoiceGroup instance) throws IOException {
      writeModelInstance(instance, value);
    }

    private void writeModelInstance(
        @NonNull IBoundInstanceModel instance,
        @NonNull Object value) throws IOException {
      IModelInstanceCollectionInfo collectionInfo = instance.getCollectionInfo();
      if (!collectionInfo.isEmpty(value)) {
        QName currentQName = getParentQName();
        QName groupAsQName = instance.getXmlGroupAsQName();
        try {
          if (groupAsQName != null) {
            // write the grouping element
            writer.writeStartElement(groupAsQName.getNamespaceURI(), groupAsQName.getLocalPart());
            currentQName = groupAsQName;
          }

          collectionInfo.writeItems(
              new ModelInstanceWriteHandler(instance, new ItemWriter(currentQName)),
              value);

          if (groupAsQName != null) {
            writer.writeEndElement();
          }
        } catch (XMLStreamException ex) {
          throw new IOException(ex);
        }
      }
    }
  }

  private class ItemWriter
      extends AbstractItemWriter {

    public ItemWriter(@NonNull QName parentQName) {
      super(parentQName);
    }

    private <T extends IBoundInstanceModelNamed & IFeatureComplexItemValueHandler> void writeFlags(
        @NonNull Object parentItem,
        @NonNull T instance) throws IOException {
      writeFlags(parentItem, instance.getDefinition());
    }

    private <T extends IBoundInstanceModelGroupedNamed & IFeatureComplexItemValueHandler> void writeFlags(
        @NonNull Object parentItem,
        @NonNull T instance) throws IOException {
      writeFlags(parentItem, instance.getDefinition());
    }

    private void writeFlags(
        @NonNull Object parentItem,
        @NonNull IBoundDefinitionModel definition) throws IOException {
      for (IBoundInstanceFlag flag : definition.getFlagInstances()) {
        assert flag != null;

        Object value = flag.getValue(parentItem);
        if (value != null) {
          writeItemFlag(value, flag);
        }
      }
    }

    private <T extends IBoundInstanceModelAssembly & IFeatureComplexItemValueHandler> void writeAssemblyModel(
        @NonNull Object parentItem,
        @NonNull T instance) throws IOException {
      writeAssemblyModel(parentItem, instance.getDefinition());
    }

    private <T extends IBoundInstanceModelGroupedAssembly & IFeatureComplexItemValueHandler> void writeAssemblyModel(
        @NonNull Object parentItem,
        @NonNull T instance) throws IOException {
      writeAssemblyModel(parentItem, instance.getDefinition());
    }

    private void writeAssemblyModel(
        @NonNull Object parentItem,
        @NonNull IBoundDefinitionAssembly definition) throws IOException {
      for (IBoundInstanceModel modelInstance : definition.getModelInstances()) {
        assert modelInstance != null;

        Object value = modelInstance.getValue(parentItem);
        if (value != null) {
          modelInstance.writeItem(value, new InstanceWriter(getParentQName()));
        }
      }
    }

    private void writeFieldValue(
        @NonNull Object parentItem,
        @NonNull IBoundInstanceModelFieldComplex instance) throws IOException {
      writeFieldValue(parentItem, instance.getDefinition());
    }

    private void writeFieldValue(
        @NonNull Object parentItem,
        @NonNull IBoundInstanceModelGroupedField instance) throws IOException {
      writeFieldValue(parentItem, instance.getDefinition());
    }

    private void writeFieldValue(
        @NonNull Object parentItem,
        @NonNull IBoundDefinitionFieldComplex definition) throws IOException {
      definition.getFieldValue().writeItem(parentItem, this);
    }

    private <T extends IFeatureComplexItemValueHandler & IBoundInstanceModelNamed> void writeModelObject(
        @NonNull T instance,
        @NonNull Object parentItem,
        @NonNull ObjectWriter<T> propertyWriter) throws IOException {
      try {
        QName wrapperQName = instance.getXmlQName();
        writer.writeStartElement(wrapperQName.getNamespaceURI(), wrapperQName.getLocalPart());

        propertyWriter.accept(parentItem, instance);

        writer.writeEndElement();
      } catch (XMLStreamException ex) {
        throw new IOException(ex);
      }
    }

    private <T extends IFeatureComplexItemValueHandler & IBoundInstanceModelGroupedNamed> void writeGroupedModelObject(
        @NonNull T instance,
        @NonNull Object parentItem,
        @NonNull ObjectWriter<T> propertyWriter) throws IOException {
      try {
        QName wrapperQName = instance.getXmlQName();
        writer.writeStartElement(wrapperQName.getNamespaceURI(), wrapperQName.getLocalPart());

        propertyWriter.accept(parentItem, instance);

        writer.writeEndElement();
      } catch (XMLStreamException ex) {
        throw new IOException(ex);
      }
    }

    private <T extends IFeatureComplexItemValueHandler & IBoundDefinitionModelComplex> void writeDefinitionObject(
        @NonNull T instance,
        @NonNull Object parentItem,
        @NonNull ObjectWriter<T> propertyWriter) throws IOException {

      try {
        QName wrapperQName = instance.getXmlQName();
        NamespaceContext nsContext = writer.getNamespaceContext();
        String prefix = nsContext.getPrefix(wrapperQName.getNamespaceURI());
        if (prefix == null) {
          prefix = "";
        }

        writer.writeStartElement(prefix, wrapperQName.getLocalPart(), wrapperQName.getNamespaceURI());

        propertyWriter.accept(parentItem, instance);

        writer.writeEndElement();
      } catch (XMLStreamException ex) {
        throw new IOException(ex);
      }
    }

    @Override
    public void writeItemFlag(Object item, IBoundInstanceFlag instance) throws IOException {
      String itemString = instance.getJavaTypeAdapter().asString(item);
      QName name = instance.getXmlQName();
      try {
        if (name.getNamespaceURI().isEmpty()) {
          writer.writeAttribute(name.getLocalPart(), itemString);
        } else {
          writer.writeAttribute(name.getNamespaceURI(), name.getLocalPart(), itemString);
        }
      } catch (XMLStreamException ex) {
        throw new IOException(ex);
      }
    }

    @Override
    public void writeItemField(Object item, IBoundInstanceModelFieldScalar instance) throws IOException {
      try {
        if (instance.isValueWrappedInXml()) {
          QName wrapperQName = instance.getXmlQName();
          writer.writeStartElement(wrapperQName.getNamespaceURI(), wrapperQName.getLocalPart());
          instance.getJavaTypeAdapter().writeXmlValue(item, wrapperQName, writer);
          writer.writeEndElement();
        } else {
          instance.getJavaTypeAdapter().writeXmlValue(item, getParentQName(), writer);
        }
      } catch (XMLStreamException ex) {
        throw new IOException(ex);
      }
    }

    @Override
    public void writeItemField(Object item, IBoundInstanceModelFieldComplex instance) throws IOException {
      ItemWriter itemWriter = new ItemWriter(instance.getXmlQName());
      writeModelObject(
          instance,
          item,
          ((ObjectWriter<IBoundInstanceModelFieldComplex>) this::writeFlags)
              .andThen(itemWriter::writeFieldValue));
    }

    @Override
    public void writeItemField(Object item, IBoundInstanceModelGroupedField instance) throws IOException {
      ItemWriter itemWriter = new ItemWriter(instance.getXmlQName());
      writeGroupedModelObject(
          instance,
          item,
          ((ObjectWriter<IBoundInstanceModelGroupedField>) this::writeFlags)
              .andThen(itemWriter::writeFieldValue));
    }

    @Override
    public void writeItemField(Object item, IBoundDefinitionFieldComplex definition) throws IOException {
      ItemWriter itemWriter = new ItemWriter(definition.getXmlQName());
      writeDefinitionObject(
          definition,
          item,
          ((ObjectWriter<IBoundDefinitionFieldComplex>) this::writeFlags)
              .andThen(itemWriter::writeFieldValue));
    }

    @Override
    public void writeItemFieldValue(Object parentItem, IBoundFieldValue fieldValue) throws IOException {
      Object item = fieldValue.getValue(parentItem);
      if (item != null) {
        try {
          fieldValue.getJavaTypeAdapter().writeXmlValue(item, getParentQName(), writer);
        } catch (XMLStreamException ex) {
          throw new IOException(ex);
        }
      }
    }

    @Override
    public void writeItemAssembly(Object item, IBoundInstanceModelAssembly instance) throws IOException {
      ItemWriter itemWriter = new ItemWriter(instance.getXmlQName());
      writeModelObject(
          instance,
          item,
          ((ObjectWriter<IBoundInstanceModelAssembly>) this::writeFlags)
              .andThen(itemWriter::writeAssemblyModel));
    }

    @Override
    public void writeItemAssembly(Object item, IBoundInstanceModelGroupedAssembly instance) throws IOException {
      ItemWriter itemWriter = new ItemWriter(instance.getXmlQName());
      writeGroupedModelObject(
          instance,
          item,
          ((ObjectWriter<IBoundInstanceModelGroupedAssembly>) this::writeFlags)
              .andThen(itemWriter::writeAssemblyModel));
    }

    @Override
    public void writeItemAssembly(Object item, IBoundDefinitionAssembly definition) throws IOException {
      ItemWriter itemWriter = new ItemWriter(definition.getXmlQName());
      writeDefinitionObject(
          definition,
          item,
          ((ObjectWriter<IBoundDefinitionAssembly>) this::writeFlags)
              .andThen(itemWriter::writeAssemblyModel));
    }

    @Override
    public void writeChoiceGroupItem(Object item, IBoundInstanceModelChoiceGroup instance) throws IOException {
      IBoundInstanceModelGroupedNamed actualInstance = instance.getItemInstance(item);
      assert actualInstance != null;
      actualInstance.writeItem(item, this);
    }
  }

  private abstract class AbstractItemWriter implements IItemWriteHandler {
    @NonNull
    private final QName parentQName;

    protected AbstractItemWriter(@NonNull QName parentQName) {
      this.parentQName = parentQName;
    }

    /**
     * @return the startElement
     */
    @NonNull
    protected QName getParentQName() {
      return parentQName;
    }
  }
}
