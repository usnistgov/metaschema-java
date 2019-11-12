package gov.nist.secauto.metaschema.schemagen.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import gov.nist.csrc.ns.oscal.metaschema.x10.AssemblyDocument;
import gov.nist.csrc.ns.oscal.metaschema.x10.ChoiceDocument;
import gov.nist.csrc.ns.oscal.metaschema.x10.DefineAssemblyDocument;
import gov.nist.csrc.ns.oscal.metaschema.x10.FieldDocument;
import gov.nist.csrc.ns.oscal.metaschema.x10.FlagDocument;
import gov.nist.secauto.metaschema.schemagen.AbstractAssemblyDefinition;
import gov.nist.secauto.metaschema.schemagen.AssemblyDefinition;
import gov.nist.secauto.metaschema.schemagen.ChoiceInstance;
import gov.nist.secauto.metaschema.schemagen.InfoElementInstance;
import gov.nist.secauto.metaschema.schemagen.Type;

public class XmlAssemblyDefinition extends AbstractAssemblyDefinition<XmlMetaschema> implements AssemblyDefinition {
	private final DefineAssemblyDocument.DefineAssembly xAssembly;
	private final Map<String, XmlFlagInstance> flagInstances;
	private final Map<String, InfoElementInstance> namedModelInstances;
	private final Map<String, XmlFieldInstance> fieldInstances;
	private final Map<String, XmlAssemblyInstance> assemblyInstances;
	private final List<InfoElementInstance> modelInstances;

	public XmlAssemblyDefinition(DefineAssemblyDocument.DefineAssembly xAssembly, XmlMetaschema metaschema) {
		super(metaschema);
		this.xAssembly = xAssembly;

		Map<String, InfoElementInstance> infoElementInstances = new LinkedHashMap<>();
		int numFlags = xAssembly.sizeOfFlagArray();
		if (numFlags > 0) {
			Map<String, XmlFlagInstance> flagInstances = new LinkedHashMap<>();
			for (FlagDocument.Flag xFlag : xAssembly.getFlagList()) {
				XmlFlagInstance flagInstance = new XmlFlagInstance(xFlag, this);
				flagInstances.put(flagInstance.getName(), flagInstance);
				infoElementInstances.put(flagInstance.getName(), flagInstance);
			}
			this.flagInstances = Collections.unmodifiableMap(flagInstances);
		} else {
			flagInstances = Collections.emptyMap();
		}

		XmlCursor cursor = xAssembly.getModel().newCursor();
		cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';" +
				"$this/m:assembly|$this/m:field|$this/m:choice");

		Map<String, XmlFieldInstance> fieldInstances = new LinkedHashMap<>();
		Map<String, XmlAssemblyInstance> assemblyInstances = new LinkedHashMap<>();
		List<InfoElementInstance> modelInstances = new ArrayList<>(cursor.getSelectionCount());
		while (cursor.toNextSelection()) {
			XmlObject obj = cursor.getObject();
			if (obj instanceof FieldDocument.Field) {
				XmlFieldInstance field = new XmlFieldInstance((FieldDocument.Field) obj, this);
				fieldInstances.put(field.getName(), field);
				infoElementInstances.put(field.getName(), field);
				modelInstances.add(field);
			} else if (obj instanceof AssemblyDocument.Assembly) {
				XmlAssemblyInstance assembly = new XmlAssemblyInstance((AssemblyDocument.Assembly) obj, this);
				assemblyInstances.put(assembly.getName(), assembly);
				infoElementInstances.put(assembly.getName(), assembly);
				modelInstances.add(assembly);
			} else if (obj instanceof ChoiceDocument.Choice) {
				XmlChoiceInstance choice = new XmlChoiceInstance((ChoiceDocument.Choice) obj, this);
				assemblyInstances.putAll(choice.getAssemblyInstances());
				fieldInstances.putAll(choice.getFieldInstances());
				infoElementInstances.putAll(choice.getNamedModelInstances());
				modelInstances.add(choice);
			}

		}

		this.namedModelInstances = infoElementInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(infoElementInstances);
		this.fieldInstances = fieldInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(fieldInstances);
		this.assemblyInstances = assemblyInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(assemblyInstances);
		this.modelInstances = modelInstances.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(modelInstances);
	}

	@Override
	public String getName() {
		return xAssembly.getName();
	}

	@Override
	public Map<String, XmlFlagInstance> getFlagInstances() {
		return flagInstances;
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
	public Map<String, XmlAssemblyInstance> getAssemblyInstances() {
		return assemblyInstances;
	}

	@Override
	public List<? extends ChoiceInstance> getChoiceInstances() {
		// this shouldn't get called all that often, so this is better than allocating memory
		return modelInstances.stream()
				.filter(p -> Type.CHOICE.equals(p.getType()))
				.map(obj -> (ChoiceInstance) obj)
				.collect(Collectors.toList());
	}

	@Override
	public List<InfoElementInstance> getModelInstances() {
		return modelInstances;
	}
}
