package gov.nist.secauto.metaschema.schemagen.xml;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import gov.nist.csrc.ns.oscal.metaschema.x10.DefineAssemblyDocument;
import gov.nist.csrc.ns.oscal.metaschema.x10.DefineFieldDocument;
import gov.nist.csrc.ns.oscal.metaschema.x10.DefineFlagDocument;
import gov.nist.csrc.ns.oscal.metaschema.x10.ImportDocument;
import gov.nist.csrc.ns.oscal.metaschema.x10.METASCHEMADocument;
import gov.nist.secauto.metaschema.schemagen.AbstractMetaschema;
import gov.nist.secauto.metaschema.schemagen.InfoElementDefinition;
import gov.nist.secauto.metaschema.schemagen.Metaschema;
import gov.nist.secauto.metaschema.schemagen.MetaschemaException;

public class XmlMetaschema extends AbstractMetaschema {

	public static XmlMetaschema loadMetaschema(URI resource, Stack<URI> visitedMetaschema) throws MetaschemaException, IOException {
		// first check if the current Metaschema has been visited to prevent cycles
		if (visitedMetaschema.contains(resource)) {
			throw new MetaschemaException("Cycle detected in metaschema includes for '" + resource + "'. Call stack: '"
					+ visitedMetaschema.stream().map(n -> n.toString()).collect(Collectors.joining(",")));
		}

		// parse this metaschema
		METASCHEMADocument metaschemaXml;
		try {
			metaschemaXml = METASCHEMADocument.Factory.parse(resource.toURL());
		} catch (MalformedURLException e) {
			throw new MetaschemaException(e);
		} catch (XmlException e) {
			throw new MetaschemaException(e);
		}
		
		// now check if this Metaschema imports other metaschema
		int size = metaschemaXml.getMETASCHEMA().sizeOfImportArray();
		Map<URI, Metaschema> importedMetaschema;
		if (size == 0) {
			importedMetaschema = Collections.emptyMap();
		} else {
			visitedMetaschema.push(resource);
			try {
				importedMetaschema = new LinkedHashMap<>();
				for (ImportDocument.Import imported : metaschemaXml.getMETASCHEMA().getImportList()) {
					URI importedResource = URI.create(imported.getHref());
					importedResource = resource.resolve(importedResource);
					importedMetaschema.put(importedResource, loadMetaschema(importedResource, visitedMetaschema));
				}
			} finally {
				visitedMetaschema.pop();
			}
		}

		// now create this metaschema
		return new XmlMetaschema(resource, metaschemaXml, importedMetaschema);
	}

	private final METASCHEMADocument metaschema;
	private final Map<URI, Metaschema> importedMetaschema;
	private final Map<String, InfoElementDefinition> infoElementDefinitions;
	private final Map<String, XmlFlagDefinition> flagDefinitions;
	private final Map<String, XmlFieldDefinition> fieldDefinitions;
	private final Map<String, XmlAssemblyDefinition> assemblyDefinitions;


	public XmlMetaschema(URI resource, METASCHEMADocument metaschemaXml, Map<URI, Metaschema> importedMetaschema) throws MetaschemaException {
		super(resource, importedMetaschema);
		this.metaschema = metaschemaXml;
		this.importedMetaschema = Collections.unmodifiableMap(importedMetaschema);

		XmlCursor cursor = metaschema.getMETASCHEMA().newCursor();
		cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';" +
//				"$this/(define-assembly|define-field|define-flag)");
				"$this/m:define-assembly|$this/m:define-field|$this/m:define-flag");

		// handle definitions in this metaschema
		Map<String, InfoElementDefinition> infoElementDefinitions = new LinkedHashMap<>();
		Map<String, XmlFlagDefinition> flagDefinitions = new LinkedHashMap<>();
		Map<String, XmlFieldDefinition> fieldDefinitions = new LinkedHashMap<>();
		Map<String, XmlAssemblyDefinition> assemblyDefinitions = new LinkedHashMap<>();

		while (cursor.toNextSelection()) {
			XmlObject obj = cursor.getObject();
			if (obj instanceof DefineFlagDocument.DefineFlag) {
				XmlFlagDefinition flag = new XmlFlagDefinition((DefineFlagDocument.DefineFlag) obj, this);
				flagDefinitions.put(flag.getName(), flag);
				infoElementDefinitions.put(flag.getName(), flag);
			} else if (obj instanceof DefineFieldDocument.DefineField) {
				XmlFieldDefinition field = new XmlFieldDefinition((DefineFieldDocument.DefineField) obj, this);
				fieldDefinitions.put(field.getName(), field);
				infoElementDefinitions.put(field.getName(), field);
			} else if (obj instanceof DefineAssemblyDocument.DefineAssembly) {
				XmlAssemblyDefinition assembly = new XmlAssemblyDefinition((DefineAssemblyDocument.DefineAssembly) obj, this);
				assemblyDefinitions.put(assembly.getName(), assembly);
				infoElementDefinitions.put(assembly.getName(), assembly);
			}
		}
		this.flagDefinitions = flagDefinitions.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(flagDefinitions);
		this.fieldDefinitions = fieldDefinitions.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(fieldDefinitions);
		this.assemblyDefinitions = assemblyDefinitions.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(assemblyDefinitions);
		this.infoElementDefinitions = infoElementDefinitions.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(infoElementDefinitions);

		parseUsedDefinitions();
	}

	public Map<URI, Metaschema> getImportedMetaschema() {
		return importedMetaschema;
	}

	@Override
	public String getShortName() {
		return metaschema.getMETASCHEMA().getShortName().toString();
	}

	@Override
	public Map<String, InfoElementDefinition> getInfoElementDefinitions() {
		return infoElementDefinitions;
	}

	@Override
	public Map<String, XmlAssemblyDefinition> getAssemblyDefinitions() {
		return assemblyDefinitions;
	}

	@Override
	public Map<String, XmlFieldDefinition> getFieldDefinitions() {
		return fieldDefinitions;
	}

	@Override
	public Map<String, XmlFlagDefinition> getFlagDefinitions() {
		return flagDefinitions;
	}

	@Override
	public XmlAssemblyDefinition getRootAssemblyDefinition() {
		return getAssemblyDefinitions().get(metaschema.getMETASCHEMA().getRoot());
	}
}
