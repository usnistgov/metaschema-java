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

package gov.nist.secauto.metaschema.binding.io.context;

import gov.nist.secauto.metaschema.binding.model.property.AssemblyProperty;
import gov.nist.secauto.metaschema.binding.model.property.FieldProperty;
import gov.nist.secauto.metaschema.binding.model.property.FlagProperty;
import gov.nist.secauto.metaschema.binding.model.property.NamedModelProperty;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Deque;
import java.util.LinkedList;

public class DefaultPathBuilder implements PathBuilder {
  private static final Logger logger = LogManager.getLogger(DefaultPathBuilder.class);

  private final LinkedList<InstanceHandler<?>> instanceStack = new LinkedList<>();
  private final LinkedList<IPathInstance> pathStack = new LinkedList<>();

  // public void pushInstance(INamedInstance instance) {
  // instanceStack.push(instance);
  // }

  @Override
  public void pushInstance(FlagProperty instance) {
    InstanceHandler<FlagProperty> handler = new FlagInstanceHandler(instance);
    instanceStack.push(handler);
  }

  @Override
  public void pushInstance(NamedModelProperty instance) {
    InstanceHandler<?> handler;
    if (instance instanceof FieldProperty) {
      handler = new FieldInstanceHandler((FieldProperty) instance);
    } else {
      // assembly
      handler = new AssemblyInstanceHandler((AssemblyProperty) instance);
    }
    instanceStack.push(handler);
  }

  @Override
  public InstanceHandler<?> popInstance() {
    return instanceStack.pop();
  }

  @Override
  public void pushItem() {
    InstanceHandler<?> currentHandler = instanceStack.peek();
    IPathInstance pathInstance = currentHandler.newPathInstance();
    pathStack.push(pathInstance);
//    logger.info(getPath(PathBuilder.PathType.METAPATH));
  }

  @Override
  public void pushItem(int position) {
    InstanceHandler<?> currentHandler = instanceStack.peek();
    IPathInstance pathInstance = currentHandler.newPathInstance(position);
    pathStack.push(pathInstance);
//    logger.info(getPath(PathBuilder.PathType.METAPATH));
  }

  @Override
  public void pushItem(String key) {
    InstanceHandler<?> currentHandler = instanceStack.peek();
    IPathInstance pathInstance = currentHandler.newPathInstance(key);
    pathStack.push(pathInstance);
//    logger.info(getPath(PathBuilder.PathType.METAPATH));
  }

  @Override
  public IPathInstance popItem() {
    return pathStack.pop();
  }

  protected Deque<IPathInstance> getPath() {
    return pathStack;
  }

  @Override
  public String getPath(PathBuilder.PathType pathType) {
    return pathType.getFormatter().apply(getPath());

  }
}
