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

package gov.nist.secauto.metaschema.binding.io;

import gov.nist.secauto.metaschema.model.common.metapath.IDocumentLoader;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IValuedNodeItem;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public interface IBoundLoader extends IDocumentLoader, MutableConfiguration {
  @NotNull
  Format detectFormat(@NotNull URL url) throws IOException;

  @NotNull
  Format detectFormat(@NotNull File file) throws FileNotFoundException, IOException;

  @NotNull
  Format detectFormat(@NotNull InputStream is) throws IOException;

  @NotNull
  <CLASS> CLASS load(@NotNull URL url) throws IOException;

  @NotNull
  <CLASS> CLASS load(@NotNull File file) throws FileNotFoundException, IOException;

  @NotNull
  <CLASS> CLASS load(@NotNull InputStream is, @NotNull URI documentUri) throws IOException;

  /**
   * Load the specified data file as the specified Java class.
   * 
   * @param <CLASS>
   *          the Java type to load data into
   * @param clazz
   *          the class for the java type
   * @param file
   *          the file to load
   * @return the loaded instance data
   * @throws IOException
   *           if an error occurred while loading the data in the specified file
   * @throws FileNotFoundException
   *           if the specified file does not exist
   */
  @NotNull
  <CLASS> CLASS load(@NotNull Class<CLASS> clazz, @NotNull File file) throws FileNotFoundException, IOException;

  @NotNull
  <CLASS> CLASS load(@NotNull Class<CLASS> clazz, @NotNull URL url) throws IOException;

  @NotNull
  <CLASS> CLASS load(@NotNull Class<CLASS> clazz, @NotNull InputStream is, @NotNull URI documentUri) throws IOException;

  @NotNull
  public static <CLASS> CLASS toBoundObject(@NotNull INodeItem nodeItem) {
    IAssemblyNodeItem result = toAssemblyNodeItem(nodeItem);
    @SuppressWarnings("unchecked") CLASS retval = (CLASS) result.getValue();
    return retval;
  }

  @NotNull
  public static IAssemblyNodeItem toAssemblyNodeItem(@NotNull INodeItem nodeItem) {
    IAssemblyNodeItem retval;
    if (nodeItem instanceof IDocumentNodeItem) {
      retval = ((IDocumentNodeItem) nodeItem).getRootAssemblyNodeItem();
    } else if (nodeItem instanceof IAssemblyNodeItem) {
      retval = (IAssemblyNodeItem) nodeItem;
    } else {
      throw new IllegalArgumentException(
          String.format("The node item type '%s' cannot be cast to an assembly node item.", nodeItem.getItemName()));
    }
    return retval;
  }

}
