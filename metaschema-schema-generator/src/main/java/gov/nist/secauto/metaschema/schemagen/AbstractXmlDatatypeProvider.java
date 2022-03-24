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
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

public abstract class AbstractXmlDatatypeProvider implements IDatatypeProvider {
  private Map<@NotNull String, IDatatypeContent> datatypes;

  protected abstract InputStream getSchemaResource();

  private void initSchema() {
    synchronized (this) {
      if (datatypes == null) {
        try (InputStream is = getSchemaResource()) {
          JDom2XmlSchemaLoader loader = new JDom2XmlSchemaLoader(is);

          List<@NotNull Element> elements = queryElements(loader);

          datatypes = Collections.unmodifiableMap(handleResults(elements));
        } catch (JDOMException | IOException ex) {
          throw new IllegalStateException(ex);
        }
      }
    }
  }

  protected abstract List<@NotNull Element> queryElements(JDom2XmlSchemaLoader loader);

  @NotNull
  protected abstract Map<@NotNull String, IDatatypeContent> handleResults(@NotNull List<@NotNull Element> items);

  @Override
  public Map<@NotNull String, IDatatypeContent> getDatatypes() {
    initSchema();
    return datatypes;
  }

  @Override
  public Set<@NotNull String> generateDatatypes(Set<@NotNull String> requiredTypes, @NotNull XMLStreamWriter2 writer)
      throws XMLStreamException {
    Map<@NotNull String, IDatatypeContent> datatypes = getDatatypes();

    Set<@NotNull String> providedDatatypes = new LinkedHashSet<>();
    for (IDatatypeContent datatype : datatypes.values()) {
      String type = datatype.getTypeName();
      if (requiredTypes.contains(type)) {
        providedDatatypes.add(type);
        providedDatatypes.addAll(datatype.getDependencies());
      }
    }

    for (IDatatypeContent datatype : datatypes.values()) {
      String type = datatype.getTypeName();
      if (providedDatatypes.contains(type)) {
        datatype.write(writer);
      }
    }
    return providedDatatypes;
  }

}
