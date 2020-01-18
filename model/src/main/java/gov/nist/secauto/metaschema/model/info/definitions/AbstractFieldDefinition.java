package gov.nist.secauto.metaschema.model.info.definitions;

import java.util.Objects;

import com.sun.xml.bind.api.impl.NameConverter;

import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.configuration.FieldBindingConfiguration;
import gov.nist.secauto.metaschema.model.info.Type;
import gov.nist.secauto.metaschema.model.info.instances.FlagInstance;

public abstract class AbstractFieldDefinition<METASCHEMA extends Metaschema> extends AbstractInfoElementDefinition<METASCHEMA> implements FieldDefinition {
	private final FieldBindingConfiguration bindingConfiguration;

	public AbstractFieldDefinition(FieldBindingConfiguration bindingConfiguration, METASCHEMA metaschema) {
		super(metaschema);
		Objects.requireNonNull(bindingConfiguration, "bindingConfiguration");
		this.bindingConfiguration = bindingConfiguration;
	}

	@Override
	public Type getType() {
		return Type.FIELD;
	}

	protected FieldBindingConfiguration getBindingConfiguration() {
		return bindingConfiguration;
	}

	@Override
	public String getClassName() {
		String retval = getBindingConfiguration().getClassName();
		if (retval == null) {
			retval = NameConverter.standard.toClassName(getName());;
		}
		return retval;
	}

	@Override
	public String getPackageName() {
		return getContainingMetaschema().getPackageName();
	}

	@Override
	public FlagInstance getFlagInstanceByName(String name) {
		return getFlagInstances().get(name);
	}

	@Override
	public JsonValueKeyEnum getJsonValueKeyType() {
		JsonValueKeyEnum retval = JsonValueKeyEnum.NONE;
		if (hasJsonValueKey()) {
			FlagInstance valueKeyFlag = getJsonValueKeyFlagInstance();
			if (valueKeyFlag != null) {
				retval = JsonValueKeyEnum.FLAG;
			} else {
				retval = JsonValueKeyEnum.LABEL;
			}
		}
		return retval;
	}
}
