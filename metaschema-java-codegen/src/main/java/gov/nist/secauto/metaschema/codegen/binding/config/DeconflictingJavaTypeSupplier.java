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

package gov.nist.secauto.metaschema.codegen.binding.config;

import com.sun.xml.bind.api.impl.NameConverter;

import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.model.info.definitions.ManagedObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class DeconflictingJavaTypeSupplier implements JavaTypeSupplier {
  private static final Logger logger = LogManager.getLogger(DeconflictingJavaTypeSupplier.class);

  private final Map<String, Set<String>> packageToClassNamesMap = new HashMap<>();
  private final Map<ManagedObject, JavaType> definitionToTypeMap = new HashMap<>();

  private final BindingConfiguration bindingConfiguration;

  public DeconflictingJavaTypeSupplier(BindingConfiguration bindingConfiguration) {
    this.bindingConfiguration = bindingConfiguration;
  }

  @Override
  public JavaType getClassJavaType(ManagedObject managedObject) {
    JavaType retval = definitionToTypeMap.get(managedObject);
    if (retval == null) {
      String packageName = bindingConfiguration.getPackageName(managedObject.getContainingMetaschema());
      String className = bindingConfiguration.getClassName(managedObject);

      Set<String> classNames = packageToClassNamesMap.get(packageName);
      if (classNames == null) {
        classNames = new HashSet<>();
        packageToClassNamesMap.put(packageName, classNames);
      }

      if (classNames.contains(className)) {
        logger.warn(String.format("Class name '%s' in metaschema '%s' conflicts with a previously used class name.",
            className, managedObject.getContainingMetaschema().getLocation()));
        // first try to append the metaschema's short name
        String metaschemaShortName = managedObject.getContainingMetaschema().getShortName();
        className = NameConverter.standard.toClassName(className + metaschemaShortName);
      }

      String classNameBase = className;
      int index = 1;
      while (classNames.contains(className)) {
        className = classNameBase + Integer.toString(index);
      }
      classNames.add(className);

      retval = JavaType.create(packageName, className);
      definitionToTypeMap.put(managedObject, retval);
    }
    return retval;
  }

}
