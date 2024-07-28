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

package gov.nist.secauto.metaschema.databind.model;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.DefaultBindingContext;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.model.test.RootBoundAssembly;

import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.io.Reader;

import edu.umd.cs.findbugs.annotations.NonNull;

public class AbstractBoundModelTestSupport {
  @RegisterExtension
  private final JUnit5Mockery context = new JUnit5Mockery();

  @NonNull
  private final IBindingContext bindingContext = DefaultBindingContext.instance();
  //
  // @BeforeAll
  // void initContext() {
  // /**
  // * Setup bound classes
  // */
  // registerMetaschema(TestMetaschema.class);
  // registerClassBinding(CollapsibleFlaggedBoundField.class);
  // registerClassBinding(EmptyBoundAssembly.class);
  // registerClassBinding(FlaggedBoundAssembly.class);
  // registerClassBinding(FlaggedBoundField.class);
  // registerClassBinding(OnlyModelBoundAssembly.class);
  // registerClassBinding(RootBoundAssembly.class);
  // }

  @NonNull
  protected JUnit5Mockery getJUnit5Mockery() {
    return ObjectUtils.requireNonNull(context);
  }

  @NonNull
  protected IBindingContext getBindingContext() {
    return bindingContext;
  }

  @NonNull
  protected IBoundDefinitionModelComplex registerClassBinding(@NonNull Class<? extends IBoundObject> clazz) {
    IBoundDefinitionModelComplex definition = getBindingContext().getBoundDefinitionForClass(clazz);
    if (definition == null) {
      throw new IllegalArgumentException(String.format("Unable to find bound definition for class '%s'.",
          clazz.getName()));
    }
    return definition;
  }

  @NonNull
  protected IBoundModule registerModule(@NonNull Class<? extends IBoundModule> clazz) {
    return getBindingContext().registerModule(clazz);
  }

  @NonNull
  protected IBoundDefinitionModelAssembly getRootAssemblyClassBinding() {
    return ObjectUtils.requireNonNull((IBoundDefinitionModelAssembly) getBindingContext()
        .getBoundDefinitionForClass(RootBoundAssembly.class));
  }

  @SuppressWarnings("resource")
  @NonNull
  protected JsonParser newJsonParser(@NonNull Reader reader) throws JsonParseException, IOException {
    JsonFactory factory = new JsonFactory();
    JsonParser jsonParser = factory.createParser(reader); // NOPMD - reader not owned by this method
    return ObjectUtils.notNull(jsonParser);
  }
}
