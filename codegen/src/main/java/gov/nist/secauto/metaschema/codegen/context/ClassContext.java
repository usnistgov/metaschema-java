package gov.nist.secauto.metaschema.codegen.context;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.secauto.metaschema.codegen.AbstractClassGenerator;
import gov.nist.secauto.metaschema.codegen.FieldClassGenerator;
import gov.nist.secauto.metaschema.codegen.FlagInstanceGenerator;
import gov.nist.secauto.metaschema.codegen.ModelInstanceGenerator;

public class ClassContext {
	private static final Logger logger = LogManager.getLogger(ClassContext.class);

	private final AbstractClassGenerator classGenerator;
	private Map<String, AbstractInstanceContext<?>> instanceNameToContextMap = new LinkedHashMap<>();

	public ClassContext(AbstractClassGenerator classGenerator) {
		this.classGenerator = classGenerator;
	}

	protected AbstractClassGenerator getClassGenerator() {
		return classGenerator;
	}

	private void addInstance(AbstractInstanceContext<?> context) {
		String name = context.getPropertyName();
		AbstractInstanceContext<?> oldContext = instanceNameToContextMap.put(name, context);
		if (oldContext != null) {
			logger.error("Unexpected duplicate instance property name '{}'", name);
			throw new RuntimeException(String.format("Unexpected duplicate instance property name '%s'", name));
		}
	}

	public FlagInstanceContext newFlagInstance(FlagInstanceGenerator flag) {
		FlagInstanceContext context = new FlagInstanceContext(flag, this);
		addInstance(context);
		return context;
	}

	public FieldInstanceContext newFieldInstance(FieldClassGenerator field) {
		FieldInstanceContext context = new FieldInstanceContext(field, this);
		addInstance(context);
		return context;
	}

	public ModelInstanceContext newModelInstance(ModelInstanceGenerator modelInstance) {
		ModelInstanceContext context = new ModelInstanceContext(modelInstance, this);
		addInstance(context);
		return context;
	}

	public boolean hasInstanceWithName(String newName) {
		return instanceNameToContextMap.containsKey(newName);
	}

	public Collection<AbstractInstanceContext<?>> getInstanceContexts() {
		return instanceNameToContextMap.values();
	}

	public Object getClassName() {
		return getClassGenerator().getClassName();
	}

	public void writeClass(PrintWriter writer) {

		// Handle Imports
		Set<String> imports = new HashSet<>();
		for (AbstractInstanceContext<?> instance : getInstanceContexts()) {
			imports.addAll(instance.getImports());
		}
		imports.addAll(getAdditionalImports());

		if (!imports.isEmpty()) {
			// sort
			imports = imports.stream().sorted((String s1,String s2)->{       
			    return s1.compareTo(s2);
			}).collect(Collectors.toCollection(LinkedHashSet::new));;

			for (String importEntry : imports) {
				if (!importEntry.startsWith("java.lang.")) {
					writer.printf("import %s;%n", importEntry);
				}
			}
			writer.println();
		}
		
		writer.printf("public class %s {%n", getClassName());

		// handle instance variables
		for (AbstractInstanceContext<?> instance : getInstanceContexts()) {
			instance.writeVariable(writer);
		}
		
		writeOtherVariables(writer);

		writer.println();

		// handle flag getters/setters
		for (AbstractInstanceContext<?> instance : getInstanceContexts()) {
			instance.writeGetter(writer);
			instance.writeSetter(writer);
		}

		writeOtherAccessors(writer);

		writeOtherMethods(writer);
		
		writer.println("}");
	}

    protected Set<String> getAdditionalImports() {
		return Collections.emptySet();
	}

	protected void writeOtherVariables(@SuppressWarnings("unused") PrintWriter writer) { }
    protected void writeOtherAccessors(@SuppressWarnings("unused") PrintWriter writer) { }
    protected void writeOtherMethods(@SuppressWarnings("unused") PrintWriter writer) { }
}
