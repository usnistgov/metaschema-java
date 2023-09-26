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

package gov.nist.secauto.metaschema.core.testing;

import gov.nist.secauto.metaschema.core.model.IFlagContainer;
import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.MetaschemaModelConstants;

import org.jmock.Expectations;
import org.jmock.Mockery;

import edu.umd.cs.findbugs.annotations.NonNull;

public class FlagBuilder
    extends AbstractModelBuilder<FlagBuilder> {

  private boolean required;

  private FlagBuilder(@NonNull Mockery ctx) {
    super(ctx);
  }

  @NonNull
  public static FlagBuilder builder(@NonNull Mockery ctx) {
    return new FlagBuilder(ctx).reset();
  }

  @Override
  public FlagBuilder reset() {
    this.required = MetaschemaModelConstants.DEFAULT_FLAG_REQUIRED;
    return this;
  }

  public FlagBuilder required(boolean required) {
    this.required = required;
    return this;
  }

  @NonNull
  public IFlagInstance toInstance(
      @NonNull IFlagContainer parent) {
    IFlagDefinition def = toDefinition();
    return toInstance(parent, def);
  }

  @NonNull
  public IFlagInstance toInstance(
      @NonNull IFlagContainer parent,
      @NonNull IFlagDefinition definition) {
    validate();

    IFlagInstance retval = mock(IFlagInstance.class);

    applyNamedInstance(retval, definition, parent);

    getContext().checking(new Expectations() {
      {
        allowing(retval).isRequired();
        will(returnValue(required));
      }
    });

    return retval;
  }

  @NonNull
  public IFlagDefinition toDefinition() {
    validate();

    IFlagDefinition retval = mock(IFlagDefinition.class);
    applyDefinition(retval);
    return retval;
  }

  // @Override
  // public Object getValue(Object parentInstance) {
  // // TODO Auto-generated method stub
  // return null;
  // }
  // }
}
