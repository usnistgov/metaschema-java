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

import gov.nist.secauto.metaschema.binding.model.annotations.Field;
import gov.nist.secauto.metaschema.binding.model.annotations.Flag;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.StringAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.UuidAdapter;
import gov.nist.secauto.metaschema.model.common.instance.JsonGroupAsBehavior;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@MetaschemaAssembly(rootName = "root")
public class RootBoundAssembly {
  @Flag(useName = "uuid", defaultValue = "374dd648-b247-483c-afd8-a66ba8876070", typeAdapter = UuidAdapter.class)
  private UUID uuid;

  /**
   * An optional singleton simple field.
   */
  @Field(useName = "simple-singleton-field",
      typeAdapter = StringAdapter.class)
  private String simpleSingletonField;

  /**
   * A required keyed assembly.
   */
  @Field(useName = "keyed-field",
      minOccurs = 1,
      maxOccurs = -1,
      groupName = "keyed-field-items",
      inJson = JsonGroupAsBehavior.KEYED)
  private Map<String, FlaggedBoundField> keyedField;  

  /**
   * A required singleton or array assembly.
   */
  @Field(useName = "singleton-or-array-assembly",
      minOccurs = 1,
      maxOccurs = -1,
      groupName = "singleton-or-array-assembly-items",
      inJson = JsonGroupAsBehavior.SINGLETON_OR_LIST)
  private List<OnlyModelBoundAssembly> singletonOrArrayAssembly;
}
