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

package gov.nist.secauto.metaschema.core.model.constraint;

import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.model.ModelWalker;

// TODO: remove if unused
abstract class ConstraintVisitingModelWalker<DATA>
    extends ModelWalker<DATA> {

  @Override
  protected boolean visit(IAssemblyDefinition def, DATA data) {
    boolean retval = super.visit(def, data);
    walkConstraints(def, data);
    return retval;
  }

  /**
   * A callback called when visiting the {@code constraint} associated with {@code def}.
   *
   * @param definition
   *          the definition the constraint is associated with
   * @param constraint
   *          the constraint associated with the definition
   * @param data
   *          other data to use for processing
   */
  protected void visit(IAssemblyDefinition definition, IAllowedValuesConstraint constraint, DATA data) { // NOPMD -
                                                                                                         // intentional
    // subclasses may override this method to process the constraint
  }

  /**
   * A callback called when visiting the {@code constraint} associated with {@code def}.
   *
   * @param definition
   *          the definition the constraint is associated with
   * @param constraint
   *          the constraint associated with the definition
   * @param data
   *          other data to use for processing
   */
  protected void visit(IAssemblyDefinition definition, ICardinalityConstraint constraint, DATA data) { // NOPMD -
                                                                                                       // intentional
    // subclasses may override this method to process the constraint
  }

  /**
   * A callback called when visiting the {@code constraint} associated with {@code def}.
   *
   * @param definition
   *          the definition the constraint is associated with
   * @param constraint
   *          the constraint associated with the definition
   * @param data
   *          other data to use for processing
   */
  protected void visit(IAssemblyDefinition definition, IExpectConstraint constraint, DATA data) { // NOPMD - intentional
    // subclasses may override this method to process the constraint
  }

  /**
   * A callback called when visiting the {@code constraint} associated with {@code def}.
   *
   * @param definition
   *          the definition the constraint is associated with
   * @param constraint
   *          the constraint associated with the definition
   * @param data
   *          other data to use for processing
   */
  protected void visit(IAssemblyDefinition definition, IIndexConstraint constraint, DATA data) { // NOPMD - intentional
    // subclasses may override this method to process the constraint
  }

  /**
   * A callback called when visiting the {@code constraint} associated with {@code def}.
   *
   * @param definition
   *          the definition the constraint is associated with
   * @param constraint
   *          the constraint associated with the definition
   * @param data
   *          other data to use for processing
   */
  protected void visit(IAssemblyDefinition definition, IIndexHasKeyConstraint constraint, // NOPMD - intentional
      DATA data) {
    // subclasses may override this method to process the constraint
  }

  /**
   * A callback called when visiting the {@code constraint} associated with {@code def}.
   *
   * @param definition
   *          the definition the constraint is associated with
   * @param constraint
   *          the constraint associated with the definition
   * @param data
   *          other data to use for processing
   */
  protected void visit(IAssemblyDefinition definition, IMatchesConstraint constraint, // NOPMD - intentional
      DATA data) {
    // subclasses may override this method to process the constraint
  }

  /**
   * A callback called when visiting the {@code constraint} associated with {@code def}.
   *
   * @param definition
   *          the definition the constraint is associated with
   * @param constraint
   *          the constraint associated with the definition
   * @param data
   *          other data to use for processing
   */
  protected void visit(IAssemblyDefinition definition, IUniqueConstraint constraint, DATA data) { // NOPMD - intentional
    // subclasses may override this method to process the constraint
  }

  @Override
  protected boolean visit(IFieldDefinition def, DATA data) {
    boolean retval = super.visit(def, data);
    walkConstraints(def, data);
    return retval;
  }

  /**
   * A callback called when visiting the {@code constraint} associated with {@code def}.
   *
   * @param definition
   *          the definition the constraint is associated with
   * @param constraint
   *          the constraint associated with the definition
   * @param data
   *          other data to use for processing
   */
  protected void visit(IFieldDefinition definition, IAllowedValuesConstraint constraint, // NOPMD - intentional
      DATA data) {
    // subclasses may override this method to process the constraint
  }

  /**
   * A callback called when visiting the {@code constraint} associated with {@code def}.
   *
   * @param definition
   *          the definition the constraint is associated with
   * @param constraint
   *          the constraint associated with the definition
   * @param data
   *          other data to use for processing
   */
  protected void visit(IFieldDefinition definition, IExpectConstraint constraint, DATA data) { // NOPMD - intentional
    // subclasses may override this method to process the constraint
  }

  /**
   * A callback called when visiting the {@code constraint} associated with {@code def}.
   *
   * @param definition
   *          the definition the constraint is associated with
   * @param constraint
   *          the constraint associated with the definition
   * @param data
   *          other data to use for processing
   */
  protected void visit(IFieldDefinition definition, IIndexHasKeyConstraint constraint, // NOPMD - intentional
      DATA data) {
    // subclasses may override this method to process the constraint
  }

  /**
   * A callback called when visiting the {@code constraint} associated with {@code def}.
   *
   * @param definition
   *          the definition the constraint is associated with
   * @param constraint
   *          the constraint associated with the definition
   * @param data
   *          other data to use for processing
   */
  protected void visit(IFieldDefinition definition, IMatchesConstraint constraint, DATA data) { // NOPMD - intentional
    // subclasses may override this method to process the constraint
  }

  @Override
  protected void visit(IFlagDefinition def, DATA data) {
    walkConstraints(def, data);
  }

  /**
   * A callback called when visiting the {@code constraint} associated with {@code def}.
   *
   * @param definition
   *          the definition the constraint is associated with
   * @param constraint
   *          the constraint associated with the definition
   * @param data
   *          other data to use for processing
   */
  protected void visit(IFlagDefinition definition, IAllowedValuesConstraint constraint, // NOPMD - intentional
      DATA data) {
    // subclasses may override this method to process the constraint
  }

  /**
   * A callback called when visiting the {@code constraint} associated with {@code def}.
   *
   * @param definition
   *          the definition the constraint is associated with
   * @param constraint
   *          the constraint associated with the definition
   * @param data
   *          other data to use for processing
   */
  protected void visit(IFlagDefinition definition, IExpectConstraint constraint, DATA data) { // NOPMD - intentional
    // subclasses may override this method to process the constraint
  }

  /**
   * A callback called when visiting the {@code constraint} associated with {@code def}.
   *
   * @param definition
   *          the definition the constraint is associated with
   * @param constraint
   *          the constraint associated with the definition
   * @param data
   *          other data to use for processing
   */
  protected void visit(IFlagDefinition definition, IIndexHasKeyConstraint constraint, // NOPMD - intentional
      DATA data) {
    // subclasses may override this method to process the constraint
  }

  /**
   * A callback called when visiting the {@code constraint} associated with {@code def}.
   *
   * @param definition
   *          the definition the constraint is associated with
   * @param constraint
   *          the constraint associated with the definition
   * @param data
   *          other data to use for processing
   */
  protected void visit(IFlagDefinition definition, IMatchesConstraint constraint, DATA data) { // NOPMD - intentional
    // subclasses may override this method to process the constraint
  }

  /**
   * Walk the constraints associated with the {@code definition}.
   *
   * @param definition
   *          the definition the constraints to walk are associated with
   * @param data
   *          other data to use for processing
   */
  protected void walkConstraints(IAssemblyDefinition definition, DATA data) {
    for (IAllowedValuesConstraint constraint : definition.getAllowedValuesConstraints()) {
      visit(definition, constraint, data);
    }

    for (IMatchesConstraint constraint : definition.getMatchesConstraints()) {
      visit(definition, constraint, data);
    }

    for (IExpectConstraint constraint : definition.getExpectConstraints()) {
      visit(definition, constraint, data);
    }

    for (IUniqueConstraint constraint : definition.getUniqueConstraints()) {
      visit(definition, constraint, data);
    }

    for (IIndexConstraint constraint : definition.getIndexConstraints()) {
      visit(definition, constraint, data);
    }

    for (IIndexHasKeyConstraint constraint : definition.getIndexHasKeyConstraints()) {
      visit(definition, constraint, data);
    }

    for (ICardinalityConstraint constraint : definition.getHasCardinalityConstraints()) {
      visit(definition, constraint, data);
    }
  }

  /**
   * Walk the constraints associated with the {@code definition}.
   *
   * @param definition
   *          the definition the constraints to walk are associated with
   * @param data
   *          other data to use for processing
   */
  protected void walkConstraints(IFieldDefinition definition, DATA data) {
    for (IAllowedValuesConstraint constraint : definition.getAllowedValuesConstraints()) {
      visit(definition, constraint, data);
    }

    for (IMatchesConstraint constraint : definition.getMatchesConstraints()) {
      visit(definition, constraint, data);
    }

    for (IExpectConstraint constraint : definition.getExpectConstraints()) {
      visit(definition, constraint, data);
    }

    for (IIndexHasKeyConstraint constraint : definition.getIndexHasKeyConstraints()) {
      visit(definition, constraint, data);
    }
  }

  /**
   * Walk the constraints associated with the {@code definition}.
   *
   * @param definition
   *          the definition the constraints to walk are associated with
   * @param data
   *          other data to use for processing
   */
  protected void walkConstraints(IFlagDefinition definition, DATA data) {
    for (IAllowedValuesConstraint constraint : definition.getAllowedValuesConstraints()) {
      visit(definition, constraint, data);
    }

    for (IMatchesConstraint constraint : definition.getMatchesConstraints()) {
      visit(definition, constraint, data);
    }

    for (IExpectConstraint constraint : definition.getExpectConstraints()) {
      visit(definition, constraint, data);
    }

    for (IIndexHasKeyConstraint constraint : definition.getIndexHasKeyConstraints()) {
      visit(definition, constraint, data);
    }
  }

}
