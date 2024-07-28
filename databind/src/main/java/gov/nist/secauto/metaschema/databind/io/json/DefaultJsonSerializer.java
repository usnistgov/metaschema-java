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

package gov.nist.secauto.metaschema.databind.io.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import gov.nist.secauto.metaschema.core.configuration.IMutableConfiguration;
import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.AbstractSerializer;
import gov.nist.secauto.metaschema.databind.io.SerializationFeature;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;

import java.io.IOException;
import java.io.Writer;

import edu.umd.cs.findbugs.annotations.NonNull;

public class DefaultJsonSerializer<CLASS extends IBoundObject>
    extends AbstractSerializer<CLASS> {
  private JsonFactory jsonFactory;

  /**
   * Construct a new Module binding-based deserializer that reads JSON-based
   * Module content.
   *
   * @param definition
   *          the assembly class binding describing the Java objects this
   *          deserializer parses data into
   */
  public DefaultJsonSerializer(@NonNull IBoundDefinitionModelAssembly definition) {
    super(definition);
  }

  /**
   * Constructs a new JSON factory.
   * <p>
   * Subclasses can override this method to create a JSON factory with a specific
   * configuration.
   *
   * @return the factory
   */
  @NonNull
  protected JsonFactory getJsonFactoryInstance() {
    return JsonFactoryFactory.instance();
  }

  @SuppressWarnings("PMD.NullAssignment")
  @Override
  protected void configurationChanged(IMutableConfiguration<SerializationFeature<?>> config) {
    synchronized (this) {
      jsonFactory = null;
    }
  }

  @NonNull
  private JsonFactory getJsonFactory() {
    synchronized (this) {
      if (jsonFactory == null) {
        jsonFactory = getJsonFactoryInstance();
      }
      assert jsonFactory != null;
      return jsonFactory;
    }
  }

  @SuppressWarnings("resource")
  @NonNull
  private JsonGenerator newJsonGenerator(@NonNull Writer writer) throws IOException {
    JsonFactory factory = getJsonFactory();
    return ObjectUtils.notNull(factory.createGenerator(writer)
        .setPrettyPrinter(new DefaultPrettyPrinter()));
  }

  @Override
  public void serialize(IBoundObject data, Writer writer) throws IOException {
    try (JsonGenerator generator = newJsonGenerator(writer)) {
      IBoundDefinitionModelAssembly definition = getDefinition();

      boolean serializeRoot = get(SerializationFeature.SERIALIZE_ROOT);
      if (serializeRoot) {
        // first write the initial START_OBJECT
        generator.writeStartObject();

        generator.writeFieldName(definition.getRootJsonName());
      }

      MetaschemaJsonWriter jsonWriter = new MetaschemaJsonWriter(generator);
      jsonWriter.write(definition, data);

      if (serializeRoot) {
        generator.writeEndObject();
      }
    }
  }
}
