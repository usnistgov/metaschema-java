package gov.nist.secauto.metaschema.model;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import gov.nist.secauto.metaschema.model.xml.XmlMetaschema;

public class MetaschemaLoader {
	private final Set<XmlMetaschema> loadedMetaschema = new LinkedHashSet<>();
	private final Map<URI, XmlMetaschema> metaschemaCache = new LinkedHashMap<>();

	public MetaschemaLoader() {
	}

	public Set<XmlMetaschema> getLoadedMetaschema() {
		return loadedMetaschema.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(loadedMetaschema);
	}

	protected Map<URI, XmlMetaschema> getMetaschemaCache() {
		return metaschemaCache;
	}

	public XmlMetaschema loadXmlMetaschema(File file) throws MetaschemaException, IOException {
		return loadXmlMetaschema(file.toURI());
	}

	public XmlMetaschema loadXmlMetaschema(URL url) throws MetaschemaException, IOException {
		try {
			URI resource = url.toURI();
			return loadXmlMetaschema(resource);
		} catch (URISyntaxException e) {
			// this should not happen
			throw new RuntimeException(e);
		}
	}

	protected XmlMetaschema loadXmlMetaschema(URI resource) throws MetaschemaException, IOException {
		if (!resource.isAbsolute()) {
			throw new IllegalStateException(String.format("The URI '%s' must be absolute.", resource.toString()));
		}
		return XmlMetaschema.loadMetaschema(resource, new Stack<>(), getMetaschemaCache());
	}
}
