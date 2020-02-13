/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */
package gov.nist.secauto.metaschema.codegen.type;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class MapJavaType extends AbstractCollectionJavaType {
	private final JavaType keyClass;

	MapJavaType(Class<?> keyClass, JavaType valueClass) {
		this(new ClassJavaType(keyClass), valueClass);
	}

	MapJavaType(JavaType keyClass, JavaType itemClass) {
		super(LinkedHashMap.class, itemClass);
		Objects.requireNonNull(keyClass);
		this.keyClass = keyClass;
	}

	protected JavaType getKeyClass() {
		return keyClass;
	}

	@Override
	public Set<JavaType> getImports(JavaType classType) {
		Set<JavaType> retval = new HashSet<>(super.getImports(classType));
		retval.addAll(getKeyClass().getImports(classType));
		return Collections.unmodifiableSet(retval);
	}

	@Override
	protected String getGenericArguments(Function<String, Boolean> clashEvaluator) {
		return getKeyClass().getType(clashEvaluator) + "," + getValueClass().getType(clashEvaluator);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((keyClass == null) ? 0 : keyClass.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof MapJavaType)) {
			return false;
		}
		MapJavaType other = (MapJavaType) obj;
		if (keyClass == null) {
			if (other.keyClass != null) {
				return false;
			}
		} else if (!keyClass.equals(other.keyClass)) {
			return false;
		}
		return true;
	}
}
