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
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.ModelItemPropertyBinding;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class AssemblyXmlParsePlan<CLASS> extends AbstractXmlParsePlan<CLASS, AssemblyClassBinding<CLASS>> {
  private static final Logger logger = LogManager.getLogger(AssemblyXmlParsePlan.class);

  private final List<XmlObjectPropertyParser> modelParsers;

  public AssemblyXmlParsePlan(AssemblyClassBinding<CLASS> classBinding, BindingContext bindingContext)
      throws BindingException {
    this(classBinding, newXmlAttributeParsers(classBinding, bindingContext),
        newModelParsers(classBinding, bindingContext));
  }

  public AssemblyXmlParsePlan(AssemblyClassBinding<CLASS> classBinding,
      Map<QName, XmlAttributePropertyParser> attributeParsers, List<XmlObjectPropertyParser> modelParsers) {
    super(classBinding, attributeParsers);
    Objects.requireNonNull(modelParsers, "modelParsers");
    this.modelParsers = modelParsers;
  }

  protected List<XmlObjectPropertyParser> getModelParsers() {
    return modelParsers;
  }

  protected static <CLASS> List<XmlObjectPropertyParser> newModelParsers(AssemblyClassBinding<CLASS> classBinding,
      BindingContext bindingContext) throws BindingException {
    List<ModelItemPropertyBinding> bindings = classBinding.getModelItemPropertyBindings();
    List<XmlObjectPropertyParser> retval;
    if (bindings.isEmpty()) {
      retval = Collections.emptyList();
    } else {
      retval = new ArrayList<>(bindings.size());
      for (ModelItemPropertyBinding binding : bindings) {
        retval.add(binding.newXmlPropertyParser(bindingContext));
      }
      retval = Collections.unmodifiableList(retval);
    }
    return retval;
  }

  /**
   * This will be called on the next element after the assembly START_ELEMENT after any attributes
   * have been parsed. The parser will continue until the end element for the assembly is reached.
   */
  @Override
  protected void parseBody(CLASS obj, XmlParsingContext parsingContext, StartElement start) throws BindingException {
    XMLEventReader2 reader = parsingContext.getEventReader();
    try {
      XMLEvent nextEvent;
      for (XmlObjectPropertyParser modelParser : getModelParsers()) {

        nextEvent = XmlEventUtil.skipWhitespace(reader);

        if (logger.isDebugEnabled()) {
          logger.debug("Assembly Body: {}", XmlEventUtil.toString(nextEvent));
        }

        if (nextEvent.isEndElement()) {
          // TODO: handle unparsed elements
          break;
        }

        StartElement nextStart = nextEvent.asStartElement();
        QName nextName = nextStart.getName();

        if (modelParser.canConsume(nextName)) {
          // the parser will consume the START_ELEMENT event
          modelParser.parse(obj, parsingContext);
          nextEvent = reader.peek();
          // } else {
          // logger.debug("Assembly Body Element(skipping): {}", nextName.toString());
          // nextEvent = XmlEventUtil.advanceTo(reader, XMLStreamConstants.END_ELEMENT);
        }

        // skip inter-element whitespace
        nextEvent = XmlEventUtil.skipWhitespace(reader);

        // the parser should be now at the next child START_ELEMENT or the assembly's END_ELEMENT
        assert nextEvent.isStartElement() || XmlEventUtil.isNextEventEndElement(reader, start.getName()) : XmlEventUtil
            .toString(nextEvent);

        if (logger.isDebugEnabled()) {
          logger.debug("Assembly Body Element(after parse): {}", XmlEventUtil.toString(nextEvent));
        }
        //
        // // Advance only if the child is wrapped
        // if (modelParser.isChildWrappedInXml()) {
        // nextEvent = reader.nextEvent();
        // }
      }

      nextEvent = reader.peek();
      if (!nextEvent.isEndElement()) {
        nextEvent = XmlEventUtil.advanceTo(reader, XMLStreamConstants.END_ELEMENT);
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Assembly Body(check): {}", XmlEventUtil.toString(nextEvent));
      }
      // the parser is now at the END_ELEMENT for this assembly
      assert XmlEventUtil.isNextEventEndElement(reader, start.getName()) : XmlEventUtil.toString(nextEvent);

      // the AbstractXmlParsePlan caller will advance past the END_ELEMENT for this assembly
      if (logger.isDebugEnabled()) {
        logger.debug("Assembly Body(end): {}", XmlEventUtil.toString(reader.peek()));
      }
    } catch (XMLStreamException ex) {
      throw new BindingException("Parse error", ex);
    }
  }
}
