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

package gov.nist.secauto.metaschema.model.common.metapath;

import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

class StreamSequenceImpl<ITEM_TYPE extends IItem> implements ISequence<ITEM_TYPE> {
  private Stream<ITEM_TYPE> stream;
  private List<ITEM_TYPE> list;

  public StreamSequenceImpl(@NonNull Stream<ITEM_TYPE> stream) {
    Objects.requireNonNull(stream, "stream");
    this.stream = stream;
  }

  @Override
  public boolean isEmpty() {
    return asList().isEmpty();
  }

  @Override
  public List<ITEM_TYPE> asList() {
    synchronized (this) {
      if (list == null) {
        list = asStream().collect(Collectors.toUnmodifiableList());
      }
      assert list != null;
      return list;
    }
  }

  @Override
  public Stream<ITEM_TYPE> asStream() {
    @NonNull
    Stream<ITEM_TYPE> retval;
    synchronized (this) {
      if (list == null) {
        if (stream == null) {
          throw new IllegalStateException("stream is already consumed");
        }
        assert stream != null;
        retval = stream;
        stream = null; // NOPMD - readability
      } else {
        retval = ObjectUtils.notNull(list.stream());
      }
    }
    return retval;
  }

  @Override
  public String toString() {
    return asList().toString();
  }

  @Override
  public int size() {
    return asList().size();
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true; // NOPMD - readability
    }

    if (!(other instanceof ISequence)) {
      return false; // NOPMD - readability
    }

    return asList().equals(((ISequence<?>) other).asList());
  }

  @Override
  public int hashCode() {
    return asList().hashCode();
  }
}
