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

package gov.nist.secauto.metaschema.binding.model.property;

import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.info.ClassDataTypeHandler;
import gov.nist.secauto.metaschema.binding.model.property.info.DataTypeHandler;
import gov.nist.secauto.metaschema.binding.model.property.info.ModelPropertyInfo;
import gov.nist.secauto.metaschema.binding.model.property.info.SingletonPropertyInfo;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.instance.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.instance.XmlGroupAsBehavior;

import javax.xml.namespace.QName;

public class RootAssemblyProperty
    extends AbstractAssemblyProperty
    implements AssemblyProperty {

  public RootAssemblyProperty(AssemblyClassBinding rootAssemblyDefinition) {
    super(rootAssemblyDefinition, null);
  }

  @Override
  public AssemblyClassBinding getDefinition() {
    return getParentClassBinding();
  }

  @Override
  protected DataTypeHandler newDataTypeHandler() {
    return new ClassDataTypeHandler(getDefinition(), this);
  }

  @Override
  protected ModelPropertyInfo newPropertyInfo() {
    return new SingletonPropertyInfo(this);
  }

  @Override
  public String getName() {
    return getDefinition().getRootName();
  }

  @Override
  public String getUseName() {
    return null;
  }

  @Override
  public String getXmlNamespace() {
    return getDefinition().getRootXmlQName().getNamespaceURI();
  }

  @Override
  public QName getXmlQName() {
    return getDefinition().getRootXmlQName();
  }

  @Override
  public String toCoordinates() {
    return getDefinition().toCoordinates();
  }

  @Override
  public MarkupMultiline getRemarks() {
    return getDefinition().getRemarks();
  }

  @Override
  public AssemblyClassBinding getContainingDefinition() {
    return getParentClassBinding();
  }

  @Override
  public int getMinOccurs() {
    return 1;
  }

  @Override
  public int getMaxOccurs() {
    return 1;
  }

  @Override
  public String getGroupAsName() {
    return null;
  }

  @Override
  public String getGroupAsXmlNamespace() {
    return null;
  }

  @Override
  public JsonGroupAsBehavior getJsonGroupAsBehavior() {
    return JsonGroupAsBehavior.NONE;
  }

  @Override
  public XmlGroupAsBehavior getXmlGroupAsBehavior() {
    return XmlGroupAsBehavior.UNGROUPED;
  }
}
