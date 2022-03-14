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
package gov.nist.secauto.metaschema.schemagen.xml;

import gov.nist.secauto.metaschema.schemagen.CompositeDatatypeProvider;
import gov.nist.secauto.metaschema.schemagen.IDatatypeContent;
import gov.nist.secauto.metaschema.schemagen.IDatatypeProvider;

import org.codehaus.stax2.XMLStreamWriter2;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

public class ProseCompositDatatypeProvider
    extends CompositeDatatypeProvider {
  
  private final ProseBaseDatatypeProvider proseBaseProvider = new ProseBaseDatatypeProvider();

  public ProseCompositDatatypeProvider(List<@NotNull IDatatypeProvider> proxiedProviders) {
    super(proxiedProviders);
  }

  @Override
  public @NotNull Set<@NotNull String> generateDatatypes(Set<@NotNull String> requiredTypes,
      @NotNull XMLStreamWriter2 writer) throws XMLStreamException {
    Set<@NotNull String> result =  super.generateDatatypes(requiredTypes, writer);

    if (!result.isEmpty()) {
      // apply core markup types
      Collection<@NotNull IDatatypeContent> datatypes = proseBaseProvider.getDatatypes().values();
      Set<String> proseBaseTypes = datatypes.stream().map(content -> content.getTypeName()).collect(Collectors.toSet());
      proseBaseProvider.generateDatatypes(proseBaseTypes, writer);
    }
    return result;
  }

}
