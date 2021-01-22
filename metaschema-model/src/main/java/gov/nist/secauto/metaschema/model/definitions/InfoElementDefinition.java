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

package gov.nist.secauto.metaschema.model.definitions;

import gov.nist.secauto.metaschema.datatypes.types.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.InfoElement;

public interface InfoElementDefinition extends InfoElement {
  /**
   * The formal display name for a definition.
   * 
   * @return the formal name
   */
  String getFormalName();

  /**
   * Get the text that describes the basic use of the definition.
   * 
   * @return a line of markup text
   */
  MarkupLine getDescription();

  /**
   * Retrieve the definition's scope within the context of its defining module.
   * 
   * @return the module scope
   */
  ModuleScopeEnum getModuleScope();

  /**
   * A definition can be locally defined (inline with instances) or globally defined. In the former
   * case {@link #isGlobal()} will be {@code false}, and in the latter case {@link #isGlobal()} will
   * be {@code true}.
   * 
   * @return {@code true} if the definition is globally defined, or {@code false} otherwise
   */
  boolean isGlobal();

  /**
   * Generates a "coordinate" string that consists of the definition's containing metaschema's short
   * name, the type of definition, the definition's name, and the hashcode.
   * 
   * @param definition
   *          the definition to generate coordinates for
   * @return the coordinate string
   */
  static String toCoordinates(InfoElementDefinition definition) {
    return String.format("%s:%s:%s@%d", definition.getContainingMetaschema().getShortName(), definition.getModelType(),
        definition.getName(), definition.hashCode());
  }
}
