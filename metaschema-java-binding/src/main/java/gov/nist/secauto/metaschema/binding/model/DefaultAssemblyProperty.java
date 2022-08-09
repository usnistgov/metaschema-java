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

package gov.nist.secauto.metaschema.binding.model;

import gov.nist.secauto.metaschema.binding.model.annotations.BoundAssembly;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.lang.reflect.Field;

import edu.umd.cs.findbugs.annotations.NonNull;

class DefaultAssemblyProperty
    extends AbstractAssemblyProperty {

  @NonNull
  private final BoundAssembly assembly;
  @NonNull
  private final IAssemblyClassBinding definition;

  protected DefaultAssemblyProperty(
      @NonNull Field field,
      @NonNull IAssemblyClassBinding definition,
      @NonNull IAssemblyClassBinding parentClassBinding) {
    super(field, parentClassBinding);
    if (field.isAnnotationPresent(BoundAssembly.class)) {
      this.assembly = ObjectUtils.notNull(field.getAnnotation(BoundAssembly.class));
    } else {
      throw new IllegalArgumentException(String.format("BoundField '%s' on class '%s' is missing the '%s' annotation.",
          field.getName(), parentClassBinding.getBoundClass().getName(), BoundAssembly.class.getName()));
    }
    this.definition = definition;
  }

  protected BoundAssembly getAssemblyAnnotation() {
    return assembly;
  }

  @Override
  public IAssemblyClassBinding getDefinition() {
    return definition;
  }

  @Override
  public String getFormalName() {
    return ModelUtil.resolveToString(getAssemblyAnnotation().formalName());
  }

  @Override
  public MarkupLine getDescription() {
    return ModelUtil.resolveToMarkupLine(getAssemblyAnnotation().description());
  }

  @Override
  public MarkupMultiline getRemarks() {
    return ModelUtil.resolveToMarkupMultiline(getAssemblyAnnotation().remarks());
  }

  @Override
  public String getUseName() {
    return ModelUtil.resolveLocalName(getAssemblyAnnotation().useName(), getName());
  }

  @Override
  public String getXmlNamespace() {
    return ObjectUtils
        .notNull(ModelUtil.resolveNamespace(getAssemblyAnnotation().namespace(), getParentClassBinding()));
  }

  @Override
  public int getMinOccurs() {
    return getAssemblyAnnotation().minOccurs();
  }

  @Override
  public int getMaxOccurs() {
    return getAssemblyAnnotation().maxOccurs();
  }
}
