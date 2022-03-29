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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gov.nist.secauto.metaschema.model.MetaschemaLoader;
import gov.nist.secauto.metaschema.model.common.INamedDefinition;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class JsonDatatypeManager
    extends AbstractDatatypeManager {
  private static final JsonNode jsonDatatypes;
  private static final Map<String, String> jsonDatatypeDependencyMap = new HashMap<>();
  private static final Pattern DEFINITION_REF_PATTERN = Pattern.compile("^#/definitions/(.+)$");
  private static final Map<String, JsonNode> JSON_DATATYPES = new HashMap<>();

  static {
    try (InputStream is
        = MetaschemaLoader.class.getClassLoader().getResourceAsStream("schema/json/metaschema-datatypes.json")) {
      ObjectMapper objectMapper = new ObjectMapper();
      jsonDatatypes = objectMapper.readTree(is);
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }

    // analyze datatypes for dependencies
    for (String ref : getDatatypeTranslationMap().values()) {
      JsonNode refNode = jsonDatatypes.at("/definitions/" + ref);
      if (!refNode.isMissingNode()) {
        JSON_DATATYPES.put(ref, refNode);

        JsonNode refKeyword = refNode.get("$ref");
        if (refKeyword != null) {
          Matcher matcher = DEFINITION_REF_PATTERN.matcher(refKeyword.asText());
          if (matcher.matches()) {
            String dependency = matcher.group(1);
            jsonDatatypeDependencyMap.put(ref, dependency);
          }
        }
      }
    }
  }

  public static JsonNode getJsonDatatypes() {
    return jsonDatatypes;
  }

  public void generateDatatypes(@NotNull ObjectNode definitionsObject) throws IOException {
    Set<String> requiredJsonDatatypes = getUsedTypes();
    // resolve dependencies
    for (String datatype : CollectionUtil.toIterable(requiredJsonDatatypes.stream()
        .flatMap(datatype -> {
          Stream<String> result;
          String dependency = jsonDatatypeDependencyMap.get(datatype);
          if (dependency == null) {
            result = Stream.of(datatype);
          } else {
            result = Stream.of(datatype, dependency);
          }
          return result;
        }).distinct()
        .sorted()
        .iterator())) {

      JsonNode definition = JSON_DATATYPES.get(datatype);
      if (definition == null) {
        throw new IOException("Missing JSON datatype definition for: /definitions/" + datatype);
      }
      definitionsObject.set(datatype, definition);
    }
  }

  @SuppressWarnings("null")
  @NotNull
  protected String getJsonDefinitionRefForDefinition(@NotNull INamedDefinition definition,
      @NotNull IGenerationState<?, ?> state) {
    return new StringBuilder()
        .append("#/definitions/")
        .append(getTypeNameForDefinition(definition, state))
        .toString();
  }

  @SuppressWarnings("null")
  @NotNull
  protected String getJsonDefinitionRefForDatatype(@NotNull IJavaTypeAdapter<?> datatype) {
    return new StringBuilder()
        .append("#/definitions/")
        .append(getTypeNameForDatatype(datatype))
        .toString();
  }
}
