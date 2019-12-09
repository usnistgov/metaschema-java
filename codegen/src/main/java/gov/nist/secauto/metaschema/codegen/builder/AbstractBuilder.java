package gov.nist.secauto.metaschema.codegen.builder;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import gov.nist.secauto.metaschema.codegen.type.JavaType;

public abstract class AbstractBuilder<T extends AbstractBuilder<T>> {
	private Visibility visibility;
	private Set<JavaType> imports = new HashSet<>();
	private List<String> annotations = new LinkedList<>();

	@SuppressWarnings("unchecked")
	public T visibility(Visibility visibility) {
		this.visibility = visibility;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T importEntry(Class<?> clazz) {
		this.imports.add(JavaType.create(clazz));
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T importEntry(JavaType javaType) {
		this.imports.add(javaType);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T importEntries(Set<JavaType> imports) {
		for (JavaType javaType : imports) {
			importEntry(javaType);
		}
		return (T) this;
	}

	public abstract ClassBuilder getClassBuilder();

	public <A extends Annotation> T annotation(Class<A> annotation) {
		return annotation(annotation, null);
	}

	@SuppressWarnings("unchecked")
	public <A extends Annotation> T annotation(Class<A> annotation, String arguments) {
		JavaType annotationType = JavaType.create(annotation);
		
		StringBuilder builder = new StringBuilder();
		builder.append('@');
		builder.append(annotationType.getType(getClassBuilder().getJavaType()));
		if (arguments != null && !arguments.isBlank()) {
			builder.append('(');
			builder.append(arguments);
			builder.append(')');
		}
		this.annotations.add(builder.toString());
		importEntry(annotationType);
		return (T) this;
	}

	public abstract void build(PrintWriter writer) throws IOException;

	protected String getPadding() {
		return "";
	}
	
	protected Visibility getVisibility() {
		return visibility;
	}
	
	protected String getVisibilityValue(Visibility defaultVisibility) {
		Visibility visibility = getVisibility();
		if (visibility == null) {
			visibility = defaultVisibility;
		}
		StringBuilder builder = new StringBuilder();
		String visibilityValue = visibility.getModifier();
		if (visibilityValue != null) {
			builder.append(visibilityValue);
			builder.append(' ');
		}

		return builder.toString();
	}

	protected Set<JavaType> getImports() {
		return Collections.unmodifiableSet(imports);
	}

	protected List<String> getAnnotations() {
		return Collections.unmodifiableList(annotations);
	}

	protected void buildAnnotations(PrintWriter out) {
		for (String annotation : getAnnotations()) {
			out.format("%s%s%n", getPadding(), annotation);
		}
	}

}
