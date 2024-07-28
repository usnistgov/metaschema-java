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

package gov.nist.secauto.metaschema.databind.model.binding.metaschema;

import gov.nist.secauto.metaschema.core.datatype.adapter.NonNegativeIntegerAdapter;
import gov.nist.secauto.metaschema.core.datatype.adapter.PositiveIntegerAdapter;
import gov.nist.secauto.metaschema.core.datatype.adapter.StringAdapter;
import gov.nist.secauto.metaschema.core.datatype.adapter.TokenAdapter;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLineAdapter;
import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.core.model.IMetaschemaData;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.annotations.AllowedValue;
import gov.nist.secauto.metaschema.databind.model.annotations.AllowedValues;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundField;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundFlag;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.Expect;
import gov.nist.secauto.metaschema.databind.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.databind.model.annotations.Matches;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings({
    "PMD.DataClass",
    "PMD.FieldNamingConventions",
    "PMD.TooManyFields"
})
@MetaschemaAssembly(
    formalName = "Inline Field Definition",
    name = "inline-define-field",
    moduleClass = MetaschemaModelModule.class)
public final class InlineDefineField implements IBoundObject {
  private final IMetaschemaData __metaschemaData;

  @BoundFlag(
      formalName = "Inline Field Name",
      name = "name",
      required = true,
      typeAdapter = TokenAdapter.class,
      valueConstraints = @ValueConstraints(expect = @Expect(level = IConstraint.Level.WARNING,
          test = "not(.=$keywords)", message = "Names cannot be non-delimiting terminal symbols in Metapath syntax.")))
  private String _name;

  @BoundFlag(
      formalName = "Inline Field Binary Name",
      name = "index",
      typeAdapter = PositiveIntegerAdapter.class)
  private BigInteger _index;

  @BoundFlag(
      formalName = "Deprecated Version",
      name = "deprecated",
      typeAdapter = StringAdapter.class)
  private String _deprecated;

  @BoundFlag(
      formalName = "Field Value Data Type",
      name = "as-type",
      defaultValue = "string",
      typeAdapter = TokenAdapter.class,
      valueConstraints = @ValueConstraints(allowedValues = @AllowedValues(level = IConstraint.Level.ERROR,
          allowOthers = true,
          values = { @AllowedValue(value = "markup-line", description = ""),
              @AllowedValue(value = "markup-multiline", description = ""),
              @AllowedValue(value = "base64", description = ""), @AllowedValue(value = "boolean", description = ""),
              @AllowedValue(value = "date", description = ""), @AllowedValue(value = "date-time", description = ""),
              @AllowedValue(value = "date-time-with-timezone", description = ""),
              @AllowedValue(value = "date-with-timezone", description = ""),
              @AllowedValue(value = "day-time-duration", description = ""),
              @AllowedValue(value = "decimal", description = ""),
              @AllowedValue(value = "email-address", description = ""),
              @AllowedValue(value = "hostname", description = ""), @AllowedValue(value = "integer", description = ""),
              @AllowedValue(value = "ip-v4-address", description = ""),
              @AllowedValue(value = "ip-v6-address", description = ""),
              @AllowedValue(value = "non-negative-integer", description = ""),
              @AllowedValue(value = "positive-integer", description = ""),
              @AllowedValue(value = "string", description = ""), @AllowedValue(value = "token", description = ""),
              @AllowedValue(value = "uri", description = ""), @AllowedValue(value = "uri-reference", description = ""),
              @AllowedValue(value = "uuid", description = "") })))
  private String _asType;

  @BoundFlag(
      formalName = "Default Field Value",
      name = "default",
      typeAdapter = StringAdapter.class)
  private String _default;

  @BoundFlag(
      formalName = "Minimum Occurrence",
      name = "min-occurs",
      defaultValue = "0",
      typeAdapter = NonNegativeIntegerAdapter.class)
  private BigInteger _minOccurs;

  @BoundFlag(
      formalName = "Maximum Occurrence",
      name = "max-occurs",
      defaultValue = "1",
      typeAdapter = StringAdapter.class,
      valueConstraints = @ValueConstraints(
          matches = @Matches(level = IConstraint.Level.ERROR, pattern = "^[1-9][0-9]*|unbounded$")))
  private String _maxOccurs;

  @BoundFlag(
      formalName = "Field In XML",
      name = "in-xml",
      defaultValue = "WRAPPED",
      typeAdapter = TokenAdapter.class,
      valueConstraints = @ValueConstraints(allowedValues = @AllowedValues(level = IConstraint.Level.ERROR, values = {
          @AllowedValue(value = "WRAPPED",
              description = "Block contents of a markup-multiline field will be represented with a containing (wrapper) element in the XML."),
          @AllowedValue(value = "UNWRAPPED",
              description = "Block contents of a markup-multiline will be represented in the XML with no wrapper, making the field implicit. Among sibling fields in a given model, only one of them may be designated as UNWRAPPED."),
          @AllowedValue(value = "WITH_WRAPPER", description = "Alias for WRAPPED.") })))
  private String _inXml;

  @BoundField(
      formalName = "Formal Name",
      description = "A formal name for the data construct, to be presented in documentation.",
      useName = "formal-name")
  private String _formalName;

  @BoundField(
      formalName = "Description",
      description = "A short description of the data construct's purpose, describing the constructs semantics.",
      useName = "description",
      typeAdapter = MarkupLineAdapter.class)
  private MarkupLine _description;

  @BoundAssembly(
      formalName = "Property",
      useName = "prop",
      maxOccurs = -1,
      groupAs = @GroupAs(name = "props", inJson = JsonGroupAsBehavior.LIST))
  private List<Property> _props;

