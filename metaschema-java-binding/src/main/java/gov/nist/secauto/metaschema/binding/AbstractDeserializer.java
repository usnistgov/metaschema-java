/**
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

package gov.nist.secauto.metaschema.binding;

import gov.nist.secauto.metaschema.binding.io.Configuration;
import gov.nist.secauto.metaschema.binding.io.Deserializer;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

abstract class AbstractDeserializer<CLASS> extends AbstractSerializationBase<CLASS> implements Deserializer<CLASS> {

  public AbstractDeserializer(BindingContext bindingContext, AssemblyClassBinding<CLASS> classBinding,
      Configuration configuration) {
    super(bindingContext, classBinding, configuration);
  }

  @Override
  public CLASS deserialize(InputStream in) throws BindingException {
    return deserialize(new InputStreamReader(in));
  }

  @Override
  public CLASS deserialize(File file) throws BindingException {

    try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"))) {
      CLASS retval = deserialize(reader);
      reader.close();
      return retval;
    } catch (IOException ex) {
      throw new BindingException("Unable to open file: " + file.getPath(), ex);
    }
  }

  @Override
  public CLASS deserialize(URL url) throws BindingException {
    try (InputStream in = url.openStream()) {
      CLASS retval = deserialize(in);
      in.close();
      return retval;
    } catch (IOException ex) {
      throw new BindingException("Unable to open url: " + url.toString(), ex);
    }
  }
}
