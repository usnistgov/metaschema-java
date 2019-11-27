package gov.nist.secauto.metaschema.codegen;

import java.io.PrintWriter;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.secauto.metaschema.codegen.context.model.ListModelInstanceContext;
import gov.nist.secauto.metaschema.codegen.context.model.MapModelInstanceContext;
import gov.nist.secauto.metaschema.codegen.context.model.ModelInstanceContext;
import gov.nist.secauto.metaschema.codegen.context.model.ModelItemInstanceContext;
import gov.nist.secauto.metaschema.codegen.context.model.SimpleModelInstanceContext;
import gov.nist.secauto.metaschema.codegen.context.model.item.AssemblyItemInstance;
import gov.nist.secauto.metaschema.codegen.context.model.item.DataTypeItemInstance;
import gov.nist.secauto.metaschema.codegen.context.model.item.FieldItemInstance;
import gov.nist.secauto.metaschema.codegen.type.DataType;
import gov.nist.secauto.metaschema.model.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.AssemblyInstance;
import gov.nist.secauto.metaschema.model.ChoiceInstance;
import gov.nist.secauto.metaschema.model.FieldInstance;
import gov.nist.secauto.metaschema.model.InfoElementInstance;
import gov.nist.secauto.metaschema.model.JsonGroupBehavior;
import gov.nist.secauto.metaschema.model.ModelContainer;
import gov.nist.secauto.metaschema.model.ModelInstance;

public class AssemblyClassGenerator extends AbstractClassGenerator<AssemblyDefinition> {
	private static final Logger logger = LogManager.getLogger(AssemblyClassGenerator.class);

	public AssemblyClassGenerator(AssemblyDefinition definition) {
		super(definition);
		
		processModel(getDefinition());
	}

	private void processModel(ModelContainer model) {
		for (InfoElementInstance instance : model.getInstances()) {
			if (instance instanceof ChoiceInstance) {
				processModel((ChoiceInstance)instance);
				continue;
			}

			// else the instance is a model instance with a named instance
			ModelInstance modelInstance = (ModelInstance)instance;
			newModelInstance(modelInstance);
		}
	}

	public ModelInstanceContext newModelInstance(ModelInstance instance) {
		ModelItemInstanceContext itemInstanceContext;
		if (instance instanceof FieldInstance) {
			FieldInstance fieldInstance = (FieldInstance)instance;
			if (fieldInstance.getDefinition().getFlagInstances().isEmpty()) {
				// this is a simple value
				itemInstanceContext = new DataTypeItemInstance(fieldInstance, this, DataType.lookupByDatatype(fieldInstance.getDatatype()));
			} else {
				itemInstanceContext = new FieldItemInstance(fieldInstance, this);
			}
		} else if (instance instanceof AssemblyInstance) {
			itemInstanceContext = new AssemblyItemInstance((AssemblyInstance)instance, this);
		} else {
			String msg = String.format("Unknown model instance type: %s", instance.getClass().getCanonicalName());
			logger.error(msg);
			throw new RuntimeException(msg);
		}

		ModelInstanceContext context;
		if (instance.getMaxOccurs() > 1) {
			if (JsonGroupBehavior.KEYED.equals(instance.getGroupBehaviorJson())) {
				context = new MapModelInstanceContext(itemInstanceContext, this);
			} else {
				context = new ListModelInstanceContext(itemInstanceContext, this);
			}
		} else {
			context = new SimpleModelInstanceContext(itemInstanceContext, this);
		}
		
		addInstance(context);
		return context;
	}

	@Override
	protected void writeClassJava(PrintWriter writer) {
		AssemblyDefinition definition = getDefinition();
		if (Objects.equals(definition, definition.getContainingMetaschema().getRootAssemblyDefinition())) {
			writer.printf("@JsonRootName(value=\"%s\", namespace=\"%s\")%n", getDefinition().getName(), getDefinition().getContainingMetaschema().getXmlNamespace());
			writer.println("@JsonIgnoreProperties({ \"$schema\" })");
			writer.printf("@XmlRootElement(name=\"%s\", namespace=\"%s\")%n", getDefinition().getName(), getDefinition().getContainingMetaschema().getXmlNamespace());
		}
		super.writeClassJava(writer);
	}

}