  @BoundAssembly(
      formalName = "JSON Key",
      description = "Used in JSON (and similar formats) to identify a flag that will be used as the property name in an object hold a collection of sibling objects. Requires that siblings must never share `json-key` values.",
      useName = "json-key")
  private JsonKey _jsonKey;

  @BoundField(
      formalName = "Field Value JSON Property Name",
      useName = "json-value-key",
      typeAdapter = TokenAdapter.class)
  private String _jsonValueKey;

  @BoundAssembly(
      formalName = "Flag Used as the Field Value's JSON Property Name",
      useName = "json-value-key-flag")
  private JsonValueKeyFlag _jsonValueKeyFlag;

  @BoundAssembly(
      formalName = "Group As",
      useName = "group-as")
  private GroupingAs _groupAs;

  @BoundChoiceGroup(
      maxOccurs = -1,
      assemblies = {
          @BoundGroupedAssembly(formalName = "Inline Flag Definition", useName = "define-flag",
              binding = InlineDefineFlag.class),
          @BoundGroupedAssembly(formalName = "Flag Reference", useName = "flag", binding = FlagReference.class)
      },
      groupAs = @GroupAs(name = "flags", inJson = JsonGroupAsBehavior.LIST))
  private List<Object> _flags;

  @BoundAssembly(
      useName = "constraint")
  private FieldConstraints _constraint;

  @BoundField(
      formalName = "Remarks",
      description = "Any explanatory or helpful information to be provided about the remarks parent.",
      useName = "remarks")
  private Remarks _remarks;

  @BoundAssembly(
      formalName = "Example",
      useName = "example",
      maxOccurs = -1,
      groupAs = @GroupAs(name = "examples", inJson = JsonGroupAsBehavior.LIST))
  private List<Example> _examples;

  public InlineDefineField() {
    this(null);
  }

  public InlineDefineField(IMetaschemaData data) {
    this.__metaschemaData = data;
  }

  @Override
  public IMetaschemaData getMetaschemaData() {
    return __metaschemaData;
  }

  public String getName() {
    return _name;
  }

  public void setName(String value) {
    _name = value;
  }

  public BigInteger getIndex() {
    return _index;
  }

  public void setIndex(BigInteger value) {
    _index = value;
  }

  public String getDeprecated() {
    return _deprecated;
  }

  public void setDeprecated(String value) {
    _deprecated = value;
  }

  public String getAsType() {
    return _asType;
  }

  public void setAsType(String value) {
    _asType = value;
  }

  public String getDefault() {
    return _default;
  }

  public void setDefault(String value) {
    _default = value;
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

  public String getInXml() {
    return _inXml;
  }

  public void setInXml(String value) {
    _inXml = value;
  }

  public String getFormalName() {
    return _formalName;
  }

  public void setFormalName(String value) {
    _formalName = value;
  }

  public MarkupLine getDescription() {
    return _description;
  }

  public void setDescription(MarkupLine value) {
    _description = value;
  }

  public List<Property> getProps() {
    return _props;
  }

  public void setProps(List<Property> value) {
    _props = value;
  }

  /**
   * Add a new {@link Property} item to the underlying collection.
   *
   * @param item
   *          the item to add
   * @return {@code true}
   */
  public boolean addProp(Property item) {
    Property value = ObjectUtils.requireNonNull(item, "item cannot be null");
    if (_props == null) {
      _props = new LinkedList<>();
    }
    return _props.add(value);
  }

  /**
   * Remove the first matching {@link Property} item from the underlying
   * collection.
   *
   * @param item
   *          the item to remove
   * @return {@code true} if the item was removed or {@code false} otherwise
   */
  public boolean removeProp(Property item) {
    Property value = ObjectUtils.requireNonNull(item, "item cannot be null");
    return _props != null && _props.remove(value);
  }

  public JsonKey getJsonKey() {
    return _jsonKey;
  }

  public void setJsonKey(JsonKey value) {
    _jsonKey = value;
  }

  public String getJsonValueKey() {
    return _jsonValueKey;
  }

  public void setJsonValueKey(String value) {
    _jsonValueKey = value;
  }

  public JsonValueKeyFlag getJsonValueKeyFlag() {
    return _jsonValueKeyFlag;
  }

  public void setJsonValueKeyFlag(JsonValueKeyFlag value) {
    _jsonValueKeyFlag = value;
  }

  public GroupingAs getGroupAs() {
    return _groupAs;
  }

  public void setGroupAs(GroupingAs value) {
    _groupAs = value;
  }

  public List<Object> getFlags() {
    return _flags;
  }

  public void setFlags(List<Object> value) {
    _flags = value;
  }

  public FieldConstraints getConstraint() {
    return _constraint;
  }

  public void setConstraint(FieldConstraints value) {
    _constraint = value;
  }

  public Remarks getRemarks() {
    return _remarks;
  }

  public void setRemarks(Remarks value) {
    _remarks = value;
  }

  public List<Example> getExamples() {
    return _examples;
  }

  public void setExamples(List<Example> value) {
    _examples = value;
  }

  /**
   * Add a new {@link Example} item to the underlying collection.
   *
   * @param item
   *          the item to add
   * @return {@code true}
   */
  public boolean addExample(Example item) {
    Example value = ObjectUtils.requireNonNull(item, "item cannot be null");
    if (_examples == null) {
      _examples = new LinkedList<>();
    }
    return _examples.add(value);
  }

  /**
   * Remove the first matching {@link Example} item from the underlying
   * collection.
   *
   * @param item
   *          the item to remove
   * @return {@code true} if the item was removed or {@code false} otherwise
   */
  public boolean removeExample(Example item) {
    Example value = ObjectUtils.requireNonNull(item, "item cannot be null");
    return _examples != null && _examples.remove(value);
  }

  @Override
  public String toString() {
    return new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).toString();
  }
}
