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

import gov.nist.secauto.metaschema.databind.codegen.typeinfo.IMetaschemaClassFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

class PackageProductionImpl implements IPackageProduction {
  @NonNull
  private final URI xmlNamespace;
  @NonNull
  private final IGeneratedClass packageInfoClass;

  @SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW", justification = "Use of final fields")
  public PackageProductionImpl(
      @NonNull PackageMetadata metadata,
      @NonNull IMetaschemaClassFactory classFactory,
      @NonNull Path targetDirectory)
      throws IOException {
    this.xmlNamespace = metadata.getXmlNamespace();
    this.packageInfoClass = classFactory.generatePackageInfoClass(
        metadata.getPackageName(),
        this.xmlNamespace,
        metadata.getModuleProductions(),
        targetDirectory);
  }
  //
  // public static String formatMarkdown(@NonNull IFlexmarkMarkupString<?> markup)
  // {
  // // TODO: seemingly unused method. Remove?
  // Formatter.Builder builder =
  // Formatter.builder(FlexmarkConfiguration.FLEXMARK_CONFIG);
  // builder.set(ObjectUtils.notNull(Formatter.FORMAT_FLAGS),
  // LineAppendable.F_WHITESPACE_REMOVAL);
  // // builder.set(Formatter.ESCAPE_SPECIAL_CHARS, false);
  // Formatter formatter = builder.build();
  // String markdown = markup.toMarkdown(formatter).trim();
  // markdown = markdown.replace("\\&", "&");
  // markdown = markdown.replace("\"", "\\\"");
  // return markdown;
  // }

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
  public IGeneratedClass getGeneratedClass() {
    return packageInfoClass;
  }
}
