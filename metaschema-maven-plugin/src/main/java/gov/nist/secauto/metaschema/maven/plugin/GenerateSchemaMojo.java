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

import gov.nist.secauto.metaschema.core.configuration.DefaultConfiguration;
import gov.nist.secauto.metaschema.core.configuration.IConfiguration;
import gov.nist.secauto.metaschema.core.configuration.IMutableConfiguration;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraintSet;
import gov.nist.secauto.metaschema.core.model.xml.ExternalConstraintsModulePostProcessor;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.metaschema.BindingModuleLoader;
import gov.nist.secauto.metaschema.schemagen.ISchemaGenerator;
import gov.nist.secauto.metaschema.schemagen.SchemaGenerationFeature;
import gov.nist.secauto.metaschema.schemagen.json.JsonSchemaGenerator;
import gov.nist.secauto.metaschema.schemagen.xml.XmlSchemaGenerator;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Goal which generates Java source files for a given set of Module definitions.
 */
@Mojo(name = "generate-schemas", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class GenerateSchemaMojo
    extends AbstractMetaschemaMojo {
  public enum SchemaFormat {
    XSD,
    JSON_SCHEMA;
  }

  @NonNull
  private static final String STALE_FILE_NAME = "generateSschemaStaleFile";

  @NonNull
  private static final XmlSchemaGenerator XML_SCHEMA_GENERATOR = new XmlSchemaGenerator();
  @NonNull
  private static final JsonSchemaGenerator JSON_SCHEMA_GENERATOR = new JsonSchemaGenerator();

  /**
   * Specifies the formats of the schemas to generate. Multiple formats can be
   * supplied and this plugin will generate a schema for each of the desired
   * formats.
   * <p>
   * A format is specified by supplying one of the following values in a
   * &lt;format&gt; subelement:
   * <ul>
   * <li><em>json</em> - Creates a JSON Schema</li>
   * <li><em>xsd</em> - Creates an XML Schema Definition</li>
   * </ul>
   */
  @Parameter
  private List<String> formats;

  /**
   * If enabled, definitions that are defined inline will be generated as inline
   * types. If disabled, definitions will always be generated as global types.
   */
  @Parameter(defaultValue = "true")
  private boolean inlineDefinitions = true;

  /**
   * If enabled, child definitions of a choice that are defined inline will be
   * generated as inline types. If disabled, child definitions of a choice will
   * always be generated as global types. This option will only be used if
   * <code>inlineDefinitions</code> is also enabled.
   */
  @Parameter(defaultValue = "false")
  private boolean inlineChoiceDefinitions; // false;

  /**
   * Determine if inlining definitions is required.
   *
   * @return {@code true} if inlining definitions is required, or {@code false}
   *         otherwise
   */
  protected boolean isInlineDefinitions() {
    return inlineDefinitions;
  }

  /**
   * Determine if inlining choice definitions is required.
   *
   * @return {@code true} if inlining choice definitions is required, or
   *         {@code false} otherwise
   */
  protected boolean isInlineChoiceDefinitions() {
    return inlineChoiceDefinitions;
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
  @Override
  protected String getStaleFileName() {
    return STALE_FILE_NAME;
  }

  /**
   * Performs schema generation using the provided Metaschema modules.
   *
   * @param modules
   *          the Metaschema modules to generate the schema for
   * @throws MojoExecutionException
   *           if an error occurred during generation
   */
  protected void generate(@NonNull Set<IModule> modules) throws MojoExecutionException {
    IMutableConfiguration<SchemaGenerationFeature<?>> schemaGenerationConfig
        = new DefaultConfiguration<>();

    if (isInlineDefinitions()) {
      schemaGenerationConfig.enableFeature(SchemaGenerationFeature.INLINE_DEFINITIONS);
    } else {
      schemaGenerationConfig.disableFeature(SchemaGenerationFeature.INLINE_DEFINITIONS);
    }

    if (isInlineChoiceDefinitions()) {
      schemaGenerationConfig.enableFeature(SchemaGenerationFeature.INLINE_CHOICE_DEFINITIONS);
    } else {
      schemaGenerationConfig.disableFeature(SchemaGenerationFeature.INLINE_CHOICE_DEFINITIONS);
    }

    Set<SchemaFormat> schemaFormats;
    if (formats != null) {
      schemaFormats = ObjectUtils.notNull(EnumSet.noneOf(SchemaFormat.class));
      for (String format : formats) {
        switch (format.toLowerCase(Locale.ROOT)) {
        case "xsd":
          schemaFormats.add(SchemaFormat.XSD);
          break;
        case "json":
          schemaFormats.add(SchemaFormat.JSON_SCHEMA);
          break;
        default:
          throw new IllegalStateException("Unsupported schema format: " + format);
        }
      }
    } else {
      schemaFormats = ObjectUtils.notNull(EnumSet.allOf(SchemaFormat.class));
    }

    Path outputDirectory = ObjectUtils.notNull(getOutputDirectory().toPath());
    for (IModule module : modules) {
      if (getLog().isInfoEnabled()) {
        getLog().info(String.format("Processing metaschema: %s", module.getLocation()));
      }
      if (module.getExportedRootAssemblyDefinitions().isEmpty()) {
        continue;
      }
      generateSchemas(module, schemaGenerationConfig, outputDirectory, schemaFormats);
    }
  }

  private static void generateSchemas(
      @NonNull IModule module,
      @NonNull IConfiguration<SchemaGenerationFeature<?>> schemaGenerationConfig,
      @NonNull Path outputDirectory,
      @NonNull Set<SchemaFormat> schemaFormats) throws MojoExecutionException {

    String shortName = module.getShortName();

    if (schemaFormats.contains(SchemaFormat.XSD)) {
      try { // XML Schema
        String filename = String.format("%s_schema.xsd", shortName);
        Path xmlSchema = ObjectUtils.notNull(outputDirectory.resolve(filename));
        generateSchema(module, schemaGenerationConfig, xmlSchema, XML_SCHEMA_GENERATOR);
      } catch (Exception ex) {
        throw new MojoExecutionException("Unable to generate XML schema.", ex);
      }
    }

    if (schemaFormats.contains(SchemaFormat.JSON_SCHEMA)) {
      try { // JSON Schema
        String filename = String.format("%s_schema.json", shortName);
        Path xmlSchema = ObjectUtils.notNull(outputDirectory.resolve(filename));
        generateSchema(module, schemaGenerationConfig, xmlSchema, JSON_SCHEMA_GENERATOR);
      } catch (Exception ex) {
        throw new MojoExecutionException("Unable to generate JSON schema.", ex);
      }
    }
  }

  private static void generateSchema(
      @NonNull IModule module,
      @NonNull IConfiguration<SchemaGenerationFeature<?>> schemaGenerationConfig,
      @NonNull Path schemaPath,
      @NonNull ISchemaGenerator generator) throws IOException {
    try (@SuppressWarnings("resource") Writer writer = ObjectUtils.notNull(Files.newBufferedWriter(
        schemaPath,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE,
        StandardOpenOption.TRUNCATE_EXISTING))) {
      generator.generateFromModule(module, writer, schemaGenerationConfig);
    }
  }

  @Override
  public void execute() throws MojoExecutionException {
    File staleFile = getStaleFile();
    try {
      staleFile = staleFile.getCanonicalFile();
    } catch (IOException ex) {
      if (getLog().isWarnEnabled()) {
        getLog().warn("Unable to resolve canonical path to stale file. Treating it as not existing.", ex);
      }
    }

    boolean generate;
    if (shouldExecutionBeSkipped()) {
      if (getLog().isDebugEnabled()) {
        getLog().debug(String.format("Schema generation is configured to be skipped. Skipping."));
      }
      generate = false;
    } else if (staleFile.exists()) {
      generate = isGenerationRequired();
    } else {
      if (getLog().isInfoEnabled()) {
        getLog().info(String.format("Stale file '%s' doesn't exist! Generating source files.", staleFile.getPath()));
      }
      generate = true;
    }

    if (generate) {
      File outputDir = getOutputDirectory();
      if (getLog().isDebugEnabled()) {
        getLog().debug(String.format("Using outputDirectory: %s", outputDir.getPath()));
      }

      if (!outputDir.exists() && !outputDir.mkdirs()) {
        throw new MojoExecutionException("Unable to create output directory: " + outputDir);
      }

      List<IConstraintSet> constraints;
      try {
        constraints = getConstraints();
      } catch (MetaschemaException | IOException ex) {
        throw new MojoExecutionException("Unable to load external constraints.", ex);
      }

      // generate Java sources based on provided metaschema sources
      final BindingModuleLoader loader = constraints.isEmpty()
          ? new BindingModuleLoader()
          : new BindingModuleLoader(
              CollectionUtil.singletonList(new ExternalConstraintsModulePostProcessor(constraints)));
      loader.allowEntityResolution();
      final Set<IModule> modules = new HashSet<>();
      for (File source : getModuleSources().collect(Collectors.toList())) {
        if (getLog().isInfoEnabled()) {
          getLog().info("Using metaschema source: " + source.getPath());
        }
        IModule module;
        try {
          module = loader.load(source);
        } catch (MetaschemaException | IOException ex) {
          throw new MojoExecutionException("Loading of metaschema failed", ex);
        }
        modules.add(module);
      }

      generate(modules);

      // create the stale file
      if (!staleFileDirectory.exists() && !staleFileDirectory.mkdirs()) {
        throw new MojoExecutionException("Unable to create output directory: " + staleFileDirectory);
      }
      try (OutputStream os
          = Files.newOutputStream(staleFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
              StandardOpenOption.TRUNCATE_EXISTING)) {
        os.close();
        if (getLog().isInfoEnabled()) {
          getLog().info("Created stale file: " + staleFile);
        }
      } catch (IOException ex) {
        throw new MojoExecutionException("Failed to write stale file: " + staleFile.getPath(), ex);
      }

      // for m2e
      getBuildContext().refresh(getOutputDirectory());
    }

    // // add generated sources to Maven
    // try {
    // getMavenProject()..addCompileSourceRoot(getOutputDirectory().getCanonicalFile().getPath());
    // } catch (IOException ex) {
    // throw new MojoExecutionException("Unable to add output directory to maven
    // sources.", ex);
    // }
  }
}
