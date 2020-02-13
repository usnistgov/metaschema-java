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
package gov.nist.secauto.metaschema.codegen.builder;

import java.io.PrintWriter;
import java.util.Objects;

import gov.nist.secauto.metaschema.codegen.type.JavaType;

public class FieldBuilder extends AbstractMemberBuilder<FieldBuilder> {
	private static final Visibility DEFAULT_VISIBILITY = Visibility.PRIVATE;
	private final JavaType javaType;
	private final String name;

	FieldBuilder(AbstractClassBuilder<?> classBuilder, JavaType javaType, String name) {
		super(classBuilder);
		Objects.requireNonNull(javaType, "javaType");
		Objects.requireNonNull(name, "name");
		this.javaType = javaType;
		this.name = name;
		importEntries(javaType.getImports(classBuilder.getJavaType()));
	}

	protected JavaType getJavaType() {
		return javaType;
	}

	protected String getName() {
		return name;
	}

	@Override
	public void build(PrintWriter out) {
		buildAnnotations(out);
		out.printf("%s%s%s %s;%n",getPadding(), getVisibilityValue(DEFAULT_VISIBILITY), getJavaType().getType(getClashEvaluator()), getName());
	}
}
