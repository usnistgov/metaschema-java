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

import gov.nist.secauto.metaschema.core.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.core.model.AbstractLoader;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraintSet;
import gov.nist.secauto.metaschema.core.model.constraint.ITargetedConstaints;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.LetType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.MetaschemaMetaConstraintsDocument;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.ModelContextType;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class MetaConstraintLoader
    extends AbstractLoader<IConstraintSet> {
  private static final Logger LOGGER = LogManager.getLogger(MetaConstraintLoader.class);

  @Override
  protected IConstraintSet parseResource(URI resource, Deque<URI> visitedResources) throws IOException {

    // parse this metaschema
    MetaschemaMetaConstraintsDocument xmlObject = parseConstraintSet(resource);

    MetaschemaMetaConstraintsDocument.MetaschemaMetaConstraints constraints = xmlObject.getMetaschemaMetaConstraints();

    for (ModelContextType context : constraints.getContextList()) {
      assert context != null;
      List<ITargetedConstaints> targets = parseContext(context, null).getTargetedConstraints();
    }

    // TODO Auto-generated method stub
    return null;
  }

  private Context parseContext(@NonNull ModelContextType context, @Nullable Context parent) {
    Stream<Let> letStream = context.getLetList().stream()
        .map(let -> new Let(let));

    List<MetapathExpression> metapaths;
    if (parent == null) {
      metapaths = context.getMetaschemaMetapathList().stream()
          .map(path -> path.getTarget())
          .collect(Collectors.toList());
    } else {
      letStream = Stream.concat(parent.getLets().stream(), letStream);

      List<String> parentMetapaths = parent.getMetapaths().stream()
          .map(path -> path.getPath())
          .collect(Collectors.toList());
      metapaths = context.getMetaschemaMetapathList().stream()
          .map(path -> path.getTarget())
          .flatMap(childPath -> {
            return (Stream<MetapathExpression>) parentMetapaths.stream()
                .map(parentPath -> MetapathExpression.compile(parentPath + childPath));
          })
          .collect(Collectors.toList());
    }

    Map<String, Let> lets = letStream
        .collect(CustomCollectors.toMap(
            Let::getName,
            ObjectUtils.notNull(Function.identity()),
            (key, v1, v2) -> {
              if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Let expressions '{}' and '{}' have duplicate names '{}'. Using the second.",
                    v1.getMetapath().getPath(),
                    v2.getMetapath().getPath(),
                    key);
              }
              assert v2 != null;
              return v2;
            },
            LinkedHashMap::new));
    //
    // context.getConstraints().
    return null;
  }

  /**
   * Parse the provided XML resource as a Metaschema constraints.
   *
   * @param resource
   *          the resource to parse
   * @return the XMLBeans representation of the Metaschema contraints
   * @throws IOException
   *           if a parsing error occurred
   */
  @NonNull
  protected MetaschemaMetaConstraintsDocument parseConstraintSet(@NonNull URI resource) throws IOException {
    try {
      XmlOptions options = new XmlOptions();
      options.setBaseURI(resource);
      options.setLoadLineNumbers();
      return ObjectUtils.notNull(MetaschemaMetaConstraintsDocument.Factory.parse(resource.toURL(),
          options));
    } catch (XmlException ex) {
      throw new IOException(ex);
    }
  }

  private static class Context {

    public List<ITargetedConstaints> getTargetedConstraints() {
      return Collections.emptyList();
    }

    public List<MetapathExpression> getMetapaths() {
      return Collections.emptyList();
    }

    public List<Let> getLets() {
      return Collections.emptyList();
    }
  }

  private static class Let {
    private final String name;
    private final MetapathExpression metapath;

    private Let(LetType let) {
      this.name = let.getVar();
      this.metapath = let.getExpression();
    }

    public String getName() {
      return name;
    }

    public MetapathExpression getMetapath() {
      return metapath;
    }
  }
}
