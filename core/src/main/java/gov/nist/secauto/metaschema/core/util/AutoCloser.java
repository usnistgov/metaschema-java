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

package gov.nist.secauto.metaschema.core.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Wraps a resource to make it {@link AutoCloseable}.
 *
 * @param <T>
 *          the resource type
 * @param <E>
 *          the exception type that may be thrown if an error occurs when
 *          closing the resource
 */
public class AutoCloser<T, E extends Exception> implements AutoCloseable {
  @NonNull
  private final T resource;
  @NonNull
  private final Closer<T, E> closeLambda;

  /**
   * Adapt the the provided {@code resource} to be {@link AutoCloseable}, using a
   * provided closer {@code lambda}.
   *
   * @param <T>
   *          the resource's type
   * @param <E>
   *          the exception type that can be thrown when closing
   * @param resource
   *          the object to adapt
   * @param lambda
   *          the lambda to use as a callback on close
   * @return the resource wrapped in an {@link AutoCloseable}
   */
  @NonNull
  public static <T, E extends Exception> AutoCloser<T, E> autoClose(
      @NonNull T resource,
      @NonNull Closer<T, E> lambda) {
    return new AutoCloser<>(resource, lambda);
  }

  /**
   * Adapt the provided {@code resource} to be {@link AutoCloseable}, using a
   * provided closer {@code lambda}.
   *
   * @param resource
   *          the object to adapt
   * @param lambda
   *          the lambda to use as a callback on close
   */
  public AutoCloser(@NonNull T resource, @NonNull Closer<T, E> lambda) {
    this.resource = resource;
    this.closeLambda = lambda;
  }

  /**
   * Get the wrapped resource.
   *
   * @return the resource object
   */
  @NonNull
  public T getResource() {
    return resource;
  }

  @Override
  @SuppressFBWarnings("THROWS_METHOD_THROWS_CLAUSE_BASIC_EXCEPTION")
  public void close() throws E {
    closeLambda.close(getResource());
  }

  @FunctionalInterface
  public interface Closer<T, E extends Exception> {
    /**
     * This method is called to auto-close the resource.
     *
     * @param object
     *          the resource to auto-close
     * @throws E
     *           the exception type that can be thrown when closing
     */
    @SuppressFBWarnings("THROWS_METHOD_THROWS_CLAUSE_BASIC_EXCEPTION")
    void close(@NonNull T object) throws E;
  }
}
