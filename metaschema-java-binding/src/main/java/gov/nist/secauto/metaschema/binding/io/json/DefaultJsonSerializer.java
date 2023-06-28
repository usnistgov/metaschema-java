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
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.io.AbstractSerializer;
import gov.nist.secauto.metaschema.binding.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.RootAssemblyDefinition;

import java.io.IOException;
import java.io.Writer;

import edu.umd.cs.findbugs.annotations.NonNull;

public class DefaultJsonSerializer<CLASS>
    extends AbstractSerializer<CLASS> {
  private JsonFactory jsonFactory;

  public DefaultJsonSerializer(@NonNull IBindingContext bindingContext, @NonNull IAssemblyClassBinding classBinding) {
    super(bindingContext, classBinding);
  }

  @NonNull
  protected JsonFactory getJsonFactoryInstance() {
    return JsonFactoryFactory.instance();
  }

  @NonNull
  protected JsonFactory getJsonFactory() {
    synchronized (this) {
      if (jsonFactory == null) {
        jsonFactory = getJsonFactoryInstance();
      }
      assert jsonFactory != null;
      return jsonFactory;
    }
  }

  protected void setJsonFactory(JsonFactory jsonFactory) {
    synchronized (this) {
      this.jsonFactory = jsonFactory;
    }
  }

  protected JsonGenerator newJsonGenerator(Writer writer) throws IOException {
    JsonFactory factory = getJsonFactory();
    JsonGenerator retval = factory.createGenerator(writer);
    // avoid automatically closing streams not owned by the generator
    retval.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
    retval.setPrettyPrinter(new DefaultPrettyPrinter());
    return retval;
  }

  @Override
  public void serialize(CLASS data, Writer writer) throws IOException {
    try (JsonGenerator generator = newJsonGenerator(writer)) {
      IAssemblyClassBinding classBinding = getClassBinding();
      IJsonWritingContext writingContext = new DefaultJsonWritingContext(generator);

      RootAssemblyDefinition root = new RootAssemblyDefinition(classBinding);

      root.writeRoot(data, writingContext);
    }
  }

}
