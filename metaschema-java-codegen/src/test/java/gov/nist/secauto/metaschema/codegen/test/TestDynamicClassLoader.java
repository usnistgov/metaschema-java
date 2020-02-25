/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */
package gov.nist.secauto.metaschema.codegen.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

import net.openhft.compiler.CompilerUtils;

public class TestDynamicClassLoader extends ClassLoader {
	private final File classDir;

	public TestDynamicClassLoader(File classDir) {
		this(null, classDir, getSystemClassLoader());
	}

	public TestDynamicClassLoader(File classDir, ClassLoader parent) {
		this(null, classDir, parent);
	}

	public TestDynamicClassLoader(String name, File classDir, ClassLoader parent) {
		super(name, parent);
		Objects.requireNonNull(classDir, "classDir must not be null");
		this.classDir = classDir;
		CompilerUtils.addClassPath(classDir.getPath());
	}

	protected File getClassDir() {
		return classDir;
	}

	private static String readFile(Path filePath) throws IOException {
		StringBuilder contentBuilder = new StringBuilder();
		try (Stream<String> stream = Files.lines(filePath, StandardCharsets.UTF_8)) {
			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		}
		return contentBuilder.toString();
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> retval = null;
//		try {
//			retval = super.findClass(name);
//		} catch (ClassNotFoundException ex) {
			
			File classFile = new File(getClassDir(), name.replace(".", "/") + ".java");
			try {
				retval = CompilerUtils.CACHED_COMPILER.loadFromJava(this, name, readFile(classFile.toPath()));
			} catch (IOException e) {
				throw new ClassNotFoundException("An IO error occured while loading java class code",e);
			}
//		}
		if (retval == null) {
			throw new ClassNotFoundException(name);
		}
		return retval;
	}
}
