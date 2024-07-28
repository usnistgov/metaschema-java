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

package gov.nist.secauto.metaschema.core.metapath;

import gov.nist.secauto.metaschema.core.configuration.DefaultConfiguration;
import gov.nist.secauto.metaschema.core.configuration.IConfiguration;
import gov.nist.secauto.metaschema.core.configuration.IMutableConfiguration;
import gov.nist.secauto.metaschema.core.metapath.function.DefaultFunction.CallingContext;
import gov.nist.secauto.metaschema.core.metapath.function.IFunction.FunctionProperty;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.model.IUriResolver;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

// TODO: add support for in-scope namespaces
/**
 * The implementation of a Metapath
 * <a href="https://www.w3.org/TR/xpath-31/#eval_context">dynamic context</a>.
 */
public class DynamicContext { // NOPMD - intentional data class
  @NonNull
  private final Map<QName, ISequence<?>> letVariableMap;
  @NonNull
  private final SharedState sharedState;

  /**
   * Construct a new dynamic context with a default static context.
   */
  public DynamicContext() {
    this(StaticContext.instance());
  }

  /**
   * Construct a new Metapath dynamic context using the provided static context.
   *
   * @param staticContext
   *          the Metapath static context
   */
  public DynamicContext(@NonNull StaticContext staticContext) {
    this.letVariableMap = new ConcurrentHashMap<>();
    this.sharedState = new SharedState(staticContext);
  }

  private DynamicContext(@NonNull DynamicContext context) {
    this.letVariableMap = new ConcurrentHashMap<>(context.letVariableMap);
    this.sharedState = context.sharedState;
  }

  private static class SharedState {
    @NonNull
    private final StaticContext staticContext;
    @NonNull
    private final ZoneId implicitTimeZone;
    @NonNull
    private final ZonedDateTime currentDateTime;
    @NonNull
    private final Map<URI, IDocumentNodeItem> availableDocuments;
    @NonNull
    private final Map<CallingContext, ISequence<?>> functionResultCache;
    @Nullable
    private CachingLoader documentLoader;
    @NonNull
    private final IMutableConfiguration<MetapathEvaluationFeature<?>> configuration;

    public SharedState(@NonNull StaticContext staticContext) {
      this.staticContext = staticContext;

      Clock clock = Clock.systemDefaultZone();

      this.implicitTimeZone = ObjectUtils.notNull(clock.getZone());
      this.currentDateTime = ObjectUtils.notNull(ZonedDateTime.now(clock));
      this.availableDocuments = new HashMap<>();
      this.functionResultCache = new HashMap<>();
      this.configuration = new DefaultConfiguration<>();
      this.configuration.enableFeature(MetapathEvaluationFeature.METAPATH_EVALUATE_PREDICATES);
    }
  }

  /**
   * Generate a new dynamic context that is a copy of this dynamic context.
   * <p>
   * This method can be used to create a new sub-context where changes can be made
   * without affecting this context. This is useful for setting information that
   * is only used in a limited evaluation sub-scope, such as for handling variable
   * assignment.
   *
   * @return a new dynamic context
   */
  @NonNull
  public DynamicContext subContext() {
    return new DynamicContext(this);
  }

  /**
   * Get the static context associated with this dynamic context.
   *
   * @return the associated static context
   */
  @NonNull
  public StaticContext getStaticContext() {
    return sharedState.staticContext;
  }

  /**
   * Get the default time zone used for evaluation.
   *
   * @return the time zone identifier object
   */
  @NonNull
  public ZoneId getImplicitTimeZone() {
    return sharedState.implicitTimeZone;
  }

  /**
   * Get the current date and time.
   *
   * @return the current date and time
   */
  @NonNull
  public ZonedDateTime getCurrentDateTime() {
    return sharedState.currentDateTime;
  }

  /**
   * Get the mapping of loaded documents from the document URI to the document
   * node.
   *
   * @return the map of document URIs to document nodes
   */
  @SuppressWarnings("null")
  @NonNull
  public Map<URI, IDocumentNodeItem> getAvailableDocuments() {
    return Collections.unmodifiableMap(sharedState.availableDocuments);
  }

  /**
   * Get the document loader assigned to this dynamic context.
   *
   * @return the loader
   * @throws DynamicMetapathException
   *           with an error code
   *           {@link DynamicMetapathException#DYNAMIC_CONTEXT_ABSENT} if a
   *           document loader is not configured for this dynamic context
   */
  @NonNull
  public IDocumentLoader getDocumentLoader() {
    IDocumentLoader retval = sharedState.documentLoader;
    if (retval == null) {
      throw new DynamicMetapathException(DynamicMetapathException.DYNAMIC_CONTEXT_ABSENT,
          "No document loader configured for the dynamic context.");
    }
    return retval;
  }

