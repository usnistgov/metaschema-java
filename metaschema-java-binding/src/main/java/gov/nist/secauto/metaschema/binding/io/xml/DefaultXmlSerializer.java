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

package gov.nist.secauto.metaschema.binding.io.xml;

import com.ctc.wstx.api.WstxOutputProperties;
import com.ctc.wstx.stax.WstxOutputFactory;

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.io.AbstractSerializer;
import gov.nist.secauto.metaschema.binding.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.RootAssemblyDefinition;

import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

public class DefaultXmlSerializer<CLASS>
    extends AbstractSerializer<CLASS> {
  private XMLOutputFactory2 xmlOutputFactory;

  public DefaultXmlSerializer(@NotNull IBindingContext bindingContext, @NotNull IAssemblyClassBinding classBinding) {
    super(bindingContext, classBinding);
  }

  protected XMLOutputFactory2 getXMLOutputFactory() {
    synchronized (this) {
      if (xmlOutputFactory == null) {
        xmlOutputFactory = (XMLOutputFactory2) WstxOutputFactory.newInstance();
        xmlOutputFactory.configureForSpeed();
        xmlOutputFactory.setProperty(WstxOutputProperties.P_USE_DOUBLE_QUOTES_IN_XML_DECL, true);
        xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
      }
      return xmlOutputFactory;
    }
  }

  protected void setXMLOutputFactory(@NotNull XMLOutputFactory2 xmlOutputFactory) {
    synchronized (this) {
      this.xmlOutputFactory = xmlOutputFactory;
    }
  }

  protected XMLStreamWriter2 newXMLStreamWriter(@NotNull Writer writer) throws IOException {
    try {
      XMLStreamWriter2 streamWriter = (XMLStreamWriter2) getXMLOutputFactory().createXMLStreamWriter(writer);
      streamWriter = new IndentingXmlStreamWriter2(streamWriter);
      return streamWriter;
    } catch (XMLStreamException ex) {
      throw new IOException(ex);
    }
  }

  @Override
  public void serialize(CLASS data, Writer writer) throws IOException {
    XMLStreamWriter2 streamWriter = newXMLStreamWriter(writer);
    IOException caughtException = null;
    IAssemblyClassBinding classBinding = getClassBinding();
    IXmlWritingContext writingContext = new DefaultXmlWritingContext(streamWriter);

    RootAssemblyDefinition root = new RootAssemblyDefinition(classBinding);

    try {
      root.writeRoot(data, writingContext);
      streamWriter.flush();
    } catch (XMLStreamException ex) {
      caughtException = new IOException(ex);
      throw caughtException;
    } finally { // NOPMD - exception handling is needed
      try {
        streamWriter.close();
      } catch (XMLStreamException ex) {
        if (caughtException == null) {
          throw new IOException(ex);
        }
        caughtException.addSuppressed(ex);
        throw caughtException; // NOPMD - intentional
      }
    }
  }
}
