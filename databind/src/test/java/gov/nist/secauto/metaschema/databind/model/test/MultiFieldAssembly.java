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

package gov.nist.secauto.metaschema.databind.model.test;

import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.core.model.IMetaschemaData;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundField;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundFieldValue;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundFlag;
import gov.nist.secauto.metaschema.databind.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaField;

import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// Used
@SuppressWarnings("PMD")
@MetaschemaAssembly(name = "test-field", moduleClass = TestMetaschema.class)
public class MultiFieldAssembly implements IBoundObject {
  private final IMetaschemaData metaschemaData;

  @BoundField
  private String field1;

  @BoundField(useName = "field2",
      maxOccurs = -1,
      groupAs = @GroupAs(name = "fields2",
          inXml = XmlGroupAsBehavior.GROUPED,
          inJson = JsonGroupAsBehavior.LIST))
  private List<String> _field2;

  @BoundField
  private ValueKeyField field3;

  @BoundField
  private DefaultValueKeyField field4;

  public MultiFieldAssembly() {
    this(null);
  }

  public MultiFieldAssembly(IMetaschemaData metaschemaData) {
    this.metaschemaData = metaschemaData;
  }

  @Override
  public IMetaschemaData getMetaschemaData() {
    return metaschemaData;
  }

  public String getField1() {
    return field1;
  }

  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "this is a data holder")
  public List<String> getField2() {
    return _field2;
  }

  public ValueKeyField getField3() {
    return field3;
  }

  public void setField3(ValueKeyField field3) {
    this.field3 = field3;
  }

  public DefaultValueKeyField getField4() {
    return field4;
  }

  public void setField4(DefaultValueKeyField field4) {
    this.field4 = field4;
  }

  @SuppressWarnings("PMD")
  @MetaschemaField(
      name = "field-value-key",
      moduleClass = TestMetaschema.class)
  public static class ValueKeyField implements IBoundObject {
    private final IMetaschemaData metaschemaData;

    @BoundFlag
    private String flag;

    @BoundFieldValue(valueKeyName = "a-value")
    private String _value;

    public ValueKeyField() {
      this(null);
    }

    public ValueKeyField(IMetaschemaData metaschemaData) {
      this.metaschemaData = metaschemaData;
    }

    @Override
    public IMetaschemaData getMetaschemaData() {
      return metaschemaData;
    }

    public String getValue() {
      return _value;
    }
  }

  @SuppressWarnings("PMD")
  @MetaschemaField(
      name = "field-default-value-key",
      moduleClass = TestMetaschema.class)
  public static class DefaultValueKeyField implements IBoundObject {
    private final IMetaschemaData metaschemaData;

    @BoundFlag
    private String flag;

    @BoundFieldValue
    private String _value;

    public DefaultValueKeyField() {
      this(null);
    }

    public DefaultValueKeyField(IMetaschemaData metaschemaData) {
      this.metaschemaData = metaschemaData;
    }

    @Override
    public IMetaschemaData getMetaschemaData() {
      return metaschemaData;
    }

    public String getValue() {
      return _value;
    }
  }
}
