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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IntermediateNodeItem extends AbstractNodeItem {
  private final INodeItem parent;

  public IntermediateNodeItem(Object value, IPathSegment currentPathSegment, INodeItem parentNode) {
    super(value, currentPathSegment);
    Objects.requireNonNull(parentNode,"parentNode");
    this.parent = parentNode;
  }

  @Override
  public INodeItem getParent() {
    return parent;
  }

  @Override
  public List<IPathSegment> getPath() {
    List<IPathSegment> retval;
    INodeItem parent = getParent();
    if (parent == null) {
      retval = List.of(getPathSegment());
    } else {
      retval = getPathStream().collect(Collectors.toList());
    }
    return retval;
  }

  @Override
  public List<INodeItem> getNodePath() {
    List<INodeItem> retval;
    INodeItem parent = getParent();
    if (parent == null) {
      retval = List.of(this);
    } else {
      retval = getNodeItemStream().collect(Collectors.toList());
    }
    return retval;
  }

  @Override
  public Stream<IPathSegment> getPathStream() {
    Stream<IPathSegment> retval;
    INodeItem parent = getParent();
    if (parent == null) {
      retval = Stream.of(getPathSegment()).sequential();
    } else {
      retval = Stream.concat(parent.getPathStream(), Stream.of(getPathSegment()));
    }
    return retval;
  }

  @Override
  public Stream<INodeItem> getNodeItemStream() {
    Stream<INodeItem> retval;
    INodeItem parent = getParent();
    if (parent == null) {
      retval = Stream.of(this);
    } else {
      retval = Stream.concat(parent.getNodeItemStream(), Stream.of(this));
    }
    return retval;
  }

  @Override
  public boolean isRootNode() {
    return false;
  }

}
