package gov.nist.secauto.metaschema.codegen;

public class ModelInstanceGenerator extends AbstractInstanceGenerator implements InstanceGenerator {
	private final String javaTypePackage;
	private final String javaType;
	private final int minOccurs;
	private final int maxOccurs;

	@Override
	public String getJavaType() {
		return javaType;
	}

	@Override
	public String getJavaTypePackage() {
		return javaTypePackage;
	}

	public int getMinOccurs() {
		return minOccurs;
	}

	public int getMaxOccurs() {
		return maxOccurs;
	}

	public static abstract class Builder<C extends ModelInstanceGenerator, B extends Builder<C, B>>
			extends AbstractInstanceGenerator.Builder<C, B> {

		private String packageName;
		private String className;
		private int minOccurs;
		private int maxOccurs;

		@SuppressWarnings("unchecked")
		public B packageName(String name) {
			this.packageName = name;
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B className(String name) {
			this.className = name;
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B minOccurs(int occurance) {
			this.minOccurs = occurance;
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B maxOccurs(int occurance) {
			this.maxOccurs = occurance;
			return (B) this;
		}

		@Override
		public abstract C build();
	}

	private static class DefaultBuilder extends Builder<ModelInstanceGenerator, DefaultBuilder> {
		@Override
		public ModelInstanceGenerator build() {
			return new ModelInstanceGenerator(this);
		}
	}

	public static Builder<?, ?> builder() {
		return new DefaultBuilder();
	}

	protected ModelInstanceGenerator(Builder<?, ?> builder) {
		super(builder);
		this.javaTypePackage = builder.packageName;
		this.javaType = builder.className;
		this.minOccurs = builder.minOccurs;
		this.maxOccurs = builder.maxOccurs;
	}
}
