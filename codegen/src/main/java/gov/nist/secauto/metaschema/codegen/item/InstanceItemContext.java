package gov.nist.secauto.metaschema.codegen.item;

import java.net.URI;

import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.markup.MarkupString;
import gov.nist.secauto.metaschema.model.info.instances.ModelInstance;

public interface InstanceItemContext {
	ModelInstance getModelInstance();
	JavaType getJavaType();
	String getInstanceName();
	URI getXmlNamespace();
	MarkupString getDescription();
}
