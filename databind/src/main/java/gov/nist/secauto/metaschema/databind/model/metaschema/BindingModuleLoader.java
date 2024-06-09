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

package gov.nist.secauto.metaschema.databind.model.metaschema;

import gov.nist.secauto.metaschema.core.configuration.IConfiguration;
import gov.nist.secauto.metaschema.core.configuration.IMutableConfiguration;
import gov.nist.secauto.metaschema.core.model.AbstractModuleLoader;
import gov.nist.secauto.metaschema.core.model.IMetaschemaModule;
import gov.nist.secauto.metaschema.core.model.IModuleLoader;
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.DeserializationFeature;
import gov.nist.secauto.metaschema.databind.io.IBoundLoader;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.METASCHEMA;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.METASCHEMA.Import;
import gov.nist.secauto.metaschema.databind.model.metaschema.impl.BindingModule;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

public class BindingModuleLoader
    extends AbstractModuleLoader<METASCHEMA, IMetaschemaModule>
    implements IMutableConfiguration<DeserializationFeature<?>> {
  @NonNull
  private final IBoundLoader loader;

  // @NonNull
  // private final

  /**
   * Construct a new Metaschema loader.
   */
  public BindingModuleLoader() {
    this(CollectionUtil.emptyList());
  }

  /**
   * Construct a new Metaschema loader, which use the provided module post
   * processors when loading a module.
   *
   * @param modulePostProcessors
   *          post processors to perform additional module customization when
   *          loading
   */
  public BindingModuleLoader(@NonNull List<IModuleLoader.IModulePostProcessor> modulePostProcessors) {
    super(modulePostProcessors);
    this.loader = IBindingContext.instance().newBoundLoader();
  }

  @Override
  protected IMetaschemaModule newModule(
      URI resource,
      METASCHEMA binding,
      List<? extends IMetaschemaModule> importedModules)
      throws MetaschemaException {
    return new BindingModule(
        resource,
        ObjectUtils.notNull(
            (IBoundDefinitionModelAssembly) getLoader().getBindingContext()
                .getBoundDefinitionForClass(METASCHEMA.class)),
        binding,
        importedModules);
  }

  @Override
  protected List<URI> getImports(METASCHEMA binding) {
    return ObjectUtils.notNull(binding.getImports().stream()
        .map(Import::getHref)
        .collect(Collectors.toUnmodifiableList()));
  }

  @Override
  protected METASCHEMA parseModule(URI resource) throws IOException {
    return getLoader().load(METASCHEMA.class, resource);
  }

  protected IBoundLoader getLoader() {
    return loader;
  }

  @Override
  public boolean isFeatureEnabled(DeserializationFeature<?> feature) {
    return getLoader().isFeatureEnabled(feature);
  }

  @Override
  public Map<DeserializationFeature<?>, Object> getFeatureValues() {
    return getLoader().getFeatureValues();
  }

  @Override
  public IMutableConfiguration<DeserializationFeature<?>>
      applyConfiguration(IConfiguration<DeserializationFeature<?>> other) {
    return getLoader().applyConfiguration(other);
  }

  @Override
  public IMutableConfiguration<DeserializationFeature<?>> set(DeserializationFeature<?> feature, Object value) {
    return getLoader().set(feature, value);
  }

  public void allowEntityResolution() {
    enableFeature(DeserializationFeature.DESERIALIZE_XML_ALLOW_ENTITY_RESOLUTION);
  }
}
