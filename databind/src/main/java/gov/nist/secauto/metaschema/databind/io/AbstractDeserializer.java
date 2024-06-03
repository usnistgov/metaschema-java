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
import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.StaticContext;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.model.constraint.DefaultConstraintValidator;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraintValidationHandler;
import gov.nist.secauto.metaschema.core.model.constraint.LoggingConstraintValidationHandler;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The base class of all format-specific deserializers.
 *
 * @param <CLASS>
 *          the bound class to deserialize to
 */
public abstract class AbstractDeserializer<CLASS>
    extends AbstractSerializationBase<DeserializationFeature<?>>
    implements IDeserializer<CLASS> {

  private IConstraintValidationHandler constraintValidationHandler;

  /**
   * Construct a new deserializer.
   *
   * @param definition
   *          the bound class information for the Java type this deserializer is
   *          operating on
   */
  protected AbstractDeserializer(@NonNull IBoundDefinitionModelAssembly definition) {
    super(definition);
  }

  /**
   * Get the constraint validation handler configured for this deserializer, which
   * will be used to validate loaded data.
   *
   * @return the deserializer
   */
  @Override
  @NonNull
  public IConstraintValidationHandler getConstraintValidationHandler() {
    synchronized (this) {
      if (constraintValidationHandler == null) {
        constraintValidationHandler = new LoggingConstraintValidationHandler();
      }
      return ObjectUtils.notNull(constraintValidationHandler);
    }
  }

  @Override
  public void setConstraintValidationHandler(@NonNull IConstraintValidationHandler constraintValidationHandler) {
    synchronized (this) {
      this.constraintValidationHandler = constraintValidationHandler;
    }
  }

  @Override
  public INodeItem deserializeToNodeItem(Reader reader, URI documentUri) throws IOException {

    INodeItem nodeItem;
    try {
      nodeItem = deserializeToNodeItemInternal(reader, documentUri);
    } catch (Exception ex) { // NOPMD - this is intentional
      throw new IOException(ex);
    }

    if (isValidating()) {
      DynamicContext dynamicContext = new DynamicContext(
          StaticContext.builder()
              .defaultModelNamespace(nodeItem.getNamespace())
              .build());
      dynamicContext.setDocumentLoader(getBindingContext().newBoundLoader());
      DefaultConstraintValidator validator = new DefaultConstraintValidator(getConstraintValidationHandler());
      validator.validate(nodeItem, dynamicContext);
      validator.finalizeValidation(dynamicContext);
    }
    return nodeItem;
  }

  /**
   * This abstract method delegates parsing to the concrete implementation.
   *
   * @param reader
   *          the reader instance to read data from
   * @param documentUri
   *          the URI of the document that is being read
   * @return a new node item containing the read contents
   * @throws IOException
   *           if an error occurred while reading data from the stream
   */
  @NonNull
  protected abstract INodeItem deserializeToNodeItemInternal(@NonNull Reader reader, @NonNull URI documentUri)
      throws IOException;

  @Override
  public IDeserializer<CLASS> enableFeature(DeserializationFeature<?> feature) {
    return set(feature, true);
  }

  @Override
  public IDeserializer<CLASS> disableFeature(DeserializationFeature<?> feature) {
    return set(feature, false);
  }

  @Override
  public IDeserializer<CLASS> applyConfiguration(
      @NonNull IConfiguration<DeserializationFeature<?>> other) {
    IMutableConfiguration<DeserializationFeature<?>> config = getConfiguration();
    config.applyConfiguration(other);
    configurationChanged(config);
    return this;
  }

  @Override
  public IDeserializer<CLASS> set(DeserializationFeature<?> feature, Object value) {
    IMutableConfiguration<DeserializationFeature<?>> config = getConfiguration();
    config.set(feature, value);
    configurationChanged(config);
    return this;
  }
}
