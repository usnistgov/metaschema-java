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

package gov.nist.secauto.metaschema.binding.io.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.io.AbstractSerializer;
import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class DefaultJsonSerializer<CLASS>
    extends AbstractSerializer<CLASS> {
  private JsonFactory jsonFactory;

  public DefaultJsonSerializer(BindingContext bindingContext, AssemblyClassBinding classBinding) {
    super(bindingContext, classBinding);
  }

  protected JsonFactory getJsonFactoryInstance() {
    return JsonFactoryFactory.singletonInstance();
  }

  protected JsonFactory getJsonFactory() {
    synchronized (this) {
      if (jsonFactory == null) {
        jsonFactory = getJsonFactoryInstance();
      }
      return jsonFactory;
    }
  }

  protected void setJsonFactory(JsonFactory jsonFactory) {
    synchronized (this) {
      this.jsonFactory = jsonFactory;
    }
  }

  protected JsonParser newJsonParser(Reader reader) throws BindingException {
    try {
      JsonFactory factory = getJsonFactory();
      JsonParser retval = factory.createParser(reader);
      return retval;
    } catch (IOException ex) {
      throw new BindingException(ex);
    }
  }

  protected JsonGenerator newJsonGenerator(Writer writer) throws BindingException {
    try {
      JsonFactory factory = getJsonFactory();
      JsonGenerator retval = factory.createGenerator(writer);
      retval.setPrettyPrinter(new DefaultPrettyPrinter());
      return retval;
    } catch (IOException ex) {
      throw new BindingException(ex);
    }
  }

  @Override
  public void serialize(CLASS data, Writer writer) throws BindingException {
    JsonGenerator generator = newJsonGenerator(writer);
    try {
      AssemblyClassBinding classBinding = getClassBinding();
      JsonWritingContext writingContext = new DefaultJsonWritingContext(generator);
      classBinding.writeRoot(data, writingContext);
      generator.close();
    } catch (IOException ex) {
      throw new BindingException(ex);
    }
  }

}
