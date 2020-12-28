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

package gov.nist.secauto.metaschema.model.util;

import gov.nist.secauto.metaschema.model.definitions.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.definitions.FieldDefinition;
import gov.nist.secauto.metaschema.model.definitions.FlagDefinition;
import gov.nist.secauto.metaschema.model.definitions.InfoElementDefinition;
import gov.nist.secauto.metaschema.model.instances.AssemblyInstance;
import gov.nist.secauto.metaschema.model.instances.ChoiceInstance;
import gov.nist.secauto.metaschema.model.instances.FieldInstance;
import gov.nist.secauto.metaschema.model.instances.FlagInstance;
import gov.nist.secauto.metaschema.model.instances.ModelInstance;

import java.util.Collection;
import java.util.List;

public class ModelWalker {

  public void walk(FlagDefinition flag) {
    visit(flag);
  }

  public void walk(FieldDefinition field) {
    if (visit(field)) {
      walkFlagInstances(field.getFlagInstances().values());
    }
  }

  public void walk(AssemblyDefinition assembly) {
    if (visit(assembly)) {
      walkFlagInstances(assembly.getFlagInstances().values());
      walkModelInstances(assembly.getModelInstances());
    }
  }

  public void walk(FlagInstance<?> instance) {
    if (visit(instance)) {
      walk(instance.getDefinition());
    }
  }

  public void walk(FieldInstance<?> instance) {
    if (visit(instance)) {
      walk(instance.getDefinition());
    }
  }

  public void walk(AssemblyInstance<?> instance) {
    if (visit(instance)) {
      walk(instance.getDefinition());
    }
  }

  public void walk(ChoiceInstance instance) {
    if (visit(instance)) {
      walkModelInstances(instance.getModelInstances());
    }
  }

  protected void walkFlagInstances(Collection<? extends FlagInstance<?>> instances) {
    for (FlagInstance<?> instance : instances) {
      walk(instance);
    }
  }

  private void walkModelInstances(List<ModelInstance> instances) {
    for (ModelInstance instance : instances) {
      walkModelInstance(instance);
    }
  }

  protected void walkModelInstance(ModelInstance instance) {
    if (instance instanceof AssemblyInstance) {
      walk((AssemblyInstance<?>) instance);
    } else if (instance instanceof FieldInstance) {
      walk((FieldInstance<?>) instance);
    } else if (instance instanceof ChoiceInstance) {
      walk((ChoiceInstance) instance);
    }
  }

  protected void walkDefinition(InfoElementDefinition def) {
    if (def instanceof FlagDefinition) {
      walk((FlagDefinition) def);
    } else if (def instanceof FieldDefinition) {
      walk((FieldDefinition) def);
    } else if (def instanceof AssemblyDefinition) {
      walk((AssemblyDefinition) def);
    }
  }

  protected void visit(FlagDefinition def) {
  }

  protected boolean visit(FieldDefinition def) {
    return true;
  }

  protected boolean visit(AssemblyDefinition def) {
    return true;
  }

  protected boolean visit(FlagInstance<?> instance) {
    return true;
  }

  protected boolean visit(FieldInstance<?> instance) {
    return true;
  }

  protected boolean visit(AssemblyInstance<?> instance) {
    return true;
  }

  protected boolean visit(ChoiceInstance instance) {
    return true;
  }
}
