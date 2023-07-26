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

import gov.nist.secauto.metaschema.model.common.metapath.item.node.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.node.IFieldNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.node.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.node.IMetaschemaNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.node.IRootAssemblyNodeItem;

import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * This interface provides an implementation contract for all path formatters. When
 * {@link #format(IPathSegment)} is called on a formatter implementation, the formatter will render
 * the path segments based on the implemented path syntax. This allows a collection of path segments
 * to be rendered in different forms by swapping out the formatter used.
 *
 * A path formatter is expected to be stateless and thus thread safe.
 */
public interface IPathFormatter {
  /**
   * A path formatter that produces Metapath-based paths.
   */
  @NonNull
  IPathFormatter METAPATH_PATH_FORMATER = new MetapathFormatter();

  /**
   * Format the path represented by the provided path segment. The provided segment is expected to be
   * the last node in this path. A call to {@link IPathSegment#getPathStream()} or
   * {@link IPathSegment#getPath()} can be used to walk the path tree in descending order.
   *
   * @param segment
   *          The last segment in a sequence of path segments
   * @return a formatted path
   * @see IPathSegment#getPathStream()
   * @see IPathSegment#getPath()
   */
  @SuppressWarnings("null")
  @NonNull
  default String format(@NonNull IPathSegment segment) {
    return segment.getPathStream().map(pathSegment -> {
      return pathSegment.format(this);
    }).collect(Collectors.joining("/"));
  }

  /**
   * This visitor callback is used to format an individual flag path segment.
   *
   * @param flag
   *          the node to format
   * @return the formatted text for the segment
   */
  @NonNull
  String formatFlag(@NonNull IFlagNodeItem flag);

  /**
   * This visitor callback is used to format an individual field path segment.
   *
   * @param field
   *          the node to format
   * @return the formatted text for the segment
   */
  @NonNull
  String formatField(@NonNull IFieldNodeItem field);

  /**
   * This visitor callback is used to format an individual assembly path segment.
   *
   * @param assembly
   *          the node to format
   * @return the formatted text for the segment
   */
  @NonNull
  String formatAssembly(@NonNull IAssemblyNodeItem assembly);

  /**
   * This visitor callback is used to format a root assembly path segment.
   *
   * @param root
   *          the node to format
   * @return the formatted text for the segment
   */
  @NonNull
  String formatRootAssembly(@NonNull IRootAssemblyNodeItem root);

  /**
   * This visitor callback is used to format an individual document path segment.
   *
   * @param document
   *          the node to format
   * @return the formatted text for the segment
   */
  @NonNull
  String formatDocument(@NonNull IDocumentNodeItem document);

  /**
   * This visitor callback is used to format an individual metaschema path segment.
   *
   * @param metaschema
   *          the node to format
   * @return the formatted text for the segment
   */
  @NonNull
  String formatMetaschema(@NonNull IMetaschemaNodeItem metaschema);
}
