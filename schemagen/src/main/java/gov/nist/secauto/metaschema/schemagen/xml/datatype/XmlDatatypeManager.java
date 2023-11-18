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

package gov.nist.secauto.metaschema.schemagen.xml.datatype;

import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.datatype.AbstractDatatypeManager;
import gov.nist.secauto.metaschema.schemagen.datatype.IDatatypeProvider;

import org.codehaus.stax2.XMLStreamWriter2;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

public class XmlDatatypeManager
    extends AbstractDatatypeManager {
  public static final String NS_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

  @NonNull
  private static final Lazy<List<IDatatypeProvider>> DATATYPE_PROVIDERS = ObjectUtils.notNull(Lazy.lazy(() -> List.of(
      new XmlCoreDatatypeProvider(),
      new XmlProseCompositDatatypeProvider(
          ObjectUtils.notNull(List.of(
              new XmlMarkupMultilineDatatypeProvider(),
              new XmlMarkupLineDatatypeProvider()))))));

  public void generateDatatypes(@NonNull XMLStreamWriter2 writer) throws XMLStreamException {
    // resolve dependencies
    Set<String> used = getUsedTypes();

    Set<String> requiredTypes = getDatatypeTranslationMap().values().stream()
        .filter(type -> used.contains(type))
        .collect(Collectors.toCollection(LinkedHashSet::new));

    for (IDatatypeProvider provider : DATATYPE_PROVIDERS.get()) {
      Set<String> providedDatatypes = provider.generateDatatypes(requiredTypes, writer);
      requiredTypes.removeAll(providedDatatypes);
    }

    if (!requiredTypes.isEmpty()) {
      throw new IllegalStateException(
          String.format("The following datatypes were not provided: %s",
              requiredTypes.stream().collect(Collectors.joining(","))));
    }
  }
}
