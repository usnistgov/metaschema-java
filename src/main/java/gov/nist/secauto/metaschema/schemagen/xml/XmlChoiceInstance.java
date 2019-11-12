package gov.nist.secauto.metaschema.schemagen.xml;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import gov.nist.csrc.ns.oscal.metaschema.x10.AssemblyDocument;
import gov.nist.csrc.ns.oscal.metaschema.x10.ChoiceDocument;
import gov.nist.csrc.ns.oscal.metaschema.x10.FieldDocument;
import gov.nist.secauto.metaschema.schemagen.AbstractChoiceInstance;
import gov.nist.secauto.metaschema.schemagen.AssemblyDefinition;
import gov.nist.secauto.metaschema.schemagen.InfoElementInstance;

public class XmlChoiceInstance extends AbstractChoiceInstance {
	private final ChoiceDocument.Choice xChoice;
	private final Map<String, InfoElementInstance> namedModelInstances;
	private final Map<String, XmlFieldInstance> fieldInstances;
	private final Map<String, XmlAssemblyInstance> assemblyInstances;


	public XmlChoiceInstance(ChoiceDocument.Choice xChoice, AssemblyDefinition containingAssembly) {
		super(containingAssembly);
		this.xChoice = xChoice;

		XmlCursor cursor = xChoice.newCursor();
		cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';" +
				"$this/m:assembly|$this/m:field");

		Map<String, InfoElementInstance> infoElementInstances = new LinkedHashMap<>();
		Map<String, XmlFieldInstance> fieldInstances = new LinkedHashMap<>();
		Map<String, XmlAssemblyInstance> assemblyInstances = new LinkedHashMap<>();
		while (cursor.toNextSelection()) {
			XmlObject obj = cursor.getObject();
			if (obj instanceof FieldDocument.Field) {
				XmlFieldInstance field = new XmlFieldInstance((FieldDocument.Field) obj, this.getContainingDefinition());
				fieldInstances.put(field.getName(), field);
				infoElementInstances.put(field.getName(), field);
			} else if (obj instanceof AssemblyDocument.Assembly) {
				XmlAssemblyInstance assembly = new XmlAssemblyInstance((AssemblyDocument.Assembly) obj, this.getContainingDefinition());
				assemblyInstances.put(assembly.getName(), assembly);
				infoElementInstances.put(assembly.getName(), assembly);
			}
		}

		this.namedModelInstances = infoElementInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(infoElementInstances);
		this.fieldInstances = fieldInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(fieldInstances);
		this.assemblyInstances = assemblyInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(assemblyInstances);
	}

	@Override
	public Map<String, InfoElementInstance> getNamedModelInstances() {
		return namedModelInstances;
	}

	@Override
	public Map<String, XmlFieldInstance> getFieldInstances() {
		return fieldInstances;
	}

	@Override
	public Map<String,XmlAssemblyInstance> getAssemblyInstances() {
		return assemblyInstances;
	}

	@Override
	public List<InfoElementInstance> getModelInstances() {
		return namedModelInstances.values().stream().collect(Collectors.toList());
	}

}
