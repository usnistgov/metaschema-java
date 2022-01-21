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

import gov.nist.secauto.metaschema.codegen.binding.config.DefaultBindingConfiguration;
import gov.nist.secauto.metaschema.model.MetaschemaLoader;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.MetaschemaException;
import gov.nist.secauto.metaschema.model.common.definition.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IDefinition;
import gov.nist.secauto.metaschema.model.common.instance.IInstance;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathFactory;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ASTPrinter;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.instance.DefaultMetaschemaContext;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.instance.IInstanceSet;
import gov.nist.secauto.metaschema.model.common.util.ConstraintValidatingModelWalker;
import gov.nist.secauto.metaschema.model.tree.UsedDefinitionModelWalker;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

class JavaGeneratorTest {
  // @TempDir
  // File generationDir;
  File generationDir = new File("target/generated-sources/metaschema");

  @Test
  void test() throws IOException, MetaschemaException {
    // Metaschema metaschema = new MetaschemaLoader().loadXmlMetaschema(new URL(
    // "https://raw.githubusercontent.com/usnistgov/OSCAL/main/src/metaschema/oscal_complete_metaschema.xml"));
    IMetaschema metaschema;
    try {
      MetaschemaLoader loader = new MetaschemaLoader();
      loader.allowEntityResolution();
      metaschema = loader
          .loadXmlMetaschema(new File("../../liboscal-java/oscal/src/metaschema/oscal_complete_metaschema.xml"));
    } catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }

    DefaultBindingConfiguration bindingConfiguration = new DefaultBindingConfiguration();
    JavaGenerator.generate(metaschema, generationDir, bindingConfiguration);

    ConstraintValidatingModelWalker walker = new ConstraintValidatingModelWalker();
    List<IAssemblyDefinition> rootDefinitions = new LinkedList<>();
    for (IDefinition definition : UsedDefinitionModelWalker.collectUsedDefinitionsFromMetaschema(metaschema)) {

      if (definition instanceof IAssemblyDefinition) {
        IAssemblyDefinition assembly = (IAssemblyDefinition) definition;
        if (assembly.isRoot()) {
          rootDefinitions.add(assembly);
          walker.walk(assembly);
        }
      }
    }
    // AssemblyDefinition definition = metaschema.getAssemblyDefinitionByName("system-component");
    //
    MetapathExpression exp = MetapathFactory.parseMetapathString("*/*/@name");
    // MetapathExpression exp = Metapath.parseMetapathString("//test/@flag = 1+1+1");
    // MetapathExpression exp = Metapath.parseMetapathString("//test[@flag='value']/@flag = 0.1");
    System.out.println(new ASTPrinter().visit(exp.getASTNode()));

    IInstanceSet result = exp.evaluateMetaschemaInstance(new DefaultMetaschemaContext(rootDefinitions));
    for (IInstance instance : result.getInstances()) {
      System.out.println(instance);
    }
  }
  //
  // private void printDefinition(IDefinition definition, String padding) {
  // if (definition instanceof IFlagDefinition) {
  // printFlag((IFlagDefinition) definition, padding);
  // } else if (definition instanceof IFieldDefinition) {
  // printField((IFieldDefinition) definition, padding);
  // } else if (definition instanceof IAssemblyDefinition) {
  // printAssembly((IAssemblyDefinition) definition, padding);
  // }
  // }
  //
  // private void printFlag(IFlagDefinition flag, String padding) {
  // System.out.println(String.format("%sFlag Definition: %s", padding, flag.getName()));
  // List<? extends IAllowedValuesConstraint> constraints;
  // try {
  // constraints = flag.getAllowedValuesContraints();
  // } catch (Exception ex) {
  // ex.printStackTrace();
  // throw ex;
  // }
  //
  // printConstraints(constraints, padding);
  // }
  //
  // private void printField(IFieldDefinition field, String padding) {
  // System.out.println(String.format("%sField Definition: %s", padding, field.getName()));
  //
  // List<? extends IAllowedValuesConstraint> constraints;
  // try {
  // constraints = field.getAllowedValuesContraints();
  // } catch (Exception ex) {
  // ex.printStackTrace();
  // throw ex;
  // }
  //
  // printConstraints(constraints, padding);
  //
  // for (IFlagInstance flag : field.getFlagInstances().values()) {
  // printFlag(flag.getDefinition(), padding + " ");
  // }
  // }
  //
  // private void printAssembly(IAssemblyDefinition assembly, String padding) {
  // System.out.println(String.format("%sAssembly Definition: %s", padding, assembly.getName()));
  //
  // List<? extends IAllowedValuesConstraint> constraints;
  // try {
  // constraints = assembly.getAllowedValuesContraints();
  // } catch (Exception ex) {
  // ex.printStackTrace();
  // throw ex;
  // }
  //
  // printConstraints(constraints, padding);
  //
  // for (IFlagInstance flag : assembly.getFlagInstances().values()) {
  // printFlag(flag.getDefinition(), padding + " ");
  // }
  //
  // for (IModelInstance instance : assembly.getNamedModelInstances().values()) {
  // if (instance instanceof IFieldInstance) {
  // printField(((IFieldInstance) instance).getDefinition(), padding + " ");
  // } else if (instance instanceof IAssemblyInstance) {
  // // printAssembly(((IAssemblyInstance) instance).getDefinition(), padding + " ");
  // }
  // }
  // }
  //
  // private void printConstraints(List<? extends IAllowedValuesConstraint> constraints, String
  // padding) {
  // for (IAllowedValuesConstraint constraint : constraints) {
  // System.out.println(String.format("%s %s: %s", padding, constraint.getId(),
  // constraint.getTarget().getPath()));
  // for (IAllowedValue value : constraint.getAllowedValues().values()) {
  // System.out
  // .println(String.format("%s %s: %s", padding, value.getValue(),
  // value.getDescription().toMarkdown()));
  // }
  // }
  // }
}
