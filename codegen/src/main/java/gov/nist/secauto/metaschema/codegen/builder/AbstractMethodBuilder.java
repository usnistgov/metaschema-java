package gov.nist.secauto.metaschema.codegen.builder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.stream.Collectors;

public abstract class AbstractMethodBuilder<T extends AbstractMethodBuilder<T>> extends AbstractMemberBuilder<T> {
	private String arguments;
	private StringWriter bodyWriter = new StringWriter();

	public AbstractMethodBuilder(ClassBuilder classBuilder) {
		super(classBuilder);
	}

	@SuppressWarnings("unchecked")
	public T arguments(String args) {
		this.arguments = args;
		return (T)this;
	}

	public PrintWriter getBodyWriter() {
		return new PrintWriter(bodyWriter);
	}

	protected String getArguments() {
		return arguments;
	}

	protected String getBody() {
		String padding = getPadding()+"    ";
		return bodyWriter.toString().lines().map(str -> String.format("%s%s%n", padding, str)).collect(Collectors.joining());
	}

}
