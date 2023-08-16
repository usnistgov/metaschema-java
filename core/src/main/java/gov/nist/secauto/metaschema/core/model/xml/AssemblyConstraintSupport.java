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

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.metapath.MetapathException;
import gov.nist.secauto.metaschema.core.model.IMetaschema;
import gov.nist.secauto.metaschema.core.model.constraint.DefaultAllowedValuesConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.DefaultCardinalityConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.DefaultExpectConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.DefaultIndexConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.DefaultIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.DefaultMatchesConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.DefaultUniqueConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IAllowedValue;
import gov.nist.secauto.metaschema.core.model.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.ICardinalityConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint.ISource;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraintVisitor;
import gov.nist.secauto.metaschema.core.model.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IIndexConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IKeyConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IKeyField;
import gov.nist.secauto.metaschema.core.model.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IModelConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.IUniqueConstraint;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.ConstraintType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.DefineAssemblyConstraintsType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.EnumType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GlobalAssemblyDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.HasCardinalityConstraintType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.KeyConstraintType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.KeyConstraintType.KeyField;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.PropertyType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.RemarksType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.ScopedAllowedValuesType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.ScopedExpectConstraintType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.ScopedIndexConstraintType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.ScopedIndexHasKeyConstraintType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.ScopedKeyConstraintType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.ScopedMatchesConstraintType;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.values.XmlValueNotSupportedException;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Provides support for parsing and maintaining a set of Metaschema constraints. Constraints are
 * parsed from XML.
 */
@SuppressWarnings("PMD.CouplingBetweenObjects")
class AssemblyConstraintSupport implements IModelConstrained {
  @NonNull
  private static final String PATH = "declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';"
      + "$this/m:allowed-values|$this/m:index|$this/m:index-has-key|$this/m:is-unique|"
      + "$this/m:has-cardinality|$this/m:matches|$this/m:expect";
  @NonNull
  private final List<IConstraint> constraints = new LinkedList<>();
  @NonNull
  private final List<IAllowedValuesConstraint> allowedValuesConstraints = new LinkedList<>();
  @NonNull
  private final List<IMatchesConstraint> matchesConstraints = new LinkedList<>();
  @NonNull
  private final List<IIndexHasKeyConstraint> indexHasKeyConstraints = new LinkedList<>();
  @NonNull
  private final List<IExpectConstraint> expectConstraints = new LinkedList<>();
  @NonNull
  private final List<IIndexConstraint> indexConstraints = new LinkedList<>();
  @NonNull
  private final List<IUniqueConstraint> uniqueConstraints = new LinkedList<>();
  @NonNull
  private final List<ICardinalityConstraint> cardinalityConstraints = new LinkedList<>();

  public static AssemblyConstraintSupport newInstance(
      @NonNull GlobalAssemblyDefinitionType definition,
      @NonNull ISource source) {
    AssemblyConstraintSupport retval;
    if (definition.isSetConstraint()) {
      retval = new AssemblyConstraintSupport(ObjectUtils.notNull(definition.getConstraint()), source);
    } else {
      retval = new AssemblyConstraintSupport();
    }
    return retval;
  }

  public AssemblyConstraintSupport() {
    // do nothing
  }

  /**
   * Generate a set of constraints from the provided XMLBeans instance.
   *
   * @param xmlConstraints
   *          the XMLBeans instance
   * @param source
   *          information about the source of the constraints
   */
  public AssemblyConstraintSupport( // NOPMD - unavoidable
      @NonNull DefineAssemblyConstraintsType xmlConstraints,
      @NonNull ISource source) {
    try (XmlCursor cursor = xmlConstraints.newCursor()) {
      cursor.selectPath(PATH);

      while (cursor.toNextSelection()) {
        XmlObject obj = cursor.getObject();
        if (obj instanceof ScopedAllowedValuesType) {
          DefaultAllowedValuesConstraint constraint
              = ModelFactory.newAllowedValuesConstraint((ScopedAllowedValuesType) obj, source);
          addConstraint(constraint);
        } else if (obj instanceof ScopedIndexConstraintType) {
          DefaultIndexConstraint constraint
              = ModelFactory.newIndexConstraint((ScopedIndexConstraintType) obj, source);
          addConstraint(constraint);
        } else if (obj instanceof ScopedIndexHasKeyConstraintType) {
          DefaultIndexHasKeyConstraint constraint
              = ModelFactory.newIndexHasKeyConstraint((ScopedIndexHasKeyConstraintType) obj, source);
          addConstraint(constraint);
        } else if (obj instanceof ScopedKeyConstraintType) {
          DefaultUniqueConstraint constraint
              = ModelFactory.newUniqueConstraint((ScopedKeyConstraintType) obj, source);
          addConstraint(constraint);
        } else if (obj instanceof HasCardinalityConstraintType) {
          DefaultCardinalityConstraint constraint
              = ModelFactory.newCardinalityConstraint((HasCardinalityConstraintType) obj, source);
          addConstraint(constraint);
        } else if (obj instanceof ScopedMatchesConstraintType) {
          DefaultMatchesConstraint constraint
              = ModelFactory.newMatchesConstraint((ScopedMatchesConstraintType) obj, source);
          addConstraint(constraint);
        } else if (obj instanceof ScopedExpectConstraintType) {
          DefaultExpectConstraint constraint
              = ModelFactory.newExpectConstraint((ScopedExpectConstraintType) obj, source);
          addConstraint(constraint);
        }
      }
    } catch (MetapathException | XmlValueNotSupportedException ex) {
      if (ex.getCause() instanceof MetapathException) {
        throw new MetapathException(
            String.format("Unable to compile a Metapath in '%s'. %s", source.getSource(), ex.getLocalizedMessage()),
            ex);
      }
      throw ex;
    }
  }

