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

import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class GeneratedClass {
  @NotNull
  private final Path classFile;
  @NotNull
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
  public GeneratedClass(@NotNull Path classFile, @NotNull ClassName className, boolean rootClass) {
    this.classFile = ObjectUtils.requireNonNull(classFile, "classFile");
    this.className = ObjectUtils.requireNonNull(className, "className");
    this.rootClass = rootClass;
  }

  /**
   * The file the class was written to.
   * 
   * @return the class file
   */
  public Path getClassFile() {
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