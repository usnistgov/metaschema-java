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

package gov.nist.secauto.metaschema.codegen.type;

import com.squareup.javapoet.ClassName;

import gov.nist.secauto.metaschema.codegen.binding.config.BindingConfiguration;
import gov.nist.secauto.metaschema.model.definitions.LocalInfoElementDefinition;
import gov.nist.secauto.metaschema.model.definitions.ObjectDefinition;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jaxb.core.api.impl.NameConverter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultTypeResolver implements TypeResolver {
  private static final Logger logger = LogManager.getLogger(DefaultTypeResolver.class);

  private final Map<String, Set<String>> packageToClassNamesMap = new HashMap<>();
  private final Map<ObjectDefinition, ClassName> definitionToTypeMap = new HashMap<>();

  private final BindingConfiguration bindingConfiguration;

  public DefaultTypeResolver(BindingConfiguration bindingConfiguration) {
    this.bindingConfiguration = bindingConfiguration;
  }

  @Override
  public ClassName getClassName(ObjectDefinition definition) {
    ClassName retval = definitionToTypeMap.get(definition);
    if (retval == null) {
      String packageName = bindingConfiguration.getPackageNameForMetaschema(definition.getContainingMetaschema());
      if (definition.isGlobal()) {
        String className = generateClassName(packageName, definition);
        retval = ClassName.get(packageName, className);
      } else {
        // this is a local definition, which means a child class needs to be generated
        ObjectDefinition parentDefinition = ((LocalInfoElementDefinition<?>) definition)
            .getDefiningInstance().getContainingDefinition();
        ClassName parentClassName = getClassName(parentDefinition);
        String name = generateClassName(parentClassName.canonicalName(), definition);
        retval = parentClassName.nestedClass(name);
      }
      definitionToTypeMap.put(definition, retval);
    }
    return retval;
  }

  private String generateClassName(String packageOrTypeName, ObjectDefinition definition) {
    String className = bindingConfiguration.getClassName(definition);

    Set<String> classNames = packageToClassNamesMap.get(packageOrTypeName);
    if (classNames == null) {
      classNames = new HashSet<>();
      packageToClassNamesMap.put(packageOrTypeName, classNames);
    }

    if (classNames.contains(className)) {
      logger.warn(String.format("Class name '%s' in metaschema '%s' conflicts with a previously used class name.",
          className, definition.getContainingMetaschema().getLocation()));
      // first try to append the metaschema's short name
      String metaschemaShortName = definition.getContainingMetaschema().getShortName();
      className = NameConverter.standard.toClassName(className + metaschemaShortName);
    }

    String classNameBase = className;
    int index = 1;
    while (classNames.contains(className)) {
      className = classNameBase + Integer.toString(index);
    }
    classNames.add(className);
    return className;
  }
}
