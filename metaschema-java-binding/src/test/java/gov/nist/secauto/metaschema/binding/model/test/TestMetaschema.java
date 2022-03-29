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

import gov.nist.secauto.metaschema.binding.AbstractBoundMetaschema;
import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.model.annotations.Metaschema;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;

@Metaschema(assemblies = {
    EmptyBoundAssembly.class,
    FlaggedBoundAssembly.class,
    OnlyModelBoundAssembly.class,
    RootBoundAssembly.class
}, fields = {
    CollapsibleFlaggedBoundField.class,
    FlaggedBoundField.class
})
public class TestMetaschema
    extends AbstractBoundMetaschema {

  public TestMetaschema(@NotNull List<@NotNull ? extends IMetaschema> importedMetaschema,
      @NotNull IBindingContext bindingContext) {
    super(importedMetaschema, bindingContext);
  }

  @Override
  public MarkupLine getName() {
    return MarkupLine.fromMarkdown("Test Metaschema");
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public MarkupMultiline getRemarks() {
    return null;
  }

  @Override
  public @NotNull String getShortName() {
    return "test-metaschema";
  }

  @Override
  public @NotNull URI getXmlNamespace() {
    return URI.create("https://csrc.nist.gov/ns/test/xml");
  }

  @Override
  public @NotNull URI getJsonBaseUri() {
    return URI.create("https://csrc.nist.gov/ns/test/json");
  }

}
