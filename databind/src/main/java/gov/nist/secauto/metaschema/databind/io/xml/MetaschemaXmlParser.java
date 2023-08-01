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

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.model.util.XmlEventUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.AbstractParser;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.IBoundAssemblyInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldValueInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundFlagInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundNamedModelInstance;
import gov.nist.secauto.metaschema.databind.model.IClassBinding;
import gov.nist.secauto.metaschema.databind.model.IFieldClassBinding;
import gov.nist.secauto.metaschema.databind.model.IPropertyCollector;
import gov.nist.secauto.metaschema.databind.model.IRootAssemblyClassBinding;

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLStreamReader2;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class MetaschemaXmlParser
    extends AbstractParser
    implements IXmlParsingContext {
  @NonNull
  private final XMLEventReader2 reader;
  @NonNull
  private final IXmlProblemHandler problemHandler;

  public MetaschemaXmlParser(
      @NonNull XMLEventReader2 reader) {
    this(reader, new DefaultXmlProblemHandler());
  }

  public MetaschemaXmlParser(
      @NonNull XMLEventReader2 reader,
      @NonNull IXmlProblemHandler problemHandler) {
    this.reader = reader;
    this.problemHandler = problemHandler;
  }

  @Override
  public XMLEventReader2 getReader() {
    return reader;
  }

  @Override
  public IXmlProblemHandler getProblemHandler() {
    return problemHandler;
  }

  /**
   * Parses XML into a bound object.
   * <p>
   * Parses the {@link XMLStreamConstants#START_DOCUMENT} and the root element.
   *
   * @param <CLASS>
   *          the returned object type
   *
   * @param definition
   *          the root definition describing the root element to parse
   * @return the parsed object
   * @throws XMLStreamException
   *           if an error occurred while parsing into XML
   * @throws IOException
   *           if an error occurred while reading the input
   */
  @NonNull
  public <CLASS> CLASS read(@NonNull IRootAssemblyClassBinding definition) throws IOException, XMLStreamException {

    // we may be at the START_DOCUMENT
    if (reader.peek().isStartDocument()) {
      XmlEventUtil.consumeAndAssert(reader, XMLStreamConstants.START_DOCUMENT);
    }

    XmlEventUtil.skipEvents(reader, XMLStreamConstants.CHARACTERS, XMLStreamConstants.PROCESSING_INSTRUCTION);

    QName rootQName = definition.getRootXmlQName();
    XMLEvent event = XmlEventUtil.consumeAndAssert(reader, XMLStreamConstants.START_ELEMENT, rootQName);

    StartElement start = ObjectUtils.notNull(event.asStartElement());

    @SuppressWarnings("unchecked") CLASS retval = (CLASS) ObjectUtils.requireNonNull(
        readDefinitionValue(definition.getRootDefinition(), null, start));

    XmlEventUtil.consumeAndAssert(reader, XMLStreamConstants.END_ELEMENT, rootQName);

    // if (reader.hasNext() && LOGGER.isDebugEnabled()) {
    // LOGGER.debug("After Parse: {}", XmlEventUtil.toString(reader.peek()));
    // }

    return retval;
  }

  /**
   * Reads a XML element storing the associated data in a Java class instance, returning the resulting
   * instance.
   * <p>
   * When called the next {@link XMLEvent} of the {@link XMLStreamReader2} is expected to be a
   * {@link XMLStreamConstants#START_ELEMENT} that is the XML element associated with the Java class.
   * <p>
   * After returning the next {@link XMLEvent} of the {@link XMLStreamReader2} is expected to be a the
   * next event after the {@link XMLStreamConstants#END_ELEMENT} for the XML
   * {@link XMLStreamConstants#START_ELEMENT} element associated with the Java class.
   *
   * @param targetDefinition
   *          the Metaschema definition for the target object being read
   * @param parentObject
   *          the object target's parent object, which can be {@code null} if there is no parent
   * @param start
   *          the containing start element
   * @return the instance or {@code null} if no data was parsed
   * @throws IOException
   *           if an error occurred while reading the parsed content
   * @throws XMLStreamException
   *           if an error occurred while parsing the content as XML
   */
  @Override
  @NonNull
  public Object readDefinitionValue(
      @NonNull IClassBinding targetDefinition,
      @Nullable Object parentObject,
      @NonNull StartElement start) throws IOException, XMLStreamException {

    try {
      Object targetObject = targetDefinition.newInstance();
      targetDefinition.callBeforeDeserialize(targetObject, parentObject);

      for (IBoundFlagInstance flag : targetDefinition.getFlagInstances()) {
        assert flag != null;
        readFlagInstanceValue(flag, targetObject, start);
      }

      if (targetDefinition instanceof IAssemblyClassBinding) {
        readDefinitionContents((IAssemblyClassBinding) targetDefinition, targetObject, start);
      } else if (targetDefinition instanceof IFieldClassBinding) {
        readDefinitionContents((IFieldClassBinding) targetDefinition, targetObject);
      } else {
        throw new UnsupportedOperationException(
            String.format("Unsupported class binding type: %s", targetDefinition.getClass().getName()));
      }

      XmlEventUtil.skipWhitespace(reader);
      XmlEventUtil.assertNext(reader, XMLStreamConstants.END_ELEMENT, start.getName());

      targetDefinition.callAfterDeserialize(targetObject, parentObject);
      return targetObject;
    } catch (BindingException ex) {
      throw new IOException(ex);
    }
  }

  protected void readDefinitionContents(
      @NonNull IAssemblyClassBinding assembly,
      @NonNull Object targetObject,
      @NonNull StartElement start)
      throws IOException, XMLStreamException {
    Set<IBoundNamedModelInstance> unhandledProperties = new HashSet<>();
    for (IBoundNamedModelInstance modelProperty : assembly.getModelInstances()) {
      assert modelProperty != null;
      if (!readModelInstanceValues(modelProperty, targetObject, start)) {
        unhandledProperties.add(modelProperty);
      }
    }

    // process all properties that did not get a value
    for (IBoundNamedModelInstance property : unhandledProperties) {
      // use the default value of the collector
      property.setValue(targetObject, property.newPropertyCollector().getValue());
    }
  }

  /**
   * Read the XML data associated with this property and apply it to the provided
   * {@code objectInstance} on which this property exists.
   *
   * @param field
   *          the field instance to parse
   * @param targetObject
   *          an instance of the class on which this property exists
   * @throws IOException
   *           if there was an error when reading XML data
   */
  protected void readDefinitionContents(
      @NonNull IFieldClassBinding field,
      @NonNull Object targetObject)
      throws IOException {
    IBoundFieldValueInstance fieldValue = field.getFieldValueInstance();

    // parse the value
    Object value = fieldValue.getJavaTypeAdapter().parse(reader);
    fieldValue.setValue(targetObject, value);
  }

  /**
   * Read the XML data associated with this property and apply it to the provided
   * {@code objectInstance} on which this property exists.
   *
   * @param flag
   *          the flag instance to parse data for
   *
   * @param parentObject
   *          the parent object to store this parsed attribute in
   * @param start
   *          the containing XML element that was previously parsed
   * @return {@code true} if the property was parsed, or {@code false} if the data did not contain
   *         information for this property
   * @throws IOException
   *           if there was an error when reading XML data
   */
  protected boolean readFlagInstanceValue(
      @NonNull IBoundFlagInstance flag,
      @NonNull Object parentObject,
      @NonNull StartElement start) throws IOException {

    // when reading an attribute:
    // - "parent" will contain the attributes to read
    // - the event reader "peek" will be on the end element or the next start element
    boolean handled = false;
    Attribute attribute = start.getAttributeByName(flag.getXmlQName());
    if (attribute != null) {
      // get the attribute value
      Object value = flag.getDefinition().getJavaTypeAdapter().parse(ObjectUtils.notNull(attribute.getValue()));
      // apply the value to the parentObject
      flag.setValue(parentObject, value);

      handled = true;
    }
    return handled;
  }

  protected boolean isNextProperty(
      @NonNull IBoundNamedModelInstance instance)
      throws XMLStreamException {

    XmlEventUtil.skipWhitespace(reader);

    XMLEvent nextEvent = reader.peek();
    if (!nextEvent.isStartElement()) {
      return false;
    }

    QName nextQName = ObjectUtils.notNull(nextEvent.asStartElement().getName());

    if (nextQName.equals(instance.getXmlGroupAsQName())) {
      // we are to parse the grouping element
      return true;
    }

    if (nextQName.equals(instance.getXmlQName())) {
      // we are to parse the element
      return true;
    }

    if (instance instanceof IBoundFieldInstance) {
      IBoundFieldInstance fieldInstance = (IBoundFieldInstance) instance;
      IDataTypeAdapter<?> adapter = fieldInstance.getDefinition().getJavaTypeAdapter();
      // we are to parse the data type
      return !fieldInstance.isInXmlWrapped()
          && adapter.isUnrappedValueAllowedInXml()
          && adapter.canHandleQName(nextQName);
    }
    return false;
  }

  /**
   * Read the XML data associated with this property and apply it to the provided
   * {@code objectInstance} on which this property exists.
   *
   * @param instance
   *          the instance to read
   * @param parentObject
   *          an instance of the class on which this property exists
   * @param start
   *          the containing XML element that was previously parsed
   * @return {@code true} if the property was parsed, or {@code false} if the data did not contain
   *         information for this property
   * @throws IOException
   *           if there was an error when reading XML data
   * @throws XMLStreamException
   *           if there was an error generating an {@link XMLEvent} from the XML
   */
  protected boolean readModelInstanceValues(
      @NonNull IBoundNamedModelInstance instance,
      @NonNull Object parentObject,
      @NonNull StartElement start)
      throws IOException, XMLStreamException {
    boolean handled = isNextProperty(instance);
    if (handled) {
      XmlEventUtil.skipWhitespace(reader);

      StartElement currentStart = start;

      QName groupQName = instance.getXmlGroupAsQName();
      if (groupQName != null) {
        // we are to parse the grouping element, if the next token matches
        XMLEvent groupEvent = XmlEventUtil.consumeAndAssert(reader, XMLStreamConstants.START_ELEMENT, groupQName);
        currentStart = ObjectUtils.notNull(groupEvent.asStartElement());
      }

      IPropertyCollector collector = instance.newPropertyCollector();
      // There are zero or more named values based on cardinality
      instance.getPropertyInfo().readValue(collector, parentObject, currentStart, this);

      Object value = collector.getValue();

      // consume extra whitespace between elements
      XmlEventUtil.skipWhitespace(reader);

      if (groupQName != null) {
        // consume the end of the group
        XmlEventUtil.consumeAndAssert(reader, XMLStreamConstants.END_ELEMENT, groupQName);
      }

      instance.setValue(parentObject, value);
    }
    return handled;
  }

  // TODO: figure out how to call the type specific readItem directly
  @Override
  public Object readModelInstanceValue(IBoundNamedModelInstance instance, Object parentObject, StartElement start)
      throws XMLStreamException, IOException {
    Object retval;
    if (instance instanceof IBoundAssemblyInstance) {
      retval = readModelInstanceValue((IBoundAssemblyInstance) instance, parentObject, start);
    } else if (instance instanceof IBoundFieldInstance) {
      retval = readModelInstanceValue((IBoundFieldInstance) instance, parentObject, start);
    } else {
      throw new UnsupportedOperationException(
          String.format("Unsupported instance type: %s", instance.getClass().getName()));
    }
    return retval;
  }

  /**
   * Reads an individual XML item from the XML stream.
   *
   * @param instance
   *          the instance to parse data for
   * @param parentObject
   *          the object the data is parsed into
   * @param start
   *          the current containing XML element
   * @return the item read, or {@code null} if no item was read
   * @throws XMLStreamException
   *           if an error occurred while generating an {@link XMLEvent}
   * @throws IOException
   *           if an error occurred reading the underlying XML file
   */
  @Nullable
  protected Object readModelInstanceValue(
      @NonNull IBoundAssemblyInstance instance,
      @NonNull Object parentObject,
      @NonNull StartElement start) throws XMLStreamException, IOException {
    // consume extra whitespace between elements
    XmlEventUtil.skipWhitespace(reader);

    Object retval = null;
    XMLEvent event = reader.peek();
    if (event.isStartElement()) {
      StartElement nextStart = event.asStartElement();
      QName nextQName = nextStart.getName();
      if (instance.getXmlQName().equals(nextQName)) {
        // Consume the start element
        reader.nextEvent();

        // consume the value
        retval = instance.getPropertyInfo().getDataTypeHandler().get(parentObject, nextStart, this);

        // consume the end element
        XmlEventUtil.consumeAndAssert(reader, XMLStreamConstants.END_ELEMENT, nextQName);
      }
    }
    return retval;
  }

  /**
   * Reads an individual XML item from the XML stream.
   *
   * @param instance
   *          the instance to parse data for
   * @param parentObject
   *          the object the data is parsed into
   * @param start
   *          the containing XML element
   * @return the item read, or {@code null} if no item was read
   * @throws XMLStreamException
   *           if an error occurred while generating an {@link XMLEvent}
   * @throws IOException
   *           if an error occurred reading the underlying XML file
   */
  @NonNull
  protected Object readModelInstanceValue(
      @NonNull IBoundFieldInstance instance,
      @NonNull Object parentObject,
      @NonNull StartElement start) throws XMLStreamException, IOException {
    // figure out if we need to parse the wrapper or not
    IDataTypeAdapter<?> adapter = instance.getDefinition().getJavaTypeAdapter();
    boolean parseWrapper = true;
    if (!instance.isInXmlWrapped() && adapter.isUnrappedValueAllowedInXml()) {
      parseWrapper = false;
    }

    StartElement currentStart = start;
    if (parseWrapper) {
      // TODO: not sure this is needed, since there is a peek just before this
      // parse any whitespace before the element
      XmlEventUtil.skipWhitespace(reader);

      QName xmlQName = instance.getXmlQName();
      XMLEvent event = reader.peek();
      if (event.isStartElement() && xmlQName.equals(event.asStartElement().getName())) {
        // Consume the start element
        reader.nextEvent();
        currentStart = ObjectUtils.notNull(event.asStartElement());
      } else {
        throw new IOException(String.format("Did not find expected element '%s'.", xmlQName));
      }
    }

    // consume the value
    Object retval = instance.getPropertyInfo().getDataTypeHandler().get(parentObject, currentStart, this);

    if (parseWrapper) {
      // consume the end element
      XmlEventUtil.consumeAndAssert(reader, XMLStreamConstants.END_ELEMENT, currentStart.getName());
    }

    return retval;
  }
}
