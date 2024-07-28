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

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.model.AbstractBoundModule;
import gov.nist.secauto.metaschema.databind.model.IBoundModule;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaModule;

import java.net.URI;
import java.util.List;

@MetaschemaModule(
    fields = {
        UseName.class,
        Remarks.class,
        ConstraintValueEnum.class
    },
    assemblies = {
        METASCHEMA.class,
        InlineDefineAssembly.class,
        InlineDefineField.class,
        InlineDefineFlag.class,
        Any.class,
        AssemblyReference.class,
        FieldReference.class,
        FlagReference.class,
        AssemblyModel.class,
        JsonValueKeyFlag.class,
        GroupingAs.class,
        Example.class,
        Property.class,
        JsonKey.class,
        AssemblyConstraints.class,
        FieldConstraints.class,
        FlagConstraints.class,
        ConstraintLetExpression.class,
        FlagAllowedValues.class,
        FlagExpect.class,
        FlagIndexHasKey.class,
        FlagMatches.class,
        TargetedAllowedValuesConstraint.class,
        TargetedMatchesConstraint.class,
        TargetedExpectConstraint.class,
        TargetedIndexHasKeyConstraint.class,
        KeyConstraintField.class,
        TargetedIsUniqueConstraint.class,
        TargetedIndexConstraint.class,
        TargetedHasCardinalityConstraint.class,
        MetaschemaModuleConstraints.class,
        MetaschemaMetaConstraints.class,
        MetaschemaMetapath.class,
        MetapathContext.class
    })
public final class MetaschemaModelModule
    extends AbstractBoundModule {
  private static final MarkupLine NAME = MarkupLine.fromMarkdown("Metaschema Model");

  private static final String SHORT_NAME = "metaschema-model";

  private static final String VERSION = "1.0.0-M2";

  private static final URI XML_NAMESPACE = URI.create("http://csrc.nist.gov/ns/oscal/metaschema/1.0");

  private static final URI JSON_BASE_URI = URI.create("http://csrc.nist.gov/ns/oscal/metaschema/1.0");

  public MetaschemaModelModule(List<? extends IBoundModule> importedModules,
      IBindingContext bindingContext) {
    super(importedModules, bindingContext);
  }

  @Override
  public MarkupLine getName() {
    return NAME;
  }

  @Override
  public String getShortName() {
    return SHORT_NAME;
  }

  @Override
  public String getVersion() {
    return VERSION;
  }

  @Override
  public URI getXmlNamespace() {
    return XML_NAMESPACE;
  }

  @Override
  public URI getJsonBaseUri() {
    return JSON_BASE_URI;
  }

  @Override
  public MarkupMultiline getRemarks() {
    return null;
  }
}
