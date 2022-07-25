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

package gov.nist.secauto.metaschema.codegen;

import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.IModelDefinition;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MetaschemaProductionImpl implements IMetaschemaProduction {
  @NotNull
  private final IMetaschema metaschema;
  @NotNull
  private final IGeneratedClass generatedMetaschema;
  @NotNull
  private final Map<@NotNull IModelDefinition, IDefinitionProduction> definitionProductions;
  @NotNull
  private final String packageName;

  public MetaschemaProductionImpl(@NotNull IMetaschema metaschema, @NotNull ITypeResolver typeResolver,
      @NotNull Path targetDirectory) throws IOException {
    this.metaschema = metaschema;

    MetaschemaClassGenerator generator = new MetaschemaClassGenerator(metaschema, typeResolver);
    this.generatedMetaschema = generator.generateClass(targetDirectory);

    Set<String> classNames = new HashSet<>();

    Stream<@NotNull DefinitionProductionImpl> productions
        = Stream.concat(metaschema.getAssemblyDefinitions().stream(), metaschema.getFieldDefinitions().stream())
            .map(definition -> {
              IModelDefinitionTypeInfo typeInfo = null;
              if (definition instanceof IAssemblyDefinition) {
                typeInfo = IAssemblyDefinitionTypeInfo.newTypeInfo((IAssemblyDefinition) definition, typeResolver);
              } else if (definition instanceof IFieldDefinition) {
                IFieldDefinition fieldDefinition = (IFieldDefinition) definition;

                if (!fieldDefinition.getFlagInstances().isEmpty()) {
                  typeInfo = IFieldDefinitionTypeInfo.newTypeInfo((IFieldDefinition) definition, typeResolver);
                } // otherwise field is just a simple data value, then no class is needed
              }
              return typeInfo;
            })
            .flatMap(ObjectUtils::filterNull)
            .map(typeInfo -> {
              IModelDefinition definition = typeInfo.getDefinition();
              DefaultGeneratedDefinitionClass generatedClass;
              try {
                generatedClass = typeInfo.generateClass(targetDirectory);
              } catch (RuntimeException ex) {
                throw new IllegalStateException(
                    String.format("Unable to generate class for definition '%s' in Metaschema '%s'",
                        definition.getName(),
                        metaschema.getLocation()),
                    ex);
              } catch (IOException ex) {
                throw new IllegalStateException(ex);
              }
              String className = generatedClass.getClassName().canonicalName();
              if (classNames.contains(className)) {
                throw new IllegalStateException(String.format(
                    "Found duplicate class '%s' in metaschema '%s'."
                        + " All class names must be unique within the same namespace.",
                    className, metaschema.getLocation()));
              }
              classNames.add(className);
              return new DefinitionProductionImpl(definition, generatedClass);
            });

    @NotNull
    @SuppressWarnings("null")
    Map<@NotNull IModelDefinition, IDefinitionProduction> retval = productions.collect(
        Collectors.toUnmodifiableMap(DefinitionProductionImpl::getDefinition, Function.identity()));

    this.definitionProductions = retval;

    this.packageName = typeResolver.getPackageName(metaschema);
  }

  @Override
  public IMetaschema getMetaschema() {
    return metaschema;
  }

  @Override
  public IGeneratedClass getGeneratedMetaschema() {
    return generatedMetaschema;
  }

  @Override
  @SuppressWarnings("null")
  public Collection<@NotNull ? extends IModelDefinition> getGlobalDefinitions() {
    return definitionProductions.keySet();
  }

  @Override
  @SuppressWarnings("null")
  public Collection<@NotNull IDefinitionProduction> getDefinitionProductions() {
    return definitionProductions.values();
  }

  @Override
  public String getPackageName() {
    return packageName;
  }

  @Override
  public Stream<@NotNull IGeneratedClass> getGeneratedClasses() {
    return ObjectUtils.notNull(Stream.concat(
        Stream.of(getGeneratedMetaschema()),
        getDefinitionProductions().stream()
            .map(definition -> definition.getGeneratedClass())));
  }
}
