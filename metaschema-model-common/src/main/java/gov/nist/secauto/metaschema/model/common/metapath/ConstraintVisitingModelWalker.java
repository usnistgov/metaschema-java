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

package gov.nist.secauto.metaschema.model.common.metapath;

import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.ICardinalityConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IUniqueConstraint;
import gov.nist.secauto.metaschema.model.common.definition.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.util.ModelWalker;

public class ConstraintVisitingModelWalker<DATA> extends ModelWalker<DATA> {

  @Override
  protected boolean visit(IAssemblyDefinition def, DATA data) {
    boolean retval = super.visit(def, data);
    walkConstraints(def, data);
    return retval;
  }

  protected void visit(IAssemblyDefinition def, IAllowedValuesConstraint constraint, DATA data) {
  }

  protected void visit(IAssemblyDefinition def, ICardinalityConstraint constraint, DATA data) {
  }

  protected void visit(IAssemblyDefinition def, IExpectConstraint constraint, DATA data) {
  }

  protected void visit(IAssemblyDefinition def, IIndexConstraint constraint, DATA data) {
  }

  protected void visit(IAssemblyDefinition def, IIndexHasKeyConstraint constraint, DATA data) {
  }

  protected void visit(IAssemblyDefinition def, IMatchesConstraint constraint, DATA data) {
  }

  protected void visit(IAssemblyDefinition def, IUniqueConstraint constraint, DATA data) {
  }

  @Override
  protected boolean visit(IFieldDefinition def, DATA data) {
    boolean retval = super.visit(def, data);
    walkConstraints(def, data);
    return retval;
  }

  protected void visit(IFieldDefinition def, IAllowedValuesConstraint constraint, DATA data) {
  }

  protected void visit(IFieldDefinition def, IExpectConstraint constraint, DATA data) {
  }

  protected void visit(IFieldDefinition def, IIndexHasKeyConstraint constraint, DATA data) {
  }

  protected void visit(IFieldDefinition def, IMatchesConstraint constraint, DATA data) {
  }

  @Override
  protected void visit(IFlagDefinition def, DATA data) {
    walkConstraints(def, data);
  }

  protected void visit(IFlagDefinition def, IAllowedValuesConstraint constraint, DATA data) {
  }

  protected void visit(IFlagDefinition def, IExpectConstraint constraint, DATA data) {
  }

  protected void visit(IFlagDefinition def, IIndexHasKeyConstraint constraint, DATA data) {
  }

  protected void visit(IFlagDefinition def, IMatchesConstraint constraint, DATA data) {
  }

  protected void walkConstraints(IAssemblyDefinition def, DATA data) {
    for (IAllowedValuesConstraint constraint : def.getAllowedValuesContraints()) {
      visit(def, constraint, data);
    }

    for (IMatchesConstraint constraint : def.getMatchesConstraints()) {
      visit(def, constraint, data);
    }

    for (IExpectConstraint constraint : def.getExpectConstraints()) {
      visit(def, constraint, data);
    }

    for (IUniqueConstraint constraint : def.getUniqueConstraints()) {
      visit(def, constraint, data);
    }

    for (IIndexConstraint constraint : def.getIndexConstraints()) {
      visit(def, constraint, data);
    }

    for (IIndexHasKeyConstraint constraint : def.getIndexHasKeyConstraints()) {
      visit(def, constraint, data);
    }

    for (ICardinalityConstraint constraint : def.getHasCardinalityConstraints()) {
      visit(def, constraint, data);
    }
  }

  protected void walkConstraints(IFieldDefinition def, DATA data) {
    for (IAllowedValuesConstraint constraint : def.getAllowedValuesContraints()) {
      visit(def, constraint, data);
    }

    for (IMatchesConstraint constraint : def.getMatchesConstraints()) {
      visit(def, constraint, data);
    }

    for (IExpectConstraint constraint : def.getExpectConstraints()) {
      visit(def, constraint, data);
    }

    for (IIndexHasKeyConstraint constraint : def.getIndexHasKeyConstraints()) {
      visit(def, constraint, data);
    }
  }

  protected void walkConstraints(IFlagDefinition def, DATA data) {
    for (IAllowedValuesConstraint constraint : def.getAllowedValuesContraints()) {
      visit(def, constraint, data);
    }

    for (IMatchesConstraint constraint : def.getMatchesConstraints()) {
      visit(def, constraint, data);
    }

    for (IExpectConstraint constraint : def.getExpectConstraints()) {
      visit(def, constraint, data);
    }

    for (IIndexHasKeyConstraint constraint : def.getIndexHasKeyConstraints()) {
      visit(def, constraint, data);
    }
  }

}
