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

import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.datatype.IDatatypeContent;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

public class XmlCoreDatatypeProvider
    extends AbstractXmlDatatypeProvider {

  @Override
  protected InputStream getSchemaResource() {
    return JDom2XmlSchemaLoader.class.getResourceAsStream("/schema/xml/metaschema-datatypes.xsd");
  }

  @Override
  protected List<Element> queryElements(JDom2XmlSchemaLoader loader) {
    return loader.getContent(
        "/xs:schema/xs:simpleType",
        CollectionUtil.singletonMap("xs", JDom2XmlSchemaLoader.NS_XML_SCHEMA));
  }

  @NonNull
  private static List<String> analyzeDependencies(@NonNull Element element) {
    XPathExpression<Attribute> xpath = XPathFactory.instance().compile(".//@base", Filters.attribute());
    return ObjectUtils.notNull(xpath.evaluate(element).stream()
        .map(attr -> attr.getValue())
        .filter(type -> !type.startsWith("xs:"))
        .distinct()
        .collect(Collectors.toList()));
  }

  @Override
  protected @NonNull Map<String, IDatatypeContent> handleResults(
      @NonNull List<Element> items) {
    return ObjectUtils.notNull(items.stream()
        .map(element -> {
          return (IDatatypeContent) new JDom2DatatypeContent(
              ObjectUtils.requireNonNull(element.getAttributeValue("name")),
              CollectionUtil.singletonList(element),
              analyzeDependencies(element));
        }).collect(Collectors.toMap(content -> content.getTypeName(), Function.identity(), (e1, e2) -> e2,
            LinkedHashMap::new)));
  }
}
