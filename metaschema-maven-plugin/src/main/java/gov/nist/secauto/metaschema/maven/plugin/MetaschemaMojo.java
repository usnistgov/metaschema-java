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

import gov.nist.secauto.metaschema.codegen.JavaGenerator;
import gov.nist.secauto.metaschema.codegen.binding.config.DefaultBindingConfiguration;
import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.MetaschemaException;
import gov.nist.secauto.metaschema.model.MetaschemaLoader;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Goal which generates Java source files for a given set of Metaschema definitions.
 */
@Mojo(name = "generate-sources", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class MetaschemaMojo extends AbstractMojo {
  private static final String SYSTEM_FILE_ENCODING_PROPERTY = "file.encoding";
  private static final String METASCHEMA_STAE_FILE_NAME = "metaschemaStateFile";
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
   * This will be injected if this plugin is executed as part of the standard Maven lifecycle. If the
   * mojo is directly invoked, this parameter will not be injected.
   */
  @Parameter(defaultValue = "${mojoExecution}", readonly = true)
  private MojoExecution mojoExecution;

  @Component
  private BuildContext buildContext;

  /**
   * <p>
   * The directory where the staleFile is found. The staleFile is used to determine if re-generation
   * of generated Java classes is needed, by recording when the last build occurred.
   * </p>
   * <p>
   * This directory is expected to be located within the <code>${project.build.directory}</code>, to
   * ensure that code (re)generation occurs after cleaning the project.
   * </p>
   */
  @Parameter(defaultValue = "${project.build.directory}/metaschema", readonly = true, required = true)
  protected File staleFileDirectory;

  /**
   * <p>
   * Defines the encoding used for generating Java Source files.
   * </p>
   * <p>
   * The algorithm for finding the encoding to use is as follows (where the first non-null value found
   * is used for encoding):
   * <ol>
   * <li>If the configuration property is explicitly given within the plugin's configuration, use that
   * value.</li>
   * <li>If the Maven property <code>project.build.sourceEncoding</code> is defined, use its
   * value.</li>
   * <li>Otherwise use the value from the system property <code>file.encoding</code>.</li>
   * </ol>
   * </p>
   * 
   * @see #getEncoding(boolean)
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
   * A set of inclusion patterns used to select which metaschema are to be processed. By default, all
   * files are processed.
   */
  @Parameter
  protected String[] includes;

  /**
   * A set of exclusion patterns used to prevent certain files from being processed. By default, this
   * set is empty such that no files are excluded.
   */
  @Parameter
  protected String[] excludes;

  /**
   * Indicate if the execution should be skipped.
   */
  @Parameter(property = "metaschema.skip", defaultValue = "false")
  private boolean skip;

  /**
   * A set of binding configurations.
   */
  @Parameter
  protected File[] configs;

  /**
   * The BuildContext is used to identify which files or directories were modified since last build.
   * This is used to determine if java code generation must be performed again.
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
   * <p>
   * Gets the last part of the stale filename.
   * </p>
   * <p>
   * The full stale filename will be generated by pre-pending
   * {@code "." + getExecution().getExecutionId()} to this staleFileName.
   *
   * @return the stale filename postfix
   */
  protected String getStaleFileName() {
    return METASCHEMA_STAE_FILE_NAME;
  }

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
   * Gets the file encoding to use for generated classes.
   * <p>
   * The algorithm for finding the encoding to use is as follows (where the first non-null value found
   * is used for encoding):
   * </p>
   * <ol>
   * <li>If the configuration property is explicitly given within the plugin's configuration, use that
   * value.</li>
   * <li>If the Maven property <code>project.build.sourceEncoding</code> is defined, use its
   * value.</li>
   * <li>Otherwise use the value from the system property <code>file.encoding</code>.</li>
   * </ol>
   *
   * @return The encoding to be used by this AbstractJaxbMojo and its tools.
   * @see #encoding
   */
  protected final String getEncoding() {
    String encoding;
    if (this.encoding != null) {
      // first try to use the provided encoding
      encoding = this.encoding;
      getLog().debug(String.format("Using configured encoding [%s].", encoding));
    } else {
      encoding = System.getProperty(SYSTEM_FILE_ENCODING_PROPERTY);
      getLog().warn(String.format("Using system encoding [%s]. This build is platform dependent!", encoding));
    }
    return encoding;
  }

  /**
   * Retrieve a stream of Metaschema file sources.
   * 
   * @return the stream
   */
  protected Stream<File> getSources() {
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

  /**
   * Retrieve a list of binding configurations.
   * 
   * @return the collection of binding configurations
   */
  protected List<File> getConfigs() {
    List<File> retval;
    if (configs == null) {
      retval = Collections.emptyList();
    } else {
      retval = Arrays.asList(configs);
    }
    return retval;
  }

  /**
   * Determine if the execution of this mojo should be skipped.
   * 
   * @return {@code true} if the mojo execution should be skipped, or {@code false} otherwise
   */
  protected boolean shouldExecutionBeSkipped() {
    return skip;
  }

  /**
   * Determine if code generation is required. This is done by comparing the last modified time of
   * each Metaschema source file against the stale file managed by this plugin.
   * 
   * @return {@code true} if the code generation is needed, or {@code false} otherwise
   */
  protected boolean isGenerationRequired() {
    final File staleFile = getStaleFile();
    boolean generate = !staleFile.exists();
    if (generate) {
      getLog().info(String.format("Stale file '%s' doesn't exist! Generating source files.", staleFile.getPath()));
      generate = true;
    } else {
      generate = false;
      // check for staleness
      long staleLastModified = staleFile.lastModified();
      for (File sourceFile : getSources().collect(Collectors.toList())) {
        if (sourceFile.lastModified() > staleLastModified) {
          generate = true;
        }
      }
    }
    return generate;
  }

  @Override
  public void execute() throws MojoExecutionException {
    File staleFile = getStaleFile();
    try {
      staleFile = staleFile.getCanonicalFile();
    } catch (IOException ex) {
      getLog().warn("Unable to resolve canonical path to stale file. Treating it as not existing.", ex);
    }

    boolean generate;
    if (shouldExecutionBeSkipped()) {
      getLog().debug(String.format("Source file generation is configured to be skipped. Skipping."));
      generate = false;
    } else if (!staleFile.exists()) {
      getLog().info(String.format("Stale file '%s' doesn't exist! Generating source files.", staleFile.getPath()));
      generate = true;
    } else {
      generate = isGenerationRequired();
    }

    if (generate) {

      File outputDir = getOutputDirectory();
      getLog().debug(String.format("Using outputDirectory: %s", outputDir.getPath()));

      if (!outputDir.exists()) {
        outputDir.mkdirs();
      }

      // generate Java sources based on provided metaschema sources
      final MetaschemaLoader loader = new MetaschemaLoader();
      final Set<Metaschema> metaschemaCollection = new HashSet<>();
      for (File source : getSources().collect(Collectors.toList())) {
        getLog().info("Using metaschema source: " + source.getPath());
        Metaschema metaschema;
        try {
          metaschema = loader.loadXmlMetaschema(source);
        } catch (MetaschemaException | IOException ex) {
          throw new MojoExecutionException("Loading of metaschema failed", ex);
        }
        metaschemaCollection.add(metaschema);
      }

      // TODO: load this from the requested file
      DefaultBindingConfiguration bindingConfiguration = new DefaultBindingConfiguration();
      for (File config : getConfigs()) {
        try {
          getLog().info("Loading binding configuration: " + config.getPath());
          bindingConfiguration.load(config);
        } catch (IOException | MetaschemaException ex) {
          throw new MojoExecutionException(
              String.format("Unable to load binding configuration from '%s'.", config.getPath()), ex);
        }
      }

      try {
        getLog().info("Generating Java classes in: " + getOutputDirectory().getPath());
        JavaGenerator.generate(metaschemaCollection, getOutputDirectory(), bindingConfiguration);
      } catch (IOException ex) {
        throw new MojoExecutionException("Creation of Java classes failed.", ex);
      }

      // create the stale file
      staleFileDirectory.mkdirs();
      try (OutputStream os = new FileOutputStream(staleFile)) {
        os.close();
        getLog().info("Created stale file: " + staleFile);
      } catch (IOException ex) {
        throw new MojoExecutionException("Failed to write stale file: " + staleFile.getPath(), ex);
      }

      // for m2e
      // buildContext.refresh(getOutputDirectory());
    }

    // add generated sources to Maven
    try {
      getMavenProject().addCompileSourceRoot(getOutputDirectory().getCanonicalFile().getPath());
    } catch (IOException ex) {
      throw new MojoExecutionException("Unable to add output directory to maven sources.", ex);
    }
  }
}
