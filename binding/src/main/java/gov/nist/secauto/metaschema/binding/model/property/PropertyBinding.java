package gov.nist.secauto.metaschema.binding.model.property;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlPropertyParser;
import gov.nist.secauto.metaschema.binding.model.annotations.Assembly;
import gov.nist.secauto.metaschema.binding.model.annotations.Field;
import gov.nist.secauto.metaschema.binding.model.annotations.FieldValue;
import gov.nist.secauto.metaschema.binding.model.annotations.Flag;
import gov.nist.secauto.metaschema.binding.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonFieldValueKey;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonFieldValueName;

/**
 * Represents a property (field or method) on a Java class that is bound to a
 * Metaschema object (i.e., Assembly, Field, or Flag).
 * <p>
 * Properties are bound to constructs in XML and JSON/YAML formats.
 * <p>
 * In XML key characteristics of a property binding include:
 * <ul>
 * <li>The name and namespace of the property which will be used for the
 * associated element or attribute provided by the {@link #getXmlQName()}
 * method.
 * </ul>
 * <p>
 * In JSON/YAML key characteristics of a property binding include:
 * <ul>
 * <li>A string-based key representing the field name of a JSON/YAML object
 * </ul>
 */
public interface PropertyBinding {
	PropertyBindingType getPropertyBindingType();

	/**
	 * Identifies the QName to use for the element or attribute associated with this
	 * property in an XML serialization.
	 * <p>
	 * The QName value returned will be based on the type of binding identified by
	 * {@link #getPropertyBindingType()} as follows:
	 * <ul>
	 * <li>{@link PropertyBindingType#FLAG}: the QName of the attribute will be
	 * provided based on the bound {@link Flag} annotation using the
	 * {@link Flag#name()} and {@link Flag#namespace()}.
	 * <li>{@link PropertyBindingType#FIELD}: the QName of the element will be
	 * provided based on the bound {@link Field} annotation using the
	 * {@link Field#name()} and {@link Field#namespace()}.
	 * <li>{@link PropertyBindingType#ASSEMBLY}: the QName of the element will be
	 * provided based on the bound {@link Assembly} annotation using the
	 * {@link Assembly#name()} and {@link Assembly#namespace()}.
	 * <li>{@link PropertyBindingType#FIELD_VALUE}: the QName will be {@code null}, since
	 * the property is bound based on the {@link FieldValue} annotation. In such a
	 * case contents of this property are not wrapped by an element, but are instead
	 * the contents of the outer Field's element.
	 * </ul>
	 * 
	 * @return the {@link QName} for the bound element or attribute, or {@code null}
	 *     if the property is based on a {@link FieldValue} annotation
	 */
	QName getXmlQName();

	/**
	 * Identifies the name to use for the field associated with this property in a
	 * JSON or YAML serialization.
	 * <p>
	 * The name value returned will be based on the type of binding identified by
	 * {@link #getPropertyBindingType()}.
	 * <p>
	 * When {@link #getPropertyBindingType()} == {@link PropertyBindingType#FLAG},
	 * the name of the field will be provided based on the bound {@link Flag}
	 * annotation using the {@link Flag#name()} and {@link Flag#namespace()}.
	 * <p>
	 * When {@link #getPropertyBindingType()} ==
	 * {@link PropertyBindingType#ASSEMBLY} or {@link #getPropertyBindingType()} ==
	 * {@link PropertyBindingType#FIELD}, one of two possible states can occur:
	 * <p>
	 * <ul>
	 * <li>If the {@link GroupAs} annotation is present, then the name value will be
	 * the value of {@link GroupAs#name()}.
	 * <li>Otherwise, the name value will be the value of {@link Assembly#name()} or
	 * {@link Field#name()} respectively.
	 * </ul>
	 * <p>
	 * When {@link #getPropertyBindingType()} == {@link PropertyBindingType#FIELD_VALUE},
	 * the field name value is determined as follows (in order):
	 * <p>
	 * <ul>
	 * <li>If the {@link JsonFieldValueKey} annotation is present on any sibling
	 * {@link Flag}, then the field name is {@code} null. The name must be
	 * determined by analyzing an remaining properties to parse the flag's value and
	 * the field value.
	 * <li>Else if the {@link JsonFieldValueName} annotation is present on the property,
	 * then the field name is the value of {@link JsonFieldValueName#name}.
	 * <li>Otherwise, the field name is determined by the bound type of the property
	 * using the {@link JavaTypeAdapter#getDefaultJsonFieldName().
	 * <ul>
	 * <p>
	 * Note: Use of the {@link JsonFieldValueName} and {@link JsonFieldValueKey}
	 * annotations are mutually exclusive.
	 * 
	 * @return
	 * @throws BindingException 
	 */
	String getJsonFieldName(BindingContext bindingContext) throws BindingException;

	/**
	 * Gets information about the bound property.
	 * 
	 * @return
	 */
	PropertyInfo getPropertyInfo();

	XmlPropertyParser newXmlPropertyParser(BindingContext bindingContext) throws BindingException;

}
