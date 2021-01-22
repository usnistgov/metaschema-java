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

import gov.nist.itl.metaschema.model.m4.xml.AssemblyDocument;
import gov.nist.itl.metaschema.model.m4.xml.ChoiceDocument;
import gov.nist.itl.metaschema.model.m4.xml.FieldDocument;
import gov.nist.itl.metaschema.model.m4.xml.FlagDocument;
import gov.nist.itl.metaschema.model.m4.xml.LocalAssemblyDefinition;
import gov.nist.itl.metaschema.model.m4.xml.LocalFieldDefinition;
import gov.nist.itl.metaschema.model.m4.xml.LocalFlagDefinition;
import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.definitions.AbstractAssemblyDefinition;
import gov.nist.secauto.metaschema.model.instances.AssemblyInstance;
import gov.nist.secauto.metaschema.model.instances.ChoiceInstance;
import gov.nist.secauto.metaschema.model.instances.FieldInstance;
import gov.nist.secauto.metaschema.model.instances.FlagInstance;
import gov.nist.secauto.metaschema.model.instances.ModelInstance;
import gov.nist.secauto.metaschema.model.instances.ObjectModelInstance;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractXmlAssemblyDefinition<DEF extends AbstractXmlAssemblyDefinition<DEF,
    INSTANCE>, INSTANCE extends AssemblyInstance<DEF>>
    extends AbstractAssemblyDefinition {
  private Map<String, FlagInstance<?>> flagInstances;
  private Map<String, ObjectModelInstance<?>> namedModelInstances;
  private Map<String, FieldInstance<?>> fieldInstances;
  private Map<String, AssemblyInstance<?>> assemblyInstances;
  private List<ModelInstance> modelInstances;

  /**
   * Constructs a new Metaschema assembly definition from an XML representation bound to Java objects.
   * 
   * @param metaschema
   *          the containing Metaschema
   */
  public AbstractXmlAssemblyDefinition(Metaschema metaschema) {
    super(metaschema);
  }

  /**
   * Get the underlying XML data.
   * 
   * @return the underlying XML data
   */
  protected abstract XmlObject getXmlAssembly();

  /**
   * Used to generate the instances for the assembly's model in a lazy fashion when the model is first
   * accessed.
   */
  protected void generateModel() {
    // handle flags
    {
      XmlCursor cursor = getXmlAssembly().newCursor();
      cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';"
          + "$this/m:flag|$this/m:define-flag");

      Map<String, FlagInstance<?>> flagInstances = new LinkedHashMap<>();
      while (cursor.toNextSelection()) {
        XmlObject obj = cursor.getObject();
        if (obj instanceof FlagDocument.Flag) {
          FlagInstance<?> flagInstance = new XmlFlagInstance((FlagDocument.Flag) obj, this);
          flagInstances.put(flagInstance.getUseName(), flagInstance);
        } else if (obj instanceof LocalFlagDefinition) {
          FlagInstance<?> flagInstance = new XmlLocalFlagDefinition((LocalFlagDefinition) obj, this);
          flagInstances.put(flagInstance.getUseName(), flagInstance);
        }
      }

      this.flagInstances
          = flagInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(flagInstances);
    }

    // handle the model
    {
      XmlCursor cursor = getXmlAssembly().newCursor();
      cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';"
          + "$this/m:model/m:assembly|$this/m:model/m:define-assembly|$this/m:model/m:field"
          + "|$this/m:model/m:define-field|$this/m:model/m:choice");

      Map<String, FieldInstance<?>> fieldInstances = new LinkedHashMap<>();
      List<AssemblyInstance<?>> assemblyInstances = new LinkedList<>();
      List<ModelInstance> modelInstances = new ArrayList<>(cursor.getSelectionCount());
      List<ObjectModelInstance<?>> namedModelInstances = new LinkedList<>();

      while (cursor.toNextSelection()) {
        XmlObject obj = cursor.getObject();
        if (obj instanceof FieldDocument.Field) {
          XmlFieldInstance field = new XmlFieldInstance((FieldDocument.Field) obj, this);
          fieldInstances.put(field.getUseName(), field);
          modelInstances.add(field);
          namedModelInstances.add(field);
        } else if (obj instanceof LocalFieldDefinition) {
          XmlLocalFieldDefinition field = new XmlLocalFieldDefinition((LocalFieldDefinition) obj, this);
          fieldInstances.put(field.getUseName(), field);
          modelInstances.add(field);
          namedModelInstances.add(field);
        } else if (obj instanceof AssemblyDocument.Assembly) {
          XmlAssemblyInstance assembly = new XmlAssemblyInstance((AssemblyDocument.Assembly) obj, this);
          assemblyInstances.add(assembly);
          modelInstances.add(assembly);
          namedModelInstances.add(assembly);
        } else if (obj instanceof LocalAssemblyDefinition) {
          XmlLocalAssemblyDefinition assembly = new XmlLocalAssemblyDefinition((LocalAssemblyDefinition) obj, this);
          assemblyInstances.add(assembly);
          modelInstances.add(assembly);
          namedModelInstances.add(assembly);
        } else if (obj instanceof ChoiceDocument.Choice) {
          XmlChoiceInstance choice = new XmlChoiceInstance((ChoiceDocument.Choice) obj, this);
          assemblyInstances.addAll(choice.getAssemblyInstances().values());
          fieldInstances.putAll(choice.getFieldInstances());
          modelInstances.add(choice);
        }
      }

      this.fieldInstances
          = fieldInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(fieldInstances);

      if (assemblyInstances.isEmpty()) {
        this.assemblyInstances = Collections.emptyMap();
      } else {
        this.assemblyInstances = Collections.unmodifiableMap(
            assemblyInstances.stream().collect(Collectors.toMap(AssemblyInstance::getUseName, v -> v)));
      }

      this.modelInstances
          = modelInstances.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(modelInstances);

      if (namedModelInstances.isEmpty()) {
        this.namedModelInstances = Collections.emptyMap();
      } else {
        this.namedModelInstances = Collections.unmodifiableMap(
            namedModelInstances.stream().collect(Collectors.toMap(ObjectModelInstance::getUseName, v -> v)));
      }
    }
  }

  @Override
  public Map<String, FlagInstance<?>> getFlagInstances() {
    synchronized (this) {
      if (flagInstances == null) {
        generateModel();
      }
    }
    return flagInstances;
  }

  @Override
  public Map<String, ObjectModelInstance<?>> getNamedModelInstances() {
    synchronized (this) {
      if (namedModelInstances == null) {
        generateModel();
      }
    }
    return namedModelInstances;
  }

  @Override
  public Map<String, FieldInstance<?>> getFieldInstances() {
    synchronized (this) {
      if (fieldInstances == null) {
        generateModel();
      }
    }
    return fieldInstances;
  }

  @Override
  public Map<String, AssemblyInstance<?>> getAssemblyInstances() {
    synchronized (this) {
      if (assemblyInstances == null) {
        generateModel();
      }
    }
    return assemblyInstances;
  }

  @Override
  public List<ModelInstance> getModelInstances() {
    synchronized (this) {
      if (modelInstances == null) {
        generateModel();
      }
    }
    return modelInstances;
  }

  @Override
  public List<ChoiceInstance> getChoiceInstances() {
    synchronized (this) {
      if (modelInstances == null) {
        generateModel();
      }
    }
    // this shouldn't get called all that often, so this is better than allocating memory
    return modelInstances.stream().filter(obj -> obj instanceof ChoiceInstance)
        .map(obj -> (ChoiceInstance) obj)
        .collect(Collectors.toList());
  }
}
