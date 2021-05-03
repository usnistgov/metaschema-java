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

import gov.nist.secauto.metaschema.binding.model.annotations.Assembly;
import gov.nist.secauto.metaschema.binding.model.annotations.Field;
import gov.nist.secauto.metaschema.binding.model.annotations.FieldValue;
import gov.nist.secauto.metaschema.binding.model.annotations.Flag;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonKey;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.binding.model.annotations.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.datatypes.adapter.types.StringAdapter;

import java.util.List;
import java.util.Map;

@MetaschemaAssembly(rootName = "root")
public class BoundClass {
  @Flag(name = "id", typeAdapter = StringAdapter.class)
  private String id;

  /**
   * An optional simple field.
   */
  @Field(name = "single-simple",
      typeAdapter = StringAdapter.class,
      valueName = "value")
  private String singleSimpleField;

  /**
   * An optional simple field.
   */
  @Field(name = "grouped-list-simple-item",
      typeAdapter = StringAdapter.class,
      valueName = "value",
      minOccurs = 0,
      maxOccurs = -1,
      groupName = "grouped-list-simple-items",
      inJson = JsonGroupAsBehavior.LIST,
      inXml = XmlGroupAsBehavior.GROUPED)
  private List<String> groupedListSimpleField;

  /**
   * An optional field with a flag.
   */
  @Field(name = "single-flagged")
  private BoundClass.FlaggedField singleFlaggedField;

  /**
   * A list of fields with a flag, that is grouped in XML.
   */
  @Field(name = "grouped-list-item",
      minOccurs = 0,
      maxOccurs = -1,
      groupName = "grouped-list-items",
      inJson = JsonGroupAsBehavior.LIST,
      inXml = XmlGroupAsBehavior.GROUPED)
  private List<BoundClass.FlaggedField> groupedListField;

  /**
   * A list of fields with a flag, which may be a singleton in JSON.
   */
  @Field(name = "ungrouped-list-item",
      minOccurs = 0,
      maxOccurs = -1,
      groupName = "ungrouped-list-items",
      inJson = JsonGroupAsBehavior.SINGLETON_OR_LIST,
      inXml = XmlGroupAsBehavior.UNGROUPED)
  private List<BoundClass.FlaggedField> ungroupedListField;

  /**
   * A map of fields with an id key and name.
   */
  @Field(name = "mapped-item",
      minOccurs = 0,
      maxOccurs = -1,
      groupName = "mapped-items",
      inJson = JsonGroupAsBehavior.KEYED,
      inXml = XmlGroupAsBehavior.UNGROUPED)
  private Map<String, BoundClass.KeyedField> mappedField;

  @Assembly(name = "single-flagged-assembly")
  private FlaggedAssemblyClass singleFlaggedAssembly;

  public BoundClass() {
  }

  public String getId() {
    return id;
  }

  public void setId(String value) {
    id = value;
  }

  protected String getSingleSimpleField() {
    return singleSimpleField;
  }

  protected void setSingleSimpleField(String value) {
    this.singleSimpleField = value;
  }

  protected List<String> getGroupedListSimpleField() {
    return groupedListSimpleField;
  }

  protected void setGroupedListSimpleField(List<String> value) {
    this.groupedListSimpleField = value;
  }

  protected BoundClass.FlaggedField getSingleFlaggedField() {
    return singleFlaggedField;
  }

  protected void setSingleFlaggedField(BoundClass.FlaggedField value) {
    this.singleFlaggedField = value;
  }

  protected List<BoundClass.FlaggedField> getGroupedListField() {
    return groupedListField;
  }

  protected void setGroupedListField(List<BoundClass.FlaggedField> value) {
    this.groupedListField = value;
  }

  protected List<BoundClass.FlaggedField> getUngroupedListField() {
    return ungroupedListField;
  }

  protected void setUngroupedListField(List<BoundClass.FlaggedField> value) {
    this.ungroupedListField = value;
  }

  protected Map<String, BoundClass.KeyedField> getMappedField() {
    return mappedField;
  }

  protected void setMappedField(Map<String, BoundClass.KeyedField> value) {
    this.mappedField = value;
  }

  protected FlaggedAssemblyClass getSingleFlaggedAssembly() {
    return singleFlaggedAssembly;
  }

  protected void setSingleFlaggedAssembly(FlaggedAssemblyClass singleFlaggedAssembly) {
    this.singleFlaggedAssembly = singleFlaggedAssembly;
  }

  /**
   * A complex field with a flag.
   */
  @MetaschemaField(isCollapsible = false)
  public static class FlaggedField {
    @Flag(name = "id", typeAdapter = StringAdapter.class)
    private String _id;

    @FieldValue(name = "STRXVALUE", typeAdapter = StringAdapter.class)
    private String _value;

    public FlaggedField() {
    }

    public String getId() {
      return _id;
    }

    public void setId(String value) {
      _id = value;
    }

    public String getValue() {
      return _value;
    }

    public void setValue(String value) {
      _value = value;
    }
  }

  /**
   * A complex field with multiple flag.
   */
  @MetaschemaField(isCollapsible = true)
  public static class KeyedField {
    @JsonKey
    @Flag(name = "id", typeAdapter = StringAdapter.class)
    private String _id;

    @Flag(name = "name", typeAdapter = StringAdapter.class)
    private String _name;

    @FieldValue(name = "VALUE", typeAdapter = StringAdapter.class)
    private String _value;

    public KeyedField() {
    }

    public String getId() {
      return _id;
    }

    public void setId(String value) {
      _id = value;
    }

    protected String getName() {
      return _name;
    }

    protected void setName(String value) {
      this._name = value;
    }

    public String getValue() {
      return _value;
    }

    public void setValue(String value) {
      _value = value;
    }
  }
}