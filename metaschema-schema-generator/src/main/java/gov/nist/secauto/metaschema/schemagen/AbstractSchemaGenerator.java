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

package gov.nist.secauto.metaschema.schemagen;

import gov.nist.secauto.metaschema.core.configuration.IConfiguration;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.core.model.IMetaschema;
import gov.nist.secauto.metaschema.core.model.IRootAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.RootAssemblyDefinitionWrapper;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.datatype.IDatatypeManager;

import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Thsi abstract class provides a common implementation shared by all schema generators.
 *
 * @param <T>
 *          the writer type
 * @param <D>
 *          the {@link IDatatypeManager} type
 * @param <S>
 *          the {@link IGenerationState} type
 */
public abstract class AbstractSchemaGenerator<T extends AutoCloseable, D extends IDatatypeManager, S extends AbstractGenerationState<
    T, D>>
    implements ISchemaGenerator {

  /**
   * Create a new writer to use to write the schema.
   *
   * @param out
   *          the {@link Writer} to write the schema content to
   * @return the schema writer
   * @throws SchemaGenerationException
   *           if an error occurred while creating the writer
   */
  @NonNull
  protected abstract T newWriter(@NonNull Writer out);

  /**
   * Create a new schema generation state object.
   *
   * @param metaschema
   *          the Metaschema to generate the schema for
   * @param schemaWriter
   *          the writer to use to write the schema
   * @param configuration
   *          the generation configuration
   * @return the schema generation state used for context and writing
   * @throws SchemaGenerationException
   *           if an error occurred while creating the generation state object
   */
  @NonNull
  protected abstract S newGenerationState(
      @NonNull IMetaschema metaschema,
      @NonNull T schemaWriter,
      @NonNull IConfiguration<SchemaGenerationFeature<?>> configuration);

  /**
   * Called to generate the actual schema content.
   *
   * @param generationState
   *          the generation state object
   */
  protected abstract void generateSchema(@NonNull S generationState);

  @Override
  public void generateFromMetaschema(
      IMetaschema metaschema,
      Writer out,
      IConfiguration<SchemaGenerationFeature<?>> configuration) {
    // IInlineStrategy inlineStrategy = IInlineStrategy.newInlineStrategy(configuration);
    try {
      // avoid automatically closing streams not owned by the generator
      @SuppressWarnings("PMD.CloseResource") T schemaWriter = newWriter(out);
      S generationState = newGenerationState(metaschema, schemaWriter, configuration);
      generateSchema(generationState);
      generationState.flushWriter();
    } catch (SchemaGenerationException ex) { // NOPMD avoid nesting same exception
      throw ex;
    } catch (Exception ex) { // NOPMD need to catch close exception
      throw new SchemaGenerationException(ex);
    }
  }

  /**
   * Determine the collection of root definitions.
   *
   * @param generationState
   *          the schema generation state used for context and writing
   * @param handler
   *          a callback to execute on each identified root definition
   * @return the list of identified root definitions
   */
  protected List<IRootAssemblyDefinition> analyzeDefinitions(
      @NonNull S generationState,
      @Nullable BiConsumer<MetaschemaIndex.DefinitionEntry, IDefinition> handler) {
    // TODO: use of handler here is confusing and introduces side effects. Consider refactoring this in
    // the caller

    List<IRootAssemblyDefinition> rootAssemblyDefinitions = new LinkedList<>();
    for (MetaschemaIndex.DefinitionEntry entry : generationState.getMetaschemaIndex().getDefinitions()) {

      boolean referenced = entry.isReferenced();

      IDefinition definition = ObjectUtils.notNull(entry.getDefinition());
      if (definition instanceof IAssemblyDefinition && ((IAssemblyDefinition) definition).isRoot()) {
        // found root definition
        IRootAssemblyDefinition assemblyDefinition
            = new RootAssemblyDefinitionWrapper<>((IAssemblyDefinition) definition); // NOPMD necessary instantiation
        rootAssemblyDefinitions.add(assemblyDefinition);
        if (!referenced) {
          referenced = true;
          entry.incrementReferenceCount();
        }

      }

      if (!referenced) {
        // skip unreferenced definitions
        continue;
      }

      if (handler != null) {
        handler.accept(entry, definition);
      }
    }
    return rootAssemblyDefinitions;
  }

}
