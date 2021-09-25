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

package gov.nist.secauto.metaschema.model.common.metapath.function;

import gov.nist.secauto.metaschema.model.common.metapath.ast.IExpression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractFunctionLibrary implements FunctionLibrary {

  private final HashMap<String, Set<Function>> library = new HashMap<>();

  protected HashMap<String, Set<Function>> getLibrary() {
    return library;
  }

  public synchronized void registerFunction(Function function) {
    String name = function.getName();

    Set<Function> functions = getLibrary().get(name);
    if (functions == null) {
      functions = new HashSet<>();
      library.put(name, functions);
    }
    functions.add(function);
  }

  @Override
  public synchronized boolean hasFunction(String name, List<IExpression> args) {
    Set<Function> functions = getLibrary().get(name);
    boolean retval;
    if (functions == null) {
      retval = false;
    } else {
      retval = functions.stream().anyMatch(x -> x.isArgumentsSupported(args));
    }
    return retval;
  }

  @Override
  public Function getFunction(String name, List<IExpression> args) {
    Set<Function> functions = getLibrary().get(name);
    Function retval;
    if (functions == null) {
      retval = null;
    } else {
      retval = functions.stream().filter(x -> x.isArgumentsSupported(args)).findFirst().orElse(null);
    }
    return retval;
  }

}