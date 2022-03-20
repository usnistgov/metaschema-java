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
package gov.nist.secauto.metaschema.schemagen;

import org.codehaus.stax2.XMLStreamWriter2;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

public class CompositeDatatypeProvider implements IDatatypeProvider {
  @NotNull
  private final List<@NotNull IDatatypeProvider> proxiedProviders;

  public CompositeDatatypeProvider(@NotNull List<@NotNull IDatatypeProvider> proxiedProviders) {
    this.proxiedProviders = proxiedProviders;
  }

  @NotNull
  protected List<@NotNull IDatatypeProvider> getProxiedProviders() {
    return proxiedProviders;
  }

  @Override
  public @NotNull Map<@NotNull String, IDatatypeContent> getDatatypes() {
    return proxiedProviders.stream()
        .flatMap(provider -> provider.getDatatypes().values().stream())
        .collect(Collectors.toMap(content -> content.getTypeName(), Function.identity(), (e1, e2) -> e2, LinkedHashMap::new));
  }

  @Override
  public Set<@NotNull String> generateDatatypes(Set<@NotNull String> requiredTypes, XMLStreamWriter2 writer)
      throws XMLStreamException {
    Set<@NotNull String> retval = new HashSet<>();
    
    for (IDatatypeProvider provider : getProxiedProviders()) {
      retval.addAll(provider.generateDatatypes(requiredTypes, writer));
    }
    return retval;
  }

}
