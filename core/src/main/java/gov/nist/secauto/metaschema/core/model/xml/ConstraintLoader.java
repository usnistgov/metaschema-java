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
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraintSet;
import gov.nist.secauto.metaschema.core.model.constraint.IModelConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.IScopedContraints;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.model.constraint.ITargetedConstaints;
import gov.nist.secauto.metaschema.core.model.constraint.impl.AssemblyConstraintSet;
import gov.nist.secauto.metaschema.core.model.constraint.impl.AssemblyTargetedConstraints;
import gov.nist.secauto.metaschema.core.model.constraint.impl.DefaultConstraintSet;
import gov.nist.secauto.metaschema.core.model.constraint.impl.DefaultScopedContraints;
import gov.nist.secauto.metaschema.core.model.constraint.impl.FieldTargetedConstraints;
import gov.nist.secauto.metaschema.core.model.constraint.impl.FlagTargetedConstraints;
import gov.nist.secauto.metaschema.core.model.constraint.impl.ValueConstraintSet;
import gov.nist.secauto.metaschema.core.model.xml.impl.ConstraintXmlSupport;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.METASCHEMACONSTRAINTSDocument;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.METASCHEMACONSTRAINTSDocument.METASCHEMACONSTRAINTS.Scope;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Provides methods to load a constraint set expressed in XML.
 * <p>
 * Loaded constraint instances are cached to avoid the need to load them for
 * every use. Any constraint set imported is also loaded and cached
 * automatically.
 */
public class ConstraintLoader
    extends AbstractLoader<IConstraintSet> {

  @Override
  protected IConstraintSet parseResource(@NonNull URI resource, @NonNull Deque<URI> visitedResources)
      throws IOException {

    // parse this metaschema
    METASCHEMACONSTRAINTSDocument xmlObject = parseConstraintSet(resource);

    // now check if this constraint set imports other constraint sets
    int size = xmlObject.getMETASCHEMACONSTRAINTS().sizeOfImportArray();
    @NonNull Map<URI, IConstraintSet> importedConstraints;
    if (size == 0) {
      importedConstraints = ObjectUtils.notNull(Collections.emptyMap());
    } else {
      try {
        importedConstraints = new LinkedHashMap<>();
        for (METASCHEMACONSTRAINTSDocument.METASCHEMACONSTRAINTS.Import imported : xmlObject.getMETASCHEMACONSTRAINTS()
            .getImportList()) {
          URI importedResource = URI.create(imported.getHref());
          importedResource = ObjectUtils.notNull(resource.resolve(importedResource));
          importedConstraints.put(importedResource, loadInternal(importedResource, visitedResources));
        }
      } catch (MetaschemaException ex) {
        throw new IOException(ex);
      }
    }

    // now create this constraint set
    Collection<IConstraintSet> values = importedConstraints.values();
    return new DefaultConstraintSet(resource, parseScopedConstraints(xmlObject, resource), new LinkedHashSet<>(values));
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
  private static METASCHEMACONSTRAINTSDocument parseConstraintSet(@NonNull URI resource) throws IOException {
    try {
      XmlOptions options = new XmlOptions();
      options.setBaseURI(resource);
      options.setLoadLineNumbers();
      return ObjectUtils.notNull(METASCHEMACONSTRAINTSDocument.Factory.parse(resource.toURL(), options));
    } catch (XmlException ex) {
      throw new IOException(ex);
    }
  }

  /**
   * Parse individual constraint definitions from the provided XMLBeans object.
   *
   * @param xmlObject
   *          the XMLBeans object
   * @param source
   *          the source of the constraint content
   * @return the scoped constraint definitions
   */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops") // intentional
  @NonNull
  protected List<IScopedContraints> parseScopedConstraints(
      @NonNull METASCHEMACONSTRAINTSDocument xmlObject,
      @NonNull URI source) {
    List<IScopedContraints> scopedConstraints = new LinkedList<>();
    ISource constraintSource = ISource.externalSource(source);

    for (Scope scope : xmlObject.getMETASCHEMACONSTRAINTS().getScopeList()) {
      URI namespace = ObjectUtils.notNull(URI.create(scope.getMetaschemaNamespace()));
      String shortName = ObjectUtils.requireNonNull(scope.getMetaschemaShortName());

      try (XmlCursor cursor = scope.newCursor()) {
        cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';"
            + "$this/m:assembly|$this/m:field|$this/m:flag");

        List<ITargetedConstaints> targetedConstraints = new LinkedList<>(); // NOPMD - intentional
        while (cursor.toNextSelection()) {
          XmlObject obj = cursor.getObject();
          if (obj instanceof Scope.Assembly) {
            Scope.Assembly assembly = (Scope.Assembly) obj;
            MetapathExpression expression = ObjectUtils.requireNonNull(assembly.getTarget());

            IModelConstrained constraints = new AssemblyConstraintSet();
            ConstraintXmlSupport.parse(constraints, assembly, constraintSource);

            targetedConstraints.add(new AssemblyTargetedConstraints(expression, constraints));
          } else if (obj instanceof Scope.Field) {
            Scope.Field field = (Scope.Field) obj;
            MetapathExpression expression = ObjectUtils.requireNonNull(field.getTarget());

            ValueConstraintSet constraints = new AssemblyConstraintSet();
            ConstraintXmlSupport.parse(constraints, field, constraintSource);

            targetedConstraints.add(new FieldTargetedConstraints(expression, constraints));
          } else if (obj instanceof Scope.Flag) {
            Scope.Flag flag = (Scope.Flag) obj;
            MetapathExpression expression = ObjectUtils.requireNonNull(flag.getTarget());

            ValueConstraintSet constraints = new AssemblyConstraintSet();
            ConstraintXmlSupport.parse(constraints, flag, constraintSource);

            targetedConstraints.add(new FlagTargetedConstraints(expression, constraints));
          }
        }
        scopedConstraints.add(
            new DefaultScopedContraints(namespace, shortName, CollectionUtil.unmodifiableList(targetedConstraints)));
      }
    }
    return CollectionUtil.unmodifiableList(scopedConstraints);
  }

}
