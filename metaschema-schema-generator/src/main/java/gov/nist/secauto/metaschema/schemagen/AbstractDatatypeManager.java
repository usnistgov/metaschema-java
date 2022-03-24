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

package gov.nist.secauto.metaschema.schemagen;

import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.definition.INamedDefinition;
import gov.nist.secauto.metaschema.model.common.definition.INamedModelDefinition;
import gov.nist.secauto.metaschema.model.common.instance.INamedInstance;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractDatatypeManager implements IDatatypeManager {
  @NotNull
  private static final Map<String, String> datatypeTranslationMap = new LinkedHashMap<>();

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
  }

  @SuppressWarnings("null")
  @NotNull
  protected static Map<String, String> getDatatypeTranslationMap() {
    return Collections.unmodifiableMap(datatypeTranslationMap);
  }

  @NotNull
  private final Map<@NotNull IJavaTypeAdapter<?>, String> datatypeToTypeMap = new HashMap<>();

  protected abstract boolean isNestInlineDefinitions();

  @Override
  public Set<String> getUsedTypes() {
    return new HashSet<>(datatypeToTypeMap.values());
  }

  @SuppressWarnings("null")
  @Override
  @NotNull
  public String getTypeForDatatype(@NotNull IJavaTypeAdapter<?> datatype) {
    synchronized (this) {
      String name = datatypeToTypeMap.get(datatype);
      if (name == null) {
        name = getDatatypeTranslationMap().get(datatype.getName());
        datatypeToTypeMap.put(datatype, name);
      }
      return name;
    }
  }

  public CharSequence getTypeNameForDefinition(@NotNull INamedDefinition definition) {
    StringBuilder builder = new StringBuilder();

    if (!definition.isInline()) {
      builder.append(toCamelCase(definition.getContainingMetaschema().getShortName()));
    }

    if (definition.isInline() && !isNestInlineDefinitions()) {
      // need to append the parent name(s) to disambiguate this type name
      builder.append(getTypeContext(definition, definition.getContainingMetaschema()));
    } else {
      builder
          .append(toCamelCase(definition.getEffectiveName()))
          .append(toCamelCase(definition.getModelType().name()));
    }
    builder.append("Type");

    return builder;
  }

  /**
   * Get the name of the definition (and any parent instances/definition) to ensure an inline type is
   * unique.
   * 
   * @param definition
   *          the definition to generate a type name for
   * @param childMetaschema
   *          the metaschema of the left node
   * @return the unique type name
   */
  private CharSequence getTypeContext(@NotNull INamedDefinition definition,
      @NotNull IMetaschema childMetaschema) {
    StringBuilder builder = new StringBuilder();
    if (definition.isInline()) {
      INamedInstance inlineInstance = definition.getInlineInstance();
      INamedModelDefinition parentDefinition = inlineInstance.getContainingDefinition();
      if (parentDefinition == null) {
        throw new IllegalStateException();
      }
      builder.append(getTypeContext(parentDefinition, childMetaschema));
      builder.append(toCamelCase(inlineInstance.getEffectiveName()));
    } else {
      builder.append(toCamelCase(definition.getEffectiveName()));
    }
    return builder;
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
