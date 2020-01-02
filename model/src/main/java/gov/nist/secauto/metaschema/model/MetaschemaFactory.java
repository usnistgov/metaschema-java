package gov.nist.secauto.metaschema.model;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import gov.nist.secauto.metaschema.model.xml.XmlMetaschema;

public class MetaschemaFactory {

	private MetaschemaFactory() {
		// disable construction
	}

	public static XmlMetaschema loadMetaschemaFromXml(File file) throws MetaschemaException, IOException {
		return loadMetaschemaFromXml(file.toURI().toURL());
	}

	public static XmlMetaschema loadMetaschemaFromXml(URL resource) throws MetaschemaException, IOException {
		try {
			return XmlMetaschema.loadMetaschema(resource.toURI());
		} catch (URISyntaxException e) {
			// this should not happen
			throw new RuntimeException(e);
		}
	}
}
