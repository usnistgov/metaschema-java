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

package gov.nist.secauto.metaschema.binding.model.property;

import gov.nist.secauto.metaschema.binding.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.model.annotations.XmlGroupAsBehavior;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;

import javax.xml.namespace.QName;

public abstract class AbstractCollectionPropertyInfo extends AbstractPropertyInfo<ParameterizedType>
    implements CollectionPropertyInfo {
  private final GroupAs groupAs;
  private final QName groupXmlQName;

  public AbstractCollectionPropertyInfo(ParameterizedType type, PropertyAccessor propertyAccessor, GroupAs groupAs) {
    super(type, propertyAccessor);
    Objects.requireNonNull(groupAs, "groupAs");
    this.groupAs = groupAs;

    String resolvedLocalName = ModelUtil.resolveLocalName(getGroupAs().name(), propertyAccessor.getSimpleName());
    String resolvedNamespace
        = ModelUtil.resolveNamespace(getGroupAs().namespace(), propertyAccessor.getContainingClass());
    this.groupXmlQName = new QName(resolvedNamespace, resolvedLocalName);
  }

  protected GroupAs getGroupAs() {
    return groupAs;
  }

  @Override
  public Class<?> getRawType() {
    return (Class<?>) getType().getRawType();
  }

  @Override
  public int getMinimumOccurance() {
    return groupAs.minOccurs();
  }

  @Override
  public int getMaximumOccurance() {
    return groupAs.maxOccurs();
  }

  @Override
  public QName getGroupXmlQName() {
    return groupXmlQName;
  }

  @Override
  public XmlGroupAsBehavior getXmlGroupAsBehavior() {
    return getGroupAs().inXml();
  }

  @Override
  public JsonGroupAsBehavior getJsonGroupAsBehavior() {
    return getGroupAs().inJson();
  }
}
