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
package gov.nist.secauto.metaschema.databind.model.metaschema;

import gov.nist.secauto.metaschema.core.datatype.adapter.NonNegativeIntegerAdapter;
import gov.nist.secauto.metaschema.core.datatype.adapter.StringAdapter;
import gov.nist.secauto.metaschema.core.datatype.adapter.TokenAdapter;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundField;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundFlag;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.Matches;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.math.BigInteger;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@MetaschemaAssembly(
    formalName = "Choice Grouping",
    name = "choice-group",
    moduleClass = MetaschemaModule.class
)
public class ChoiceGroup {
  @BoundFlag(
      formalName = "Minimum Occurrence",
      useName = "min-occurs",
      defaultValue = "0",
      typeAdapter = NonNegativeIntegerAdapter.class
  )
  private BigInteger _minOccurs;

  @BoundFlag(
      formalName = "Maximum Occurrence",
      useName = "max-occurs",
      defaultValue = "unbounded",
      typeAdapter = StringAdapter.class,
      valueConstraints = @ValueConstraints(matches = @Matches(level = IConstraint.Level.ERROR, pattern = "^[1-9][0-9]*|unbounded$"))
  )
  private String _maxOccurs;

  @BoundAssembly(
      formalName = "JSON Key",
      description = "Used in JSON (and similar formats) to identify a flag that will be used as the property name in an object hold a collection of sibling objects. Requires that siblings must never share `json-key` values.",
      useName = "json-key"
  )
  private JsonKey _jsonKey;

  @BoundAssembly(
      formalName = "Group As",
      useName = "group-as",
      minOccurs = 1
  )
  private GroupAs _groupAs;

  @BoundField(
      formalName = "Discriminator JSON Property",
      useName = "discriminator",
      defaultValue = "object-type",
      typeAdapter = TokenAdapter.class
  )
  private String _discriminator;

  @BoundChoiceGroup(
      minOccurs = 1,
      maxOccurs = -1,
      assemblies = {
          @BoundGroupedAssembly(formalName = "Grouping Assembly Reference", useName = "assembly", binding = GroupedAssemblyReference.class),
          @BoundGroupedAssembly(formalName = "Inline Assembly Definition", useName = "define-assembly", binding = GroupedInlineDefineAssembly.class),
          @BoundGroupedAssembly(formalName = "Grouping Field Reference", useName = "field", binding = GroupedFieldReference.class),
          @BoundGroupedAssembly(formalName = "Inline Field Definition", useName = "define-field", binding = GroupedInlineDefineField.class)
      },
      groupAs = @gov.nist.secauto.metaschema.databind.model.annotations.GroupAs(name = "choices", inJson = JsonGroupAsBehavior.LIST)
  )
  private List<Object> _choices;

  @BoundField(
      formalName = "Remarks",
      description = "Any explanatory or helpful information to be provided about the remarks parent.",
      useName = "remarks"
  )
  private Remarks _remarks;

  public ChoiceGroup() {
  }

  public BigInteger getMinOccurs() {
    return _minOccurs;
  }

  public void setMinOccurs(BigInteger value) {
    _minOccurs = value;
  }

  public String getMaxOccurs() {
    return _maxOccurs;
  }

  public void setMaxOccurs(String value) {
    _maxOccurs = value;
  }

  public JsonKey getJsonKey() {
    return _jsonKey;
  }

  public void setJsonKey(JsonKey value) {
    _jsonKey = value;
  }

  public GroupAs getGroupAs() {
    return _groupAs;
  }

  public void setGroupAs(GroupAs value) {
    _groupAs = value;
  }

  public String getDiscriminator() {
    return _discriminator;
  }

  public void setDiscriminator(String value) {
    _discriminator = value;
  }

  public List<Object> getChoices() {
    return _choices;
  }

  public void setChoices(List<Object> value) {
    _choices = value;
  }

  public Remarks getRemarks() {
    return _remarks;
  }

  public void setRemarks(Remarks value) {
    _remarks = value;
  }

  @Override
  public String toString() {
    return new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).toString();
  }
}
