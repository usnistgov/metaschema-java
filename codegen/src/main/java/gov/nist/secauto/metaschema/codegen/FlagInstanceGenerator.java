package gov.nist.secauto.metaschema.codegen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FlagInstanceGenerator extends AbstractInstanceGenerator implements InstanceGenerator {
	private static final Logger logger = LogManager.getLogger(AbstractInstanceGenerator.class);

	private final DataType datatype;

    public DataType getDatatype() {
		return datatype;
	}

	@Override
	public String getJavaType() {
		return getDatatype().getJavaType();
	}

	@Override
	public String getJavaTypePackage() {
		return getDatatype().getJavaTypePackage();
	}

	public static abstract class Builder<C extends FlagInstanceGenerator, B extends Builder<C, B>>
			extends AbstractInstanceGenerator.Builder<C, B> {
		private DataType datatype;

		public B type(gov.nist.secauto.metaschema.model.DataType type) {
			DataType e = DataType.lookupByDatatype(type);
			if (e == null) {
				logger.warn("Unsupported datatype '{}', using String", type);
				e = DataType.STRING;
			}
			return type(e);
		}
        
        @SuppressWarnings( "unchecked" )
		public B type(DataType type) {
			this.datatype = type;
			return (B)this;
		}

		@Override
		public abstract C build();
	}

	public static Builder<?, ?> builder() {
		return new DefaultBuilder();
	}

	private static class DefaultBuilder extends Builder<FlagInstanceGenerator, DefaultBuilder> {
		@Override
		public FlagInstanceGenerator build() {
			return new FlagInstanceGenerator(this);
		}
	}

	protected FlagInstanceGenerator(Builder<?, ?> builder) {
		super(builder);
        this.datatype = builder.datatype;
	}
}
