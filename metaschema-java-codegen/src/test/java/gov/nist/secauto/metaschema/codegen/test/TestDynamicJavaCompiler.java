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

import gov.nist.secauto.metaschema.codegen.JavaGenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class TestDynamicJavaCompiler {
  private static final Logger logger = LogManager.getLogger(TestDynamicJavaCompiler.class);

  public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

    StringBuilder builder = new StringBuilder();
    builder.append("package test;\n");
    builder.append("public class HelloWorld {\n");
    builder.append("    public HelloWorld() {\n");
    builder.append("        System.out.println(\"Hello World\");\n");
    builder.append("    }\n");
    builder.append("}");
    String javaCode = builder.toString();

    JavaFileObject classCode = newJavaFileObject("test.HelloWorld", javaCode);

    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

    TestDynamicJavaCompiler compiler = new TestDynamicJavaCompiler(new File("target/generated-classes/dynamic"));
    if (!compiler.compile(Collections.singletonList(classCode), diagnostics)) {
      logger.error(diagnostics.getDiagnostics().toString());
    }
    ClassLoader classLoader = compiler.getClassLoader();
    Class<?> clazz = classLoader.loadClass("test.HelloWorld");
    clazz.getConstructor().newInstance();
  }

  public static JavaFileObject newJavaFileObject(String className, String source) {
    return new StringSource(className, JavaFileObject.Kind.SOURCE, source);
  }

  private final File compilationLocation;
  private ClassLoader classLoader;

  public TestDynamicJavaCompiler(File compilationLocation) {
    Objects.requireNonNull(compilationLocation, "compilationLocation");
    this.compilationLocation = compilationLocation;
  }

  protected File getCompilationLocation() {
    return compilationLocation;
  }

  protected ClassLoader getClassLoader() {
    synchronized (this) {
      if (classLoader == null) {
        try {
          classLoader = new URLClassLoader(new URL[] { getCompilationLocation().toURI().toURL() });
        } catch (MalformedURLException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return classLoader;
  }

  public boolean compile(List<JavaFileObject> compilationUnits, DiagnosticCollector<JavaFileObject> diagnostics) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    return compile(compiler, null, diagnostics, compilationUnits);

  }

  private boolean compile(JavaCompiler compiler, JavaFileManager fileManager,
      DiagnosticCollector<JavaFileObject> diagnostics, List<JavaFileObject> compilationUnits) {
    List<String> options = new LinkedList<String>();
    options.add("-d");
    options.add(getCompilationLocation().getAbsolutePath());
    options.add("-classpath");
    options.add(System.getProperty("java.class.path"));

    JavaCompiler.CompilationTask task
        = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
    boolean retval = task.call();
    return retval;
  }

  private static class StringSource extends SimpleJavaFileObject {
    private final String content;

    public StringSource(String name, JavaFileObject.Kind kind, String content) {
      super(URI.create("memo:///" + name.replace('.', '/') + kind.extension), kind);
      this.content = content;
    }

    @Override
    public CharSequence getCharContent(boolean ignore) {
      return this.content;
    }
  }

  public boolean compileGeneratedClasses(List<JavaGenerator.GeneratedClass> classesToCompile,
      DiagnosticCollector<JavaFileObject> diagnostics) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

    List<JavaFileObject> compilationUnits = new ArrayList<>(classesToCompile.size());
    for (JavaGenerator.GeneratedClass generatedClass : classesToCompile) {
      compilationUnits.add(fileManager.getJavaFileObjects(generatedClass.getClassFile()).iterator().next());
    }

    return compile(compiler, fileManager, diagnostics, compilationUnits);
  }
}
