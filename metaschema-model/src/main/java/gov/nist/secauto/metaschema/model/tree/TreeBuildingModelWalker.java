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

package gov.nist.secauto.metaschema.model.tree;

import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.common.definition.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.instance.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.instance.IChoiceInstance;
import gov.nist.secauto.metaschema.model.common.instance.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.instance.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.instance.IModelInstance;
import gov.nist.secauto.metaschema.model.common.util.ModelWalker;
import gov.nist.secauto.metaschema.model.definitions.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.definitions.MetaschemaDefinition;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Function;

/**
 * This model walker can be used to gather metaschema definitions that are defined globally.
 * 
 * The associated data represents the definition that is the current root node.
 */
public class TreeBuildingModelWalker
    extends ModelWalker<TreeBuildingModelWalker.WalkerData> {
  private static final Logger logger = LogManager.getLogger(TreeBuildingModelWalker.class);

  private static final Function<MetaschemaDefinition, Boolean> FILTER = (def) -> {
    return true;
    // return def.isGlobal();
    // return def.isGlobal() || (def instanceof AssemblyDefinition &&
    // ((AssemblyDefinition)def).getRootName() != null);
  };

  /**
   * Collect the globally defined metaschema definitions from the provided metaschemas, and any
   * metaschema imported by these metaschema.
   * 
   * @param metaschemas
   *          the metaschemas to analyze
   * @return a collection of matching definitions
   */
  public static Collection<? extends Node<?, ?>> buildTrees(Collection<? extends Metaschema> metaschemas) {
    TreeBuildingModelWalker walker = new TreeBuildingModelWalker();
    for (Metaschema metaschema : metaschemas) {
      // get local roots in case they are scope=local
      for (AssemblyDefinition rootDef : metaschema.getRootAssemblyDefinitions().values()) {
        WalkerData data = new WalkerData(rootDef);
        walker.walk(rootDef, data);
      }

      // get roots from exported
      for (AssemblyDefinition assembly : metaschema.getExportedAssemblyDefinitions().values()) {
        if (assembly.isRoot()) {
          WalkerData data = new WalkerData(assembly);
          walker.walk(assembly, data);
        }
      }
    }
    return walker.getNodes();
  }

  /**
   * Collect the globally defined metaschema definitions from the provided metaschema, and any
   * metaschema imported by this metaschema.
   * 
   * @param metaschema
   *          the metaschema to analyze
   * @return a collection of matching definitions
   */
  public static Collection<? extends Node<?, ?>> collectUsedDefinitions(Metaschema metaschema) {
    return buildTrees(Collections.singleton(metaschema));
  }

  private final Function<MetaschemaDefinition, Boolean> filter;
  private final Set<Node<?, ?>> nodes = new LinkedHashSet<>();

  /**
   * Construct a new walker.
   */
  protected TreeBuildingModelWalker() {
    this.filter = FILTER;
  }

  /**
   * Retrieves the filter used for matching.
   * 
   * @return the filter
   */
  protected Function<MetaschemaDefinition, Boolean> getFilter() {
    return filter;
  }

  /**
   * Return the collection of definitions matching the configured filter.
   * 
   * @return the collection of definitions
   */
  public Collection<? extends Node<?, ?>> getNodes() {
    return nodes;
  }

  private int depth = 0;

  @Override
  public void walk(IFieldInstance instance, TreeBuildingModelWalker.WalkerData data) {
    ++depth;
    super.walk(instance, data);
    --depth;
  }

  @Override
  public void walk(IAssemblyDefinition assembly, TreeBuildingModelWalker.WalkerData data) {
    if (data.isCycle(assembly)) {
      logger.info(" Assembly Cycle: {} {}", String.join("", Collections.nCopies(depth * 2, " ")),
          assembly.toCoordinates());
    } else {
      // try {
      // Thread.sleep(500);
      // } catch (InterruptedException e) {
      // throw new RuntimeException(e);
      // }
      data.push(assembly);
      super.walk(assembly, data);
      assert assembly.equals(data.pop());
    }
  }

  @Override
  protected void walkFlagInstances(Collection<? extends IFlagInstance> instances, WalkerData data) {
    ++depth;
    super.walkFlagInstances(instances, data);
    --depth;
  }

  @Override
  protected void walkModelInstances(Collection<? extends IModelInstance> instances, WalkerData data) {
    ++depth;
    super.walkModelInstances(instances, data);
    --depth;
  }

  @Override
  protected boolean visit(IAssemblyDefinition def, TreeBuildingModelWalker.WalkerData data) {
    if (logger.isInfoEnabled() && def.equals(data.getRootAssemblyDefinition())) {
      logger.info("Root:         {} {}", String.join("", Collections.nCopies(depth * 2, " ")), def.toCoordinates());
    }
    return true;
  }

  @Override
  protected boolean visit(IFlagInstance instance, TreeBuildingModelWalker.WalkerData data) {
    if (logger.isInfoEnabled()) {
      logger.info("Flag:           {} {}", String.join("", Collections.nCopies(depth * 2, " ")),
          instance.toCoordinates());
    }
    return true;
  }

  @Override
  protected boolean visit(IFieldInstance instance, TreeBuildingModelWalker.WalkerData data) {
    if (logger.isInfoEnabled()) {
      logger.info("Field:          {} {}", String.join("", Collections.nCopies(depth * 2, " ")),
          instance.toCoordinates());
    }
    return true;
  }

  @Override
  protected boolean visit(IAssemblyInstance instance, TreeBuildingModelWalker.WalkerData data) {
    if (logger.isInfoEnabled()) {
      logger.info("Assembly:       {} {}", String.join("", Collections.nCopies(depth * 2, " ")),
          instance.toCoordinates());
    }
    return true;
  }

  @Override
  protected boolean visit(IChoiceInstance instance, TreeBuildingModelWalker.WalkerData data) {
    return true;
  }

  static class WalkerData {
    private final IAssemblyDefinition rootAssemblyDefinition;
    private Deque<IAssemblyDefinition> assemblyStack = new LinkedList<>();

    public WalkerData(AssemblyDefinition rootAssemblyDefinition) {
      this.rootAssemblyDefinition = rootAssemblyDefinition;
    }

    public IAssemblyDefinition getRootAssemblyDefinition() {
      return rootAssemblyDefinition;
    }

    public void push(IAssemblyDefinition assemblyDefintion) {
      assemblyStack.push(assemblyDefintion);
    }

    public IAssemblyDefinition pop() {
      return assemblyStack.pop();
    }

    public boolean isCycle(IAssemblyDefinition assemblyDefintion) {
      return assemblyStack.contains(assemblyDefintion);
    }
  }
}
