package gov.nist.secauto.metaschema.codegen;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.secauto.metaschema.codegen.builder.ClassBuilder;
import gov.nist.secauto.metaschema.codegen.item.AssemblyInstanceItemContext;
import gov.nist.secauto.metaschema.codegen.item.DataTypeInstanceItemContext;
import gov.nist.secauto.metaschema.codegen.item.FieldInstanceItemContext;
import gov.nist.secauto.metaschema.codegen.item.InstanceItemContext;
import gov.nist.secauto.metaschema.codegen.type.DataType;
import gov.nist.secauto.metaschema.datatype.annotations.RootWrapper;
import gov.nist.secauto.metaschema.model.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.AssemblyInstance;
import gov.nist.secauto.metaschema.model.ChoiceInstance;
import gov.nist.secauto.metaschema.model.FieldInstance;
import gov.nist.secauto.metaschema.model.InfoElementInstance;
import gov.nist.secauto.metaschema.model.JsonGroupAsBehavior;
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

	public CardinalityInstanceGenerator newModelInstance(ModelInstance instance) {
		InstanceItemContext itemInstanceContext;
		if (instance instanceof FieldInstance) {
			FieldInstance fieldInstance = (FieldInstance)instance;
			if (fieldInstance.getDefinition().getFlagInstances().isEmpty()) {
				// this is a simple value
				itemInstanceContext = new DataTypeInstanceItemContext(fieldInstance, this, DataType.lookupByDatatype(fieldInstance.getDatatype()));
			} else {
				itemInstanceContext = new FieldInstanceItemContext(fieldInstance, this);
			}
		} else if (instance instanceof AssemblyInstance) {
			itemInstanceContext = new AssemblyInstanceItemContext((AssemblyInstance)instance, this);
		} else {
			String msg = String.format("Unknown model instance type: %s", instance.getClass().getCanonicalName());
			logger.error(msg);
			throw new RuntimeException(msg);
		}

		CardinalityInstanceGenerator context;
		int maxOccurance = instance.getMaxOccurs();
		if (maxOccurance == -1 || maxOccurance > 1) {
			if (JsonGroupAsBehavior.KEYED.equals(instance.getJsonGroupAsBehavior())) {
				context = new MapInstanceGenerator(itemInstanceContext, this);
			} else {
				context = new ListInstanceGenerator(itemInstanceContext, this);
			}
		} else {
			context = new SingletonInstanceGenerator(itemInstanceContext, this);
		}
		
		addInstance(context);
		return context;
	}

	@Override
	protected boolean isRootClass() {
		return Objects.equals(getDefinition(), getDefinition().getContainingMetaschema().getRootAssemblyDefinition());
	}

	@Override
	protected void buildClass(ClassBuilder builder) {
		super.buildClass(builder);

		AssemblyDefinition definition = getDefinition();
		if (Objects.equals(definition, definition.getContainingMetaschema().getRootAssemblyDefinition())) {
			
			StringBuilder instanceBuilder = new StringBuilder();
			instanceBuilder.append("name = \"");
			instanceBuilder.append(getDefinition().getName());
			instanceBuilder.append("\"");

			String namespace = getXmlNamespace().toString();
			String containingNamespace = getDefinition().getContainingMetaschema().getXmlNamespace().toString();
			if (!containingNamespace.equals(namespace)) {
				instanceBuilder.append(", namespace = \"");
				instanceBuilder.append(namespace);
				instanceBuilder.append("\"");
			}

			builder.annotation(RootWrapper.class, instanceBuilder.toString());
		}
	}

}