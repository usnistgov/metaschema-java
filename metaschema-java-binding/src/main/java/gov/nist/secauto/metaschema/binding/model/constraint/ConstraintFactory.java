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

package gov.nist.secauto.metaschema.binding.model.constraint;

import gov.nist.secauto.metaschema.binding.model.annotations.NullJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.AllowedValue;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.AllowedValues;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.Expect;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.HasCardinality;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.Index;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.IndexHasKey;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.IsUnique;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.KeyField;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.Matches;
import gov.nist.secauto.metaschema.datatypes.DataTypes;
import gov.nist.secauto.metaschema.datatypes.adapter.JavaTypeAdapter;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupLine;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultAllowedValue;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultCardinalityConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultIndexConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultKeyField;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultUniqueConstraint;
import gov.nist.secauto.metaschema.model.common.metapath.Metapath;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ConstraintFactory {
  private ConstraintFactory() {
    // disable
  }

  static Map<String, DefaultAllowedValue> toAllowedValues(AllowedValues constraint) {
    AllowedValue[] values = constraint.values();

    Map<String, DefaultAllowedValue> allowedValues = new LinkedHashMap<>(values.length);
    for (AllowedValue value : values) {
      DefaultAllowedValue allowedValue
          = new DefaultAllowedValue(value.value(), MarkupLine.fromMarkdown(value.description()));
      allowedValues.put(allowedValue.getValue(), allowedValue);
    }
    return Collections.unmodifiableMap(allowedValues);
  }

  static String toId(String id) {
    if (id.isBlank()) {
      id = null;
    }
    return id;
  }

  static MarkupMultiline toRemarks(String remarks) {
    MarkupMultiline retval;
    if (remarks.isBlank()) {
      retval = null;
    } else {
      retval = MarkupMultiline.fromMarkdown(remarks);
    }
    return retval;
  }

  static Pattern toPattern(String pattern) {
    Pattern retval;
    if (pattern.isBlank()) {
      retval = null;
    } else {
      retval = Pattern.compile(pattern);
    }
    return retval;
  }

  static DataTypes toDataType(Class<? extends JavaTypeAdapter<?>> adapterClass) {
    DataTypes retval;
    if (adapterClass.isAssignableFrom(NullJavaTypeAdapter.class)) {
      retval = null;
    } else {
      retval = DataTypes.getDataTypeForAdapter(adapterClass);
    }
    return retval;
  }

  static MetapathExpression toMetapath(String metapath) {
    MetapathExpression retval;
    if (metapath == null || metapath.isBlank()) {
      retval = null;
    } else {
      retval = Metapath.parseMetapathString(metapath);
    }
    return retval;
  }

  static DefaultAllowedValuesConstraint newAllowedValuesConstraint(AllowedValues constraint) {
    return new DefaultAllowedValuesConstraint(toId(constraint.id()), toMetapath(constraint.target()),
        toAllowedValues(constraint), constraint.allowOthers(), toRemarks(constraint.remarks()));
  }

  static DefaultMatchesConstraint newMatchesConstraint(Matches constraint) {
    return new DefaultMatchesConstraint(toId(constraint.id()), toMetapath(constraint.target()),
        toPattern(constraint.pattern()), toDataType(constraint.typeAdapter()), toRemarks(constraint.remarks()));
  }

  static List<DefaultKeyField> toKeyFields(KeyField[] keyFields) {
    List<DefaultKeyField> retval;
    if (keyFields == null || keyFields.length == 0) {
      retval = Collections.emptyList();
    } else {
      retval = new ArrayList<>(keyFields.length);
      for (KeyField keyField : keyFields) {
        DefaultKeyField field = new DefaultKeyField(toMetapath(keyField.target()),
            toPattern(keyField.pattern()), toRemarks(keyField.remarks()));
        retval.add(field);
      }
      retval = Collections.unmodifiableList(retval);
    }
    return retval;
  }

  static Integer toCardinality(int value) {
    Integer retval;
    if (value < 0) {
      retval = null;
    } else {
      retval = Integer.valueOf(value);
    }
    return retval;
  }

  static DefaultUniqueConstraint newUniqueConstraint(IsUnique constraint) {
    return new DefaultUniqueConstraint(toId(constraint.id()), toMetapath(constraint.target()),
        toKeyFields(constraint.keyFields()), toRemarks(constraint.remarks()));
  }

  static DefaultIndexConstraint newIndexConstraint(Index constraint) {
    return new DefaultIndexConstraint(toId(constraint.id()), toMetapath(constraint.target()),
        constraint.name(), toKeyFields(constraint.keyFields()), toRemarks(constraint.remarks()));
  }

  static DefaultIndexHasKeyConstraint newIndexHasKeyConstraint(IndexHasKey constraint) {
    return new DefaultIndexHasKeyConstraint(toId(constraint.id()), toMetapath(constraint.target()),
        constraint.indexName(), toKeyFields(constraint.keyFields()), toRemarks(constraint.remarks()));
  }

  static DefaultExpectConstraint newExpectConstraint(Expect constraint) {
    return new DefaultExpectConstraint(toId(constraint.id()), toMetapath(constraint.target()),
        toMetapath(constraint.test()), toRemarks(constraint.remarks()));
  }

  static DefaultCardinalityConstraint newCardinalityConstraint(HasCardinality constraint) {
    return new DefaultCardinalityConstraint(toId(constraint.id()), Metapath.parseMetapathString(constraint.target()),
        toCardinality(constraint.minOccurs()),
        toCardinality(constraint.maxOccurs()),
        toRemarks(constraint.remarks()));
  }
}
