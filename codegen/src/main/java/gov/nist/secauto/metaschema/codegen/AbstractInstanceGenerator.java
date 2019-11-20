package gov.nist.secauto.metaschema.codegen;

import gov.nist.secauto.metaschema.datatype.MarkupString;

public abstract class AbstractInstanceGenerator implements InstanceGenerator {
	private final String name;
	private final MarkupString description;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public MarkupString getDescription() {
		return description;
	}

	public static abstract class Builder<C extends AbstractInstanceGenerator, B extends Builder<C, B>> {
		private String name;
		private MarkupString description;
 
        @SuppressWarnings( "unchecked" )
		public B name(String name) {
        	this.name = name;
			return (B)this;
		}

        @SuppressWarnings( "unchecked" )
		public B description(MarkupString text) {
        	this.description = text;
			return (B)this;
		}
 
		public abstract C build();
    }
  
    protected AbstractInstanceGenerator(Builder<?, ?> builder) {
        this.name = builder.name;
        this.description = builder.description;
    }
}
