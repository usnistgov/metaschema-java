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

package gov.nist.secauto.metaschema.model.xml;

import gov.nist.itl.metaschema.model.xml.AssemblyDocument;
import gov.nist.itl.metaschema.model.xml.ChoiceDocument;
import gov.nist.itl.metaschema.model.xml.DefineAssemblyDocument;
import gov.nist.itl.metaschema.model.xml.FieldDocument;
import gov.nist.itl.metaschema.model.xml.FlagDocument;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.info.Type;
import gov.nist.secauto.metaschema.model.info.definitions.AbstractAssemblyDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.info.instances.ChoiceInstance;
import gov.nist.secauto.metaschema.model.info.instances.FlagInstance;
import gov.nist.secauto.metaschema.model.info.instances.InfoElementInstance;
import gov.nist.secauto.metaschema.model.info.instances.ModelInstance;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class XmlAssemblyDefinition extends AbstractAssemblyDefinition<XmlMetaschema> implements AssemblyDefinition {
  private final DefineAssemblyDocument.DefineAssembly xmlAssembly;
  private final Map<String, XmlFlagInstance> flagInstances;
  private final Map<String, ModelInstance> namedModelInstances;
  private final Map<String, XmlFieldInstance> fieldInstances;
  private final Map<String, XmlAssemblyInstance> assemblyInstances;
  private final List<InfoElementInstance> modelInstances;

  /**
   * Constructs a new Metaschema Assembly definition from an XML representation bound to Java objects.
   * 
   * @param xmlAssembly
   *          the XML representation bound to Java objects
   * @param metaschema
   *          the containing Metaschema
   */
  public XmlAssemblyDefinition(DefineAssemblyDocument.DefineAssembly xmlAssembly, XmlMetaschema metaschema) {
    super(metaschema);
    this.xmlAssembly = xmlAssembly;
    //
    // MarkupStringConverter.toMarkupString(getXmlAssembly().getDescription());

    Map<String, InfoElementInstance> infoElementInstances = new LinkedHashMap<>();
    int numFlags = xmlAssembly.sizeOfFlagArray();
    if (numFlags > 0) {
      Map<String, XmlFlagInstance> flagInstances = new LinkedHashMap<>();
      for (FlagDocument.Flag xmlFlag : xmlAssembly.getFlagList()) {
        XmlFlagInstance flagInstance = new XmlFlagInstance(xmlFlag, this);
        flagInstances.put(flagInstance.getName(), flagInstance);
        infoElementInstances.put(flagInstance.getName(), flagInstance);
      }
      this.flagInstances = Collections.unmodifiableMap(flagInstances);
    } else {
      flagInstances = Collections.emptyMap();
    }

    XmlCursor cursor = xmlAssembly.getModel().newCursor();
    cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';"
        + "$this/m:assembly|$this/m:field|$this/m:choice");

    Map<String, XmlFieldInstance> fieldInstances = new LinkedHashMap<>();
    Map<String, XmlAssemblyInstance> assemblyInstances = new LinkedHashMap<>();
    List<InfoElementInstance> modelInstances = new ArrayList<>(cursor.getSelectionCount());
    Map<String, ModelInstance> namedModelInstances = new LinkedHashMap<>();
    while (cursor.toNextSelection()) {
      XmlObject obj = cursor.getObject();
      if (obj instanceof FieldDocument.Field) {
        XmlFieldInstance field = new XmlFieldInstance((FieldDocument.Field) obj, this);
        fieldInstances.put(field.getName(), field);
        infoElementInstances.put(field.getName(), field);
        modelInstances.add(field);
        namedModelInstances.put(field.getName(), field);
      } else if (obj instanceof AssemblyDocument.Assembly) {
        XmlAssemblyInstance assembly = new XmlAssemblyInstance((AssemblyDocument.Assembly) obj, this);
        assemblyInstances.put(assembly.getName(), assembly);
        infoElementInstances.put(assembly.getName(), assembly);
        modelInstances.add(assembly);
        namedModelInstances.put(assembly.getName(), assembly);
      } else if (obj instanceof ChoiceDocument.Choice) {
        XmlChoiceInstance choice = new XmlChoiceInstance((ChoiceDocument.Choice) obj, this);
        assemblyInstances.putAll(choice.getAssemblyInstances());
        fieldInstances.putAll(choice.getFieldInstances());
        infoElementInstances.putAll(choice.getNamedModelInstances());
        modelInstances.add(choice);
      }

    }

    this.namedModelInstances
        = infoElementInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(namedModelInstances);
    this.fieldInstances
        = fieldInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(fieldInstances);
    this.assemblyInstances
        = assemblyInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(assemblyInstances);
    this.modelInstances
        = modelInstances.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(modelInstances);
  }

  @Override
  public String getName() {
    return getXmlAssembly().getName();
  }

  @Override
  public String getFormalName() {
    return getXmlAssembly().getFormalName();
  }

  @Override
  public MarkupLine getDescription() {
    return MarkupStringConverter.toMarkupString(getXmlAssembly().getDescription());
  }

  @Override
  public Map<String, XmlFlagInstance> getFlagInstances() {
    return flagInstances;
  }

  @Override
  public Map<String, ModelInstance> getNamedModelInstances() {
    return namedModelInstances;
  }

  @Override
  public Map<String, XmlFieldInstance> getFieldInstances() {
    return fieldInstances;
  }

  @Override
  public Map<String, XmlAssemblyInstance> getAssemblyInstances() {
    return assemblyInstances;
  }

  @Override
  public List<? extends ChoiceInstance> getChoiceInstances() {
    // this shouldn't get called all that often, so this is better than allocating memory
    return modelInstances.stream().filter(p -> Type.CHOICE.equals(p.getType())).map(obj -> (ChoiceInstance) obj)
        .collect(Collectors.toList());
  }

  @Override
  public List<InfoElementInstance> getInstances() {
    return modelInstances;
  }

  protected DefineAssemblyDocument.DefineAssembly getXmlAssembly() {
    return xmlAssembly;
  }

  @Override
  public boolean hasJsonKey() {
    return getXmlAssembly().isSetJsonKey();
  }

  @Override
  public FlagInstance getJsonKeyFlagInstance() {
    FlagInstance retval = null;
    if (hasJsonKey()) {
      retval = getFlagInstanceByName(getXmlAssembly().getJsonKey().getFlagName());
    }
    return retval;
  }
}
