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

package gov.nist.secauto.metaschema.binding.metapath.type;

import gov.nist.secauto.metaschema.model.common.metapath.evaluate.context.IPathSegment;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.context.MetapathFormatter;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.context.RootPathSegment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TerminalNodeItem extends AbstractNodeItem {

  private final List<IPathSegment> precedingPathSegments;

  public TerminalNodeItem(Object value, RootPathSegment segment) {
    this(value, segment, Collections.emptyList());
  }

  public TerminalNodeItem(Object value, IPathSegment segment, List<IPathSegment> precedingSegments) {
    super(value, segment);
    Objects.requireNonNull(precedingSegments, "precedingSegments");
    this.precedingPathSegments = Collections.unmodifiableList(new ArrayList<>(precedingSegments));
  }

  public List<IPathSegment> getPrecedingPathSegments() {
    return precedingPathSegments;
  }

  @Override
  public List<IPathSegment> getPath() {
    return getPathStream().collect(Collectors.toList());
  }

  @Override
  public List<INodeItem> getNodePath() {
    return getNodeItemStream().collect(Collectors.toList());
  }

  @Override
  public Stream<IPathSegment> getPathStream() {
    return Stream.concat(getPrecedingPathSegments().stream(), Stream.of(getPathSegment()));
  }

  @Override
  public Stream<? extends INodeItem> getNodeItemStream() {
    return Stream.of(this);
  }

  @Override
  public String toString() {
    return MetapathFormatter.instance().format(getPath());
  }

  @Override
  public INodeItem getParent() {
    return null;
  }

  @Override
  public boolean isRootNode() {
    return getPathSegment() instanceof RootPathSegment;
  }
}
