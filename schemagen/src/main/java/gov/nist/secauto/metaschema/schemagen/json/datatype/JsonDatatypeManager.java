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

package gov.nist.secauto.metaschema.schemagen.json.datatype;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gov.nist.secauto.metaschema.core.model.xml.MetaschemaLoader;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.SchemaGenerationException;
import gov.nist.secauto.metaschema.schemagen.datatype.AbstractDatatypeManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public class JsonDatatypeManager
    extends AbstractDatatypeManager {
  private static final Map<String, List<String>> DATATYPE_DEPENDENCY_MAP = new ConcurrentHashMap<>();
  private static final Pattern DEFINITION_REF_PATTERN = Pattern.compile("^#/definitions/(.+)$");
  private static final Map<String, JsonNode> JSON_DATATYPES = new ConcurrentHashMap<>();

  static {
    JsonNode jsonData;
    try (InputStream is
        = MetaschemaLoader.class.getResourceAsStream("/schema/json/metaschema-datatypes.json")) {
      ObjectMapper objectMapper = new ObjectMapper();
      jsonData = objectMapper.readTree(is);
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }

    // analyze datatypes for dependencies
    for (String ref : getDatatypeTranslationMap().values()) {
      JsonNode refNode = jsonData.at("/definitions/" + ref);
      if (!refNode.isMissingNode()) {
        JSON_DATATYPES.put(ref, refNode);

        List<String> dependencies = getDependencies(refNode).collect(Collectors.toList());
        if (!dependencies.isEmpty()) {
          DATATYPE_DEPENDENCY_MAP.put(ref, dependencies);
        }
      }
    }
  }

  private static Stream<String> getDependencies(@NonNull JsonNode node) {
    Stream<String> retval = Stream.empty();
    for (Map.Entry<String, JsonNode> entry : CollectionUtil.toIterable(ObjectUtils.notNull(node.fields()))) {
      JsonNode value = entry.getValue();
      assert value != null;
      if ("$ref".equals(entry.getKey())) {
        Matcher matcher = DEFINITION_REF_PATTERN.matcher(value.asText());
        if (matcher.matches()) {
          String dependency = matcher.group(1);
          retval = Stream.concat(retval, Stream.of(dependency));
        }
      }

      if (value.isArray()) {
        for (JsonNode child : CollectionUtil.toIterable(ObjectUtils.notNull(value.elements()))) {
          assert child != null;
          retval = Stream.concat(retval, getDependencies(child));
        }
      }
    }
    return retval;
  }

  public void generateDatatypes(@NonNull ObjectNode definitionsObject) {
    Set<String> requiredJsonDatatypes = getUsedTypes();
    // resolve dependencies
    for (String datatype : CollectionUtil.toIterable(ObjectUtils.notNull(
        requiredJsonDatatypes.stream()
            .flatMap(datatype -> {
              Stream<String> result;
              List<String> dependencies = DATATYPE_DEPENDENCY_MAP.get(datatype);
              if (dependencies == null) {
                result = Stream.of(datatype);
              } else {
                result = Stream.concat(Stream.of(datatype), dependencies.stream());
              }
              return result;
            }).distinct()
            .sorted()
            .iterator()))) {

      JsonNode definition = JSON_DATATYPES.get(datatype);
      if (definition == null) {
        throw new SchemaGenerationException("Missing JSON datatype definition for: /definitions/" + datatype);
      }
      definitionsObject.set(datatype, definition);
    }
  }

}
