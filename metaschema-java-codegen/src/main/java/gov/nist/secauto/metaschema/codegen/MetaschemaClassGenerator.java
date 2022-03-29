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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import gov.nist.secauto.metaschema.binding.AbstractBoundMetaschema;
import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.model.annotations.Metaschema;
import gov.nist.secauto.metaschema.codegen.type.ITypeResolver;
import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import javax.lang.model.element.Modifier;

public class MetaschemaClassGenerator {
  @NotNull
  private final IMetaschema metaschema;
  @NotNull
  private final ITypeResolver typeResolver;

  public MetaschemaClassGenerator(@NotNull IMetaschema metaschema, @NotNull ITypeResolver typeResolver) {
    this.metaschema = ObjectUtils.requireNonNull(metaschema, "metaschema");
    this.typeResolver = ObjectUtils.requireNonNull(typeResolver, "typeResolver");
  }

  protected IMetaschema getMetaschema() {
    return metaschema;
  }

  protected ITypeResolver getTypeResolver() {
    return typeResolver;
  }

  @NotNull
  public ClassName getClassName() {
    return getTypeResolver().getClassName(getMetaschema());
  }

  @NotNull
  public GeneratedClass generateClass(Path outputDir) throws IOException {
    ClassName className = getClassName();

    TypeSpec.Builder builder = generateClass(className);

    JavaFile javaFile = JavaFile.builder(className.packageName(), builder.build()).build();
    Path classFile = javaFile.writeToPath(outputDir);

    return new GeneratedClass(classFile, className);
  }

  @NotNull
  protected TypeSpec.Builder generateClass(@NotNull ClassName className) throws IOException {
    // create the class
    TypeSpec.Builder builder = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC, Modifier.FINAL);

    builder.superclass(AbstractBoundMetaschema.class);

    AnnotationSpec.Builder metaschemaAnnotation = AnnotationSpec.builder(Metaschema.class);

    IMetaschema metaschema = getMetaschema();

    ITypeResolver typeResolver = getTypeResolver();
    for (IFieldDefinition definition : metaschema.getFieldDefinitions()) {
      if (!definition.isSimple()) {
        metaschemaAnnotation.addMember("fields", "$T.class", typeResolver.getClassName(definition));
      }
    }

    for (IAssemblyDefinition definition : metaschema.getAssemblyDefinitions()) {
      metaschemaAnnotation.addMember("assemblies", "$T.class", typeResolver.getClassName(definition));
    }

    for (IMetaschema metaschemaImport : metaschema.getImportedMetaschemas()) {
      metaschemaAnnotation.addMember("imports", "$T.class", typeResolver.getClassName(metaschemaImport));
    }

    builder.addAnnotation(metaschemaAnnotation.build());

    builder.addField(
        FieldSpec.builder(MarkupLine.class, "NAME", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("$T.fromMarkdown($S)", MarkupLine.class, metaschema.getName().toMarkdown())
            .build());

    builder.addField(
        FieldSpec.builder(String.class, "SHORT_NAME", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("$S", metaschema.getShortName())
            .build());

    builder.addField(
        FieldSpec.builder(String.class, "VERSION", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("$S", metaschema.getVersion())
            .build());

    builder.addField(
        FieldSpec.builder(URI.class, "XML_NAMESPACE", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("$T.create($S)", URI.class, metaschema.getXmlNamespace())
            .build());

    builder.addField(
        FieldSpec.builder(URI.class, "JSON_BASE_URI", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("$T.create($S)", URI.class, metaschema.getJsonBaseUri())
            .build());

    MarkupMultiline remarks = metaschema.getRemarks();
    if (remarks != null) {
      builder.addField(
          FieldSpec.builder(MarkupMultiline.class, "REMARKS", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
              .initializer("$T.fromMarkdown($S)", MarkupMultiline.class, remarks.toMarkdown())
              .build());
    }

    builder.addMethod(
        MethodSpec.constructorBuilder()
          .addModifiers(Modifier.PUBLIC)
          .addParameter(
              ParameterizedTypeName.get(ClassName.get(List.class), WildcardTypeName.subtypeOf(IMetaschema.class).box()),
              "importedMetaschema")
          .addParameter(IBindingContext.class, "bindingContext")
          .addStatement("super($N, $N)", "importedMetaschema", "bindingContext")
          .build());
    builder.addMethod(
        MethodSpec.methodBuilder("getName")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(MarkupLine.class)
            .addStatement("return NAME")
            .build());

    builder.addMethod(
        MethodSpec.methodBuilder("getShortName")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(String.class)
            .addStatement("return SHORT_NAME")
            .build());

    builder.addMethod(
        MethodSpec.methodBuilder("getVersion")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(String.class)
            .addStatement("return VERSION")
            .build());

    builder.addMethod(
        MethodSpec.methodBuilder("getXmlNamespace")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(URI.class)
            .addStatement("return XML_NAMESPACE")
            .build());

    builder.addMethod(
        MethodSpec.methodBuilder("getJsonBaseUri")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(URI.class)
            .addStatement("return JSON_BASE_URI")
            .build());

    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getRemarks")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(MarkupMultiline.class);

    if (remarks == null) {
      methodBuilder.addStatement("return null");
    } else {
      methodBuilder.addStatement("return REMARKS");
    }

    builder.addMethod(methodBuilder.build());

    return builder;
  }
}
