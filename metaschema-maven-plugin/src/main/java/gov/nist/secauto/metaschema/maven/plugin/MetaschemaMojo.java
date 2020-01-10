package gov.nist.secauto.metaschema.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/**
 * Goal which generates Java source files for a given set of Metaschema
 * definitions.
 */
@Mojo(name = "generate-sources", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class MetaschemaMojo extends AbstractMojo {
	private static final String SYSTEM_FILE_ENCODING_PROPERTY = "file.encoding";
	private static final String METASCHEMA_STAE_FILE_NAME = "metaschemaStateFile";
	private static final String[] DEFAULT_INCLUDES = { "**/*metaschema.xml" };

	/**
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	MavenProject mavenProject;

	/**
	 * this will be injected if this plugin is executed as part of the standard
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
	 * A set of inclusion patterns used to select which metaschema are to be
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
	 * since last build. This is used to determine if java code generation must be
	 * performed again.
	 *
	 * @return the active Plexus BuildContext.
	 */
	protected final BuildContext getBuildContext() {
		return buildContext;
	}

	/**
	 * @return The active MavenProject.
	 */
	protected final MavenProject getMavenProject() {
		return mavenProject;
	}

	/**
	 * @return The active MojoExecution.
	 */
	public MojoExecution getMojoExecution() {
		return mojoExecution;
	}

	protected File getOutputDirectory() {
		return outputDirectory;
	}

	protected void setOutputDirectory(File outputDirectory) {
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
	 * @param warnIfPlatformEncoding Defines if a warning should be logged if
	 *                               encoding is not configured but the platform
	 *                               encoding (system property
	 *                               {@code file.encoding}) is used
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

	protected Stream<File> getSources() {
		DirectoryScanner ds = new DirectoryScanner();
		ds.addDefaultExcludes();
		ds.setBasedir(metaschemaDir);
		ds.setIncludes(includes != null && includes.length > 0 ? includes : DEFAULT_INCLUDES);
		ds.setExcludes(excludes != null && excludes.length > 0 ? excludes : null);
		ds.addDefaultExcludes();
		ds.setCaseSensitive(true);
		ds.setFollowSymlinks(false);
		ds.scan();
		return Stream.of(ds.getIncludedFiles()).map(filename -> new File(metaschemaDir, filename)).distinct();
	}

	protected boolean shouldExecutionBeSkipped() {
		return skip;
	}

	private boolean isStale() {
		// TODO: Implement stale file checking
		return true;
	}

	@Override
	public void execute() throws MojoExecutionException {
		File staleFile;
		try {
			staleFile = getStaleFile().getCanonicalFile();
		} catch (IOException ex) {
			getLog().warn("Unable to resolve canonical path to stale file. Treating it as not existing.", ex);
			staleFile = null;
		}

		boolean generate;
		if (shouldExecutionBeSkipped()) {
			getLog().debug(String.format("Source file generation is configured to be skipped. Skipping.",
					staleFile.getPath()));
			generate = false;
		} else if (staleFile == null || !staleFile.exists()) {
			getLog().info(
					String.format("Stale file '%s' doesn't exist! Generating source files.", staleFile.getPath()));
			generate = true;
		} else {
			generate = isStale();
		}

		if (!generate) {
			return;
		}

		File outputDir = getOutputDirectory();
		getLog().debug(String.format("Using outputDirectory: %s", outputDir.getPath()));

		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}

		// generate Java sources based on provided metaschema sources
		for (File source : getSources().collect(Collectors.toList())) {
			getLog().info("Source: " + source.getPath());
		}

		// for m2e
		buildContext.refresh(getOutputDirectory());

		// add generated sources to Maven
		try {
			getMavenProject().addCompileSourceRoot(getOutputDirectory().getCanonicalFile().getPath());
		} catch (IOException ex) {
			throw new MojoExecutionException("Unable to add output directory to maven sources.", ex);
		}
	}
}
