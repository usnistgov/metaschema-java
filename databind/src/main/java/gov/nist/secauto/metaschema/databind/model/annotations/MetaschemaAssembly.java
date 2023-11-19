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

package gov.nist.secauto.metaschema.databind.model.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import gov.nist.secauto.metaschema.core.model.IModule;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * This annotation indicates that the target class represents a Module assembly.
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface MetaschemaAssembly {
  /**
   * Get the documentary formal name of the assembly.
   * <p>
   * If the value is "##none", then the description will be considered
   * {@code null}.
   *
   * @return a Markdown string or {@code "##none"} if no formal name is provided
   */
  @NonNull
  String formalName() default Constants.NO_STRING_VALUE;

  /**
   * Get the documentary description of the assembly.
   * <p>
   * If the value is "##none", then the description will be considered
   * {@code null}.
   *
   * @return a markdown string or {@code "##none"} if no description is provided
   */
  @NonNull
  String description() default Constants.NO_STRING_VALUE;

  /**
   * Get the Metaschema module class that "owns" this assembly, which is the
   * concrete implementation of the module containing the assembly.
   *
   * @return the {@link IModule} class
   */
  @NonNull
  Class<? extends IModule> moduleClass();

  /**
   * Name of the assembly.
   *
   * @return the name
   */
  @NonNull
  String name();

  /**
   * The binary name of the assembly.
   * <p>
   * The value {@link Integer#MIN_VALUE} indicates that there is no index.
   *
   * @return the index value
   */
  int index() default Integer.MIN_VALUE;

  /**
   * Name of the root XML element or the JSON/YAML property.
   * <p>
   * If the value is "##none", then there is no root name.
   *
   * @return the name
   */
  @NonNull
  String rootName() default Constants.NO_STRING_VALUE;

  /**
   * The binary root name of the assembly.
   * <p>
   * The value {@link Integer#MIN_VALUE} indicates that there is no root index.
   *
   * @return the index value
   */
  int rootIndex() default Integer.MIN_VALUE;

  /**
   * XML target namespace of the XML element.
   * <p>
   * If the value is "##default", then namespace is derived from the namespace
   * provided in the package-info.
   *
   * @return the namespace
   */
  @NonNull
  String rootNamespace() default Constants.DEFAULT_STRING_VALUE;

  /**
   * Get any remarks for this assembly.
   *
   * @return a markdown string or {@code "##none"} if no remarks are provided
   */
  @NonNull
  String remarks() default Constants.NO_STRING_VALUE;

  /**
   * Get the value constraints defined for this Metaschema assembly definition.
   *
   * @return the value constraints
   */
  ValueConstraints valueConstraints() default @ValueConstraints;

  /**
   * Get the model constraints defined for this Metaschema assembly definition.
   *
   * @return the value constraints
   */
  AssemblyConstraints modelConstraints() default @AssemblyConstraints;
}
