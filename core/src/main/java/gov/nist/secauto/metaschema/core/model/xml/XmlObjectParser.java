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

package gov.nist.secauto.metaschema.core.model.xml;

import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public class XmlObjectParser<T> {
  private final Map<QName, Handler<T>> elementNameToHandlerMap;
  private final String xpath;

  private static String generatePath(@NonNull Collection<QName> nodes) {
    // build a mapping of namespace prefix to namespace
    AtomicInteger count = new AtomicInteger();
    Map<String, String> namespaceToPrefixMap = nodes.stream()
        .map(qname -> qname.getNamespaceURI())
        .distinct()
        .map(ns -> Pair.of(ns, "m" + count.getAndIncrement()))
        .collect(Collectors.toMap(
            Pair::getKey,
            Pair::getValue,
            (k1, k2) -> k1,
            LinkedHashMap::new));

    // generate namespace declarations using prefix and namespace
    StringBuilder builder = new StringBuilder(24);
    namespaceToPrefixMap.entrySet().forEach((entry) -> {
      builder.append("declare namespace ")
          .append(entry.getValue())
          .append("='")
          .append(entry.getKey())
          .append("';");
    });

    // generate child path
    builder.append(nodes.stream()
        .map(qname -> {
          return new StringBuilder()
              .append("$this/")
              .append(namespaceToPrefixMap.get(qname.getNamespaceURI()))
              .append(':')
              .append(qname.getLocalPart())
              .toString();
        }).collect(Collectors.joining("|")));

    return builder.toString();
  }

  /**
   * Construct a new XmlObject parser.
   *
   * @param elementNameToHandlerMap
   *          the mapping of element names to associated handlers
   */
  public XmlObjectParser(@NonNull Map<QName, Handler<T>> elementNameToHandlerMap) {
    this.elementNameToHandlerMap = elementNameToHandlerMap;
    this.xpath = generatePath(ObjectUtils.notNull(elementNameToHandlerMap.keySet()));
  }

  private Map<QName, Handler<T>> getElementNameToHandlerMap() {
    return elementNameToHandlerMap;
  }

  private String getXpath() {
    return xpath;
  }

  /**
   * Parse an XmlObject element tree using the configured child element handlers.
   *
   * @param container
   *          the XmlObject container to parse
   * @param state
   *          parsing state to pass to the handlers
   */
  public void parse(@NonNull XmlObject container, T state) {
    try (XmlCursor cursor = container.newCursor()) {
      assert cursor != null;
      cursor.selectPath(getXpath());
      while (cursor.toNextSelection()) {
        QName qname = cursor.getName();
        Handler<T> handler = getElementNameToHandlerMap().get(qname);
        if (handler == null) {
          throw new IllegalStateException(String.format("Unhandled node '%s'.", qname));
        }
        XmlObject obj = cursor.getObject();
        assert obj != null;
        handler.handle(obj, state);
      }
    }
  }

  /**
   * Provides a common interface for element parsing handlers.
   *
   * @param <T>
   *          the Java type of the state that is passed to the element parsing
   *          handlers
   */
  @FunctionalInterface
  public interface Handler<T> {
    /**
     * Parse the provided {@code obj} using the provided {@code state}.
     *
     * @param obj
     *          the object to parse
     * @param state
     *          the state to use for parsing
     */
    void handle(@NonNull XmlObject obj, T state);
  }
}
