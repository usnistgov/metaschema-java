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

import gov.nist.secauto.metaschema.core.datatype.adapter.StringAdapter;
import gov.nist.secauto.metaschema.core.datatype.adapter.TokenAdapter;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLineAdapter;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.annotations.AllowedValue;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundField;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundFlag;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@MetaschemaAssembly(
    name = "flag-constraints",
    moduleClass = MetaschemaModule.class)
public class FlagConstraints {
  @BoundAssembly(
      formalName = "Constraint Let Expression",
      useName = "let",
      maxOccurs = -1,
      groupAs = @GroupAs(name = "lets", inJson = JsonGroupAsBehavior.LIST))
  private List<ConstraintLetExpression> _lets;

  @BoundChoiceGroup(
      minOccurs = 1,
      maxOccurs = -1,
      assemblies = {
          @BoundGroupedAssembly(formalName = "Allowed Values Constraint", useName = "allowed-values",
              binding = AllowedValues.class),
          @BoundGroupedAssembly(formalName = "Expect Condition Constraint", useName = "expect", binding = Expect.class),
          @BoundGroupedAssembly(formalName = "Index Has Key Constraint", useName = "index-has-key",
              binding = IndexHasKey.class),
          @BoundGroupedAssembly(formalName = "Value Matches Constraint", useName = "matches", binding = Matches.class)
      },
      groupAs = @GroupAs(name = "rules", inJson = JsonGroupAsBehavior.LIST))
  private List<Object> _rules;

  public FlagConstraints() {
  }

  public List<ConstraintLetExpression> getLets() {
    return _lets;
  }

  public void setLets(List<ConstraintLetExpression> value) {
    _lets = value;
  }

  /**
   * Add a new {@link ConstraintLetExpression} item to the underlying collection.
   *
   * @param item
   *          the item to add
   * @return {@code true}
   */
  public boolean addLet(ConstraintLetExpression item) {
    ConstraintLetExpression value = ObjectUtils.requireNonNull(item, "item cannot be null");
    if (_lets == null) {
      _lets = new LinkedList<>();
    }
    return _lets.add(value);
  }

  /**
   * Remove the first matching {@link ConstraintLetExpression} item from the
   * underlying collection.
   *
   * @param item
   *          the item to remove
   * @return {@code true} if the item was removed or {@code false} otherwise
   */
  public boolean removeLet(ConstraintLetExpression item) {
    ConstraintLetExpression value = ObjectUtils.requireNonNull(item, "item cannot be null");
    return _lets == null ? false : _lets.remove(value);
  }

  public List<Object> getRules() {
    return _rules;
  }

  public void setRules(List<Object> value) {
    _rules = value;
  }

