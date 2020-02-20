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

package gov.nist.secauto.metaschema.binding.io.xml.parser;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;

import java.util.Map;
import java.util.Objects;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class FieldXmlParsePlan<CLASS> extends AbstractXmlParsePlan<CLASS, FieldClassBinding<CLASS>> {
  private static final Logger logger = LogManager.getLogger(FieldXmlParsePlan.class);

  private final FieldValueXmlPropertyParser fieldValueParser;

  public FieldXmlParsePlan(FieldClassBinding<CLASS> classBinding, BindingContext bindingContext)
      throws BindingException {
    this(classBinding, newXmlAttributeParsers(classBinding, bindingContext),
        classBinding.getFieldValuePropertyBinding().newXmlPropertyParser(bindingContext));
  }

  public FieldXmlParsePlan(FieldClassBinding<CLASS> classBinding,
      Map<QName, XmlAttributePropertyParser> attributeParsers, FieldValueXmlPropertyParser fieldValueParser) {
    super(classBinding, attributeParsers);
    Objects.requireNonNull(fieldValueParser, "fieldValueParser");
    this.fieldValueParser = fieldValueParser;
  }

  protected FieldValueXmlPropertyParser getFieldValueParser() {
    return fieldValueParser;
  }

  /**
   * This will be called on the next element after the field START_ELEMENT after any attributes have
   * been parsed. The parser will continue until the end element for the field is reached.
   */
  @Override
  protected void parseBody(CLASS obj, XmlParsingContext parsingContext, StartElement start) throws BindingException {
    XMLEventReader2 reader = parsingContext.getEventReader();
    try {
      XMLEvent nextEvent = XmlEventUtil.skipWhitespace(reader);
      if (logger.isDebugEnabled()) {
        logger.debug("Field Body: {}", XmlEventUtil.toString(nextEvent));
      }

      FieldValueXmlPropertyParser fieldValueParser = getFieldValueParser();
      fieldValueParser.parse(obj, parsingContext);

      nextEvent = reader.peek();

      // skip inter-element whitespace
      nextEvent = XmlEventUtil.skipWhitespace(reader);

      if (logger.isDebugEnabled()) {
        logger.debug("Field Body(after): {}", XmlEventUtil.toString(nextEvent));
      }

      if (!nextEvent.isEndElement()) {
        // skip extra content
        // TODO: problem handler?
        nextEvent = XmlEventUtil.advanceTo(reader, XMLStreamConstants.END_ELEMENT);
      }

      // the parser is now at the END_ELEMENT for this field
      assert XmlEventUtil.isNextEventEndElement(reader, start.getName()) : XmlEventUtil.toString(nextEvent);

      // the AbstractXmlParsePlan caller will advance past the END_ELEMENT for this
      // field
      if (logger.isDebugEnabled()) {
        logger.debug("Field Body(end): {}", XmlEventUtil.toString(reader.peek()));
      }
    } catch (XMLStreamException ex) {
      throw new BindingException("Parse error", ex);
    }
  }
}
