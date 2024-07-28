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

package gov.nist.secauto.metaschema.databind.io;

import gov.nist.secauto.metaschema.core.configuration.IConfiguration;
import gov.nist.secauto.metaschema.core.configuration.IMutableConfiguration;
import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The base class of all format-specific serializers.
 *
 * @param <CLASS>
 *          the bound class to serialize from
 */
public abstract class AbstractSerializer<CLASS extends IBoundObject>
    extends AbstractSerializationBase<SerializationFeature<?>>
    implements ISerializer<CLASS> {

  /**
   * Construct a new serializer.
   *
   * @param definition
   *          the bound class information for the Java type this serializer is
   *          operating on
   */
  public AbstractSerializer(@NonNull IBoundDefinitionModelAssembly definition) {
    super(definition);
  }

  @Override
  public ISerializer<CLASS> enableFeature(SerializationFeature<?> feature) {
    return set(feature, true);
  }

  @Override
  public ISerializer<CLASS> disableFeature(SerializationFeature<?> feature) {
    return set(feature, false);
  }

  @Override
  public ISerializer<CLASS> applyConfiguration(
      @NonNull IConfiguration<SerializationFeature<?>> other) {
    IMutableConfiguration<SerializationFeature<?>> config = getConfiguration();
    config.applyConfiguration(other);
    configurationChanged(config);
    return this;
  }

  @Override
  public ISerializer<CLASS> set(SerializationFeature<?> feature, Object value) {
    IMutableConfiguration<SerializationFeature<?>> config = getConfiguration();
    config.set(feature, value);
    configurationChanged(config);
    return this;
  }
}
