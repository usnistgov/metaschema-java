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

package gov.nist.secauto.metaschema.codegen;

import com.sun.xml.bind.api.impl.NameConverter;

import gov.nist.secauto.metaschema.binding.model.annotations.XmlNs;
import gov.nist.secauto.metaschema.binding.model.annotations.XmlNsForm;
import gov.nist.secauto.metaschema.binding.model.annotations.XmlSchema;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.MetaschemaException;
import gov.nist.secauto.metaschema.model.MetaschemaLoader;
import gov.nist.secauto.metaschema.model.info.Type;
import gov.nist.secauto.metaschema.model.info.definitions.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.FieldDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.InfoElementDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.ManagedObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class JavaGenerator {
  private static final Logger logger = LogManager.getLogger(JavaGenerator.class);

  public static void main(String[] args) throws IOException, MetaschemaException {
    File metaschemaFile = new File("target/src/metaschema/oscal_catalog_metaschema.xml");
    Metaschema metaschema = new MetaschemaLoader().loadXmlMetaschema(metaschemaFile);

    JavaGenerator.generate(metaschema, new File(args[0]));
  }

  private JavaGenerator() {
    // disable construction
  }

  public static Map<Metaschema, List<GeneratedClass>> generate(Metaschema metaschema, File dir) throws IOException {
    return generate(Collections.singletonList(metaschema), dir);
  }

  public static Map<Metaschema, List<GeneratedClass>> generate(Collection<? extends Metaschema> metaschemas, File dir)
      throws IOException {
    logger.info("Generating Java classes in: {}", dir.getPath());

    Map<Metaschema, List<GeneratedClass>> retval = new HashMap<>();
    Map<URI, String> xmlNamespaceToPackageNameMap = new HashMap<>();
    Map<URI, Set<Metaschema>> xmlNamespaceToMetaschemaMap = new HashMap<>();

    DeconflictingJavaTypeSupplier javaTypeSupplier = new DeconflictingJavaTypeSupplier();

    Map<Metaschema, List<InfoElementDefinition>> metaschemaToInformationElementsMap = buildMetaschemaMap(metaschemas);
    for (Map.Entry<Metaschema, List<InfoElementDefinition>> entry : metaschemaToInformationElementsMap.entrySet()) {
      Metaschema metaschema = entry.getKey();
      List<GeneratedClass> generatedClasses = null;
      Set<String> classNames = new HashSet<>();

      for (InfoElementDefinition definition : entry.getValue()) {
        ClassGenerator classGenerator = null;
        if (definition instanceof AssemblyDefinition) {
          classGenerator = new AssemblyClassGenerator((AssemblyDefinition) definition, javaTypeSupplier);
        } else if (definition instanceof FieldDefinition) {
          FieldDefinition fieldDefinition = (FieldDefinition) definition;

          // if field is just a simple data value, then no class is needed
          if (!fieldDefinition.getFlagInstances().isEmpty()) {
            classGenerator = new FieldClassGenerator(fieldDefinition, javaTypeSupplier);
          }
        } else {
          // Skip others
          continue;
        }

        if (classGenerator != null) {
          GeneratedClass generatedClass = classGenerator.generateClass(dir);
          String className = generatedClass.getClassName();
          if (classNames.contains(className)) {
            throw new IllegalStateException(String.format(
                "Found duplicate class name '%s' in metaschema '%s'."
                    + " If multiple metaschema are compiled for the same namespace, all class names must be unique.",
                className, metaschema.getLocation()));
          } else {
            classNames.add(className);
          }

          if (generatedClasses == null) {
            generatedClasses = new LinkedList<>();
            retval.put(metaschema, generatedClasses);
          }
          generatedClasses.add(generatedClass);

        }
      }

      URI xmlNamespace = metaschema.getXmlNamespace();
      String packageName = metaschema.getPackageName();

      if (xmlNamespaceToPackageNameMap.containsKey(xmlNamespace)) {
        String assignedPackage = xmlNamespaceToPackageNameMap.get(xmlNamespace);
        if (!assignedPackage.equals(packageName)) {
          throw new IllegalStateException(String.format(
              "The metaschema '%s' is assigning the new package name '%s'."
                  + " This new name is different than the previously assigned package name '%s' for the same namespace."
                  + " A metaschema namespace must be assigned a consistent package name.",
              metaschema.getLocation().toString(), metaschema.getPackageName(), assignedPackage));
        }
      } else {
        xmlNamespaceToPackageNameMap.put(xmlNamespace, packageName);
      }

      Set<Metaschema> metaschemaSet = xmlNamespaceToMetaschemaMap.get(xmlNamespace);
      if (metaschemaSet == null) {
        metaschemaSet = new HashSet<>();
        xmlNamespaceToMetaschemaMap.put(xmlNamespace, metaschemaSet);
      }
      metaschemaSet.add(metaschema);
    }

    for (Map.Entry<URI, String> entry : xmlNamespaceToPackageNameMap.entrySet()) {
      String packageName = entry.getValue();
      String packagePath = packageName.replace(".", "/");
      File packageInfo = new File(dir, packagePath + "/package-info.java");
      URI namespace = entry.getKey();
      String namespaceString = namespace.toString();

      try (FileWriter fileWriter = new FileWriter(packageInfo)) {
        PrintWriter writer = new PrintWriter(fileWriter);

        writer.format(
            "@%1$s(namespace = \"%2$s\", xmlns = {@%3$s(prefix = \"\", namespace = \"%2$s\")},"
                + " xmlElementFormDefault = %4$s.QUALIFIED)%n",
            XmlSchema.class.getName(), namespaceString, XmlNs.class.getName(), XmlNsForm.class.getName());
        writer.format("package %s;%n", packageName);
      }

      for (Metaschema metaschema : xmlNamespaceToMetaschemaMap.get(namespace)) {
        retval.get(metaschema).add(new GeneratedClass(packageInfo, packageName + ".package-info", false));
      }
    }
    return Collections.unmodifiableMap(retval);
  }

  private static Map<Metaschema, List<InfoElementDefinition>>
      buildMetaschemaMap(Collection<? extends Metaschema> metaschemas) {
    Map<Metaschema, List<InfoElementDefinition>> retval = new HashMap<>();

    for (Metaschema metaschema : metaschemas) {
      processMetaschema(metaschema, retval);
    }
    return retval;
  }

  private static void processMetaschema(Metaschema metaschema, Map<Metaschema, List<InfoElementDefinition>> map) {
    for (Metaschema importedMetaschema : metaschema.getImportedMetaschema().values()) {
      processMetaschema(importedMetaschema, map);
    }

    if (!map.containsKey(metaschema)) {
      List<InfoElementDefinition> definitions = metaschema.getInfoElementDefinitions().values().stream()
          .filter(c -> !Type.FLAG.equals(c.getType())).collect(Collectors.toList());
      map.put(metaschema, definitions);
    }
  }

  public static class GeneratedClass {
    private final File classFile;
    private final String className;
    private final boolean rootClass;

    public GeneratedClass(File classFile, String className, boolean rootClass) {
      Objects.requireNonNull(classFile, "classFile");
      Objects.requireNonNull(className, "className");
      this.classFile = classFile;
      this.className = className;
      this.rootClass = rootClass;
    }

    public File getClassFile() {
      return classFile;
    }

    public String getClassName() {
      return className;
    }

    public boolean isRootClass() {
      return rootClass;
    }
  }

  private static class DeconflictingJavaTypeSupplier implements JavaTypeSupplier {
    private final Map<String, Set<String>> packageToClassNamesMap = new HashMap<>();
    private final Map<ManagedObject, JavaType> definitionToTypeMap = new HashMap<>();

    @Override
    public JavaType getClassJavaType(ManagedObject definition) {
      JavaType retval = definitionToTypeMap.get(definition);
      if (retval == null) {
        String packageName = definition.getPackageName();
        String className = definition.getClassName();

        Set<String> classNames = packageToClassNamesMap.get(packageName);
        if (classNames == null) {
          classNames = new HashSet<>();
          packageToClassNamesMap.put(packageName, classNames);
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

        retval = JavaType.create(packageName, className);
        definitionToTypeMap.put(definition, retval);
      }
      return retval;
    }

  }
}
