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

package gov.nist.secauto.metaschema.binding.model;

import com.ctc.wstx.stax.WstxInputFactory;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.binding.model.test.CollapsibleFlaggedBoundField;
import gov.nist.secauto.metaschema.binding.model.test.EmptyBoundAssembly;
import gov.nist.secauto.metaschema.binding.model.test.FlaggedBoundAssembly;
import gov.nist.secauto.metaschema.binding.model.test.FlaggedBoundField;
import gov.nist.secauto.metaschema.binding.model.test.OnlyModelBoundAssembly;
import gov.nist.secauto.metaschema.binding.model.test.RootBoundAssembly;
import gov.nist.secauto.metaschema.binding.model.test.TestMetaschema;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.MetaschemaDataTypeProvider;

import org.codehaus.stax2.XMLEventReader2;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.io.Reader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

public class AbstractBoundModelTestSupport {
  @RegisterExtension
  private final JUnit5Mockery context = new JUnit5Mockery();

  @Mock
  private IBindingContext bindingContext; // NOPMD - auto mocked

  @NotNull
  protected JUnit5Mockery getJUnit5Mockery() {
    return context;
  }

  protected void registerDatatype(@NotNull IJavaTypeAdapter<?> adapter) {
    context.checking(new Expectations() {
      { // NOPMD - intentional
        allowing(bindingContext).getJavaTypeAdapterInstance(adapter.getClass());
        will(returnValue(adapter));
      }
    });
  }

  @NotNull
  protected IFieldClassBinding registerFieldBinding(@NotNull Class<?> clazz) {
    IFieldClassBinding retval = DefaultFieldClassBinding.createInstance(clazz, bindingContext);
    context.checking(new Expectations() {
      { // NOPMD - intentional
        allowing(bindingContext).getClassBinding(clazz);
        will(returnValue(retval));
      }
    });
    return retval;
  }

  @NotNull
  protected IAssemblyClassBinding registerAssemblyBinding(@NotNull Class<?> clazz) {
    IAssemblyClassBinding retval = DefaultAssemblyClassBinding.createInstance(clazz, bindingContext);
    context.checking(new Expectations() {
      { // NOPMD - intentional
        allowing(bindingContext).getClassBinding(clazz);
        will(returnValue(retval));
      }
    });
    return retval;
  }

  @NotNull
  protected IMetaschema registerMetaschema(@NotNull Class<? extends AbstractBoundMetaschema> clazz) {
    IMetaschema retval = AbstractBoundMetaschema.createInstance(clazz, bindingContext);
    context.checking(new Expectations() {
      { // NOPMD - intentional
        allowing(bindingContext).getMetaschemaInstanceByClass(clazz);
        will(returnValue(retval));
      }
    });
    return retval;
  }

  @NotNull
  protected IAssemblyClassBinding getRootAssemblyClassBinding() {
    /**
     * Setup data types
     */
    registerDatatype(MetaschemaDataTypeProvider.BOOLEAN);
    registerDatatype(MetaschemaDataTypeProvider.STRING);
    registerDatatype(MetaschemaDataTypeProvider.TOKEN);
    registerDatatype(MetaschemaDataTypeProvider.UUID);

    /**
     * Setup bound classes
     */
    registerMetaschema(TestMetaschema.class);
    registerFieldBinding(CollapsibleFlaggedBoundField.class);
    registerAssemblyBinding(EmptyBoundAssembly.class);
    registerAssemblyBinding(FlaggedBoundAssembly.class);
    registerFieldBinding(FlaggedBoundField.class);
    registerAssemblyBinding(OnlyModelBoundAssembly.class);
    return registerAssemblyBinding(RootBoundAssembly.class);
  }

  @NotNull
  protected IXmlParsingContext newXmlParsingContext(Reader reader) throws XMLStreamException {

    XMLInputFactory factory = WstxInputFactory.newInstance();
    XMLEventReader2 parser = (XMLEventReader2) factory.createXMLEventReader(reader);

    IXmlParsingContext retval = context.mock(IXmlParsingContext.class);

    context.checking(new Expectations() {
      { // NOPMD - intentional
        allowing(retval).getReader();
        will(returnValue(parser));
      }
    });
    return retval;
  }

  @NotNull
  protected IJsonParsingContext newJsonParsingContext(Reader reader) throws JsonParseException, IOException {
    JsonFactory factory = new JsonFactory();
    JsonParser jsonParser = factory.createParser(reader); // NOPMD - reader not owned by this method

    IJsonParsingContext retval = context.mock(IJsonParsingContext.class);

    context.checking(new Expectations() {
      { // NOPMD - intentional
        allowing(retval).getReader();
        will(returnValue(jsonParser));
      }
    });
    return retval;
  }
}
