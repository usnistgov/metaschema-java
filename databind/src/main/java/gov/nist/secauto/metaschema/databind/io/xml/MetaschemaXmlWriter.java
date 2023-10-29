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

import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IChoiceGroupInstance;
import gov.nist.secauto.metaschema.core.model.IFlagContainer;
import gov.nist.secauto.metaschema.core.model.INamedModelInstance;
import gov.nist.secauto.metaschema.databind.io.json.DefaultJsonProblemHandler;
import gov.nist.secauto.metaschema.databind.model.info.IModelPropertyInfo;
import gov.nist.secauto.metaschema.databind.model.oldmodel.IBoundNamedModelInstance;
import gov.nist.secauto.metaschema.databind.strategy.IAssemblyDefinitionBindingStrategy;
import gov.nist.secauto.metaschema.databind.strategy.IClassBindingStrategy;
import gov.nist.secauto.metaschema.databind.strategy.IFlagInstanceBindingStrategy;
import gov.nist.secauto.metaschema.databind.strategy.IInstanceBindingStrategy;
import gov.nist.secauto.metaschema.databind.strategy.impl.IFieldDefinitionBindingStrategy;
import gov.nist.secauto.metaschema.databind.strategy.impl.IFieldValueBindingStrategy;
import gov.nist.secauto.metaschema.databind.strategy.impl.IModelInstanceBindingStrategy;

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
  public MetaschemaXmlWriter(@NonNull XMLStreamWriter2 writer) {
    this.writer = writer;
  }

  @Override
  public XMLStreamWriter2 getWriter() {
    return writer;
  }

  /**
   * Writes data in a bound object to XML. This assembly must be a root assembly
   * for which a call to {@link IAssemblyDefinition#isRoot()} will return
   * {@code true}.
   *
   * @param bindingStrategy
   *          the definition info describing the root element data to write
   * @param targetObject
   *          the bound object
   * @throws XMLStreamException
   *           if an error occurred while parsing into XML
   * @throws IOException
   *           if an error occurred while writing the output
   */
  public void write(
      @NonNull IClassBindingStrategy<IAssemblyDefinition> bindingStrategy,
      @NonNull Object targetObject) throws XMLStreamException, IOException {
    writer.writeStartDocument("UTF-8", "1.0");

    QName rootQName = bindingStrategy.getDefinition().getRootXmlQName();

    NamespaceContext nsContext = writer.getNamespaceContext();
    String prefix = nsContext.getPrefix(rootQName.getNamespaceURI());
    if (prefix == null) {
      prefix = "";
    }

    writer.writeStartElement(prefix, rootQName.getLocalPart(), rootQName.getNamespaceURI());

    writeDefinitionValue(bindingStrategy, targetObject, rootQName);

    writer.writeEndElement();
  }

  @Override
  public void writeDefinitionValue(
      @NonNull IClassBindingStrategy<? extends IFlagContainer> bindingStrategy,
      @NonNull Object targetObject,
      @NonNull QName parentName) throws IOException {
    // write flags
    for (IFlagInstanceBindingStrategy flag : bindingStrategy.getFlagInstances()) {
      assert flag != null;
      writeFlagInstance(flag, targetObject);
    }

    if (bindingStrategy instanceof IAssemblyDefinitionBindingStrategy) {
      for (IModelInstanceBindingStrategy<?> modelProperty : ((IAssemblyDefinitionBindingStrategy) bindingStrategy)
          .getModelInstances()) {
        assert modelProperty != null;
        if (modelProperty instanceof IBoundNamedModelInstance) {
          writeModelInstanceValues((IBoundNamedModelInstance) modelProperty, targetObject, parentName);
        } else {
          throw new UnsupportedOperationException(modelProperty.getClass().getName());
        }
      }
    } else if (bindingStrategy instanceof IFieldDefinitionBindingStrategy) {
      IFieldValueBindingStrategy fieldValue = ((IFieldDefinitionBindingStrategy) bindingStrategy)
          .getFieldValue();

      Object value = fieldValue.getValue(targetObject);
      if (value != null) {
        try {
          fieldValue.writeItem(value, parentName, this);
        } catch (XMLStreamException ex) {
          throw new IOException(ex);
        }
      }
    } else {
      throw new UnsupportedOperationException(
          String.format("Unsupported class binding type: %s", bindingStrategy.toCoordinates()));
    }
  }

  /**
   * Write the data described by the provided {@code targetInstance} as an XML
   * attribute.
   *
   * @param flagStrategy
   *          the model instance that describes the syntax of the data to write
   * @param parentObject
   *          the Java object that data written by this method is stored in
   * @throws IOException
   *           if an error occurred while writing the XML
   */
  protected void writeFlagInstance(
      @NonNull IFlagInstanceBindingStrategy flagStrategy,
      @NonNull Object parentObject)
      throws IOException {
    Object objectValue = flagStrategy.getValue(parentObject);
    String value = objectValue == null
        ? null
        : flagStrategy.toStringFromItem(objectValue);

    if (value != null) {
      QName name = flagStrategy.getInstance().getXmlQName();
      try {
        if (name.getNamespaceURI().isEmpty()) {
          writer.writeAttribute(name.getLocalPart(), value);
        } else {
          writer.writeAttribute(name.getNamespaceURI(), name.getLocalPart(), value);
        }
      } catch (XMLStreamException ex) {
        throw new IOException(ex);
      }
    }
  }

  /**
   * Write the data described by the provided {@code targetInstance} as an XML
   * element.
   *
   * @param targetInstance
   *          the model instance that describes the syntax of the data to write
   * @param parentObject
   *          the Java object that data written by this method is stored in
   * @param parentName
   *          the qualified name of the XML data's parent element
   * @return {@code true} id the value was written or {@code false} otherwise
   * @throws IOException
   *           if an error occurred while writing the XML
   */
  protected boolean writeModelInstanceValues(
      @NonNull IBoundNamedModelInstance targetInstance,
      @NonNull Object parentObject,
      @NonNull QName parentName)
      throws IOException {
    Object value = targetInstance.getValue(parentObject);
    if (value == null) {
      return false; // NOPMD - intentional
    }

    IModelPropertyInfo propertyInfo = targetInstance.getPropertyInfo();
    if (targetInstance.getMinOccurs() > 0 || propertyInfo.getItemCount(value) > 0) {
      // only write a property if the wrapper is required or if it has contents
      QName currentStart = parentName;

      try {
        QName groupQName = targetInstance.getXmlGroupAsQName();
        if (groupQName != null) {
          // write the grouping element
          writer.writeStartElement(groupQName.getNamespaceURI(), groupQName.getLocalPart());
          currentStart = groupQName;
        }

        // There are one or more named values based on cardinality
        propertyInfo.writeValues(value, currentStart, this);

        if (groupQName != null) {
          writer.writeEndElement();
        }
      } catch (XMLStreamException ex) {
        throw new IOException(ex);
      }
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void writeInstanceValue(
      IModelInstanceBindingStrategy<?> instanceStrategy,
      Object targetObject,
      QName parentName) throws IOException {
    // figure out how to parse the item
    if (instanceStrategy.isInstanceOf(INamedModelInstance.class)) {
      writeNamedModelInstanceValue(
          (IInstanceBindingStrategy<? extends INamedModelInstance>) instanceStrategy,
          targetObject,
          parentName);
    } else if (instanceStrategy.isInstanceOf(IChoiceGroupInstance.class)) {
      writeChoiceGroupInstanceValue(
          (IInstanceBindingStrategy<? extends IChoiceGroupInstance>) instanceStrategy,
          targetObject,
          parentName);
    } else {
      throw new UnsupportedOperationException(
          String.format("Unsupported instance type: %s", instanceStrategy.getClass().getName()));
    }
  }

  private void writeChoiceGroupInstanceValue(
      @NonNull IInstanceBindingStrategy<? extends IChoiceGroupInstance> instanceStrategy,
      @NonNull Object targetObject,
      @NonNull QName parentName) {
    throw new UnsupportedOperationException();
  }

  private void writeNamedModelInstanceValue(
      @NonNull IInstanceBindingStrategy<? extends INamedModelInstance> instanceStrategy,
      @NonNull Object targetObject,
      @NonNull QName parentName) throws IOException {

    // figure out if we need to write the wrapper or not
    boolean writeWrapper = !instanceStrategy.isUnwrappedValueAllowedInXml();

    try {
      QName currentParentName;
      if (writeWrapper) {
        currentParentName = instanceStrategy.getInstance().getXmlQName();
        writer.writeStartElement(currentParentName.getNamespaceURI(), currentParentName.getLocalPart());
      } else {
        currentParentName = parentName;
      }

      // write the value
      instanceStrategy.writeItem(targetObject, currentParentName, this);

      if (writeWrapper) {
        writer.writeEndElement();
      }
    } catch (XMLStreamException ex) {
      throw new IOException(ex);
    }

  }
}
