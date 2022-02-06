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

package gov.nist.secauto.metaschema.model.xml;

import gov.nist.secauto.metaschema.model.definitions.IXmlNamedModelDefinition;
import gov.nist.secauto.metaschema.model.instances.IXmlFlagInstance;
import gov.nist.secauto.metaschema.model.xml.XmlLocalAssemblyDefinition.InternalAssemblyDefinition;
import gov.nist.secauto.metaschema.model.xml.XmlLocalFieldDefinition.InternalFieldDefinition;
import gov.nist.secauto.metaschema.model.xmlbeans.FlagDocument;
import gov.nist.secauto.metaschema.model.xmlbeans.GlobalAssemblyDefinitionType;
import gov.nist.secauto.metaschema.model.xmlbeans.GlobalFieldDefinitionType;
import gov.nist.secauto.metaschema.model.xmlbeans.LocalAssemblyDefinitionType;
import gov.nist.secauto.metaschema.model.xmlbeans.LocalFieldDefinitionType;
import gov.nist.secauto.metaschema.model.xmlbeans.LocalFlagDefinitionType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class XmlFlagContainerSupport {

  @NotNull
  private final Map<@NotNull String, IXmlFlagInstance> flagInstances;

  public XmlFlagContainerSupport(
      @NotNull GlobalFieldDefinitionType xmlField,
      @NotNull IXmlNamedModelDefinition container) {
    // handle flags
    if (xmlField.getFlagList().size() > 0 || xmlField.getDefineFlagList().size() > 0) {
      this.flagInstances = parseLocalFlags(xmlField, container);
    } else {
      @SuppressWarnings("null")
      @NotNull
      Map<@NotNull String, IXmlFlagInstance> flags = Collections.emptyMap();
      this.flagInstances = flags;
    }
  }

  public XmlFlagContainerSupport(
      @NotNull GlobalAssemblyDefinitionType xmlAssembly,
      @NotNull IXmlNamedModelDefinition container) {
    // handle flags
    if (xmlAssembly.getFlagList().size() > 0 || xmlAssembly.getDefineFlagList().size() > 0) {
      this.flagInstances = parseLocalFlags(xmlAssembly, container);
    } else {
      @SuppressWarnings("null")
      @NotNull
      Map<@NotNull String, IXmlFlagInstance> flags = Collections.emptyMap();
      this.flagInstances = flags;
    }
  }

  public XmlFlagContainerSupport(
      @NotNull LocalAssemblyDefinitionType xmlAssembly,
      @NotNull InternalAssemblyDefinition container) {
    // handle flags
    if (xmlAssembly.getFlagList().size() > 0 || xmlAssembly.getDefineFlagList().size() > 0) {
      this.flagInstances = parseLocalFlags(xmlAssembly, container);
    } else {
      @SuppressWarnings("null")
      @NotNull
      Map<@NotNull String, IXmlFlagInstance> flags = Collections.emptyMap();
      this.flagInstances = flags;
    }
  }

  public XmlFlagContainerSupport(
      @NotNull LocalFieldDefinitionType xmlField,
      @NotNull InternalFieldDefinition container) {
    // handle flags
    if (xmlField.getFlagList().size() > 0 || xmlField.getDefineFlagList().size() > 0) {
      this.flagInstances = parseLocalFlags(xmlField, container);
    } else {
      @SuppressWarnings("null")
      @NotNull
      Map<@NotNull String, IXmlFlagInstance> flags = Collections.emptyMap();
      this.flagInstances = flags;
    }
  }

  @NotNull
  public Map<@NotNull String, ? extends IXmlFlagInstance> getFlagInstanceMap() {
    return flagInstances;
  }

  @NotNull
  public static Map<@NotNull String, IXmlFlagInstance> parseLocalFlags(@NotNull XmlObject xmlObject,
      @NotNull IXmlNamedModelDefinition parent) {
    // handle flags
    XmlCursor cursor = xmlObject.newCursor();
    cursor.selectPath(
        "declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';" + "$this/m:flag|$this/m:define-flag");

    Map<@NotNull String, IXmlFlagInstance> flagInstances = new LinkedHashMap<>();
    while (cursor.toNextSelection()) {
      XmlObject obj = cursor.getObject();
      if (obj instanceof FlagDocument.Flag) {
        IXmlFlagInstance flagInstance = new XmlFlagInstance((FlagDocument.Flag) obj, parent);
        flagInstances.put(flagInstance.getEffectiveName(), flagInstance);
      } else if (obj instanceof LocalFlagDefinitionType) {
        IXmlFlagInstance flagInstance = new XmlLocalFlagDefinition((LocalFlagDefinitionType) obj, parent);
        flagInstances.put(flagInstance.getEffectiveName(), flagInstance);
      }
    }

    @SuppressWarnings("null")
    @NotNull
    Map<@NotNull String, IXmlFlagInstance> retval
        = flagInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(flagInstances);
    return retval;
  }
}
