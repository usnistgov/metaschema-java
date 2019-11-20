package gov.nist.secauto.metaschema.codegen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.secauto.metaschema.codegen.context.ClassContext;
import gov.nist.secauto.metaschema.datatype.MarkupString;

public class FieldClassGenerator extends AbstractClassGenerator implements InstanceGenerator {
	private static final Logger logger = LogManager.getLogger(FieldClassGenerator.class);

	private final String name;
	private final MarkupString description;
	private final DataType datatype;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public MarkupString getDescription() {
		return description;
	}

	public DataType getDatatype() {
		return datatype;
	}

	public static abstract class Builder<C extends FieldClassGenerator, B extends Builder<C, B>>
			extends AbstractClassGenerator.Builder<C, B> {
		private String name = "_value";
		private MarkupString description;
		private DataType datatype;

		@SuppressWarnings("unchecked")
		public B name(String name) {
			this.name = name;
			return (B) this;
		}

        @SuppressWarnings( "unchecked" )
		public B description(MarkupString text) {
        	this.description = text;
			return (B)this;
		}

		public B type(gov.nist.secauto.metaschema.model.DataType type) {
			DataType e = DataType.lookupByDatatype(type);
			if (e == null) {
				logger.warn("Unsupported datatype '{}', using String", type);
				e = DataType.STRING;
			}
			return type(e);
		}

		@SuppressWarnings("unchecked")
		public B type(DataType datatype) {
			this.datatype = datatype;
			return (B) this;
		}
	}

	public static Builder<?, ?> builder() {
		return new DefaultBuilder();
	}

	protected FieldClassGenerator(Builder<?, ?> builder) {
		super(builder);
		this.name = builder.name;
		this.description = builder.description;
		this.datatype = builder.datatype;
	}

	private static class DefaultBuilder
			extends Builder<FieldClassGenerator, DefaultBuilder> {
		@Override
		public FieldClassGenerator build() {
			return new FieldClassGenerator(this);
		}
	}

	@Override
	protected void processInstances(ClassContext classContext) {
		classContext.newFieldInstance(this);
		super.processInstances(classContext);
	}

	@Override
	public String getJavaType() {
		return getDatatype().getJavaType();
	}

	@Override
	public String getJavaTypePackage() {
		return getDatatype().getJavaTypePackage();
	}
}