  @Override
  public String toString() {
    return new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).toString();
  }

  @MetaschemaAssembly(
      formalName = "Allowed Values Constraint",
      name = "allowed-values",
      moduleClass = MetaschemaModule.class)
  public static class AllowedValues {
    @BoundFlag(
        formalName = "Constraint Identifier",
        useName = "id",
        typeAdapter = TokenAdapter.class)
    private String _id;

    @BoundFlag(
        formalName = "Constraint Severity Level",
        useName = "level",
        typeAdapter = TokenAdapter.class,
        valueConstraints = @ValueConstraints(
            allowedValues = @gov.nist.secauto.metaschema.databind.model.annotations.AllowedValues(
                level = IConstraint.Level.ERROR,
                values = { @AllowedValue(value = "CRITICAL",
                    description = "A violation of the constraint represents a serious fault in the content that will prevent typical use of the content."),
                    @AllowedValue(value = "ERROR",
                        description = "A violation of the constraint represents a fault in the content. This may include issues around compatibility, integrity, consistency, etc."),
                    @AllowedValue(value = "WARNING",
                        description = "A violation of the constraint represents a potential issue with the content."),
                    @AllowedValue(value = "INFORMATIONAL",
                        description = "A violation of the constraint represents a point of interest.") })))
    private String _level;

    @BoundFlag(
        formalName = "Allow Non-Enumerated Values?",
        useName = "allow-other",
        typeAdapter = TokenAdapter.class,
        valueConstraints = @ValueConstraints(
            allowedValues = @gov.nist.secauto.metaschema.databind.model.annotations.AllowedValues(
                level = IConstraint.Level.ERROR, values = { @AllowedValue(value = "no", description = ""),
                    @AllowedValue(value = "yes", description = "") })))
    private String _allowOther;

    /**
     * "Determines if the given enumerated values may be extended by other allowed
     * value constraints."
     */
    @BoundFlag(
        formalName = "Allow Extension?",
        description = "Determines if the given enumerated values may be extended by other allowed value constraints.",
        useName = "extensible",
        typeAdapter = TokenAdapter.class,
        valueConstraints = @ValueConstraints(
            allowedValues = @gov.nist.secauto.metaschema.databind.model.annotations.AllowedValues(
                level = IConstraint.Level.ERROR,
                values = {
                    @AllowedValue(value = "model",
                        description = "Can be extended by constraints within the same module."),
                    @AllowedValue(value = "external", description = "Can be extended by external constraints."),
                    @AllowedValue(value = "none", description = "Cannot be extended.") })))
    private String _extensible;

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

    @BoundField(
        formalName = "Allowed Value Enumeration",
        useName = "enum",
        minOccurs = 1,
        maxOccurs = -1,
        groupAs = @GroupAs(name = "enums", inJson = JsonGroupAsBehavior.LIST))
    private List<ConstraintValueEnum> _enums;

    @BoundField(
        formalName = "Remarks",
        description = "Any explanatory or helpful information to be provided about the remarks parent.",
        useName = "remarks")
    private Remarks _remarks;

    public AllowedValues() {
    }

    public String getId() {
      return _id;
    }

    public void setId(String value) {
      _id = value;
    }

    public String getLevel() {
      return _level;
    }

    public void setLevel(String value) {
      _level = value;
    }

    public String getAllowOther() {
      return _allowOther;
    }

    public void setAllowOther(String value) {
      _allowOther = value;
    }

    public String getExtensible() {
      return _extensible;
    }

    public void setExtensible(String value) {
      _extensible = value;
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
      return _props == null ? false : _props.remove(value);
    }

    public List<ConstraintValueEnum> getEnums() {
      return _enums;
    }

    public void setEnums(List<ConstraintValueEnum> value) {
      _enums = value;
    }

    /**
     * Add a new {@link ConstraintValueEnum} item to the underlying collection.
     *
     * @param item
     *          the item to add
     * @return {@code true}
     */
    public boolean addEnum(ConstraintValueEnum item) {
      ConstraintValueEnum value = ObjectUtils.requireNonNull(item, "item cannot be null");
      if (_enums == null) {
        _enums = new LinkedList<>();
      }
      return _enums.add(value);
    }

    /**
     * Remove the first matching {@link ConstraintValueEnum} item from the
     * underlying collection.
     *
     * @param item
     *          the item to remove
     * @return {@code true} if the item was removed or {@code false} otherwise
     */
    public boolean removeEnum(ConstraintValueEnum item) {
      ConstraintValueEnum value = ObjectUtils.requireNonNull(item, "item cannot be null");
      return _enums == null ? false : _enums.remove(value);
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

  @MetaschemaAssembly(
      formalName = "Expect Condition Constraint",
      name = "expect",
      moduleClass = MetaschemaModule.class)
  public static class Expect {
    @BoundFlag(
        formalName = "Constraint Identifier",
        useName = "id",
        typeAdapter = TokenAdapter.class)
    private String _id;

    @BoundFlag(
        formalName = "Constraint Severity Level",
        useName = "level",
        typeAdapter = TokenAdapter.class,
        valueConstraints = @ValueConstraints(
            allowedValues = @gov.nist.secauto.metaschema.databind.model.annotations.AllowedValues(
                level = IConstraint.Level.ERROR,
                values = { @AllowedValue(value = "CRITICAL",
                    description = "A violation of the constraint represents a serious fault in the content that will prevent typical use of the content."),
                    @AllowedValue(value = "ERROR",
                        description = "A violation of the constraint represents a fault in the content. This may include issues around compatibility, integrity, consistency, etc."),
                    @AllowedValue(value = "WARNING",
                        description = "A violation of the constraint represents a potential issue with the content."),
                    @AllowedValue(value = "INFORMATIONAL",
                        description = "A violation of the constraint represents a point of interest.") })))
    private String _level;

    @BoundFlag(
        formalName = "Expect Test Condition",
        useName = "test",
        required = true,
        typeAdapter = StringAdapter.class)
    private String _test;

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

    @BoundField(
        formalName = "Expect Condition Violation Message",
        useName = "message")
    private String _message;

    @BoundField(
        formalName = "Remarks",
        description = "Any explanatory or helpful information to be provided about the remarks parent.",
        useName = "remarks")
    private Remarks _remarks;

    public Expect() {
    }

    public String getId() {
      return _id;
    }

    public void setId(String value) {
      _id = value;
    }

    public String getLevel() {
      return _level;
    }

    public void setLevel(String value) {
      _level = value;
    }

    public String getTest() {
      return _test;
    }

    public void setTest(String value) {
      _test = value;
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
      return _props == null ? false : _props.remove(value);
    }

    public String getMessage() {
      return _message;
    }

    public void setMessage(String value) {
      _message = value;
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

  @MetaschemaAssembly(
      formalName = "Index Has Key Constraint",
      name = "index-has-key",
      moduleClass = MetaschemaModule.class)
  public static class IndexHasKey {
    @BoundFlag(
        formalName = "Constraint Identifier",
        useName = "id",
        typeAdapter = TokenAdapter.class)
    private String _id;

    @BoundFlag(
        formalName = "Constraint Severity Level",
        useName = "level",
        typeAdapter = TokenAdapter.class,
        valueConstraints = @ValueConstraints(
            allowedValues = @gov.nist.secauto.metaschema.databind.model.annotations.AllowedValues(
                level = IConstraint.Level.ERROR,
                values = { @AllowedValue(value = "CRITICAL",
                    description = "A violation of the constraint represents a serious fault in the content that will prevent typical use of the content."),
                    @AllowedValue(value = "ERROR",
                        description = "A violation of the constraint represents a fault in the content. This may include issues around compatibility, integrity, consistency, etc."),
                    @AllowedValue(value = "WARNING",
                        description = "A violation of the constraint represents a potential issue with the content."),
                    @AllowedValue(value = "INFORMATIONAL",
                        description = "A violation of the constraint represents a point of interest.") })))
    private String _level;

    @BoundFlag(
        formalName = "Index Name",
        useName = "name",
        required = true,
        typeAdapter = TokenAdapter.class)
    private String _name;

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
        formalName = "Key Constraint Field",
        useName = "key-field",
        minOccurs = 1,
        maxOccurs = -1,
        groupAs = @GroupAs(name = "key-fields", inJson = JsonGroupAsBehavior.LIST))
    private List<KeyConstraintField> _keyFields;

    @BoundField(
        formalName = "Remarks",
        description = "Any explanatory or helpful information to be provided about the remarks parent.",
        useName = "remarks")
    private Remarks _remarks;

    public IndexHasKey() {
    }

    public String getId() {
      return _id;
    }

    public void setId(String value) {
      _id = value;
    }

    public String getLevel() {
      return _level;
    }

    public void setLevel(String value) {
      _level = value;
    }

    public String getName() {
      return _name;
    }

    public void setName(String value) {
      _name = value;
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
      return _props == null ? false : _props.remove(value);
    }

    public List<KeyConstraintField> getKeyFields() {
      return _keyFields;
    }

    public void setKeyFields(List<KeyConstraintField> value) {
      _keyFields = value;
    }

    /**
     * Add a new {@link KeyConstraintField} item to the underlying collection.
     *
     * @param item
     *          the item to add
     * @return {@code true}
     */
    public boolean addKeyField(KeyConstraintField item) {
      KeyConstraintField value = ObjectUtils.requireNonNull(item, "item cannot be null");
      if (_keyFields == null) {
        _keyFields = new LinkedList<>();
      }
      return _keyFields.add(value);
    }

    /**
     * Remove the first matching {@link KeyConstraintField} item from the underlying
     * collection.
     *
     * @param item
     *          the item to remove
     * @return {@code true} if the item was removed or {@code false} otherwise
     */
    public boolean removeKeyField(KeyConstraintField item) {
      KeyConstraintField value = ObjectUtils.requireNonNull(item, "item cannot be null");
      return _keyFields == null ? false : _keyFields.remove(value);
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

  @MetaschemaAssembly(
      formalName = "Value Matches Constraint",
      name = "matches",
      moduleClass = MetaschemaModule.class)
  public static class Matches {
    @BoundFlag(
        formalName = "Constraint Identifier",
        useName = "id",
        typeAdapter = TokenAdapter.class)
    private String _id;

    @BoundFlag(
        formalName = "Constraint Severity Level",
        useName = "level",
        typeAdapter = TokenAdapter.class,
        valueConstraints = @ValueConstraints(
            allowedValues = @gov.nist.secauto.metaschema.databind.model.annotations.AllowedValues(
                level = IConstraint.Level.ERROR,
                values = { @AllowedValue(value = "CRITICAL",
                    description = "A violation of the constraint represents a serious fault in the content that will prevent typical use of the content."),
                    @AllowedValue(value = "ERROR",
                        description = "A violation of the constraint represents a fault in the content. This may include issues around compatibility, integrity, consistency, etc."),
                    @AllowedValue(value = "WARNING",
                        description = "A violation of the constraint represents a potential issue with the content."),
                    @AllowedValue(value = "INFORMATIONAL",
                        description = "A violation of the constraint represents a point of interest.") })))
    private String _level;

    @BoundFlag(
        formalName = "Matches Regular Expression",
        useName = "regex",
        typeAdapter = StringAdapter.class)
    private String _regex;

    @BoundFlag(
        formalName = "Matches Data Type",
        useName = "datatype",
        typeAdapter = TokenAdapter.class,
        valueConstraints = @ValueConstraints(
            allowedValues = @gov.nist.secauto.metaschema.databind.model.annotations.AllowedValues(
                level = IConstraint.Level.ERROR, allowOthers = true,
                values = { @AllowedValue(value = "base64", description = ""),
                    @AllowedValue(value = "boolean", description = ""), @AllowedValue(value = "date", description = ""),
                    @AllowedValue(value = "date-time", description = ""),
                    @AllowedValue(value = "date-time-with-timezone", description = ""),
                    @AllowedValue(value = "date-with-timezone", description = ""),
                    @AllowedValue(value = "day-time-duration", description = ""),
                    @AllowedValue(value = "decimal", description = ""),
                    @AllowedValue(value = "email-address", description = ""),
                    @AllowedValue(value = "hostname", description = ""),
                    @AllowedValue(value = "integer", description = ""),
                    @AllowedValue(value = "ip-v4-address", description = ""),
                    @AllowedValue(value = "ip-v6-address", description = ""),
                    @AllowedValue(value = "non-negative-integer", description = ""),
                    @AllowedValue(value = "positive-integer", description = ""),
                    @AllowedValue(value = "string", description = ""), @AllowedValue(value = "token", description = ""),
                    @AllowedValue(value = "uri", description = ""),
                    @AllowedValue(value = "uri-reference", description = ""),
                    @AllowedValue(value = "uuid", description = "") })))
    private String _datatype;

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

    @BoundField(
        formalName = "Remarks",
        description = "Any explanatory or helpful information to be provided about the remarks parent.",
        useName = "remarks")
    private Remarks _remarks;

    public Matches() {
    }

    public String getId() {
      return _id;
    }

    public void setId(String value) {
      _id = value;
    }

    public String getLevel() {
      return _level;
    }

    public void setLevel(String value) {
      _level = value;
    }

    public String getRegex() {
      return _regex;
    }

    public void setRegex(String value) {
      _regex = value;
    }

    public String getDatatype() {
      return _datatype;
    }

    public void setDatatype(String value) {
      _datatype = value;
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
      return _props == null ? false : _props.remove(value);
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
}
