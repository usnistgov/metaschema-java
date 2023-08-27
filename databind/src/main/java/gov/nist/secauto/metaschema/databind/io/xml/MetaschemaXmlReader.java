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
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.IBoundAssemblyInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldValueInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundFlagInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundNamedModelInstance;
import gov.nist.secauto.metaschema.databind.model.IClassBinding;
import gov.nist.secauto.metaschema.databind.model.IFieldClassBinding;
import gov.nist.secauto.metaschema.databind.model.info.IPropertyCollector;

import org.codehaus.stax2.XMLEventReader2;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class MetaschemaXmlReader
    implements IXmlParsingContext {
  @NonNull
  private final XMLEventReader2 reader;
  @NonNull
  private final IXmlProblemHandler problemHandler;

  /**
   * Construct a new Metaschema-aware XML parser using the default problem
   * handler.
   *
   * @param reader
   *          the XML reader to parse with
   * @see DefaultXmlProblemHandler
   */
  public MetaschemaXmlReader(
      @NonNull XMLEventReader2 reader) {
    this(reader, new DefaultXmlProblemHandler());
  }

  /**
   * Construct a new Metaschema-aware parser.
   *
   * @param reader
   *          the XML reader to parse with
   * @param problemHandler
   *          the problem handler implementation to use
   */
  public MetaschemaXmlReader(
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
   * Parses XML into a bound object based on the provided {@code definition}.
   * <p>
   * Parses the {@link XMLStreamConstants#START_DOCUMENT}, the root element, and
   * the {@link XMLStreamConstants#END_DOCUMENT}.
   *
   * @param <CLASS>
   *          the returned object type
   * @param targetDefinition
   *          the definition describing the root element data to read
   * @return the parsed object
   * @throws XMLStreamException
   *           if an error occurred while parsing XML events
   * @throws IOException
   *           if an error occurred while parsing the input
   */
  @NonNull
  public <CLASS> CLASS read(@NonNull IAssemblyClassBinding targetDefinition) throws IOException, XMLStreamException {

    // we may be at the START_DOCUMENT
    if (reader.peek().isStartDocument()) {
      XmlEventUtil.consumeAndAssert(reader, XMLStreamConstants.START_DOCUMENT);
    }

    XmlEventUtil.skipEvents(reader, XMLStreamConstants.CHARACTERS, XMLStreamConstants.PROCESSING_INSTRUCTION);

    QName rootQName = targetDefinition.getRootXmlQName();
    XMLEvent event = XmlEventUtil.consumeAndAssert(reader, XMLStreamConstants.START_ELEMENT, rootQName);

    StartElement start = ObjectUtils.notNull(event.asStartElement());

    CLASS retval = readDefinitionValue(targetDefinition, null, start);

    XmlEventUtil.consumeAndAssert(reader, XMLStreamConstants.END_ELEMENT, rootQName);

    // if (reader.hasNext() && LOGGER.isDebugEnabled()) {
    // LOGGER.debug("After Parse: {}", XmlEventUtil.toString(reader.peek()));
    // }

    return retval;
  }

  @SuppressWarnings("PMD.CyclomaticComplexity")
  @Override
  public <T> T readDefinitionValue(
      IClassBinding targetDefinition,
      Object parentObject,
      StartElement start) throws IOException, XMLStreamException {

    Object targetObject;
    try {
      targetObject = targetDefinition.newInstance();
      targetDefinition.callBeforeDeserialize(targetObject, parentObject);
    } catch (BindingException ex) {
      throw new IOException(ex);
    }

    readFlagInstances(targetDefinition, targetObject, start);

    if (targetDefinition instanceof IAssemblyClassBinding) {
      readModelInstances((IAssemblyClassBinding) targetDefinition, targetObject, start);
    } else if (targetDefinition instanceof IFieldClassBinding) {
      readFieldValue((IFieldClassBinding) targetDefinition, targetObject);
    } else {
      throw new UnsupportedOperationException(
          String.format("Unsupported class binding type: %s", targetDefinition.getClass().getName()));
    }

    XmlEventUtil.skipWhitespace(reader);

    XMLEvent nextEvent = ObjectUtils.notNull(reader.peek());
    if (!XmlEventUtil.isEventEndElement(nextEvent, ObjectUtils.notNull(start.getName()))) {
      throw new IOException(
          String.format("Unrecognized element '%s'%s.",
              XmlEventUtil.toEventName(nextEvent),
              XmlEventUtil.generateLocationMessage(nextEvent)));
    }

    try {
      targetDefinition.callAfterDeserialize(targetObject, parentObject);
    } catch (BindingException ex) {
      throw new IOException(ex);
    }
    return ObjectUtils.asType(targetObject);
  }

  /**
   * Read the XML attribute data described by the {@code targetDefinition} and
   * apply it to the provided {@code targetObject}.
   *
   * @param targetDefinition
   *          the Metaschema definition that describes the syntax of the data to
   *          read
   * @param targetObject
   *          the Java object that data parsed by this method will be stored in
   * @param start
   *          the containing XML element that was previously parsed
   * @throws IOException
   *           if an error occurred while parsing the input
   * @throws XMLStreamException
   *           if an error occurred while parsing XML events
   */
  protected void readFlagInstances(
      @NonNull IClassBinding targetDefinition,
      @NonNull Object targetObject,
      @NonNull StartElement start) throws IOException, XMLStreamException {

    Map<QName, IBoundFlagInstance> flagInstanceMap = targetDefinition.getFlagInstances().stream()
        .collect(Collectors.toMap(IBoundFlagInstance::getXmlQName, Function.identity()));

    for (Attribute attribute : CollectionUtil.toIterable(ObjectUtils.notNull(start.getAttributes()))) {
      QName qname = attribute.getName();
      IBoundFlagInstance instance = flagInstanceMap.get(qname);
      if (instance == null) {
        // unrecognized flag
        if (!getProblemHandler().handleUnknownAttribute(targetDefinition, targetObject, attribute, this)) {
          throw new IOException(
              String.format("Unrecognized attribute '%s'%s.",
                  qname,
                  XmlEventUtil.generateLocationMessage(attribute)));
        }
      } else {
        // get the attribute value
        Object value = instance.getDefinition().getJavaTypeAdapter().parse(ObjectUtils.notNull(attribute.getValue()));
        // apply the value to the parentObject
        instance.setValue(targetObject, value);
        flagInstanceMap.remove(qname);
      }
    }

    if (!flagInstanceMap.isEmpty()) {
      getProblemHandler().handleMissingFlagInstances(
          targetDefinition,
          targetObject,
          ObjectUtils.notNull(flagInstanceMap.values()));
    }
  }

  /**
   * Read the XML element data described by the {@code targetDefinition} and apply
   * it to the provided {@code targetObject}.
   *
   * @param targetDefinition
   *          the Metaschema definition that describes the syntax of the data to
   *          read
   * @param targetObject
   *          the Java object that data parsed by this method will be stored in
   * @param start
   *          the XML element start and attribute data previously parsed
   * @throws IOException
   *           if an error occurred while parsing the input
   * @throws XMLStreamException
   *           if an error occurred while parsing XML events
   */
  protected void readModelInstances(
      @NonNull IAssemblyClassBinding targetDefinition,
      @NonNull Object targetObject,
      @NonNull StartElement start)
      throws IOException, XMLStreamException {
    Set<IBoundNamedModelInstance> unhandledProperties = new HashSet<>();
    for (IBoundNamedModelInstance modelProperty : targetDefinition.getModelInstances()) {
      assert modelProperty != null;
      if (!readModelInstanceValues(modelProperty, targetObject, start)) {
        unhandledProperties.add(modelProperty);
      }
    }

    // process all properties that did not get a value
    getProblemHandler().handleMissingModelInstances(targetDefinition, targetObject, unhandledProperties);
  }

  /**
   * Read the XML element and text data described by the {@code targetDefinition}
   * and apply it to the provided {@code targetObject}.
   *
   * @param targetDefinition
   *          the Metaschema definition that describes the syntax of the data to
   *          read
   * @param targetObject
   *          the Java object that data parsed by this method will be stored in
   * @throws IOException
   *           if an error occurred while parsing the input
   */
  protected void readFieldValue(
      @NonNull IFieldClassBinding targetDefinition,
      @NonNull Object targetObject)
      throws IOException {
    IBoundFieldValueInstance fieldValue = targetDefinition.getFieldValueInstance();

    // parse the value
    Object value = fieldValue.getJavaTypeAdapter().parse(reader);
    fieldValue.setValue(targetObject, value);
  }

  /**
   * Determine if the next data to read corresponds to the next model instance.
   *
   * @param targetInstance
   *          the model instance that describes the syntax of the data to read
   * @return {@code true} if the Metaschema instance needs to be parsed, or
   *         {@code false} otherwise
   * @throws XMLStreamException
   *           if an error occurred while parsing XML events
   */
  @SuppressWarnings("PMD.OnlyOneReturn")
  protected boolean isNextInstance(
      @NonNull IBoundNamedModelInstance targetInstance)
      throws XMLStreamException {

    XmlEventUtil.skipWhitespace(reader);

    XMLEvent nextEvent = reader.peek();
    if (!nextEvent.isStartElement()) {
      return false;
    }

    QName nextQName = ObjectUtils.notNull(nextEvent.asStartElement().getName());

    if (nextQName.equals(targetInstance.getXmlGroupAsQName())) {
      // we are to parse the grouping element
      return true;
    }

    if (nextQName.equals(targetInstance.getXmlQName())) {
      // we are to parse the element
      return true;
    }

    if (targetInstance instanceof IBoundFieldInstance) {
      IBoundFieldInstance fieldInstance = (IBoundFieldInstance) targetInstance;
      IDataTypeAdapter<?> adapter = fieldInstance.getDefinition().getJavaTypeAdapter();
      // we are to parse the data type
      return !fieldInstance.isInXmlWrapped()
          && adapter.isUnrappedValueAllowedInXml()
          && adapter.canHandleQName(nextQName);
    }
    return false;
  }

  /**
   * Read the data associated with the {@code instance} and apply it to the
   * provided {@code parentObject}.
   *
   * @param instance
   *          the instance to parse data for
   * @param parentObject
   *          the Java object that data parsed by this method will be stored in
   * @param start
   *          the XML element start and attribute data previously parsed
   * @return {@code true} if the instance was parsed, or {@code false} if the data
   *         did not contain information for this instance
   * @throws IOException
   *           if an error occurred while parsing the input
   * @throws XMLStreamException
   *           if an error occurred while parsing XML events
   */
  protected boolean readModelInstanceValues(
      @NonNull IBoundNamedModelInstance instance,
      @NonNull Object parentObject,
      @NonNull StartElement start)
      throws IOException, XMLStreamException {
    boolean handled = isNextInstance(instance);
    if (handled) {
      XmlEventUtil.skipWhitespace(reader);

      StartElement currentStart = start;

      QName groupQName = instance.getXmlGroupAsQName();
      if (groupQName != null) {
        // we are to parse the grouping element, if the next token matches
        XMLEvent groupEvent = XmlEventUtil.consumeAndAssert(reader, XMLStreamConstants.START_ELEMENT, groupQName);
        currentStart = ObjectUtils.notNull(groupEvent.asStartElement());
      }

      IPropertyCollector collector = instance.getPropertyInfo().newPropertyCollector();
      // There are zero or more named values based on cardinality
      instance.getPropertyInfo().readValues(collector, parentObject, currentStart, this);

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

  @Override
  public <T> T readModelInstanceValue(IBoundNamedModelInstance instance, Object parentObject, StartElement start)
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
    return ObjectUtils.asNullableType(retval);
  }

  /**
   * Read the XML data associated with the {@code instance} and apply it to the
   * provided {@code parentObject}.
   *
   * @param instance
   *          the instance to parse data for
   * @param parentObject
   *          the Java object that data parsed by this method will be stored in
   * @param start
   *          the XML element start and attribute data previously parsed
   * @return the Java object read, or {@code null} if no data was read
   * @throws IOException
   *           if an error occurred while parsing the input
   * @throws XMLStreamException
   *           if an error occurred while parsing XML events
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
        retval = instance.getDataTypeHandler().readItem(parentObject, nextStart, this);

        // consume the end element
        XmlEventUtil.consumeAndAssert(reader, XMLStreamConstants.END_ELEMENT, nextQName);
      }
    }
    return retval;
  }

  /**
   * Revise
   * <p>
   * Reads an individual XML item from the XML stream.
   *
   * @param instance
   *          the instance to parse data for
   * @param parentObject
   *          the Java object that data parsed by this method will be stored in
   * @param start
   *          the XML element start and attribute data previously parsed
   * @return the Java object read, or {@code null} if no data was read
   * @throws IOException
   *           if an error occurred while parsing the input
   * @throws XMLStreamException
   *           if an error occurred while parsing XML events
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
        currentStart = ObjectUtils.notNull(reader.nextEvent().asStartElement());
      } else {
        throw new IOException(String.format("Found '%s' instead of expected element '%s'%s.",
            event.asStartElement().getName(),
            xmlQName,
            XmlEventUtil.generateLocationMessage(event)));
      }
    }

    // consume the value
    Object retval = instance.getDataTypeHandler().readItem(parentObject, currentStart, this);

    if (parseWrapper) {
      // consume the end element
      XmlEventUtil.consumeAndAssert(reader, XMLStreamConstants.END_ELEMENT, currentStart.getName());
    }

    return retval;
  }
}
