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

package gov.nist.secauto.metaschema.model.common.metapath.format;

import gov.nist.secauto.metaschema.model.common.instance.INamedModelInstance;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * This abstract implementation represents a path segment that is part of an assembly's model.
 */
public abstract class AbstractModelPositionalPathSegment<INSTANCE extends INamedModelInstance>
    implements IModelPositionalPathSegment {
  private final IAssemblyPathSegment parent;
  private final INSTANCE instance;
  private final int position;

  /**
   * Construct a new model path segment for the provided instance. The position denotes which instance
   * this is in a collection of instances. A singleton instance will have a position value of
   * {@code 1}.
   * 
   * @param instance
   *          the model instance
   * @param position
   *          a positive integer value designating this instance's position within a collection
   */
  protected AbstractModelPositionalPathSegment(IAssemblyPathSegment parent, INSTANCE instance, int position) {
    Objects.requireNonNull(instance, "instance");
    this.parent = parent;
    this.instance = instance;
    this.position = position;
  }

  @Override
  public INSTANCE getInstance() {
    return instance;
  }

  @Override
  public int getPosition() {
    return position;
  }

  @Override
  public String getName() {
    return getInstance().getEffectiveName();
  }

  @Override
  public IModelPositionalPathSegment getParent() {
    return parent;
  }

  @Override
  public Stream<IPathSegment> getPathStream() {
    return parent == null ? Stream.of(this) : Stream.concat(parent.getPathStream(), Stream.of(this));
  }
}
