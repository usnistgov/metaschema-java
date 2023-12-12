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

import gov.nist.secauto.metaschema.core.model.util.XmlEventUtil;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionFieldComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModel;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldValue;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModel;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelFieldComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelFieldScalar;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedNamed;
import gov.nist.secauto.metaschema.databind.model.info.AbstractModelInstanceReadHandler;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureComplexItemValueHandler;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureScalarItemValueHandler;
import gov.nist.secauto.metaschema.databind.model.info.IItemReadHandler;
import gov.nist.secauto.metaschema.databind.model.info.IModelInstanceCollectionInfo;

import org.codehaus.stax2.XMLEventReader2;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
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
    implements IXmlParsingContext, IItemReadHandler {
  @NonNull
  private final XMLEventReader2 reader;
  @NonNull
  private final IXmlProblemHandler problemHandler;

  /**
   * Construct a new Module-aware XML parser using the default problem handler.
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
   * Construct a new Module-aware parser.
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
  // TODO: generalize this to work with any
  @NonNull
  public <CLASS> CLASS read(@NonNull IBoundDefinitionAssembly targetDefinition) throws IOException, XMLStreamException {

    // we may be at the START_DOCUMENT
    if (reader.peek().isStartDocument()) {
      XmlEventUtil.consumeAndAssert(reader, XMLStreamConstants.START_DOCUMENT);
    }

    XmlEventUtil.skipEvents(reader, XMLStreamConstants.CHARACTERS, XMLStreamConstants.PROCESSING_INSTRUCTION);

    XMLEvent event = ObjectUtils.requireNonNull(reader.peek());
    QName rootQName = targetDefinition.getRootXmlQName();
    CLASS retval;
    if (rootQName == null) {
      throw new IOException(
          String.format("Not a root assembly'%s.",
              XmlEventUtil.generateLocationMessage(event)));
    } else if (XmlEventUtil.isEventStartElement(event, rootQName) && targetDefinition.canHandleXmlQName(rootQName)) {
      ItemReadHandler handler = new ItemReadHandler(ObjectUtils.notNull(event.asStartElement()));
      retval = ObjectUtils.asType(targetDefinition.readItem(null, handler));

      // consume the end element
      XmlEventUtil.consumeAndAssert(reader, XMLStreamConstants.END_ELEMENT, rootQName);
    } else {
      throw new IOException(
          String.format("XML element not '%s'%s.",
              rootQName,
              XmlEventUtil.generateLocationMessage(event)));
    }
    return retval;
  }

  /**
   * Read the XML attribute data described by the {@code targetDefinition} and
   * apply it to the provided {@code targetObject}.
   *
   * @param targetDefinition
   *          the Module definition that describes the syntax of the data to read
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
      @NonNull IBoundDefinitionModel targetDefinition,
      @NonNull Object targetObject,
      @NonNull StartElement start) throws IOException, XMLStreamException {

    Map<QName, IBoundInstanceFlag> flagInstanceMap = targetDefinition.getFlagInstances().stream()
        .collect(Collectors.toMap(
            IBoundInstanceFlag::getXmlQName,
            Function.identity()));

    for (Attribute attribute : CollectionUtil.toIterable(ObjectUtils.notNull(start.getAttributes()))) {
      QName qname = attribute.getName();
      IBoundInstanceFlag instance = flagInstanceMap.get(qname);
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
   *          the Module definition that describes the syntax of the data to read
   * @param targetObject
   *          the Java object that data parsed by this method will be stored in
   * @throws IOException
   *           if an error occurred while parsing the input
   * @throws XMLStreamException
   *           if an error occurred while parsing XML events
   */
  protected void readModelInstances(
      @NonNull IBoundDefinitionAssembly targetDefinition,
      @NonNull Object targetObject)
      throws IOException, XMLStreamException {
    Set<IBoundInstanceModel> unhandledProperties = new HashSet<>();
    for (IBoundInstanceModel modelInstance : targetDefinition.getModelInstances()) {
      assert modelInstance != null;
      if (!readModelInstanceItems(modelInstance, targetObject)) {
        unhandledProperties.add(modelInstance);
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
   *          the Module definition that describes the syntax of the data to read
   * @param targetObject
   *          the Java object that data parsed by this method will be stored in
   * @throws IOException
   *           if an error occurred while parsing the input
   */
  protected void readFieldValue(
      @NonNull IBoundDefinitionFieldComplex targetDefinition,
      @NonNull Object targetObject)
      throws IOException {
    IBoundFieldValue fieldValue = targetDefinition.getFieldValue();

    // parse the value
    Object value = fieldValue.getJavaTypeAdapter().parse(reader);
    fieldValue.setValue(targetObject, value);
  }

  /**
   * Determine if the next data to read corresponds to the next model instance.
   *
   * @param targetInstance
   *          the model instance that describes the syntax of the data to read
   * @return {@code true} if the Module instance needs to be parsed, or
   *         {@code false} otherwise
   * @throws XMLStreamException
   *           if an error occurred while parsing XML events
   */
  @SuppressWarnings("PMD.OnlyOneReturn")
  protected boolean isNextInstance(
      @NonNull IBoundInstanceModel targetInstance)
      throws XMLStreamException {

    XmlEventUtil.skipWhitespace(reader);

    XMLEvent nextEvent = reader.peek();

    boolean retval = nextEvent.isStartElement();
    if (retval) {
      QName qname = ObjectUtils.notNull(nextEvent.asStartElement().getName());
      retval = qname.equals(targetInstance.getXmlGroupAsQName()) // parse the grouping element
          || targetInstance.canHandleXmlQName(qname); // parse the instance(s)
    }
    return retval;
  }

  /**
   * Read the data associated with the {@code instance} and apply it to the
   * provided {@code parentObject}.
   *
   * @param instance
   *          the instance to parse data for
   * @param parentObject
   *          the Java object that data parsed by this method will be stored in
   * @return {@code true} if the instance was parsed, or {@code false} if the data
   *         did not contain information for this instance
   * @throws IOException
   *           if an error occurred while parsing the input
   * @throws XMLStreamException
   *           if an error occurred while parsing XML events
   */
  protected boolean readModelInstanceItems(
      @NonNull IBoundInstanceModel instance,
      @NonNull Object parentObject)
      throws IOException, XMLStreamException {
    boolean handled = isNextInstance(instance);
    if (handled) {
      // XmlEventUtil.skipWhitespace(reader);

      StartElement currentStart = reader.peek().asStartElement();

      QName groupQName = instance.getXmlGroupAsQName();
      if (groupQName != null) {
        // we are to parse the grouping element, if the next token matches
        XMLEvent groupEvent = XmlEventUtil.consumeAndAssert(reader, XMLStreamConstants.START_ELEMENT, groupQName);
        currentStart = ObjectUtils.notNull(groupEvent.asStartElement());
      }

      IModelInstanceCollectionInfo collectionInfo = instance.getCollectionInfo();

      ModelInstanceReadHandler handler = new ModelInstanceReadHandler(
          collectionInfo,
          parentObject,
          currentStart);

      // let the property info decide how to parse the value
      Object value = collectionInfo.readItems(handler);
      if (value != null) {
        instance.setValue(parentObject, value);
      }

      // consume extra whitespace between elements
      XmlEventUtil.skipWhitespace(reader);

      if (groupQName != null) {
        // consume the end of the group
        XmlEventUtil.consumeAndAssert(reader, XMLStreamConstants.END_ELEMENT, groupQName);
      }
    }
    return handled;
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
   */
  @Override
  public <T> T readModelInstanceValueDispatch(
      IBoundInstanceModel instance,
      Object parentObject,
      StartElement start) throws IOException {
    // figure out if we need to parse the wrapper or not

    try {
      XMLEvent event;
      StartElement wrapper = null;
      if (instance.isValueWrappedInXml() && instance.canHandleXmlQName(start.getName())) {
        // Consume the start element
        reader.nextEvent();
        wrapper = start;
        event = reader.peek();

        // consume extra whitespace between elements
        XmlEventUtil.skipWhitespace(reader);
      } else {
        event = start;
      }

      Object retval = null;
      if (wrapper != null
          || ((event.isStartElement() && instance.canHandleXmlQName(event.asStartElement().getName())))) {
        // consume the value
        ItemReadHandler handler = new ItemReadHandler(reader.nextEvent().asStartElement());
        retval = instance.readItem(parentObject, handler);
      }

      if (wrapper != null) {
        // consume the end element
        XmlEventUtil.consumeAndAssert(reader, XMLStreamConstants.END_ELEMENT, wrapper.getName());
      }
      return ObjectUtils.asNullableType(retval);
    } catch (XMLStreamException ex) {
      throw new IOException(ex);
    }
  }

  private class ModelInstanceReadHandler
      extends AbstractModelInstanceReadHandler {
    @NonNull
    private final StartElement startElement;

    private ModelInstanceReadHandler(
        @NonNull IModelInstanceCollectionInfo collectionInfo,
        @NonNull Object parentObject,
        @NonNull StartElement startElement) {
      super(collectionInfo, parentObject);
      this.startElement = startElement;
    }

    /**
     * Get the parent start element.
     *
     * @return the startElement
     */
    @NonNull
    protected StartElement getStartElement() {
      return startElement;
    }

    @Override
    public List<?> readList() throws IOException {
      return ObjectUtils.notNull(readCollection());
    }

    @Override
    public Map<String, ?> readMap() throws IOException {
      IBoundInstanceModel instance = getCollectionInfo().getInstance();

      return ObjectUtils.notNull(readCollection().stream()
          .collect(Collectors.toMap(
              item -> {
                assert item != null;

                IBoundInstanceFlag jsonKey = instance.getItemJsonKey(item);
                assert jsonKey != null;
                return jsonKey.getValue(item).toString();
              },
              Function.identity(),
              (t, u) -> u,
              LinkedHashMap::new)));
    }

    @NonNull
    private List<Object> readCollection() throws IOException {
      XMLEventReader2 eventReader = getReader();

      List<Object> retval = new LinkedList<>();
      try {
        // TODO: is this needed?
        // consume extra whitespace between elements
        XmlEventUtil.skipWhitespace(eventReader);

        IBoundInstanceModel instance = getCollectionInfo().getInstance();
        XMLEvent event;
        while ((event = eventReader.peek()).isStartElement()
            && instance.canHandleXmlQName(ObjectUtils.notNull(event.asStartElement().getName()))) {

          // Consume the start element
          Object value = readItem();
          if (value != null) {
            retval.add(value);
          }

          // consume extra whitespace between elements
          XmlEventUtil.skipWhitespace(eventReader);
        }
      } catch (XMLStreamException ex) {
        throw new IOException(ex);
      }
      return retval;
    }

    @Override
    public Object readItem() throws IOException {
      IBoundInstanceModel instance = getCollectionInfo().getInstance();
      Object parentObject = getParentObject();
      StartElement start = getStartElement();
      return readModelInstanceValueDispatch(instance, parentObject, start);
    }
  }

  private class ItemReadHandler implements IItemReadHandler {
    @NonNull
    private final StartElement startElement;

    private ItemReadHandler(@NonNull StartElement startElement) {
      this.startElement = startElement;
    }

    /**
     * @return the startElement
     */
    @NonNull
    protected StartElement getStartElement() {
      return startElement;
    }

    @Override
    public Object readItemFlag(
        Object parent,
        IBoundInstanceFlag flag) throws IOException {
      throw new UnsupportedOperationException("handled by readFlagInstances()");
    }

    @Override
    public Object readItemField(
        Object parent,
        IBoundInstanceModelFieldScalar field)
        throws IOException {
      return readScalarItem(field);
    }

    @Override
    public Object readItemField(
        Object parent,
        IBoundInstanceModelFieldComplex field)
        throws IOException {
      return readComplexItem(parent, field);
    }

    @Override
    public Object readItemField(
        Object parent,
        IBoundDefinitionFieldComplex field) throws IOException {
      return readComplexItem(parent, field);
    }

    @Override
    public Object readItemFieldValue(
        Object parent,
        IBoundFieldValue fieldValue) throws IOException {
      return readScalarItem(fieldValue);
    }

    @Override
    public Object readItemAssembly(
        Object parent,
        IBoundInstanceModelAssembly assembly) throws IOException {
      return readComplexItem(parent, assembly);
    }

    @Override
    public Object readItemAssembly(
        Object parent,
        IBoundDefinitionAssembly assembly) throws IOException {
      return readComplexItem(parent, assembly);
    }

    @NonNull
    private Object readScalarItem(@NonNull IFeatureScalarItemValueHandler handler)
        throws IOException {
      return handler.getJavaTypeAdapter().parse(getReader());
    }

    @NonNull
    private Object readComplexItem(
        @Nullable Object parent,
        @NonNull IFeatureComplexItemValueHandler handler)
        throws IOException {

      StartElement start = getStartElement();
      IBoundDefinitionModel definition = handler.getDefinition();

      Object targetObject;
      try {
        targetObject = handler.newInstance();
        handler.callBeforeDeserialize(targetObject, parent);
      } catch (BindingException ex) {
        throw new IOException(ex);
      }

      try {
        readFlagInstances(definition, targetObject, start);

        if (definition instanceof IBoundDefinitionAssembly) {
          readModelInstances((IBoundDefinitionAssembly) definition, targetObject);
        } else if (definition instanceof IBoundDefinitionFieldComplex) {
          IBoundFieldValue fieldValue = ((IBoundDefinitionFieldComplex) definition).getFieldValue();

          // parse the value
          Object value = fieldValue.readItem(parent, this);
          fieldValue.setValue(targetObject, value);
        } else {
          throw new UnsupportedOperationException(
              String.format("Unsupported class binding type: %s", definition.getClass().getName()));
        }

        XmlEventUtil.skipWhitespace(reader);

        // XMLEvent nextEvent = ObjectUtils.notNull(reader.peek());
        // if (!XmlEventUtil.isEventEndElement(nextEvent,
        // ObjectUtils.notNull(start.getName()))) {
        // throw new IOException(
        // String.format("Unrecognized content %s.",
        // XmlEventUtil.toString(nextEvent)));
        // }
      } catch (XMLStreamException ex) {
        throw new IOException(ex);
      }

      try {
        handler.callAfterDeserialize(targetObject, parent);
      } catch (BindingException ex) {
        throw new IOException(ex);
      }
      return ObjectUtils.asType(targetObject);
    }

    @Override
    public Object readChoiceGroupItem(Object parent, IBoundInstanceModelChoiceGroup instance) throws IOException {
      Map<QName, IBoundInstanceModelGroupedNamed> qnameToInstanceMap = instance.getQNameToInstanceMap();

      try {
        XMLEventReader2 eventReader = getReader();
        // consume extra whitespace between elements
        XmlEventUtil.skipWhitespace(eventReader);

        XMLEvent event = eventReader.peek();
        QName nextQName = event.asStartElement().getName();
        IBoundInstanceModelGroupedNamed actualInstance = qnameToInstanceMap.get(nextQName);
        return actualInstance.readItem(parent, this);
        // return
        // ObjectUtils.requireNonNull(readModelInstanceValueDispatch(actualInstance,
        // parent, startElement));
      } catch (XMLStreamException ex) {
        throw new IOException(ex);
      }
    }
  }

}
