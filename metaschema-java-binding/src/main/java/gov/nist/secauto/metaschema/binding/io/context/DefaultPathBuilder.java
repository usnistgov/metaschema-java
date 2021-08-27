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

import gov.nist.secauto.metaschema.model.common.instance.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.instance.IInstance;
import gov.nist.secauto.metaschema.model.common.instance.INamedInstance;
import gov.nist.secauto.metaschema.model.common.instance.INamedModelInstance;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DefaultPathBuilder implements PathBuilder {
  private final LinkedList<INamedInstance> instanceStack = new LinkedList<>();
  private final Deque<IPathInstance> pathStack = new LinkedList<>();

//  public void pushInstance(INamedInstance instance) {
//    instanceStack.push(instance);
////    logger.info(getXPath());
//  }

  public void pushItem(Object instance) {

  }

  public void pushItem(Object instance, int position) {

  }

  public void popItem() {
    
  }
  
  public void pushInstance(IFlagInstance instance) {
//    IPathInstance pathInstance = new SingletonPathInstance(instance);
//    pushInstance(pathInstance);
  }

  public void pushInstance(INamedModelInstance instance) {
//    IPathInstance pathInstance = new SingletonPathInstance(instance);
//    pushInstance(pathInstance);
  }

  protected void pushInstance(IPathInstance pathInstance) {
//    pathStack.push(pathInstance);
  }


  @Override
  public INamedInstance popInstance() {
//    return instanceStack.pop();
    return null;
  }

  public List<IPathInstance> getPath() {
    return new ArrayList<>(pathStack);
  }

  @Override
  public String getXPath() {

    StringBuilder builder = new StringBuilder();
    boolean first = true;
    
    Iterator<INamedInstance> iter = instanceStack.descendingIterator();
    while (iter.hasNext()) {
      INamedInstance instance = iter.next();
      if (!first) {
        builder.append('/');
      } else {
        first = false;
      }
      if (instance instanceof IFlagInstance) {
        IFlagInstance flag = (IFlagInstance) instance;
        builder.append('@');
        
      }
      builder.append(instance.getEffectiveName());
    }
    return builder.toString();
  }

}
