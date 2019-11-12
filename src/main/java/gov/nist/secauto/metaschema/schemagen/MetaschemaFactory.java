package gov.nist.secauto.metaschema.schemagen;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Stack;

import org.apache.xmlbeans.XmlException;

import gov.nist.secauto.metaschema.schemagen.xml.XmlMetaschema;

public class MetaschemaFactory {
	public static void main(String[] args) throws XmlException, IOException, MetaschemaException {
		File metaschemaFile = new File("target/src/metaschema/oscal_catalog_metaschema.xml");
		Metaschema metaschema = loadMetaschemaFromXml(metaschemaFile);
		metaschema.getRootAssemblyDefinition();
//
//		for (metaschema.getU.getInfoElements()) {
//			System.out.println(infoElement.getContainingMetaschema().getLocation()+": "+infoElement.getName());
//			if (informationElements.containsKey(entry.getKey())) {
//				System.out.println("Name clash for information element: "+entry.getKey());
//	//			throw new MetaschemaException("Name clash for information element: "+entry.getKey());
//			} else {
//				informationElements.put(entry.getKey(), entry.getValue());
//			}
//		}
	}


	private MetaschemaFactory() {
		// disable construction
	}

	public static XmlMetaschema loadMetaschemaFromXml(File file) throws MetaschemaException, IOException {
		return loadMetaschemaFromXml(file.toURI().toURL());
	}

	public static XmlMetaschema loadMetaschemaFromXml(URL resource) throws MetaschemaException, IOException {
		try {
			return XmlMetaschema.loadMetaschema(resource.toURI(), new Stack<>());
		} catch (URISyntaxException e) {
			// this should not happen
			throw new RuntimeException(e);
		}
	}
}
