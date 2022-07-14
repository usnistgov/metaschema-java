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

package gov.nist.secauto.metaschema.model.common.metapath.item;

import gov.nist.secauto.metaschema.model.common.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MetaschemaNodeItemImpl
    extends AbstractMetaschemaNodeItem {

  public MetaschemaNodeItemImpl(@NotNull IMetaschema metaschema, @NotNull INodeItemFactory factory) {
    super(metaschema, factory);
  }

  @Override
  protected @NotNull Supplier<Model<IFlagNodeItem, IModelNodeItem>>
      newModelSupplier(@NotNull INodeItemFactory factory) {
    return () -> {
      // build flags from Metaschema definitions
      Map<@NotNull String, IFlagNodeItem> flags
          = CollectionUtil.unmodifiableMap(getMetaschema().getFlagDefinitions().stream()
              .collect(
                  Collectors.toMap(
                      IFlagDefinition::getEffectiveName,
                      def -> factory.newFlagNodeItem(def, getBaseUri()),
                      (v1, v2) -> v2,
                      LinkedHashMap::new)));

      // build model items from Metaschema definitions
      Stream<@NotNull IFieldNodeItem> fieldStream = getMetaschema().getFieldDefinitions().stream()
          .map(def -> factory.newFieldNodeItem(def, getBaseUri()));
      Stream<@NotNull IAssemblyNodeItem> assemblyStream = getMetaschema().getAssemblyDefinitions().stream()
          .map(def -> factory.newAssemblyNodeItem(def, getBaseUri()));

      Map<@NotNull String, List<@NotNull IModelNodeItem>> modelItems
          = ObjectUtils.notNull(Stream.concat(fieldStream, assemblyStream)
              .collect(
                  Collectors.collectingAndThen(
                      Collectors.groupingBy(IModelNodeItem::getName),
                      Collections::unmodifiableMap)));
      return new Model<>(flags, modelItems);
    };
  }
}
