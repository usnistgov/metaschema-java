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

package gov.nist.secauto.metaschema.codegen;

import com.squareup.javapoet.ClassName;

import gov.nist.secauto.metaschema.binding.model.annotations.XmlNs;
import gov.nist.secauto.metaschema.binding.model.annotations.XmlNsForm;
import gov.nist.secauto.metaschema.binding.model.annotations.XmlSchema;
import gov.nist.secauto.metaschema.codegen.binding.config.IBindingConfiguration;
import gov.nist.secauto.metaschema.codegen.type.DefaultTypeResolver;
import gov.nist.secauto.metaschema.codegen.type.ITypeResolver;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.definition.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFieldDefinition;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class JavaGenerator {
  private static final Logger LOGGER = LogManager.getLogger(JavaGenerator.class);

  private JavaGenerator() {
    // disable construction
  }

  /**
   * Generate Java sources for the provided metaschema.
   * 
   * @param metaschema
   *          the Metaschema to generate Java sources for
   * @param targetDir
   *          the directory to generate sources in
   * @param bindingConfiguration
   *          the binding customizations to use when generating the Java classes
   * @return a mapping of a list of generated classes (value) for a given Metaschema instance (key)
   * @throws IOException
   *           if a build error occurred while generating the class
   */
  public static Map<IMetaschema, List<GeneratedClass>> generate(IMetaschema metaschema, File targetDir,
      IBindingConfiguration bindingConfiguration) throws IOException {
    return generate(Collections.singletonList(metaschema), targetDir, bindingConfiguration);
  }

  /**
   * Generates Java classes for Metaschema fields and flags.
   * 
   * @param metaschemas
   *          the Metaschema instances to build classes for
   * @param targetDirectory
   *          the directory to generate classes in
   * @param bindingConfiguration
   *          binding customizations that can be used to set namespaces, class names, and other
   *          aspects of generated classes
   * @return a mapping of a list of generated classes (value) for a given Metaschema instance (key)
   * @throws IOException
   *           if a build error occurred while generating the class
   */
  public static Map<IMetaschema, List<GeneratedClass>> generate(Collection<? extends IMetaschema> metaschemas,
      File targetDirectory, IBindingConfiguration bindingConfiguration) throws IOException {
    Objects.requireNonNull(metaschemas, "metaschemas");
    Objects.requireNonNull(targetDirectory, "generationTargetDirectory");
    Objects.requireNonNull(bindingConfiguration, "bindingConfiguration");
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Generating Java classes in: {}", targetDirectory.getPath());
    }

    Map<URI, String> xmlNamespaceToPackageNameMap = new HashMap<>();
    Map<URI, Set<IMetaschema>> xmlNamespaceToMetaschemaMap = new HashMap<>();

    ITypeResolver typeResolver = new DefaultTypeResolver(bindingConfiguration);

    Map<IMetaschema, List<? extends IDefinition>> metaschemaToInformationElementsMap
        = buildMetaschemaMap(metaschemas);

    Map<IMetaschema, List<GeneratedClass>> retval = new HashMap<>();
    for (Map.Entry<IMetaschema, List<? extends IDefinition>> entry : metaschemaToInformationElementsMap
        .entrySet()) {
      IMetaschema metaschema = entry.getKey();
      List<GeneratedClass> generatedClasses = null;
      Set<String> classNames = new HashSet<>();

      for (IDefinition definition : entry.getValue()) {
        IJavaClassGenerator classGenerator = null;
        if (definition instanceof IAssemblyDefinition) {
          classGenerator = new AssemblyJavaClassGenerator((IAssemblyDefinition) definition, typeResolver);
        } else if (definition instanceof IFieldDefinition) {
          IFieldDefinition fieldDefinition = (IFieldDefinition) definition;

          if (fieldDefinition.getFlagInstances().isEmpty()) {
            // if field is just a simple data value, then no class is needed
          } else {
            classGenerator = new FieldJavaClassGenerator(fieldDefinition, typeResolver);
          }
        } else {
          // Skip others
          continue;
        }

        if (classGenerator != null) {
          GeneratedClass generatedClass;
          try {
            generatedClass = classGenerator.generateClass(targetDirectory);
          } catch (RuntimeException ex) {
            throw new IllegalStateException(
                String.format("Unable to generate class for definition '%s' in Metaschema '%s'", definition.getName(),
                    metaschema.getLocation()),
                ex);
          }
          String className = generatedClass.getClassName().canonicalName();
          if (classNames.contains(className)) {
            throw new IllegalStateException(String.format(
                "Found duplicate class name '%s' in metaschema '%s'."
                    + " If multiple metaschema are compiled for the same namespace, all class names must be unique.",
                className, metaschema.getLocation()));
          }
          classNames.add(className);

          if (generatedClasses == null) {
            generatedClasses = new LinkedList<>();
            retval.put(metaschema, generatedClasses);
          }
          generatedClasses.add(generatedClass);

        }
      }

      URI xmlNamespace = metaschema.getXmlNamespace();
      String packageName = bindingConfiguration.getPackageNameForMetaschema(metaschema);

      if (xmlNamespaceToPackageNameMap.containsKey(xmlNamespace)) {
        String assignedPackage = xmlNamespaceToPackageNameMap.get(xmlNamespace);
        if (!assignedPackage.equals(packageName)) {
          throw new IllegalStateException(String.format(
              "The metaschema '%s' is assigning the new package name '%s'."
                  + " This new name is different than the previously assigned package name '%s' for the same namespace."
                  + " A metaschema namespace must be assigned a consistent package name.",
              metaschema.getLocation().toString(), packageName, assignedPackage));
        }
      } else {
        xmlNamespaceToPackageNameMap.put(xmlNamespace, packageName);
      }

      Set<IMetaschema> metaschemaSet = xmlNamespaceToMetaschemaMap.get(xmlNamespace);
      if (metaschemaSet == null) {
        metaschemaSet = new HashSet<>();
        xmlNamespaceToMetaschemaMap.put(xmlNamespace, metaschemaSet);
      }
      metaschemaSet.add(metaschema);
    }

    for (Map.Entry<URI, String> entry : xmlNamespaceToPackageNameMap.entrySet()) {
      String packageName = entry.getValue();
      String packagePath = packageName.replace(".", "/");
      File packageInfo = new File(targetDirectory, packagePath + "/package-info.java");
      URI namespace = entry.getKey();
      String namespaceString = namespace.toString();

      try (PrintWriter writer = new PrintWriter(
          Files.newBufferedWriter(packageInfo.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE))) {
        writer.format(
            "@%1$s(namespace = \"%2$s\", xmlns = {@%3$s(prefix = \"\", namespace = \"%2$s\")},"
                + " xmlElementFormDefault = %4$s.QUALIFIED)%n",
            XmlSchema.class.getName(), namespaceString, XmlNs.class.getName(), XmlNsForm.class.getName());
        writer.format("package %s;%n", packageName);
      }

      for (IMetaschema metaschema : xmlNamespaceToMetaschemaMap.get(namespace)) {
        GeneratedClass packageInfoClass
            = new GeneratedClass(packageInfo, ClassName.get(packageName, "package-info"), false);
        List<GeneratedClass> generatedClasses = retval.get(metaschema);
        if (generatedClasses == null) {
          generatedClasses = Collections.singletonList(packageInfoClass);
        } else {
          generatedClasses.add(packageInfoClass);
        }
      }
    }
    return Collections.unmodifiableMap(retval);
  }

  private static Map<IMetaschema, List<? extends IDefinition>>
      buildMetaschemaMap(Collection<? extends IMetaschema> metaschemas) {
    Map<IMetaschema, List<? extends IDefinition>> retval = new HashMap<>();

    for (IMetaschema metaschema : metaschemas) {
      processMetaschema(metaschema, retval);
    }
    return retval;
  }

  private static void processMetaschema(IMetaschema metaschema,
      Map<IMetaschema, List<? extends IDefinition>> map) {
    for (IMetaschema importedMetaschema : metaschema.getImportedMetaschemas()) {
      processMetaschema(importedMetaschema, map);
    }

    if (!map.containsKey(metaschema)) {
      List<? extends IDefinition> definitions = metaschema.getAssemblyAndFieldDefinitions();
      map.put(metaschema, definitions);
    }
  }

  public static class GeneratedClass {
    private final File classFile;
    private final ClassName className;
    private final boolean rootClass;

    /**
     * Construct a new class information object for a generated class.
     * 
     * @param classFile
     *          the file the class was written to
     * @param className
     *          the type info for the class
     * @param rootClass
     *          {@code true} if the class is a root assembly, or {@code false} otherwise
     */
    public GeneratedClass(File classFile, ClassName className, boolean rootClass) {
      Objects.requireNonNull(classFile, "classFile");
      Objects.requireNonNull(className, "className");
      this.classFile = classFile;
      this.className = className;
      this.rootClass = rootClass;
    }

    /**
     * The file the class was written to.
     * 
     * @return the class file
     */
    public File getClassFile() {
      return classFile;
    }

    /**
     * The type info for the class.
     * 
     * @return the class's type info
     */
    public ClassName getClassName() {
      return className;
    }

    /**
     * Indicates if the class represents a root Metaschema assembly which can be the top-level
     * element/property of an XML, JSON, or YAML instance.
     * 
     * @return {@code true} if the class is a root assembly, or {@code false} otherwise
     */
    public boolean isRootClass() {
      return rootClass;
    }
  }
}
