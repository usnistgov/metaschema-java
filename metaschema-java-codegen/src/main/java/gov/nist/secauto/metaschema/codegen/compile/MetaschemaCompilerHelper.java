/*
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

package gov.nist.secauto.metaschema.codegen.compile;

import gov.nist.secauto.metaschema.codegen.GeneratedClass;
import gov.nist.secauto.metaschema.codegen.JavaGenerator;
import gov.nist.secauto.metaschema.codegen.Production;
import gov.nist.secauto.metaschema.codegen.binding.config.DefaultBindingConfiguration;
import gov.nist.secauto.metaschema.codegen.binding.config.IBindingConfiguration;
import gov.nist.secauto.metaschema.model.MetaschemaLoader;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.MetaschemaException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class MetaschemaCompilerHelper {
  private static final Logger LOGGER = LogManager.getLogger(MetaschemaCompilerHelper.class);
  private static final MetaschemaLoader LOADER = new MetaschemaLoader();

  public static Production compileMetaschema(Path metaschemaPath, Path classDir) throws IOException, MetaschemaException {
    return compileMetaschema(metaschemaPath, classDir, new DefaultBindingConfiguration());
  }

  public static Production compileMetaschema(Path metaschemaPath, Path classDir, IBindingConfiguration bindingConfiguration)
      throws IOException, MetaschemaException {
    IMetaschema metaschema = LOADER.loadXmlMetaschema(metaschemaPath);
    return compileMetaschema(metaschema, classDir, bindingConfiguration);
  }

  public static Production compileMetaschema(IMetaschema metaschema, Path classDir) throws IOException {
    return compileMetaschema(metaschema, classDir, new DefaultBindingConfiguration());
  }

  public static Production compileMetaschema(IMetaschema metaschema, Path classDir, IBindingConfiguration bindingConfiguration) throws IOException {
    Production production = JavaGenerator.generate(metaschema, classDir, bindingConfiguration);
    List<GeneratedClass> classesToCompile = production.getGeneratedClasses().collect(Collectors.toList());

    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
    if (!compileGeneratedClasses(classesToCompile, diagnostics, classDir)) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error(diagnostics.getDiagnostics().toString());
      }
      throw new IllegalStateException(String.format("failed to compile classes: %s",
          classesToCompile.stream()
              .map(clazz -> clazz.getClassName().canonicalName())
              .collect(Collectors.joining(","))));
    }
    return production;
  }

  public static ClassLoader getClassLoader(@NotNull Path classDir, @NotNull ClassLoader parent) {
    try {
      return new URLClassLoader(new URL[] { classDir.toUri().toURL() }, parent);
    } catch (MalformedURLException ex) {
      throw new IllegalStateException("unable to configure class loader", ex);
    }
  }

  private static boolean compile(
      JavaCompiler compiler,
      JavaFileManager fileManager,
      DiagnosticCollector<JavaFileObject> diagnostics,
      List<JavaFileObject> compilationUnits,
      Path classDir) {
    List<String> options = new LinkedList<String>();
//    options.add("-verbose");
//    options.add("-g");
    options.add("-d");
    options.add(classDir.toString());
    options.add("-classpath");
    options.add(System.getProperty("java.class.path"));

    JavaCompiler.CompilationTask task
        = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
    return task.call();
  }

  private static boolean compileGeneratedClasses(
      List<GeneratedClass> classesToCompile,
      DiagnosticCollector<JavaFileObject> diagnostics,
      Path classDir) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

    List<JavaFileObject> compilationUnits = new ArrayList<>(classesToCompile.size());
    for (GeneratedClass generatedClass : classesToCompile) {
      compilationUnits.add(fileManager.getJavaFileObjects(generatedClass.getClassFile()).iterator().next());
    }

    return compile(compiler, fileManager, diagnostics, compilationUnits, classDir);
  }
}
