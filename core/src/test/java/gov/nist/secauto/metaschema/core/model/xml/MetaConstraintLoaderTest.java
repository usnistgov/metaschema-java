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

package gov.nist.secauto.metaschema.core.model.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import gov.nist.secauto.metaschema.core.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.core.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.core.metapath.function.library.FnPath;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDefinitionNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IModuleNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItemFactory;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraintSet;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

public class MetaConstraintLoaderTest {

  @Test
  void test() throws MetaschemaException, IOException {

    IConstraintSet constraintSet = new MetaConstraintLoader()
        .load(Paths.get("src/test/resources/computer-metaschema-meta-constraints.xml"));

    ExternalConstraintsModulePostProcessor postProcessor
        = new ExternalConstraintsModulePostProcessor(CollectionUtil.singleton(constraintSet));
    ModuleLoader loader = new ModuleLoader(CollectionUtil.singletonList(postProcessor));
    URI moduleUri = ObjectUtils.notNull(
        Paths.get("metaschema/website/content/specification/computer-example.xml").toUri());
    IModule module = loader.load(moduleUri);

    MetapathExpression expression = MetapathExpression.compile("//@id");
    IModuleNodeItem moduleItem = INodeItemFactory.instance().newModuleNodeItem(module);
    for (IItem item : expression.evaluate(moduleItem)) {
      IDefinitionNodeItem<?, ?> nodeItem = (IDefinitionNodeItem<?, ?>) item;
      System.out.print(FnPath.fnPath(nodeItem));
      System.out.print(": ");
      System.out.println(Long.toString(nodeItem.getDefinition().getMatchesConstraints().stream()
          .filter(matches -> MetaschemaDataTypeProvider.UUID.equals(matches.getDataType()))
          .count()));
    }

    expression.evaluate(moduleItem).asStream()
        .map(item -> (IDefinitionNodeItem<?, ?>) item)
        .forEach(item -> assertEquals(1, item.getDefinition().getMatchesConstraints().stream()
            .filter(matches -> MetaschemaDataTypeProvider.UUID.equals(matches.getDataType()))
            .count()));
  }

}
