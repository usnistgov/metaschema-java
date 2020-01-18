package gov.nist.secauto.metaschema.model.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import gov.nist.itl.metaschema.model.xml.AssemblyDocument;
import gov.nist.itl.metaschema.model.xml.ChoiceDocument;
import gov.nist.itl.metaschema.model.xml.DefineAssemblyDocument;
import gov.nist.itl.metaschema.model.xml.ExtensionType;
import gov.nist.itl.metaschema.model.xml.FieldDocument;
import gov.nist.itl.metaschema.model.xml.FlagDocument;
import gov.nist.itl.metaschema.model.xml.binding.DefineAssemblyBindingDocument;
import gov.nist.secauto.metaschema.markup.MarkupString;
import gov.nist.secauto.metaschema.model.configuration.AssemblyBindingConfiguration;
import gov.nist.secauto.metaschema.model.info.Type;
import gov.nist.secauto.metaschema.model.info.definitions.AbstractAssemblyDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.info.instances.ChoiceInstance;
import gov.nist.secauto.metaschema.model.info.instances.FlagInstance;
import gov.nist.secauto.metaschema.model.info.instances.InfoElementInstance;
import gov.nist.secauto.metaschema.model.info.instances.ModelInstance;

public class XmlAssemblyDefinition extends AbstractAssemblyDefinition<XmlMetaschema> implements AssemblyDefinition {

	protected static AssemblyBindingConfiguration getBindingConfiguration(DefineAssemblyDocument.DefineAssembly xField) {
		AssemblyBindingConfiguration retval = null;
		if (xField.isSetExtensions()) {
			DefineAssemblyDocument.DefineAssembly.Extensions extensions = xField.getExtensions();
			for (ExtensionType extensionInstance : extensions.getDefineAssemblyExtensionList()) {
				System.out.println("Extension Class: "+extensionInstance.getClass().getName());
				if (extensionInstance instanceof DefineAssemblyBindingDocument.DefineAssemblyBinding) {
					DefineAssemblyBindingDocument.DefineAssemblyBinding modelConfig = (DefineAssemblyBindingDocument.DefineAssemblyBinding)extensionInstance;
					if (modelConfig.isSetJava()) {
						DefineAssemblyBindingDocument.DefineAssemblyBinding.Java modelJava = modelConfig.getJava();

						retval = new AssemblyBindingConfiguration(modelJava.getClassName(), modelJava.getBaseClassName(), modelJava.getInterfaceNameList());
						break;
					}
				}
			}
		}

		if (retval == null) {
			retval = AssemblyBindingConfiguration.NULL_CONFIG;
		}
		return retval;
	}

	private final DefineAssemblyDocument.DefineAssembly xAssembly;
	private final Map<String, XmlFlagInstance> flagInstances;
	private final Map<String, ModelInstance> namedModelInstances;
	private final Map<String, XmlFieldInstance> fieldInstances;
	private final Map<String, XmlAssemblyInstance> assemblyInstances;
	private final List<InfoElementInstance> modelInstances;

	public XmlAssemblyDefinition(DefineAssemblyDocument.DefineAssembly xAssembly, XmlMetaschema metaschema) {
		super(getBindingConfiguration(xAssembly), metaschema);
		this.xAssembly = xAssembly;

		MarkupStringConverter.toMarkupString(getXmlAssembly().getDescription());

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

		this.namedModelInstances = infoElementInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(namedModelInstances);
		this.fieldInstances = fieldInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(fieldInstances);
		this.assemblyInstances = assemblyInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(assemblyInstances);
		this.modelInstances = modelInstances.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(modelInstances);
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
	public MarkupString getDescription() {
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
		return modelInstances.stream()
				.filter(p -> Type.CHOICE.equals(p.getType()))
				.map(obj -> (ChoiceInstance) obj)
				.collect(Collectors.toList());
	}

	@Override
	public List<InfoElementInstance> getInstances() {
		return modelInstances;
	}

	protected DefineAssemblyDocument.DefineAssembly getXmlAssembly() {
		return xAssembly;
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
