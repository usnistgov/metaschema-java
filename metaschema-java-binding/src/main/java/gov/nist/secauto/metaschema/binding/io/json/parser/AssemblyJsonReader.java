/**
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

package gov.nist.secauto.metaschema.binding.io.json.parser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.RootWrapper;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class AssemblyJsonReader<CLASS> extends AbstractJsonReader<CLASS, AssemblyClassBinding<CLASS>,
    SingleBoundObjectParser<CLASS, AssemblyClassBinding<CLASS>>> {
  private static final Logger logger = LogManager.getLogger(AssemblyJsonReader.class);

  public AssemblyJsonReader(AssemblyClassBinding<CLASS> classBinding) {
    super(classBinding);
  }

  protected Map<PropertyBinding, Supplier<?>> handleUnknownProperty(JsonParsingContext parsingContext)
      throws BindingException {
    try {
      // TODO: log a warning?
      JsonUtil.skipValue(parsingContext.getEventReader());
    } catch (IOException ex) {
      throw new BindingException(ex);
    }
    return Collections.emptyMap();
  }

  @Override
  protected SingleBoundObjectParser<CLASS, AssemblyClassBinding<CLASS>> newObjectParser(PropertyBindingFilter filter,
      JsonParsingContext parsingContext) throws BindingException {
    return new SingleBoundObjectParser<>(getClassBinding(), filter, parsingContext,
        (fieldName, props, context) -> handleUnknownProperty(context));
  }

  @Override
  protected List<CLASS> readJsonInternal(SingleBoundObjectParser<CLASS, AssemblyClassBinding<CLASS>> parser,
      Object parent, boolean parseRoot) throws BindingException {
    List<CLASS> retval;
    if (parseRoot) {
      try {
        retval = parseRoot(parser);
      } catch (IOException ex) {
        throw new BindingException(ex);
      }
    } else {
      retval = super.readJsonInternal(parser, parent, false);
    }
    return retval;
  }

  protected List<CLASS> parseRoot(SingleBoundObjectParser<CLASS, AssemblyClassBinding<CLASS>> objParser)
      throws BindingException, IOException {
    JsonParsingContext parsingContext = objParser.getParsingContext();
    JsonParser parser = parsingContext.getEventReader();
    RootWrapper rootWrapper = getClassBinding().getRootWrapper();
    String rootName = rootWrapper.name();
    String[] ignoreFieldsArray = rootWrapper.ignoreJsonProperties();
    Set<String> ignoreRootFields;
    if (ignoreFieldsArray == null || ignoreFieldsArray.length == 0) {
      ignoreRootFields = Collections.emptySet();
    } else {
      ignoreRootFields = new HashSet<>(Arrays.asList(ignoreFieldsArray));
    }

    List<CLASS> retval = Collections.emptyList();
    JsonToken token;
    while ((token = parser.nextToken()) != null) {
      // logger.info("Token: {}", token.toString());
      if (JsonToken.END_OBJECT.equals(token)) {
        break;
      } else if (!JsonToken.FIELD_NAME.equals(token)) {
        throw new BindingException(String.format("Expected FIELD_NAME token, found '%s'", token.toString()));
      }

      String fieldName = parser.currentName();
      if (fieldName.equals(rootName)) {
        // process the object value, bound to the requested class
        JsonUtil.readNextToken(parser, JsonToken.START_OBJECT);
        retval = super.readJsonInternal(objParser, null, false);
      } else if (ignoreRootFields.contains(fieldName)) {
        // ignore the field
        JsonUtil.skipValue(parser);
      } else {
        if (!parsingContext.getProblemHandler().handleUnknownRootProperty(objParser.getInstance(), getClassBinding(),
            fieldName, parsingContext)) {
          logger.warn("Skipping unhandled top-level JSON field '{}'.", fieldName);
          JsonUtil.skipValue(parser);
        }
      }
    }
    JsonUtil.expectCurrentToken(parser, null);

    return retval;
  }

}
