package gov.nist.secauto.metaschema.codegen.builder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import gov.nist.secauto.metaschema.codegen.type.JavaType;

public class ClassBuilder extends AbstractClassBuilder<ClassBuilder>{
	private static final Visibility DEFAULT_VISIBILITY = Visibility.PUBLIC;

	private Map<String, InnerClassBuilder> innerClasses = new LinkedHashMap<>();
	
	public ClassBuilder(JavaType classJavaType) {
		super(classJavaType);
	}

	public InnerClassBuilder newInnerClassBuilder(String name) {
		InnerClassBuilder retval = new InnerClassBuilder(this, name);
		innerClasses.put(retval.getClassName(), retval);
		return retval;
	}

	protected Map<String, InnerClassBuilder> getInnerClasses() {
		return Collections.unmodifiableMap(innerClasses);
	}

	protected void setInnerClasses(Map<String, InnerClassBuilder> innerClasses) {
		this.innerClasses = innerClasses;
	}

	private boolean needsImport(JavaType javaType) {
//		String classPackageName = getJavaType().getPackageName();
//		String packageName = javaType.getPackageName();
//		return !"java.lang".equals(packageName) && !classPackageName.equals(packageName);
		return false;
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
			imports.stream().filter(this::needsImport).map(a -> a.getQualifiedClassName()).sorted().distinct().forEachOrdered(a -> out.printf("import %s;%n", a));
			out.println();
//			// filter
//			
//
//			// sort
//			imports.stream().filter(this::needsImport).map(a -> a.getQualifiedClassName()).sorted().forEachOrdered(a -> out.printf("import %s;%n", a));
//	
//			JavaType classJavaType = getJavaType();
//			boolean hasImport = false;
//			for (JavaType importEntry : imports) {
//				String importValue = importEntry.getImportValue(classJavaType);
//				if (importValue != null && !importValue.startsWith("java.lang.")) {
//					out.printf("import %s;%n", importValue);
//					hasImport = true;
//				}
//			}
//			if (hasImport) {
//				out.println();
//			}
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

		for (InnerClassBuilder innerClass : getInnerClasses().values()) {
			innerClass.build(out);
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
			retval.arguments("javax.xml.bind.Unmarshaller unmarshaller, Object parent");
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
			retval.arguments("javax.xml.bind.Marshaller marshaller");
			retval.importEntry(Marshaller.class);
			retval.annotation(SuppressWarnings.class, "\"unused\"");
		}
		return retval;
	}

	@Override
	public Function<String, Boolean> getClashEvaluator() {
		return this::evaluateClash;
	}

	private boolean evaluateClash(String className) {
//		boolean retval = false;
//		if (getJavaType().getClassName().equals(className)) {
//			retval = true;
//		}
//		return retval;
		return true;
	}

	@Override
	public ClassBuilder getActualClassBuilder() {
		return this;
	}
}
