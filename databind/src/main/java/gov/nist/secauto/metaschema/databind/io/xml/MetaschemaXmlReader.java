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
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModel;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelFieldComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldValue;
import gov.nist.secauto.metaschema.databind.model.IBoundInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModel;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelFieldComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelFieldScalar;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedField;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedNamed;
import gov.nist.secauto.metaschema.databind.model.info.AbstractModelInstanceReadHandler;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureScalarItemValueHandler;
import gov.nist.secauto.metaschema.databind.model.info.IItemReadHandler;
import gov.nist.secauto.metaschema.databind.model.info.IModelInstanceCollectionInfo;

import org.codehaus.stax2.XMLEventReader2;

import java.io.IOException;
import java.util.Collection;
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
    implements IXmlParsingContext {
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

  public Object readItem(
      @NonNull Object item,
      @NonNull IBoundInstance instance,
      @NonNull StartElement start) throws IOException {
    return instance.readItem(item, new ItemReadHandler(start));
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
   * Parses the {@link XMLStreamConstants#START_DOCUMENT}, any processing
   * instructions, and the element.
   *
   * @param <CLASS>
   *          the returned object type
   * @param definition
   *          the definition describing the element data to read
   * @return the parsed object
   * @throws IOException
   *           if an error occurred while parsing the input
   */
  @Override
  @NonNull
  public <CLASS> CLASS read(@NonNull IBoundDefinitionModelComplex definition) throws IOException {
    try {
      // we may be at the START_DOCUMENT
      if (reader.peek().isStartDocument()) {
        XmlEventUtil.consumeAndAssert(reader, XMLStreamConstants.START_DOCUMENT);
      }

      // advance past any other info to get to next start element
      XmlEventUtil.skipEvents(reader, XMLStreamConstants.CHARACTERS, XMLStreamConstants.PROCESSING_INSTRUCTION,
          XMLStreamConstants.DTD);

      XMLEvent event = ObjectUtils.requireNonNull(reader.peek());
      if (!event.isStartElement()) {
        throw new IOException(
            String.format("The token '%s' is not an XML element%s.",
                XmlEventUtil.toEventName(event),
                XmlEventUtil.generateLocationMessage(event)));
      }

      ItemReadHandler handler = new ItemReadHandler(ObjectUtils.notNull(event.asStartElement()));
      return ObjectUtils.asType(definition.readItem(null, handler));
    } catch (XMLStreamException ex) {
      throw new IOException(ex);
    }
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
   */
  protected void readModelInstances(
      @NonNull IBoundDefinitionModelAssembly targetDefinition,
      @NonNull Object targetObject)
      throws IOException {
    Collection<? extends IBoundInstanceModel> instances = targetDefinition.getModelInstances();
    Set<IBoundInstanceModel> unhandledProperties = new HashSet<>();
    for (IBoundInstanceModel modelInstance : instances) {
      assert modelInstance != null;
      if (!readItems(modelInstance, targetObject, true)) {
        unhandledProperties.add(modelInstance);
      }
    }

    // process all properties that did not get a value
    getProblemHandler().handleMissingModelInstances(targetDefinition, targetObject, unhandledProperties);

    // handle any
    try {
      if (!getReader().peek().isEndElement()) {
        // handle any
        XmlEventUtil.skipWhitespace(getReader());
        XmlEventUtil.skipElement(getReader());
        XmlEventUtil.skipWhitespace(getReader());
      }

      XmlEventUtil.assertNext(getReader(), XMLStreamConstants.END_ELEMENT);
    } catch (XMLStreamException ex) {
      throw new IOException(ex);
    }
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
      retval = qname.equals(targetInstance.getEffectiveXmlGroupAsQName()) // parse the grouping element
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
   */
  @Override
  public boolean readItems(
      @NonNull IBoundInstanceModel instance,
      @NonNull Object parentObject,
      boolean parseGrouping)
      throws IOException {
    try {
      boolean handled = isNextInstance(instance);
      if (handled) {
        // XmlEventUtil.skipWhitespace(reader);

        QName groupQName = parseGrouping ? instance.getEffectiveXmlGroupAsQName() : null;
        if (groupQName != null) {
          // we need to parse the grouping element, if the next token matches
          XmlEventUtil.requireStartElement(reader, groupQName);
        }

        IModelInstanceCollectionInfo collectionInfo = instance.getCollectionInfo();

        ModelInstanceReadHandler handler = new ModelInstanceReadHandler(instance, parentObject);

        // let the property info decide how to parse the value
        Object value = collectionInfo.readItems(handler);
        instance.setValue(parentObject, value);

        // consume extra whitespace between elements
        XmlEventUtil.skipWhitespace(reader);

        if (groupQName != null) {
          // consume the end of the group
          XmlEventUtil.requireEndElement(reader, groupQName);
        }
      }
      return handled;
    } catch (XMLStreamException ex) {
      throw new IOException(ex);
    }
  }

  private final class ModelInstanceReadHandler
      extends AbstractModelInstanceReadHandler {

    private ModelInstanceReadHandler(
        @NonNull IBoundInstanceModel instance,
        @NonNull Object parentObject) {
      super(instance, parentObject);
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
                return ObjectUtils.requireNonNull(jsonKey.getValue(item)).toString();
              },
              Function.identity(),
              (t, u) -> u,
              LinkedHashMap::new)));
    }

    @NonNull
    private List<Object> readCollection() throws IOException {
      List<Object> retval = new LinkedList<>();
      try {
        // consume extra whitespace between elements
        XmlEventUtil.skipWhitespace(reader);

        IBoundInstanceModel instance = getCollectionInfo().getInstance();
        XMLEvent event;
        while ((event = reader.peek()).isStartElement()
            && instance.canHandleXmlQName(ObjectUtils.notNull(event.asStartElement().getName()))) {

          // Consume the start element
          Object value = readItem();
          retval.add(value);

          // consume extra whitespace between elements
          XmlEventUtil.skipWhitespace(reader);
        }
      } catch (XMLStreamException ex) {
        throw new IOException(ex);
      }
      return retval;
    }

    @Override
    public Object readItem() throws IOException {
      try {
        return getCollectionInfo().getInstance().readItem(
            getParentObject(),
            new ItemReadHandler(ObjectUtils.notNull(getReader().peek().asStartElement())));
      } catch (XMLStreamException ex) {
        throw new IOException(ex);
      }
    }
  }

  private final class ItemReadHandler implements IItemReadHandler {
    @NonNull
    private final StartElement startElement;

    private ItemReadHandler(@NonNull StartElement startElement) {
      this.startElement = startElement;
    }

    /**
     * Get the current start element.
     *
     * @return the startElement
     */
    @NonNull
    private StartElement getStartElement() {
      return startElement;
    }

    @NonNull
    private <DEF extends IBoundDefinitionModelComplex> Object readDefinitionElement(
        @NonNull DEF definition,
        @NonNull StartElement start,
        @NonNull QName expectedQName,
        @Nullable Object parent,
        @NonNull DefinitionBodyHandler<DEF> bodyHandler) throws IOException {
      try {
        // consume the start element
        XmlEventUtil.requireStartElement(reader, expectedQName);

        // construct the item
        Object item = definition.newInstance();

        // call pre-parse initialization hook
        definition.callBeforeDeserialize(item, parent);

        // read the flags
        readFlagInstances(definition, item, start);

        // read the body
        bodyHandler.accept(definition, item);

        XmlEventUtil.skipWhitespace(reader);

        // call post-parse initialization hook
        definition.callAfterDeserialize(item, parent);

        // consume the end element
        XmlEventUtil.requireEndElement(reader, expectedQName);
        return ObjectUtils.asType(item);
      } catch (BindingException | XMLStreamException ex) {
        throw new IOException(ex);
      }
    }

    @Override
    public Object readItemFlag(
        Object parent,
        IBoundInstanceFlag flag) throws IOException {
      throw new UnsupportedOperationException("handled by readFlagInstances()");
    }

    private void handleFieldDefinitionBody(
        @NonNull IBoundDefinitionModelFieldComplex definition,
        @NonNull Object item) throws IOException {
      IBoundFieldValue fieldValue = definition.getFieldValue();

      // parse the value
      Object value = fieldValue.readItem(item, this);
      fieldValue.setValue(item, value);
    }

    @Override
    public Object readItemField(
        Object parent,
        IBoundInstanceModelFieldScalar instance)
        throws IOException {

      try {
        QName wrapper = null;
        if (instance.isEffectiveValueWrappedInXml()) {
          wrapper = instance.getXmlQName();

          XmlEventUtil.skipWhitespace(getReader());
          XmlEventUtil.requireStartElement(getReader(), wrapper);
        }

        Object retval = readScalarItem(instance);

        if (wrapper != null) {
          XmlEventUtil.skipWhitespace(getReader());

          XmlEventUtil.requireEndElement(getReader(), wrapper);
        }
        return retval;
      } catch (XMLStreamException ex) {
        throw new IOException(ex);
      }
    }

    @Override
    public Object readItemField(
        Object parent,
        IBoundInstanceModelFieldComplex instance)
        throws IOException {
      return readDefinitionElement(
          instance.getDefinition(),
          getStartElement(),
          instance.getXmlQName(),
          parent,
          this::handleFieldDefinitionBody);
    }

    @Override
    public Object readItemField(Object parent, IBoundInstanceModelGroupedField instance) throws IOException {
      return readDefinitionElement(
          instance.getDefinition(),
          getStartElement(),
          instance.getXmlQName(),
          parent,
          this::handleFieldDefinitionBody);
    }

    @Override
    public Object readItemField(
        Object parent,
        IBoundDefinitionModelFieldComplex definition) throws IOException {
      return readDefinitionElement(
          definition,
          getStartElement(),
          definition.getXmlQName(),
          parent,
          this::handleFieldDefinitionBody);
    }

    @Override
    public Object readItemFieldValue(
        Object parent,
        IBoundFieldValue fieldValue) throws IOException {
      return readScalarItem(fieldValue);
    }

    private void handleAssemblyDefinitionBody(
        @NonNull IBoundDefinitionModelAssembly definition,
        @NonNull Object item) throws IOException {
      readModelInstances(definition, item);
    }

    @Override
    public Object readItemAssembly(
        Object parent,
        IBoundInstanceModelAssembly instance) throws IOException {
      return readDefinitionElement(
          instance.getDefinition(),
          getStartElement(),
          instance.getXmlQName(),
          parent,
          this::handleAssemblyDefinitionBody);
    }

    @Override
    public Object readItemAssembly(Object parent, IBoundInstanceModelGroupedAssembly instance) throws IOException {
      return readDefinitionElement(
          instance.getDefinition(),
          getStartElement(),
          instance.getXmlQName(),
          parent,
          this::handleAssemblyDefinitionBody);
    }

    @Override
    public Object readItemAssembly(
        Object parent,
        IBoundDefinitionModelAssembly definition) throws IOException {
      return readDefinitionElement(
          definition,
          getStartElement(),
          ObjectUtils.requireNonNull(definition.getRootXmlQName()),
          parent,
          this::handleAssemblyDefinitionBody);
    }

    @NonNull
    private Object readScalarItem(@NonNull IFeatureScalarItemValueHandler handler)
        throws IOException {
      return handler.getJavaTypeAdapter().parse(getReader());
    }

    @Override
    public Object readChoiceGroupItem(Object parent, IBoundInstanceModelChoiceGroup instance) throws IOException {
      try {
        XMLEventReader2 eventReader = getReader();
        // consume extra whitespace between elements
        XmlEventUtil.skipWhitespace(eventReader);

        XMLEvent event = eventReader.peek();
        QName nextQName = ObjectUtils.notNull(event.asStartElement().getName());
        IBoundInstanceModelGroupedNamed actualInstance = instance.getGroupedModelInstance(nextQName);
        assert actualInstance != null;
        return actualInstance.readItem(parent, this);
      } catch (XMLStreamException ex) {
        throw new IOException(ex);
      }
    }
  }

  @FunctionalInterface
  private interface DefinitionBodyHandler<DEF extends IBoundDefinitionModelComplex> {
    void accept(
        @NonNull DEF definition,
        @NonNull Object item) throws IOException;
  }
}
