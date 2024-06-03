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

package gov.nist.secauto.metaschema.maven.plugin;

import gov.nist.secauto.metaschema.core.model.IConstraintLoader;
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraintSet;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.metaschema.BindingConstraintLoader;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class AbstractMetaschemaMojo
    extends AbstractMojo {
  private static final String SYSTEM_FILE_ENCODING_PROPERTY = "file.encoding";
  private static final String[] DEFAULT_INCLUDES = { "**/*.xml" };

  /**
   * The Maven project context.
   *
   * @parameter default-value="${project}"
   * @required
   * @readonly
   */
  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  MavenProject mavenProject;

  /**
   * This will be injected if this plugin is executed as part of the standard
   * Maven lifecycle. If the mojo is directly invoked, this parameter will not be
   * injected.
   */
  @Parameter(defaultValue = "${mojoExecution}", readonly = true)
  private MojoExecution mojoExecution;

  @Component
  private BuildContext buildContext;

  /**
   * <p>
   * The directory where the staleFile is found. The staleFile is used to
   * determine if re-generation of generated Java classes is needed, by recording
   * when the last build occurred.
   * </p>
   * <p>
   * This directory is expected to be located within the
   * <code>${project.build.directory}</code>, to ensure that code (re)generation
   * occurs after cleaning the project.
   * </p>
   */
  @Parameter(defaultValue = "${project.build.directory}/metaschema", readonly = true, required = true)
  protected File staleFileDirectory;

  /**
   * <p>
   * Defines the encoding used for generating Java Source files.
   * </p>
   * <p>
   * The algorithm for finding the encoding to use is as follows (where the first
   * non-null value found is used for encoding):
   * <ol>
   * <li>If the configuration property is explicitly given within the plugin's
   * configuration, use that value.</li>
   * <li>If the Maven property <code>project.build.sourceEncoding</code> is
   * defined, use its value.</li>
   * <li>Otherwise use the value from the system property
   * <code>file.encoding</code>.</li>
   * </ol>
   * </p>
   *
   * @see #getEncoding()
   * @since 2.0
   */
  @Parameter(defaultValue = "${project.build.sourceEncoding}")
  private String encoding;

  /**
   * Location to generate Java source files in.
   */
  @Parameter(defaultValue = "${project.build.directory}/generated-sources/metaschema", required = true)
  private File outputDirectory;

  /**
   * The directory to read source metaschema from.
   */
  @Parameter(defaultValue = "${basedir}/src/main/metaschema")
  private File metaschemaDir;

  /**
   * A list of <code>files</code> containing Metaschema module constraints files.
   */
  @Parameter(property = "constraints")
  private File[] constraints;

  /**
   * A set of inclusion patterns used to select which Metaschema modules are to be
   * processed. By default, all files are processed.
   */

  @Parameter
  protected String[] includes;

  /**
   * A set of exclusion patterns used to prevent certain files from being
   * processed. By default, this set is empty such that no files are excluded.
   */
  @Parameter
  protected String[] excludes;

  /**
   * Indicate if the execution should be skipped.
   */
  @Parameter(property = "metaschema.skip", defaultValue = "false")
  private boolean skip;

  /**
   * The BuildContext is used to identify which files or directories were modified
   * since last build. This is used to determine if Module-based generation must
   * be performed again.
   *
   * @return the active Plexus BuildContext.
   */
  protected final BuildContext getBuildContext() {
    return buildContext;
  }

  /**
   * Retrieve the Maven project context.
   *
   * @return The active MavenProject.
   */
  protected final MavenProject getMavenProject() {
    return mavenProject;
  }

  /**
   * Retrieve the mojo execution context.
   *
   * @return The active MojoExecution.
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "this is a data holder")
  public MojoExecution getMojoExecution() {
    return mojoExecution;
  }

  /**
   * Retrieve the directory where generated classes will be stored.
   *
   * @return the directory
   */
  protected File getOutputDirectory() {
    return outputDirectory;
  }

  /**
   * Set the directory where generated classes will be stored.
   *
   * @param outputDirectory
   *          the directory to use
   */
  protected void setOutputDirectory(File outputDirectory) {
    Objects.requireNonNull(outputDirectory, "outputDirectory");
    this.outputDirectory = outputDirectory;
  }

  /**
   * Gets the file encoding to use for generated classes.
   * <p>
   * The algorithm for finding the encoding to use is as follows (where the first
   * non-null value found is used for encoding):
   * </p>
   * <ol>
   * <li>If the configuration property is explicitly given within the plugin's
   * configuration, use that value.</li>
   * <li>If the Maven property <code>project.build.sourceEncoding</code> is
   * defined, use its value.</li>
   * <li>Otherwise use the value from the system property
   * <code>file.encoding</code>.</li>
   * </ol>
   *
   * @return The encoding to be used by this AbstractJaxbMojo and its tools.
   */
  protected final String getEncoding() {
    String encoding;
    if (this.encoding != null) {
      // first try to use the provided encoding
      encoding = this.encoding;
      if (getLog().isDebugEnabled()) {
        getLog().debug(String.format("Using configured encoding [%s].", encoding));
      }
    } else {
      encoding = System.getProperty(SYSTEM_FILE_ENCODING_PROPERTY);
      if (getLog().isWarnEnabled()) {
        getLog().warn(String.format("Using system encoding [%s]. This build is platform dependent!", encoding));
      }
    }
    return encoding;
  }

  /**
   * Retrieve a stream of Module file sources.
   *
   * @return the stream
   */
  protected Stream<File> getModuleSources() {
    DirectoryScanner ds = new DirectoryScanner();
    ds.setBasedir(metaschemaDir);
    ds.setIncludes(includes != null && includes.length > 0 ? includes : DEFAULT_INCLUDES);
    ds.setExcludes(excludes != null && excludes.length > 0 ? excludes : null);
    ds.addDefaultExcludes();
    ds.setCaseSensitive(true);
    ds.setFollowSymlinks(false);
    ds.scan();
    return Stream.of(ds.getIncludedFiles()).map(filename -> new File(metaschemaDir, filename)).distinct();
  }

  protected List<IConstraintSet> getConstraints() throws MetaschemaException, IOException {
    IConstraintLoader loader = new BindingConstraintLoader();
    List<IConstraintSet> constraintSets = new ArrayList<>(constraints.length);
    for (File constraint : this.constraints) {
      constraintSets.add(loader.load(ObjectUtils.notNull(constraint)));
    }
    return CollectionUtil.unmodifiableList(constraintSets);
  }

  /**
   * Determine if the execution of this mojo should be skipped.
   *
   * @return {@code true} if the mojo execution should be skipped, or
   *         {@code false} otherwise
   */
  protected boolean shouldExecutionBeSkipped() {
    return skip;
  }

  /**
   * Get the name of the file that is used to detect staleness.
   *
   * @return the name
   */
  protected abstract String getStaleFileName();

  /**
   * Gets the staleFile for this execution.
   *
   * @return the staleFile
   */
  protected final File getStaleFile() {
    StringBuilder builder = new StringBuilder();
    if (getMojoExecution() != null) {
      builder.append(getMojoExecution().getExecutionId()).append('-');
    }
    builder.append(getStaleFileName());
    return new File(staleFileDirectory, builder.toString());
  }

  /**
   * Determine if code generation is required. This is done by comparing the last
   * modified time of each Module source file against the stale file managed by
   * this plugin.
   *
   * @return {@code true} if the code generation is needed, or {@code false}
   *         otherwise
   */
  protected boolean isGenerationRequired() {
    final File staleFile = getStaleFile();
    boolean generate = !staleFile.exists();
    if (generate) {
      if (getLog().isInfoEnabled()) {
        getLog().info(String.format("Stale file '%s' doesn't exist! Generating source files.", staleFile.getPath()));
      }
      generate = true;
    } else {
      generate = false;
      // check for staleness
      long staleLastModified = staleFile.lastModified();

      BuildContext buildContext = getBuildContext();
      URI metaschemaDirRelative = getMavenProject().getBasedir().toURI().relativize(metaschemaDir.toURI());

      if (buildContext.isIncremental() && buildContext.hasDelta(metaschemaDirRelative.toString())) {
        if (getLog().isInfoEnabled()) {
          getLog().info("metaschemaDirRelative: " + metaschemaDirRelative.toString());
        }
        generate = true;
      }

      if (!generate) {
        for (File sourceFile : getModuleSources().collect(Collectors.toList())) {
          if (getLog().isInfoEnabled()) {
            getLog().info("Source file: " + sourceFile.getPath());
          }
          if (sourceFile.lastModified() > staleLastModified) {
            generate = true;
          }
        }
      }
    }
    return generate;
  }
}
