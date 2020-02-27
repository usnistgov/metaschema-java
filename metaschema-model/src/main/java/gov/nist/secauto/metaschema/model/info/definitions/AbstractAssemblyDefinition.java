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

package gov.nist.secauto.metaschema.model.info.definitions;

import com.sun.xml.bind.api.impl.NameConverter;

import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.configuration.AssemblyBindingConfiguration;
import gov.nist.secauto.metaschema.model.info.Type;
import gov.nist.secauto.metaschema.model.info.instances.AssemblyInstance;
import gov.nist.secauto.metaschema.model.info.instances.FieldInstance;
import gov.nist.secauto.metaschema.model.info.instances.FlagInstance;

import java.util.Objects;

public abstract class AbstractAssemblyDefinition<METASCHEMA extends Metaschema>
    extends AbstractInfoElementDefinition<METASCHEMA> implements AssemblyDefinition {
  private final AssemblyBindingConfiguration bindingConfiguration;

  /**
   * Constructs a new definition of a Metaschema Assembly.
   * 
   * @param bindingConfiguration
   *          the binding configuration used to customize the generated code for this assembly
   * @param metaschema
   *          the containing Metaschema
   */
  public AbstractAssemblyDefinition(AssemblyBindingConfiguration bindingConfiguration, METASCHEMA metaschema) {
    super(metaschema);
    Objects.requireNonNull(bindingConfiguration, "bindingConfiguration");
    this.bindingConfiguration = bindingConfiguration;
  }

  @Override
  public Type getType() {
    return Type.ASSEMBLY;
  }

  protected AssemblyBindingConfiguration getBindingConfiguration() {
    return bindingConfiguration;
  }

  @Override
  public String getClassName() {
    String retval = getBindingConfiguration().getClassName();
    if (retval == null) {
      retval = NameConverter.standard.toClassName(getName());
    }
    return retval;
  }

  @Override
  public String getPackageName() {
    return getContainingMetaschema().getPackageName();
  }

  @Override
  public FlagInstance getFlagInstanceByName(String name) {
    return getFlagInstances().get(name);
  }

  @Override
  public FieldInstance getFieldInstanceByName(String name) {
    return getFieldInstances().get(name);
  }

  @Override
  public AssemblyInstance getAssemblyInstanceByName(String name) {
    return getAssemblyInstances().get(name);
  }
}
