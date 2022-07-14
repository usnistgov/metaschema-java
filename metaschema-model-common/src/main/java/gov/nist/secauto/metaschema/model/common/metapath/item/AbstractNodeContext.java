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

package gov.nist.secauto.metaschema.model.common.metapath.item;

import gov.nist.secauto.metaschema.model.common.metapath.INodeContext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import nl.talsmasoftware.lazy4j.Lazy;

abstract class AbstractNodeContext<F extends IFlagNodeItem, L extends AbstractNodeContext.Flags<F>>
    implements INodeContext {

  private final Lazy<L> model;

  protected AbstractNodeContext(@NotNull INodeItemFactory factory) {
    this.model = Lazy.lazy(newModelSupplier(factory));
  }

  @NotNull
  protected abstract Supplier<L> newModelSupplier(@NotNull INodeItemFactory factory);

  @SuppressWarnings("null")
  @NotNull
  protected L getModel() {
    return model.get();
  }

  @Override
  public Collection<@NotNull F> getFlags() {
    return getModel().getFlags();
  }

  @Override
  public F getFlagByName(@NotNull String name) {
    return getModel().getFlagByName(name);
  }

  /**
   * Provides an abstract implementation of a lazy loaded collection of flags.
   *
   * @param <F>
   *          the type of the flag items
   */
  protected static class Flags<F extends IFlagNodeItem> {
    private final Map<@NotNull String, F> flags;

    protected Flags(@NotNull Map<@NotNull String, F> flags) {
      this.flags = flags;
    }

    @Nullable
    public F getFlagByName(@NotNull String name) {
      return flags.get(name);
    }

    @NotNull
    @SuppressWarnings("null")
    public Collection<@NotNull F> getFlags() {
      return flags.values();
    }
  }
}
