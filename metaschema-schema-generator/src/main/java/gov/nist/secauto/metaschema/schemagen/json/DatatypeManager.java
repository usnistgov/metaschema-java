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

package gov.nist.secauto.metaschema.schemagen.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.nist.secauto.metaschema.model.MetaschemaLoader;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.definition.INamedDefinition;
import gov.nist.secauto.metaschema.model.common.definition.INamedModelDefinition;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DatatypeManager {
  private static final JsonNode jsonDatatypes;
  private static final Map<String, String> datatypeTranslationMap = new LinkedHashMap<>();
  private static final Map<String, String> jsonDatatypeDependencyMap = new HashMap<>();
  private static final Pattern DEFINITION_REF_PATTERN = Pattern.compile("^#/definitions/(.+)$");

  static {
    datatypeTranslationMap.put("base64", "Base64Datatype");
    datatypeTranslationMap.put("boolean", "BooleanDatatype");
    datatypeTranslationMap.put("date", "DateDatatype");
    datatypeTranslationMap.put("date-with-timezone", "DateWithTimezoneDatatype");
    datatypeTranslationMap.put("date-time", "DateTimeDatatype");
    datatypeTranslationMap.put("date-time-with-timezone", "DateTimeWithTimezoneDatatype");
    datatypeTranslationMap.put("day-time-duration", "DayTimeDurationDatatype");
    datatypeTranslationMap.put("decimal", "DecimalDatatype");
    datatypeTranslationMap.put("email-address", "EmailAddressDatatype");
    datatypeTranslationMap.put("hostname", "HostnameDatatype");
    datatypeTranslationMap.put("integer", "IntegerDatatype");
    datatypeTranslationMap.put("ip-v4-address", "IPV4AddressDatatype");
    datatypeTranslationMap.put("ip-v6-address", "IPV6AddressDatatype");
    datatypeTranslationMap.put("markup-line", "MarkupLineDatatype");
    datatypeTranslationMap.put("markup-multiline", "MarkupMultilineDatatype");
    datatypeTranslationMap.put("non-negative-integer", "NonNegativeIntegerDatatype");
    datatypeTranslationMap.put("positive-integer", "PositiveIntegerDatatype");
    datatypeTranslationMap.put("string", "StringDatatype");
    datatypeTranslationMap.put("token", "TokenDatatype");
    datatypeTranslationMap.put("uri", "URIDatatype");
    datatypeTranslationMap.put("uri-reference", "URIReferenceDatatype");
    datatypeTranslationMap.put("uuid", "UUIDDatatype");
    datatypeTranslationMap.put("year-month-duration", "YearMonthDurationDatatype");

    try (InputStream is
        = MetaschemaLoader.class.getClassLoader().getResourceAsStream("schema/json/metaschema-datatypes.json")) {
      ObjectMapper objectMapper = new ObjectMapper();
      jsonDatatypes = objectMapper.readTree(is);
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }

    // analyze datatypes for dependencies
    for (String ref : datatypeTranslationMap.values()) {
      JsonNode refNode = jsonDatatypes.at("/definitions/" + ref + "/$ref");
      if (!refNode.isMissingNode()) {
        Matcher matcher = DEFINITION_REF_PATTERN.matcher(refNode.asText());
        if (matcher.matches()) {
          String dependency = matcher.group(1);
          jsonDatatypeDependencyMap.put(ref, dependency);
        }
      }
    }
  }

  public static JsonNode getJsonDatatypes() {
    return jsonDatatypes;
  }

  @NotNull
  private final Map<@NotNull IJavaTypeAdapter<?>, String> dataTypeToDefinitionReferenceMap = new HashMap<>();

  public void generateDatatypes(@NotNull JsonGenerator jsonGenerator) throws IOException {
    Set<String> requiredJsonDatatypes = new HashSet<>();
    // resolve dependencies
    for (String jsonDataType : dataTypeToDefinitionReferenceMap.values()) {
      requiredJsonDatatypes.add(jsonDataType);
      String dependency = jsonDatatypeDependencyMap.get(jsonDataType);
      if (dependency != null) {
        requiredJsonDatatypes.add(dependency);
      }
    }
    List<String> requiredDatatypes = requiredJsonDatatypes.stream().sorted().collect(Collectors.toList());

    JsonNode datatypesSchemaNode = getJsonDatatypes();
    for (String ref : requiredDatatypes) {
      JsonNode datatypeNode = datatypesSchemaNode.at("/definitions/" + ref);
      if (datatypeNode.isMissingNode()) {
        throw new IOException("Missing JSON datatype definition for: /definitions/"+ref);
      }

      jsonGenerator.writeFieldName(ref);
      // jsonGenerator.writeTree(datatypeNode);
      jsonGenerator.writeTree(datatypeNode);
    }
  }

  @NotNull
  protected CharSequence getJsonDefinitionRefForDefinition(@NotNull INamedDefinition definition) {
    return new StringBuilder()
        .append("#/definitions/")
        .append(getJsonDefinitionNameForDefinition(definition));
  }

  @NotNull
  protected CharSequence getJsonDefinitionNameForDefinition(@NotNull INamedDefinition definition) {
    StringBuilder builder = new StringBuilder()
        .append(definition.getContainingMetaschema().getShortName())
        .append('-')
        .append(getTypeNameForDefinition(definition))
        .append('-')
        .append(definition.getModelType())
        .append('-')
        .append("Type");
    return toCamelCase(builder.toString());
  }

  @NotNull
  protected CharSequence getJsonDefinitionRefForDatatype(@NotNull IJavaTypeAdapter<?> datatype) {
    return new StringBuilder()
        .append("#/definitions/")
        .append(getJsonDefinitionNameForDatatype(datatype));
  }

  @NotNull
  protected String getJsonDefinitionNameForDatatype(@NotNull IJavaTypeAdapter<?> datatype) {
    synchronized (this) {
      String name = dataTypeToDefinitionReferenceMap.get(datatype);
      if (name == null) {
        name = datatypeTranslationMap.get(datatype.getName());
        dataTypeToDefinitionReferenceMap.put(datatype, name);
      }
      return name;
    }
  }

  @NotNull
  protected CharSequence getTypeNameForDefinition(@NotNull INamedDefinition definition) {
    CharSequence retval;
    if (definition.isInline()) {
      INamedModelDefinition parentDefinition = definition.getInlineInstance().getContainingDefinition();
      if (parentDefinition == null) {
        throw new IllegalStateException();
      }
      retval = new StringBuilder()
          .append(getTypeNameForDefinition(parentDefinition))
          .append('-')
          .append(definition.getName());
    } else {
      retval = definition.getName();
    }
    return retval;
  }

  @NotNull
  protected static CharSequence toCamelCase(String text) {
    StringBuilder builder = new StringBuilder();
    for (String segment : text.split("\\p{Punct}")) {
      if (segment.length() > 0) {
        builder.append(segment.substring(0, 1).toUpperCase());
      }
      if (segment.length() > 1) {
        builder.append(segment.substring(1).toLowerCase());
      }
    }
    return builder;
  }
}
