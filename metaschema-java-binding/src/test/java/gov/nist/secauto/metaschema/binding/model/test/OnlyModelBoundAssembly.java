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

package gov.nist.secauto.metaschema.binding.model.test;

import gov.nist.secauto.metaschema.binding.model.annotations.BoundAssembly;
import gov.nist.secauto.metaschema.binding.model.annotations.BoundField;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.model.common.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.StringAdapter;

import java.util.List;
import java.util.Map;

@SuppressWarnings("PMD")
@MetaschemaAssembly(name= "only-model", metaschema = TestMetaschema.class)
public class OnlyModelBoundAssembly { // NOPMD - intentional
  /*
   * ================ = simple field = ================
   */
  /**
   * An optional singleton simple field.
   */
  @BoundField(useName = "simple-singleton-field",
      typeAdapter = StringAdapter.class)
  private String simpleSingletonField;

  /**
   * A required singleton simple field.
   */
  @BoundField(useName = "simple-required-singleton-field",
      typeAdapter = StringAdapter.class,
      minOccurs = 1)
  private String simpleRequiredSingletonField;

  /**
   * An optional array field.
   */
  @BoundField(useName = "simple-array-field",
      typeAdapter = StringAdapter.class,
      maxOccurs = -1,
      groupName = "simple-array-field-items",
      inJson = JsonGroupAsBehavior.LIST)
  private List<String> simpleArrayField;

  /**
   * An required array field.
   */
  @BoundField(useName = "simple-required-array-field",
      typeAdapter = StringAdapter.class,
      minOccurs = 1,
      maxOccurs = -1,
      groupName = "simple-required-array-field-items",
      inJson = JsonGroupAsBehavior.LIST)
  private List<String> simpleRequiredArrayField;

  /**
   * An optional singleton or array field.
   */
  @BoundField(useName = "simple-singleton-or-array-field",
      typeAdapter = StringAdapter.class,
      maxOccurs = -1,
      groupName = "simple-singleton-or-array-field-items",
      inJson = JsonGroupAsBehavior.SINGLETON_OR_LIST)
  private List<String> simpleSingletonOrArrayField;

  /*
   * ================= = flagged field = =================
   */
  /**
   * An optional singleton flagged field.
   */
  @BoundField(useName = "flagged-singleton-field")
  private FlaggedBoundField flaggedSingletonField;

  /**
   * A required singleton flagged field.
   */
  @BoundField(useName = "flagged-required-singleton-field",
      minOccurs = 1)
  private FlaggedBoundField flaggedRequiredSingletonField;

  /**
   * An optional array flagged field.
   */
  @BoundField(useName = "flagged-array-field",
      maxOccurs = -1,
      groupName = "flagged-array-field-items",
      inJson = JsonGroupAsBehavior.LIST)
  private List<FlaggedBoundField> flaggedArrayField;

  /**
   * An required array flagged field.
   */
  @BoundField(useName = "flagged-required-array-field",
      minOccurs = 1,
      maxOccurs = -1,
      groupName = "flagged-required-array-field-items",
      inJson = JsonGroupAsBehavior.LIST)
  private List<FlaggedBoundField> flaggedRequiredArrayField;

  /**
   * An optional singleton or array flagged field.
   */
  @BoundField(useName = "flagged-singleton-or-array-field",
      maxOccurs = -1,
      groupName = "flagged-singleton-or-array-field-items",
      inJson = JsonGroupAsBehavior.SINGLETON_OR_LIST)
  private List<FlaggedBoundField> flaggedSingletonOrArrayField;

  /*
   * ============================= = flagged collapsible field = =============================
   */
  /**
   * An optional singleton flagged field.
   */
  @BoundField(useName = "collapsible-singleton-field")
  private CollapsibleFlaggedBoundField collapsibleSingletonField;

  /**
   * A required singleton flagged field.
   */
  @BoundField(useName = "collapsible-required-singleton-field",
      minOccurs = 1)
  private CollapsibleFlaggedBoundField collapsibleRequiredSingletonField;

  /**
   * An optional array flagged field.
   */
  @BoundField(useName = "collapsible-array-field",
      maxOccurs = -1,
      groupName = "collapsible-array-field-items",
      inJson = JsonGroupAsBehavior.LIST)
  private List<CollapsibleFlaggedBoundField> collapsibleArrayField;

  /**
   * An required array flagged field.
   */
  @BoundField(useName = "collapsible-required-array-field",
      minOccurs = 1,
      maxOccurs = -1,
      groupName = "collapsible-required-array-field-items",
      inJson = JsonGroupAsBehavior.LIST)
  private List<CollapsibleFlaggedBoundField> collapsibleRequiredArrayField;

  /**
   * An optional singleton or array flagged field.
   */
  @BoundField(useName = "collapsible-singleton-or-array-field",
      maxOccurs = -1,
      groupName = "collapsible-singleton-or-array-field-items",
      inJson = JsonGroupAsBehavior.SINGLETON_OR_LIST)
  private List<CollapsibleFlaggedBoundField> collapsibleSingletonOrArrayField;

  /*
   * ============== = assemblies = ==============
   */
  /**
   * An optional singleton assembly.
   */
  @BoundAssembly(useName = "singleton-assembly")
  private EmptyBoundAssembly singletonAssembly;

  /**
   * An optional array assembly.
   */
  @BoundAssembly(useName = "array-assembly",
      maxOccurs = -1,
      groupName = "array-assembly-items",
      inJson = JsonGroupAsBehavior.LIST)
  private List<OnlyModelBoundAssembly> arrayAssembly;

  /**
   * An optional singleton or array assembly.
   */
  @BoundAssembly(useName = "singleton-or-array-assembly",
      maxOccurs = -1,
      groupName = "singleton-or-array-assembly-items",
      inJson = JsonGroupAsBehavior.SINGLETON_OR_LIST)
  private List<OnlyModelBoundAssembly> singletonOrArrayAssembly;
  /**
   * An optional keyed assembly.
   */
  @BoundAssembly(useName = "keyed-assembly",
      maxOccurs = -1,
      groupName = "keyed-assembly-items",
      inJson = JsonGroupAsBehavior.KEYED)
  private Map<String, FlaggedBoundAssembly> keyedAssembly;
}