  /**
   * Assign a document loader to this dynamic context.
   *
   * @param documentLoader
   *          the document loader to assign
   */
  public void setDocumentLoader(@NonNull IDocumentLoader documentLoader) {
    this.sharedState.documentLoader = new CachingLoader(documentLoader);
  }

  /**
   * Get the cached function call result for evaluating a function that has the
   * property {@link FunctionProperty#DETERMINISTIC}.
   *
   * @param callingContext
   *          the function calling context information that distinguishes the call
   *          from any other call
   * @return the cached result sequence for the function call
   */
  @Nullable
  public ISequence<?> getCachedResult(@NonNull CallingContext callingContext) {
    return sharedState.functionResultCache.get(callingContext);
  }

  /**
   * Cache a function call result for a that has the property
   * {@link FunctionProperty#DETERMINISTIC}.
   *
   * @param callingContext
   *          the calling context information that distinguishes the call from any
   *          other call
   * @param result
   *          the function call result
   */
  public void cacheResult(@NonNull CallingContext callingContext, @NonNull ISequence<?> result) {
    ISequence<?> old = sharedState.functionResultCache.put(callingContext, result);
    assert old == null;
  }

  /**
   * Used to disable the evaluation of predicate expressions during Metapath
   * evaluation.
   * <p>
   * This can be useful for determining the potential targets identified by a
   * Metapath expression as a partial evaluation, without evaluating that these
   * targets match the predicate.
   *
   * @return this dynamic context
   */
  @NonNull
  public DynamicContext disablePredicateEvaluation() {
    this.sharedState.configuration.disableFeature(MetapathEvaluationFeature.METAPATH_EVALUATE_PREDICATES);
    return this;
  }

  /**
   * Used to enable the evaluation of predicate expressions during Metapath
   * evaluation.
   * <p>
   * This is the default behavior if unchanged.
   *
   * @return this dynamic context
   */
  @NonNull
  public DynamicContext enablePredicateEvaluation() {
    this.sharedState.configuration.enableFeature(MetapathEvaluationFeature.METAPATH_EVALUATE_PREDICATES);
    return this;
  }

  /**
   * Get the Metapath evaluation configuration.
   *
   * @return the configuration
   */
  @NonNull
  public IConfiguration<MetapathEvaluationFeature<?>> getConfiguration() {
    return sharedState.configuration;
  }

  /**
   * Get the sequence value assigned to a let variable with the provided qualified
   * name.
   *
   * @param name
   *          the variable qualified name
   * @return the non-null variable value
   * @throws MetapathException
   *           of the variable has not been assigned or if the variable value is
   *           {@code null}
   */
  @NonNull
  public ISequence<?> getVariableValue(@NonNull QName name) {
    ISequence<?> retval = letVariableMap.get(name);
    if (retval == null) {
      if (!letVariableMap.containsKey(name)) {
        throw new MetapathException(String.format("Variable '%s' not defined in context.", name));
      }
      throw new MetapathException(String.format("Variable '%s' has null contents.", name));
    }
    return retval;
  }

  /**
   * Bind the variable {@code name} to the sequence {@code value}.
   *
   * @param name
   *          the name of the variable to bind
   * @param boundValue
   *          the value to bind to the variable
   */
  public void bindVariableValue(@NonNull QName name, @NonNull ISequence<?> boundValue) {
    letVariableMap.put(name, boundValue);
  }

  private class CachingLoader implements IDocumentLoader {
    @NonNull
    private final IDocumentLoader proxy;

    public CachingLoader(@NonNull IDocumentLoader proxy) {
      this.proxy = proxy;
    }

    @Override
    public IUriResolver getUriResolver() {
      return new ContextUriResolver();
    }

    @Override
    public void setUriResolver(@NonNull IUriResolver resolver) {
      // we delegate to the document loader proxy, so the resolver should be set there
      throw new UnsupportedOperationException("Set the resolver on the proxy");
    }

    @NonNull
    protected IDocumentLoader getProxiedDocumentLoader() {
      return proxy;
    }

    @Override
    public IDocumentNodeItem loadAsNodeItem(URI uri) throws IOException {
      IDocumentNodeItem retval = sharedState.availableDocuments.get(uri);
      if (retval == null) {
        retval = getProxiedDocumentLoader().loadAsNodeItem(uri);
        sharedState.availableDocuments.put(uri, retval);
      }
      return retval;
    }

    public class ContextUriResolver implements IUriResolver {

      /**
       * {@inheritDoc}
       * <p>
       * This method first resolves the provided URI against the static context's base
       * URI.
       */
      @Override
      public URI resolve(URI uri) {
        URI baseUri = getStaticContext().getBaseUri();

        URI resolvedUri;
        if (baseUri == null) {
          resolvedUri = uri;
        } else {
          resolvedUri = ObjectUtils.notNull(baseUri.resolve(uri));
        }

        IUriResolver resolver = getProxiedDocumentLoader().getUriResolver();
        return resolver == null ? resolvedUri : resolver.resolve(resolvedUri);
      }
    }
  }

}
