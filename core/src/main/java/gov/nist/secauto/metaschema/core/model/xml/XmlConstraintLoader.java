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

import gov.nist.secauto.metaschema.core.metapath.MetapathException;
import gov.nist.secauto.metaschema.core.model.AbstractLoader;
import gov.nist.secauto.metaschema.core.model.IConstraintLoader;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.model.constraint.AssemblyConstraintSet;
import gov.nist.secauto.metaschema.core.model.constraint.AssemblyTargetedConstraints;
import gov.nist.secauto.metaschema.core.model.constraint.DefaultConstraintSet;
import gov.nist.secauto.metaschema.core.model.constraint.DefaultScopedContraints;
import gov.nist.secauto.metaschema.core.model.constraint.FieldTargetedConstraints;
import gov.nist.secauto.metaschema.core.model.constraint.FlagTargetedConstraints;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraintSet;
import gov.nist.secauto.metaschema.core.model.constraint.IModelConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.IScopedContraints;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.model.constraint.ITargetedConstraints;
import gov.nist.secauto.metaschema.core.model.constraint.IValueConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.ValueConstraintSet;
import gov.nist.secauto.metaschema.core.model.xml.impl.ConstraintXmlSupport;
import gov.nist.secauto.metaschema.core.model.xml.impl.XmlObjectParser;
import gov.nist.secauto.metaschema.core.model.xml.impl.XmlObjectParser.Handler;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.METASCHEMACONSTRAINTSDocument;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.METASCHEMACONSTRAINTSDocument.METASCHEMACONSTRAINTS.Scope;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.values.XmlValueNotSupportedException;

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

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Provides methods to load a constraint set expressed in XML.
 * <p>
 * Loaded constraint instances are cached to avoid the need to load them for
 * every use. Any constraint set imported is also loaded and cached
 * automatically.
 */
public class XmlConstraintLoader
    extends AbstractLoader<IConstraintSet>
    implements IConstraintLoader {

  @SuppressWarnings("PMD.UseConcurrentHashMap")
  @NonNull
  private static final Map<QName,
      Handler<Pair<ISource, List<ITargetedConstraints>>>> SCOPE_OBJECT_MAPPING = ObjectUtils.notNull(
          Map.ofEntries(
              Map.entry(new QName(IModule.XML_NAMESPACE, "assembly"),
                  XmlConstraintLoader::handleScopedAssembly),
              Map.entry(new QName(IModule.XML_NAMESPACE, "field"),
                  XmlConstraintLoader::handleScopedField),
              Map.entry(new QName(IModule.XML_NAMESPACE, "flag"),
                  XmlConstraintLoader::handleScopedFlag)));

  @NonNull
  private static final XmlObjectParser<Pair<ISource, List<ITargetedConstraints>>> SCOPE_PARSER
      = new XmlObjectParser<>(SCOPE_OBJECT_MAPPING) {

        @Override
        protected Handler<Pair<ISource, List<ITargetedConstraints>>> identifyHandler(XmlCursor cursor, XmlObject obj) {
          Handler<Pair<ISource, List<ITargetedConstraints>>> retval;
          if (obj instanceof Scope.Assembly) {
            retval = XmlConstraintLoader::handleScopedAssembly;
          } else if (obj instanceof Scope.Field) {
            retval = XmlConstraintLoader::handleScopedField;
          } else if (obj instanceof Scope.Flag) {
            retval = XmlConstraintLoader::handleScopedFlag;
          } else {
            throw new IllegalStateException(String.format("Unhandled element type '%s'.", obj.getClass().getName()));
          }
          return retval;
        }

      };

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
      assert scope != null;

      List<ITargetedConstraints> targetedConstraints = new LinkedList<>(); // NOPMD - intentional
      try {
        SCOPE_PARSER.parse(scope, Pair.of(constraintSource, targetedConstraints));
      } catch (MetapathException | XmlValueNotSupportedException ex) {
        if (ex.getCause() instanceof MetapathException) {
          throw new MetapathException(
              String.format("Unable to compile a Metapath in '%s'. %s",
                  constraintSource.getSource(),
                  ex.getLocalizedMessage()),
              ex);
        }
        throw ex;
      }

      URI namespace = ObjectUtils.notNull(URI.create(scope.getMetaschemaNamespace()));
      String shortName = ObjectUtils.requireNonNull(scope.getMetaschemaShortName());

      scopedConstraints.add(new DefaultScopedContraints(
          namespace,
          shortName,
          CollectionUtil.unmodifiableList(targetedConstraints)));
    }
    return CollectionUtil.unmodifiableList(scopedConstraints);
  }

  private static void handleScopedAssembly( // NOPMD false positive
      @NonNull XmlObject obj,
      Pair<ISource, List<ITargetedConstraints>> state) {
    Scope.Assembly assembly = (Scope.Assembly) obj;

    IModelConstrained constraints = new AssemblyConstraintSet();
    ConstraintXmlSupport.parse(constraints, assembly, ObjectUtils.notNull(state.getLeft()));

    state.getRight().add(new AssemblyTargetedConstraints(
        ObjectUtils.requireNonNull(assembly.getTarget()),
        constraints));
  }

  private static void handleScopedField( // NOPMD false positive
      @NonNull XmlObject obj,
      Pair<ISource, List<ITargetedConstraints>> state) {
    Scope.Field field = (Scope.Field) obj;

    IValueConstrained constraints = new ValueConstraintSet();
    ConstraintXmlSupport.parse(constraints, field, ObjectUtils.notNull(state.getLeft()));

    state.getRight().add(new FieldTargetedConstraints(
        ObjectUtils.requireNonNull(field.getTarget()),
        constraints));
  }

  private static void handleScopedFlag( // NOPMD false positive
      @NonNull XmlObject obj,
      Pair<ISource, List<ITargetedConstraints>> state) {
    Scope.Flag flag = (Scope.Flag) obj;

    IValueConstrained constraints = new ValueConstraintSet();
    ConstraintXmlSupport.parse(constraints, flag, ObjectUtils.notNull(state.getLeft()));

    state.getRight().add(new FlagTargetedConstraints(
        ObjectUtils.requireNonNull(flag.getTarget()),
        constraints));
  }
}
