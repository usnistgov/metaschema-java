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

package gov.nist.secauto.metaschema.databind.test.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.umd.cs.findbugs.annotations.NonNull;

public class CloseDetectingInputStream
    extends InputStream {

  private final InputStream delegate;
  private boolean closed;

  /**
   * Create a new input stream that will proxy calls to the provided
   * {@code delegate}.
   *
   * @param delegate
   *          the underlying input stream
   */
  public CloseDetectingInputStream(@NonNull InputStream delegate) {
    this.delegate = delegate;
  }

  /**
   * Indicates if {@link #close()} has been called.
   *
   * @return {@code true} if {@link #close()} has been called, or {@code false}
   *         otherwise
   */
  public boolean isClosed() {
    return closed;
  }

  @Override
  public int read() throws IOException {
    return delegate.read();
  }

  @Override
  public int read(byte[] byteArray) throws IOException {
    return delegate.read(byteArray);
  }

  @Override
  public int read(byte[] byteArray, int off, int len) throws IOException {
    return delegate.read(byteArray, off, len);
  }

  @Override
  public byte[] readAllBytes() throws IOException {
    return delegate.readAllBytes();
  }

  @Override
  public byte[] readNBytes(int len) throws IOException {
    return delegate.readNBytes(len);
  }

  @Override
  public int readNBytes(byte[] byteArray, int off, int len) throws IOException {
    return delegate.readNBytes(byteArray, off, len);
  }

  @Override
  public long skip(long numBytes) throws IOException {
    return delegate.skip(numBytes);
  }

  @Override
  public int available() throws IOException {
    return delegate.available();
  }

  @Override
  public void close() throws IOException {
    delegate.close();
    closed = true;
  }

  @Override
  public synchronized void mark(int readlimit) {
    delegate.mark(readlimit);
  }

  @Override
  public synchronized void reset() throws IOException {
    delegate.reset();
  }

  @Override
  public boolean markSupported() {
    return delegate.markSupported();
  }

  @Override
  public long transferTo(OutputStream out) throws IOException {
    return delegate.transferTo(out);
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return delegate.equals(obj);
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

}
