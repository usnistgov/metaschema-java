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

package gov.nist.secauto.metaschema.core.model;

import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Provides methods to load a Metaschema expressed in XML.
 * <p>
 * Loaded Metaschema instances are cached to avoid the need to load them for
 * every use. Any Metaschema imported is also loaded and cached automatically.
 *
 * @param <T>
 *          the Java type of the module binding
 * @param <M>
 *          the Java type of the Metaschema module loaded by this loader
 */
public abstract class AbstractModuleLoader<T, M extends IModuleExtended<M, ?, ?, ?, ?>>
    extends AbstractLoader<M>
    implements IModuleLoader<M> {
  @NonNull
  private final List<IModuleLoader.IModulePostProcessor> modulePostProcessors;

  /**
   * Construct a new Metaschema module loader, which use the provided module post
   * processors when loading a module.
   *
   * @param modulePostProcessors
   *          post processors to perform additional module customization when
   *          loading
   */
  protected AbstractModuleLoader(@NonNull List<IModuleLoader.IModulePostProcessor> modulePostProcessors) {
    this.modulePostProcessors = CollectionUtil.unmodifiableList(new ArrayList<>(modulePostProcessors));
  }

  /**
   * Get the set of module post processors associated with this loader.
   *
   * @return the set of constraints
   */
  @NonNull
  protected List<IModuleLoader.IModulePostProcessor> getModulePostProcessors() {
    return modulePostProcessors;
  }

  /**
   * Parse the {@code resource} based on the provided {@code xmlObject}.
   *
   * @param resource
   *          the URI of the resource being parsed
   * @param binding
   *          the XML beans object to parse
   * @param importedModules
   *          previously parsed Metaschema modules imported by the provided
   *          {@code resource}
   * @return the parsed resource as a Metaschema module
   * @throws MetaschemaException
   *           if an error occurred while parsing the XML beans object
   */
  @NonNull
  protected abstract M newModule(
      @NonNull URI resource,
      @NonNull T binding,
      @NonNull List<? extends M> importedModules) throws MetaschemaException;

  /**
   * Get the list of Metaschema module URIs associated with the provided binding.
   *
   * @param binding
   *          the Metaschema module binding declaring the imports
   * @return the list of Metaschema module URIs
   */
  @NonNull
  protected abstract List<URI> getImports(@NonNull T binding);

  @Override
  protected M parseResource(@NonNull URI resource, @NonNull Deque<URI> visitedResources)
      throws IOException {
    // parse this Metaschema module
    T binding = parseModule(resource);

    // now check if this Metaschema imports other metaschema
    List<URI> imports = getImports(binding);
    @NonNull Map<URI, M> importedModules;
    if (imports.isEmpty()) {
      importedModules = ObjectUtils.notNull(Collections.emptyMap());
    } else {
      try {
        importedModules = new LinkedHashMap<>();
        for (URI importedResource : imports) {
          URI resolvedResource = ObjectUtils.notNull(resource.resolve(importedResource));
          importedModules.put(resolvedResource, loadInternal(resolvedResource, visitedResources));
        }
      } catch (MetaschemaException ex) {
        throw new IOException(ex);
      }
    }

    // now create this metaschema
    Collection<M> values = importedModules.values();
    try {
      M module = newModule(resource, binding, new ArrayList<>(values));

      for (IModuleLoader.IModulePostProcessor postProcessor : getModulePostProcessors()) {
        postProcessor.processModule(module);
      }
      return module;
    } catch (MetaschemaException ex) {
      throw new IOException(ex);
    }
  }

  /**
   * Parse the provided XML resource as a Metaschema module.
   *
   * @param resource
   *          the resource to parse
   * @return the XMLBeans representation of the Metaschema module
   * @throws IOException
   *           if a parsing error occurred
   */
  @NonNull
  protected abstract T parseModule(@NonNull URI resource) throws IOException;
}
