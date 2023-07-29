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

package gov.nist.secauto.metaschema.databind.codegen;

import com.squareup.javapoet.ClassName;

import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaPackage;
import gov.nist.secauto.metaschema.databind.model.annotations.XmlNs;
import gov.nist.secauto.metaschema.databind.model.annotations.XmlNsForm;
import gov.nist.secauto.metaschema.databind.model.annotations.XmlSchema;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

class PackageProductionImpl implements IPackageProduction {
  @NonNull
  private final String javaPackage;
  @NonNull
  private final URI xmlNamespace;
  @NonNull
  private final DefaultGeneratedClass packageInfoClass;

  public PackageProductionImpl(@NonNull String javaPackage, @NonNull URI xmlNamespace,
      @NonNull List<IMetaschemaProduction> metaschemaProductions, @NonNull Path targetDirectory)
      throws IOException {
    this.javaPackage = javaPackage;
    this.xmlNamespace = xmlNamespace;

    String packagePath = javaPackage.replace(".", "/");
    Path packageInfo = ObjectUtils.notNull(targetDirectory.resolve(packagePath + "/package-info.java"));

    try (PrintWriter writer = new PrintWriter(
        Files.newBufferedWriter(packageInfo, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING))) {
      writer.format("@%1$s(metaschemas = {%n", MetaschemaPackage.class.getName());

      boolean first = true;
      for (IMetaschemaProduction metaschemaProduction : metaschemaProductions) {
        if (first) {
          first = false;
        } else {
          writer.format(",%n");
        }

        IGeneratedClass generatedMetaschema = metaschemaProduction.getGeneratedMetaschema();

        writer.format("  %1$s.class", generatedMetaschema.getClassName().canonicalName());
      }

      writer.format("})%n");

      writer.format(
          "@%1$s(namespace = \"%2$s\", xmlns = {@%3$s(prefix = \"\", namespace = \"%2$s\")},"
              + " xmlElementFormDefault = %4$s.QUALIFIED)%n",
          XmlSchema.class.getName(), xmlNamespace.toString(), XmlNs.class.getName(), XmlNsForm.class.getName());
      writer.format("package %s;%n", javaPackage);
    }

    this.packageInfoClass
        = new DefaultGeneratedClass(packageInfo, ObjectUtils.notNull(ClassName.get(javaPackage, "package-info")));
  }
  //
  // public static String formatMarkdown(@NonNull IFlexmarkMarkupString<?> markup) {
  // // TODO: seemingly unused method. Remove?
  // Formatter.Builder builder = Formatter.builder(FlexmarkConfiguration.FLEXMARK_CONFIG);
  // builder.set(ObjectUtils.notNull(Formatter.FORMAT_FLAGS), LineAppendable.F_WHITESPACE_REMOVAL);
  // // builder.set(Formatter.ESCAPE_SPECIAL_CHARS, false);
  // Formatter formatter = builder.build();
  // String markdown = markup.toMarkdown(formatter).trim();
  // markdown = markdown.replace("\\&", "&");
  // markdown = markdown.replace("\"", "\\\"");
  // return markdown;
  // }

  @Override
  public String getJavaPackage() {
    return javaPackage;
  }

  @Override
  public URI getXmlNamespace() {
    return xmlNamespace;
  }

  /**
   * Get the generated package-info class associated with this package.
   *
   * @return the package-info class
   */
  @Override
  public DefaultGeneratedClass getGeneratedClass() {
    return packageInfoClass;
  }
}
