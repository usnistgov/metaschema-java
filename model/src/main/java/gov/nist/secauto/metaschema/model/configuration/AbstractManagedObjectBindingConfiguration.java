/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.model.configuration;

import java.util.Collections;
import java.util.List;

public class AbstractManagedObjectBindingConfiguration implements ManagedObjectBingingConfiguration {
  private final String className;
  private final String baseClassName;
  private final List<String> interfacesToImplement;

  /**
   * Constructs a new binding configuration for a Metaschema assembly or field.
   * 
   * @param className the name of the class to use when generating code for this bound object, or {@code null} if the default behavior is to be used
   * @param baseClassName the name of the class to extend the generated class from, or {@code null} if no extension is to be used
   * @param interfacesToImplement additional interfaces to implement in the generated class, or {@code null} no interfaces are to be implemented
   */
  public AbstractManagedObjectBindingConfiguration(String className, String baseClassName,
      List<String> interfacesToImplement) {
    this.className = className;
    this.baseClassName = baseClassName;
    this.interfacesToImplement
        = interfacesToImplement != null ? Collections.unmodifiableList(interfacesToImplement) : Collections.emptyList();
  }

  @Override
  public String getClassName() {
    return className;
  }

  @Override
  public String getQualifiedBaseClassName() {
    return baseClassName;
  }

  @Override
  public List<String> getInterfacesToImplement() {
    return interfacesToImplement;
  }

}
