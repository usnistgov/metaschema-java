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

package gov.nist.secauto.metaschema.core.model.xml.impl;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.metapath.MetapathException;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.constraint.IAllowedValue;
import gov.nist.secauto.metaschema.core.model.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.ICardinalityConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraintVisitor;
import gov.nist.secauto.metaschema.core.model.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IIndexConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IKeyConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IKeyField;
import gov.nist.secauto.metaschema.core.model.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IModelConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.model.constraint.IUniqueConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IValueConstrained;
import gov.nist.secauto.metaschema.core.model.xml.XmlObjectParser;
import gov.nist.secauto.metaschema.core.model.xml.XmlObjectParser.Handler;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.AllowedValuesType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.ConstraintType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.DefineAssemblyConstraintsType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.DefineFieldConstraintsType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.DefineFlagConstraintsType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.EnumType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.ExpectConstraintType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.HasCardinalityConstraintType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.IndexHasKeyConstraintType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.KeyConstraintType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.KeyConstraintType.KeyField;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.MatchesConstraintType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.PropertyType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.RemarksType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.ScopedAllowedValuesType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.ScopedExpectConstraintType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.ScopedIndexConstraintType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.ScopedIndexHasKeyConstraintType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.ScopedKeyConstraintType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.ScopedMatchesConstraintType;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.values.XmlValueNotSupportedException;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class ConstraintXmlSupport {
  @SuppressWarnings("PMD.UseConcurrentHashMap")
  @NonNull
  private static final Map<QName, Handler<Pair<ISource, IValueConstrained>>> FLAG_OBJECT_MAPPING = ObjectUtils.notNull(
      Map.ofEntries(
          Map.entry(new QName(IModule.METASCHEMA_XML_NS, "allowed-values"),
              ConstraintXmlSupport::handleAllowedValues),
          Map.entry(new QName(IModule.METASCHEMA_XML_NS, "index-has-key"),
              ConstraintXmlSupport::handleIndexHasKey),
          Map.entry(new QName(IModule.METASCHEMA_XML_NS, "matches"),
              ConstraintXmlSupport::handleMatches),
          Map.entry(new QName(IModule.METASCHEMA_XML_NS, "expect"),
              ConstraintXmlSupport::handleExpect)));

  @NonNull
  private static final XmlObjectParser<Pair<ISource, IValueConstrained>> FLAG_PARSER
      = new XmlObjectParser<>(FLAG_OBJECT_MAPPING);

  @SuppressWarnings("PMD.UseConcurrentHashMap")
  @NonNull
  private static final Map<QName, Handler<Pair<ISource, IValueConstrained>>> FIELD_OBJECT_MAPPING = ObjectUtils.notNull(
      Map.ofEntries(
          Map.entry(new QName(IModule.METASCHEMA_XML_NS, "allowed-values"),
              ConstraintXmlSupport::handleScopedAllowedValues),
          Map.entry(new QName(IModule.METASCHEMA_XML_NS, "index-has-key"),
              ConstraintXmlSupport::handleScopedIndexHasKey),
          Map.entry(new QName(IModule.METASCHEMA_XML_NS, "matches"),
              ConstraintXmlSupport::handleScopedMatches),
          Map.entry(new QName(IModule.METASCHEMA_XML_NS, "expect"),
              ConstraintXmlSupport::handleScopedExpect)));

  @NonNull
  private static final XmlObjectParser<Pair<ISource, IValueConstrained>> FIELD_PARSER
      = new XmlObjectParser<>(FIELD_OBJECT_MAPPING);

  @SuppressWarnings("PMD.UseConcurrentHashMap")
  @NonNull
  private static final Map<QName,
      Handler<Pair<ISource, IModelConstrained>>> ASSEMBLY_OBJECT_MAPPING = ObjectUtils.notNull(
          Map.ofEntries(
              Map.entry(new QName(IModule.METASCHEMA_XML_NS, "allowed-values"),
                  ConstraintXmlSupport::handleScopedAllowedValues),
              Map.entry(new QName(IModule.METASCHEMA_XML_NS, "index-has-key"),
                  ConstraintXmlSupport::handleScopedIndexHasKey),
              Map.entry(new QName(IModule.METASCHEMA_XML_NS, "matches"),
                  ConstraintXmlSupport::handleScopedMatches),
              Map.entry(new QName(IModule.METASCHEMA_XML_NS, "expect"),
                  ConstraintXmlSupport::handleScopedExpect),
              Map.entry(new QName(IModule.METASCHEMA_XML_NS, "index"),
                  ConstraintXmlSupport::handleScopedIndex),
              Map.entry(new QName(IModule.METASCHEMA_XML_NS, "is-unique"),
                  ConstraintXmlSupport::handleScopedIsUnique),
              Map.entry(new QName(IModule.METASCHEMA_XML_NS, "has-cardinality"),
                  ConstraintXmlSupport::handleScopedHasCardinality)));

  @NonNull
  private static final XmlObjectParser<Pair<ISource, IModelConstrained>> ASSEMBLY_PARSER
      = new XmlObjectParser<>(ASSEMBLY_OBJECT_MAPPING);

  /**
   * Parse a set of constraints from the provided XMLBeans {@code xmlObject} and
   * apply them to the provided {@code constraints}.
   *
   * @param constraints
   *          the constraint collection to add the parsed constraints to
   * @param xmlObject
   *          the XMLBeans instance
   * @param source
   *          information about the source of the constraints
   */
  public static void parse(
      @NonNull IValueConstrained constraints,
      @NonNull DefineFlagConstraintsType xmlObject,
      @NonNull ISource source) {
    parse(FLAG_PARSER, constraints, (XmlObject) xmlObject, source);
  }

  /**
   * Parse a set of constraints from the provided XMLBeans {@code xmlObject} and
   * apply them to the provided {@code constraints}.
   *
   * @param constraints
   *          the constraint collection to add the parsed constraints to
   * @param xmlObject
   *          the XMLBeans instance
   * @param source
   *          information about the source of the constraints
   */
  public static void parse(
      @NonNull IValueConstrained constraints,
      @NonNull DefineFieldConstraintsType xmlObject,
      @NonNull ISource source) {
    parse(FIELD_PARSER, constraints, (XmlObject) xmlObject, source);
  }

  /**
   * Parse a set of constraints from the provided XMLBeans {@code xmlObject} and
   * apply them to the provided {@code constraints}.
   *
   * @param constraints
   *          the constraint collection to add the parsed constraints to
   * @param xmlObject
   *          the XMLBeans instance
   * @param source
   *          information about the source of the constraints
   */
  public static void parse(
      @NonNull IModelConstrained constraints,
      @NonNull DefineAssemblyConstraintsType xmlObject,
      @NonNull ISource source) {
    parse(ASSEMBLY_PARSER, constraints, (XmlObject) xmlObject, source);
  }

  private static <T> void parse(
      @NonNull XmlObjectParser<Pair<ISource, T>> parser,
      @NonNull T constraints,
      @NonNull XmlObject xmlObject,
      @NonNull ISource source) {
    try {
      parser.parse(xmlObject, Pair.of(source, constraints));
    } catch (MetapathException | XmlValueNotSupportedException ex) {
      if (ex.getCause() instanceof MetapathException) {
        throw new MetapathException(
            String.format("Unable to compile a Metapath in '%s'. %s",
                source.getSource(),
                ex.getLocalizedMessage()),
            ex);
      }
      throw ex;
    }
  }

  private static void handleAllowedValues(
      @NonNull XmlObject obj,
      Pair<ISource, IValueConstrained> state) {
    IAllowedValuesConstraint constraint = ModelFactory.newAllowedValuesConstraint(
        (AllowedValuesType) obj,
        ObjectUtils.notNull(state.getLeft()));
    state.getRight().addConstraint(constraint);
  }

  private static void handleScopedAllowedValues(
      @NonNull XmlObject obj,
      Pair<ISource, ? extends IValueConstrained> state) {
    IAllowedValuesConstraint constraint = ModelFactory.newAllowedValuesConstraint(
        (ScopedAllowedValuesType) obj,
        ObjectUtils.notNull(state.getLeft()));
    state.getRight().addConstraint(constraint);
  }

  private static void handleMatches(
      @NonNull XmlObject obj,
      Pair<ISource, IValueConstrained> state) {
    IMatchesConstraint constraint = ModelFactory.newMatchesConstraint(
        (MatchesConstraintType) obj,
        ObjectUtils.notNull(state.getLeft()));
    state.getRight().addConstraint(constraint);
  }

  private static void handleScopedMatches(
      @NonNull XmlObject obj,
      Pair<ISource, ? extends IValueConstrained> state) {
    IMatchesConstraint constraint = ModelFactory.newMatchesConstraint(
        (ScopedMatchesConstraintType) obj,
        ObjectUtils.notNull(state.getLeft()));
    state.getRight().addConstraint(constraint);
  }

  private static void handleIndexHasKey(
      @NonNull XmlObject obj,
      Pair<ISource, IValueConstrained> state) {
    IIndexHasKeyConstraint constraint = ModelFactory.newIndexHasKeyConstraint(
        (IndexHasKeyConstraintType) obj,
        ObjectUtils.notNull(state.getLeft()));
    state.getRight().addConstraint(constraint);
  }

  private static void handleScopedIndexHasKey(
      @NonNull XmlObject obj,
      Pair<ISource, ? extends IValueConstrained> state) {
    IIndexHasKeyConstraint constraint = ModelFactory.newIndexHasKeyConstraint(
        (ScopedIndexHasKeyConstraintType) obj,
        ObjectUtils.notNull(state.getLeft()));
    state.getRight().addConstraint(constraint);
  }

  private static void handleExpect(
      @NonNull XmlObject obj,
      Pair<ISource, IValueConstrained> state) {
    IExpectConstraint constraint = ModelFactory.newExpectConstraint(
        (ExpectConstraintType) obj,
        ObjectUtils.notNull(state.getLeft()));
    state.getRight().addConstraint(constraint);
  }

  private static void handleScopedExpect(
      @NonNull XmlObject obj,
      Pair<ISource, ? extends IValueConstrained> state) {
    IExpectConstraint constraint = ModelFactory.newExpectConstraint(
        (ScopedExpectConstraintType) obj,
        ObjectUtils.notNull(state.getLeft()));
    state.getRight().addConstraint(constraint);
  }

  private static void handleScopedIndex(@NonNull XmlObject obj, Pair<ISource, IModelConstrained> state) {
    IIndexConstraint constraint = ModelFactory.newIndexConstraint(
        (ScopedIndexConstraintType) obj,
        ObjectUtils.notNull(state.getLeft()));
    state.getRight().addConstraint(constraint);
  }

  private static void handleScopedIsUnique(@NonNull XmlObject obj, Pair<ISource, IModelConstrained> state) {
    IUniqueConstraint constraint = ModelFactory.newUniqueConstraint(
        (ScopedKeyConstraintType) obj,
        ObjectUtils.notNull(state.getLeft()));
    state.getRight().addConstraint(constraint);
  }

  private static void handleScopedHasCardinality(@NonNull XmlObject obj, Pair<ISource, IModelConstrained> state) {
    ICardinalityConstraint constraint = ModelFactory.newCardinalityConstraint(
        (HasCardinalityConstraintType) obj,
        ObjectUtils.notNull(state.getLeft()));
    state.getRight().addConstraint(constraint);
  }

  private ConstraintXmlSupport() {
    // disable construction
  }

  /**
   * Generate the XMLBeans representation of a set of assembly-related
   * constraints.
   *
   * @param constraints
   *          the set of constraints
   * @return the XmlObject representation
   */
  public static DefineAssemblyConstraintsType generate(@NonNull IModelConstrained constraints) {
    // TODO: This code is orphaned. Need to implement a full writer
    DefineAssemblyConstraintsType retval = DefineAssemblyConstraintsType.Factory.newInstance();

    XmlbeanGeneratingVisitor visitor = new XmlbeanGeneratingVisitor();

    for (IConstraint constraint : constraints.getConstraints()) {
      constraint.accept(visitor, retval);
    }
    return retval;
  }

  private static final class XmlbeanGeneratingVisitor
      implements IConstraintVisitor<DefineAssemblyConstraintsType, Void> {

    private static void applyCommonValues(@NonNull IConstraint constraint, @NonNull ConstraintType bean) {
      MarkupLine description = constraint.getDescription();
      if (description != null) {
        bean.setDescription(MarkupStringConverter.toMarkupLineDatatype(description));
      }
      String formalName = constraint.getFormalName();
      if (formalName != null) {
        bean.setFormalName(formalName);
      }

      String id = constraint.getId();
      if (id != null) {
        bean.setId(constraint.getId());
      }

      IConstraint.Level level = constraint.getLevel();
      if (!IConstraint.DEFAULT_LEVEL.equals(level)) {
        bean.setLevel(level);
      }

      for (Map.Entry<QName, Set<String>> entry : constraint.getProperties().entrySet()) {
        QName qname = entry.getKey();
        Set<String> values = entry.getValue();
        for (String value : values) {
          PropertyType prop = bean.addNewProp();
          prop.setName(qname.getLocalPart());

          String namespace = qname.getNamespaceURI();
          if (namespace != null && !namespace.isEmpty()) {
            prop.setNamespace(namespace);
          }
          prop.setValue(value);
        }
      }
    }

    @Override
    public Void visitAllowedValues(IAllowedValuesConstraint constraint, DefineAssemblyConstraintsType state) {
      ScopedAllowedValuesType bean = state.addNewAllowedValues();
      assert bean != null;
      applyCommonValues(constraint, bean);

      if (Boolean.compare(IAllowedValuesConstraint.ALLOW_OTHER_DEFAULT, constraint.isAllowedOther()) != 0) {
        bean.setAllowOther(constraint.isAllowedOther());
      }
      bean.setTarget(constraint.getTarget());
      bean.setExtensible(constraint.getExtensible());

      for (Map.Entry<String, ? extends IAllowedValue> entry : constraint.getAllowedValues().entrySet()) {
        String value = entry.getKey();
        IAllowedValue allowedValue = entry.getValue();

        assert value.equals(allowedValue.getValue());

        MarkupLine description = allowedValue.getDescription();
        EnumType enumType = bean.addNewEnum();
        enumType.setValue(value);

        XmlbeansMarkupVisitor.visit(description, IModule.METASCHEMA_XML_NS, enumType);
      }

      MarkupMultiline remarks = constraint.getRemarks();
      if (remarks != null) {
        RemarksType remarksType = bean.addNewRemarks();
        assert remarksType != null;
        XmlbeansMarkupVisitor.visit(remarks, IModule.METASCHEMA_XML_NS, remarksType);
      }
      return null;
    }

    @Override
    public Void visitCardinalityConstraint(ICardinalityConstraint constraint, DefineAssemblyConstraintsType state) {
      HasCardinalityConstraintType bean = state.addNewHasCardinality();
      assert bean != null;
      applyCommonValues(constraint, bean);

      Integer minOccurs = constraint.getMinOccurs();
      if (minOccurs != null) {
        bean.setMinOccurs(BigInteger.valueOf(minOccurs));
      }

      Integer maxOccurs = constraint.getMaxOccurs();
      if (maxOccurs != null) {
        bean.setMaxOccurs(BigInteger.valueOf(maxOccurs));
      }

      MarkupMultiline remarks = constraint.getRemarks();
      if (remarks != null) {
        RemarksType remarksType = bean.addNewRemarks();
        assert remarksType != null;
        XmlbeansMarkupVisitor.visit(remarks, IModule.METASCHEMA_XML_NS, remarksType);
      }
      return null;
    }

    @Override
    public Void visitExpectConstraint(IExpectConstraint constraint, DefineAssemblyConstraintsType state) {
      ScopedExpectConstraintType bean = state.addNewExpect();
      assert bean != null;
      applyCommonValues(constraint, bean);

      bean.setTest(constraint.getTest());

      String message = constraint.getMessage();
      if (message != null) {
        bean.setMessage(message);
      }

      MarkupMultiline remarks = constraint.getRemarks();
      if (remarks != null) {
        RemarksType remarksType = bean.addNewRemarks();
        assert remarksType != null;
        XmlbeansMarkupVisitor.visit(remarks, IModule.METASCHEMA_XML_NS, remarksType);
      }
      return null;
    }

    @Override
    public Void visitMatchesConstraint(IMatchesConstraint constraint, DefineAssemblyConstraintsType state) {
      ScopedMatchesConstraintType bean = state.addNewMatches();
      assert bean != null;
      applyCommonValues(constraint, bean);

      Pattern pattern = constraint.getPattern();
      if (pattern != null) {
        bean.setRegex(pattern);
      }

      IDataTypeAdapter<?> dataType = constraint.getDataType();
      if (dataType != null) {
        bean.setDatatype(dataType);
      }

      MarkupMultiline remarks = constraint.getRemarks();
      if (remarks != null) {
        RemarksType remarksType = bean.addNewRemarks();
        assert remarksType != null;
        XmlbeansMarkupVisitor.visit(remarks, IModule.METASCHEMA_XML_NS, remarksType);
      }
      return null;
    }

    private static void applyKeyFields(@NonNull IKeyConstraint constraint, @NonNull KeyConstraintType bean) {
      for (IKeyField keyField : constraint.getKeyFields()) {
        KeyField keyFieldBean = bean.addNewKeyField();
        assert keyField != null;
        assert keyFieldBean != null;
        applyKeyField(keyField, keyFieldBean);
      }
    }

    private static void applyKeyField(@NonNull IKeyField keyField, @NonNull KeyField bean) {
      Pattern pattern = keyField.getPattern();
      if (pattern != null) {
        bean.setPattern(pattern);
      }

      bean.setTarget(keyField.getTarget());

      MarkupMultiline remarks = keyField.getRemarks();
      if (remarks != null) {
        RemarksType remarksType = bean.addNewRemarks();
        assert remarksType != null;
        XmlbeansMarkupVisitor.visit(remarks, IModule.METASCHEMA_XML_NS, remarksType);
      }
    }

    @Override
    public Void visitIndexConstraint(IIndexConstraint constraint, DefineAssemblyConstraintsType state) {
      ScopedIndexConstraintType bean = state.addNewIndex();
      assert bean != null;
      applyCommonValues(constraint, bean);
      applyKeyFields(constraint, bean);

      bean.setName(constraint.getName());

      MarkupMultiline remarks = constraint.getRemarks();
      if (remarks != null) {
        RemarksType remarksType = bean.addNewRemarks();
        assert remarksType != null;
        XmlbeansMarkupVisitor.visit(remarks, IModule.METASCHEMA_XML_NS, remarksType);
      }
      return null;
    }

    @Override
    public Void visitIndexHasKeyConstraint(IIndexHasKeyConstraint constraint, DefineAssemblyConstraintsType state) {
      ScopedIndexHasKeyConstraintType bean = state.addNewIndexHasKey();
      assert bean != null;
      applyCommonValues(constraint, bean);
      applyKeyFields(constraint, bean);

      bean.setName(constraint.getIndexName());

      MarkupMultiline remarks = constraint.getRemarks();
      if (remarks != null) {
        RemarksType remarksType = bean.addNewRemarks();
        assert remarksType != null;
        XmlbeansMarkupVisitor.visit(remarks, IModule.METASCHEMA_XML_NS, remarksType);
      }
      return null;
    }

    @Override
    public Void visitUniqueConstraint(IUniqueConstraint constraint, DefineAssemblyConstraintsType state) {
      ScopedIndexHasKeyConstraintType bean = state.addNewIndexHasKey();
      assert bean != null;
      applyCommonValues(constraint, bean);
      applyKeyFields(constraint, bean);

      MarkupMultiline remarks = constraint.getRemarks();
      if (remarks != null) {
        RemarksType remarksType = bean.addNewRemarks();
        assert remarksType != null;
        XmlbeansMarkupVisitor.visit(remarks, IModule.METASCHEMA_XML_NS, remarksType);
      }
      return null;
    }
  }
}
