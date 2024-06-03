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

package gov.nist.secauto.metaschema.databind.testing.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelField;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundField;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundFlag;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;

import java.lang.reflect.Field;
import java.util.Optional;

import edu.umd.cs.findbugs.annotations.NonNull;

@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class ModelTestBase {
  public static void assertAssemblyDefinition(
      @NonNull Class<?> assemblyClass,
      @NonNull IBoundDefinitionModelAssembly assembly) {
    MetaschemaAssembly annotation = assemblyClass.getAnnotation(MetaschemaAssembly.class);

    assertAll(
        "assembly failed",
        () -> assertEquals(
            annotation.name(),
            assembly.getName(),
            "rootName"),
        () -> assertEquals(
            annotation.moduleClass(),
            assembly.getContainingModule().getClass(),
            "moduleClass"),
        () -> assertEquals(
            ModelUtil.resolveNoneOrValue(annotation.formalName()),
            assembly.getFormalName(),
            "formalName"),
        () -> assertEquals(
            ModelUtil.resolveNoneOrValue(annotation.description()),
            Optional.ofNullable(assembly.getDescription()).map(MarkupLine::toMarkdown).orElse(null),
            "description"),
        () -> assertEquals(
            ModelUtil.resolveNoneOrValue(annotation.remarks()),
            Optional.ofNullable(assembly.getRemarks()).map(MarkupMultiline::toMarkdown).orElse(null),
            "remarks"),
        () -> {
          String rootName = ModelUtil.resolveNoneOrValue(annotation.rootName());
          if (rootName != null) {
            assertAll(
                () -> assertEquals(
                    rootName,
                    assembly.getRootName(),
                    "rootName"),
                () -> assertEquals(
                    assembly.getContainingModule().getXmlNamespace().toASCIIString(),
                    assembly.getRootXmlQName().getNamespaceURI(),
                    "rootNamespace"),
                () -> assertTrue(true));
          } else {
            assertEquals(
                null,
                assembly.getRootXmlQName(),
                "rootNamespace");
          }
        });
  }

  public static void assertFlagInstance(
      @NonNull Class<?> fieldOrAssemblyClass,
      @NonNull String flagJavaFieldName,
      @NonNull IBoundInstanceFlag flag,
      @NonNull IBindingContext context) throws NoSuchFieldException, SecurityException {
    Field field = fieldOrAssemblyClass.getDeclaredField(flagJavaFieldName);
    BoundFlag annotation = field.getAnnotation(BoundFlag.class);

    IDataTypeAdapter<?> adapter = ModelUtil.getDataTypeAdapter(annotation.typeAdapter(), context);

    String name = Optional.ofNullable(ModelUtil.resolveNoneOrValue(annotation.name())).orElse(field.getName());

    assertAll(
        flagJavaFieldName + " flag failed",
        () -> assertEquals(
            name,
            flag.getName(),
            "name"),
        () -> assertNull(
            flag.getUseName(),
            "useNname"),
        () -> assertEquals(
            adapter,
            flag.getDefinition().getJavaTypeAdapter(),
            "typeAdapter"),
        () -> assertEquals(
            annotation.required(),
            flag.isRequired(),
            "required"),
        () -> assertEquals(
            ModelUtil.resolveNullOrValue(annotation.defaultValue(), adapter),
            flag.getDefaultValue(),
            "defaultValue"),
        () -> assertEquals(
            ModelUtil.resolveNoneOrValue(annotation.formalName()),
            flag.getFormalName(),
            "formalName"),
        () -> assertEquals(
            ModelUtil.resolveNoneOrValue(annotation.description()),
            Optional.ofNullable(flag.getDescription()).map(MarkupLine::toMarkdown).orElse(null),
            "description"),
        () -> assertEquals(
            ModelUtil.resolveNoneOrValue(annotation.remarks()),
            Optional.ofNullable(flag.getRemarks()).map(MarkupMultiline::toMarkdown).orElse(null),
            "remarks"));
  }

  public static void assertFieldInstance(
      @NonNull Class<?> assemblyClass,
      @NonNull String fieldJavaFieldName,
      @NonNull IBoundInstanceModelField field,
      @NonNull IBindingContext context) throws NoSuchFieldException, SecurityException {
    Field javaField = assemblyClass.getDeclaredField(fieldJavaFieldName);
    BoundField annotation = javaField.getAnnotation(BoundField.class);

    IDataTypeAdapter<?> adapter = ModelUtil.getDataTypeAdapter(annotation.typeAdapter(), context);

    String name;
    String useName;
    if (field.getDefinition().hasChildren()) {
      name = field.getDefinition().getName();
      useName = ModelUtil.resolveNoneOrValue(annotation.useName());
    } else {
      name = Optional.ofNullable(ModelUtil.resolveNoneOrValue(annotation.useName())).orElse(javaField.getName());
      useName = null;
    }

    assertAll(
        fieldJavaFieldName + " field failed",
        () -> assertEquals(
            name,
            field.getName(),
            "name"),
        () -> assertEquals(
            useName,
            field.getUseName(),
            "useName"),
        () -> assertEquals(
            adapter,
            field.getDefinition().getJavaTypeAdapter(),
            "typeAdapter"),
        () -> assertEquals(
            ModelUtil.resolveNullOrValue(annotation.defaultValue(), adapter),
            field.getDefaultValue(),
            "defaultValue"),
        () -> assertEquals(
            field.getContainingModule().getXmlNamespace().toASCIIString(),
            field.getXmlNamespace(),
            "namespace"),
        () -> assertEquals(
            annotation.inXmlWrapped(),
            field.isInXmlWrapped(),
            "inXmlWrapped"),
        () -> assertEquals(
            annotation.minOccurs(),
            field.getMinOccurs(),
            "minOccurs"),
        () -> assertEquals(
            annotation.maxOccurs(),
            field.getMaxOccurs(),
            "maxOccurs"),
        () -> assertEquals(
            ModelUtil.resolveNoneOrValue(annotation.formalName()),
            field.getFormalName(),
            "formalName"),
        () -> assertEquals(
            ModelUtil.resolveNoneOrValue(annotation.description()),
            Optional.ofNullable(field.getDescription()).map(MarkupLine::toMarkdown).orElse(null),
            "description"),
        () -> assertEquals(
            ModelUtil.resolveNoneOrValue(annotation.remarks()),
            Optional.ofNullable(field.getRemarks()).map(MarkupMultiline::toMarkdown).orElse(null),
            "remarks"));
    // groupAs
  }
}