  @Override
  public List<IConstraint> getConstraints() {
    synchronized (this) {
      return constraints;
    }
  }

  @Override
  public List<IAllowedValuesConstraint> getAllowedValuesConstraints() {
    synchronized (this) {
      return allowedValuesConstraints;
    }
  }

  @Override
  public List<IMatchesConstraint> getMatchesConstraints() {
    synchronized (this) {
      return matchesConstraints;
    }
  }

  @Override
  public List<IIndexHasKeyConstraint> getIndexHasKeyConstraints() {
    synchronized (this) {
      return indexHasKeyConstraints;
    }
  }

  @Override
  public List<IExpectConstraint> getExpectConstraints() {
    synchronized (this) {
      return expectConstraints;
    }
  }

  @Override
  public List<IIndexConstraint> getIndexConstraints() {
    synchronized (this) {
      return indexConstraints;
    }
  }

  @Override
  public List<IUniqueConstraint> getUniqueConstraints() {
    synchronized (this) {
      return uniqueConstraints;
    }
  }

  @Override
  public List<ICardinalityConstraint> getHasCardinalityConstraints() {
    synchronized (this) {
      return cardinalityConstraints;
    }
  }

  @Override
  public final void addConstraint(@NonNull IAllowedValuesConstraint constraint) {
    synchronized (this) {
      constraints.add(constraint);
      allowedValuesConstraints.add(constraint);
    }
  }

  @Override
  public final void addConstraint(@NonNull IMatchesConstraint constraint) {
    synchronized (this) {
      constraints.add(constraint);
      matchesConstraints.add(constraint);
    }
  }

  @Override
  public final void addConstraint(@NonNull IIndexHasKeyConstraint constraint) {
    synchronized (this) {
      constraints.add(constraint);
      indexHasKeyConstraints.add(constraint);
    }
  }

  @Override
  public final void addConstraint(@NonNull IExpectConstraint constraint) {
    synchronized (this) {
      constraints.add(constraint);
      expectConstraints.add(constraint);
    }
  }

  @Override
  public final void addConstraint(@NonNull IIndexConstraint constraint) {
    synchronized (this) {
      constraints.add(constraint);
      indexConstraints.add(constraint);
    }
  }

  @Override
  public final void addConstraint(@NonNull IUniqueConstraint constraint) {
    synchronized (this) {
      constraints.add(constraint);
      uniqueConstraints.add(constraint);
    }
  }

  @Override
  public final void addConstraint(@NonNull ICardinalityConstraint constraint) {
    synchronized (this) {
      constraints.add(constraint);
      cardinalityConstraints.add(constraint);
    }
  }

  public final DefineAssemblyConstraintsType generate() {
    DefineAssemblyConstraintsType retval = DefineAssemblyConstraintsType.Factory.newInstance();

    XmlbeanGeneratingVisitor visitor = new XmlbeanGeneratingVisitor();

    for (IConstraint constraint : getConstraints()) {
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

      if (Boolean.compare(IAllowedValuesConstraint.DEFAULT_ALLOW_OTHER, constraint.isAllowedOther()) != 0) {
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

        XmlbeansMarkupVisitor.visit(description, IMetaschema.METASCHEMA_XML_NS, enumType);
      }

      MarkupMultiline remarks = constraint.getRemarks();
      if (remarks != null) {
        RemarksType remarksType = bean.addNewRemarks();
        assert remarksType != null;
        XmlbeansMarkupVisitor.visit(remarks, IMetaschema.METASCHEMA_XML_NS, remarksType);
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
        XmlbeansMarkupVisitor.visit(remarks, IMetaschema.METASCHEMA_XML_NS, remarksType);
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
        XmlbeansMarkupVisitor.visit(remarks, IMetaschema.METASCHEMA_XML_NS, remarksType);
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
        XmlbeansMarkupVisitor.visit(remarks, IMetaschema.METASCHEMA_XML_NS, remarksType);
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
        XmlbeansMarkupVisitor.visit(remarks, IMetaschema.METASCHEMA_XML_NS, remarksType);
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
        XmlbeansMarkupVisitor.visit(remarks, IMetaschema.METASCHEMA_XML_NS, remarksType);
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
        XmlbeansMarkupVisitor.visit(remarks, IMetaschema.METASCHEMA_XML_NS, remarksType);
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
        XmlbeansMarkupVisitor.visit(remarks, IMetaschema.METASCHEMA_XML_NS, remarksType);
      }
      return null;
    }
  }
}
