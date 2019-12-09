package gov.nist.secauto.metaschema.codegen.builder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import gov.nist.secauto.metaschema.codegen.type.JavaType;

public class ClassBuilder extends AbstractBuilder<ClassBuilder>{
	private static final Visibility DEFAULT_VISIBILITY = Visibility.PUBLIC;

	private final JavaType javaType;
	private Map<String, FieldBuilder> fields = new LinkedHashMap<>();
	private List<ConstructorBuilder> constructors = new LinkedList<>();
	private Map<String, MethodBuilder> methods = new LinkedHashMap<>();

	
	public ClassBuilder(JavaType classJavaType) {
		this.javaType = classJavaType;
	}

	public JavaType getJavaType() {
		return javaType;
	}

	public FieldBuilder newFieldBuilder(JavaType javaType, String name) {
		FieldBuilder retval = new FieldBuilder(this, javaType, name);
		fields.put(retval.getName(), retval);
		return retval;
	}

	public ConstructorBuilder newConstructorBuilder() {
		ConstructorBuilder retval = new ConstructorBuilder(this);
		constructors.add(retval);
		return retval;
	}

	public MethodBuilder newMethodBuilder(String name) {
		MethodBuilder retval = new MethodBuilder(this, name);
		methods.put(retval.getName(), retval);
		return retval;
	}

	protected Map<String, FieldBuilder> getFields() {
		return Collections.unmodifiableMap(fields);
	}

	protected List<ConstructorBuilder> getConstructors() {
		return Collections.unmodifiableList(constructors);
	}

	protected Map<String, MethodBuilder> getMethods() {
		return Collections.unmodifiableMap(methods);
	}

	@Override
	public void build(PrintWriter out) throws IOException {

		// package declaration
		out.format("package %s;%n", getJavaType().getPackageName());
		out.println();

		// Handle Imports
		Set<JavaType> imports = new HashSet<>(getImports());
		for (FieldBuilder field : getFields().values()) {
			imports.addAll(field.getImports());
		}
		for (ConstructorBuilder constructor : getConstructors()) {
			imports.addAll(constructor.getImports());
		}
		for (MethodBuilder method : getMethods().values()) {
			imports.addAll(method.getImports());
		}
	
		if (!imports.isEmpty()) {
			// sort
			imports = imports.stream().sorted((JavaType s1,JavaType s2)->{       
			    return s1.getQualifiedClassName().compareTo(s2.getQualifiedClassName());
			}).collect(Collectors.toCollection(LinkedHashSet::new));
	
			JavaType classJavaType = getJavaType();
			boolean hasImport = false;
			for (JavaType importEntry : imports) {
				String importValue = importEntry.getImportValue(classJavaType);
				if (importValue != null && !importValue.startsWith("java.lang.")) {
					out.printf("import %s;%n", importValue);
					hasImport = true;
				}
			}
			if (hasImport) {
				out.println();
			}
		}

		// class declaration
		buildAnnotations(out);
		
		out.printf("%sclass %s {%n", getVisibilityValue(DEFAULT_VISIBILITY), getJavaType().getClassName());

		for (FieldBuilder field : getFields().values()) {
			field.build(out);
			out.println();
		}

		for (ConstructorBuilder constructor : getConstructors()) {
			constructor.build(out);
			out.println();
		}

		for (MethodBuilder method : getMethods().values()) {
			method.build(out);
			out.println();
		}
		
		out.println("}");
		out.flush();
	}

	@Override
	public ClassBuilder getClassBuilder() {
		return this;
	}

	public MethodBuilder getAfterUnmarshalMethod() {
		MethodBuilder retval = getMethods().get("afterUnmarshal");
		if (retval == null) {
			retval = newMethodBuilder("afterUnmarshal");
			retval.visibility(Visibility.PRIVATE);
			retval.arguments("Unmarshaller unmarshaller, Object parent");
			retval.importEntry(Unmarshaller.class);
			retval.annotation(SuppressWarnings.class, "\"unused\"");
		}
		return retval;
	}

	public MethodBuilder getBeforeMarshalMethod() {
		MethodBuilder retval = getMethods().get("beforeMarshal");
		if (retval == null) {
			retval = newMethodBuilder("beforeMarshal");
			retval.visibility(Visibility.PRIVATE);
			retval.arguments("Marshaller marshaller");
			retval.importEntry(Marshaller.class);
			retval.annotation(SuppressWarnings.class, "\"unused\"");
		}
		return retval;
	}

	
}
