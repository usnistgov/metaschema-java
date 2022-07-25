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

package gov.nist.secauto.metaschema.model;

import gov.nist.secauto.metaschema.model.common.AbstractAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.MetaschemaModelConstants;
import gov.nist.secauto.metaschema.model.common.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.metaschema.model.xmlbeans.AssemblyReferenceType;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

class XmlAssemblyInstance
    extends AbstractAssemblyInstance {
  // private static final Logger logger = LogManager.getLogger(XmlAssemblyInstance.class);

  private final AssemblyReferenceType xmlAssembly;

  /**
   * Constructs a new Metaschema Assembly instance definition from an XML representation bound to Java
   * objects.
   * 
   * @param xmlAssembly
   *          the XML representation bound to Java objects
   * @param parent
   *          the assembly definition this object is an instance of
   */
  public XmlAssemblyInstance(@NotNull AssemblyReferenceType xmlAssembly, @NotNull IAssemblyDefinition parent) {
    super(parent);
    this.xmlAssembly = xmlAssembly;
  }

  /**
   * Get the underlying XML data.
   * 
   * @return the underlying XML data
   */
  protected AssemblyReferenceType getXmlAssembly() {
    return xmlAssembly;
  }

  @Override
  public IAssemblyDefinition getDefinition() {
    // This will always be not null
    return ObjectUtils.notNull(getContainingMetaschema()
        .getScopedAssemblyDefinitionByName(getName()));
  }

  @Override
  public String getFormalName() {
    return getXmlAssembly().isSetFormalName() ? getXmlAssembly().getFormalName() : null;
  }

  @SuppressWarnings("null")
  @Override
  public Map<@NotNull QName, Set<@NotNull String>> getProperties() {
    return ModelFactory.toProperties(CollectionUtil.listOrEmpty(getXmlAssembly().getPropList()));
  }

  @Override
  public MarkupLine getDescription() {
    return getXmlAssembly().isSetDescription() ? MarkupStringConverter.toMarkupString(getXmlAssembly().getDescription()) : null;
  }

  @SuppressWarnings("null")
  @Override
  public String getName() {
    return getXmlAssembly().getRef();
  }

  @Override
  public String getUseName() {
    return getXmlAssembly().isSetUseName() ? getXmlAssembly().getUseName() : getDefinition().getUseName();
  }

  @Override
  public String getGroupAsName() {
    return getXmlAssembly().isSetGroupAs() ? getXmlAssembly().getGroupAs().getName() : null;
  }

  @Override
  public int getMinOccurs() {
    int retval = MetaschemaModelConstants.DEFAULT_GROUP_AS_MIN_OCCURS;
    if (getXmlAssembly().isSetMinOccurs()) {
      retval = getXmlAssembly().getMinOccurs().intValueExact();
    }
    return retval;
  }

  @Override
  public int getMaxOccurs() {
    int retval = MetaschemaModelConstants.DEFAULT_GROUP_AS_MAX_OCCURS;
    if (getXmlAssembly().isSetMaxOccurs()) {
      Object value = getXmlAssembly().getMaxOccurs();
      if (value instanceof String) {
        // unbounded
        retval = -1;
      } else if (value instanceof BigInteger) {
        retval = ((BigInteger) value).intValueExact();
      }
    }
    return retval;
  }

  @Override
  public JsonGroupAsBehavior getJsonGroupAsBehavior() {
    JsonGroupAsBehavior retval = MetaschemaModelConstants.DEFAULT_JSON_GROUP_AS_BEHAVIOR;
    if (getXmlAssembly().isSetGroupAs() && getXmlAssembly().getGroupAs().isSetInJson()) {
      retval = getXmlAssembly().getGroupAs().getInJson();
    }
    return retval;
  }

  @Override
  public XmlGroupAsBehavior getXmlGroupAsBehavior() {
    XmlGroupAsBehavior retval = MetaschemaModelConstants.DEFAULT_XML_GROUP_AS_BEHAVIOR;
    if (getXmlAssembly().isSetGroupAs() && getXmlAssembly().getGroupAs().isSetInXml()) {
      retval = getXmlAssembly().getGroupAs().getInXml();
    }
    return retval;
  }

  @SuppressWarnings("null")
  @Override
  public MarkupMultiline getRemarks() {
    return getXmlAssembly().isSetRemarks() ? MarkupStringConverter.toMarkupString(getXmlAssembly().getRemarks()) : null;
  }

  @Override
  public Object getValue(@NotNull Object parentInstance) {
    // there is no value
    return null;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<@NotNull ?> getItemValues(Object instanceValue) {
    // there are no item values
    return Collections.emptyList();
  }
}
