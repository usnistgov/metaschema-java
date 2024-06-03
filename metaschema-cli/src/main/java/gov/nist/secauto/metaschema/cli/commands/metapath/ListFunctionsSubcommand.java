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

package gov.nist.secauto.metaschema.cli.commands.metapath;

import gov.nist.secauto.metaschema.cli.processor.CLIProcessor.CallingContext;
import gov.nist.secauto.metaschema.cli.processor.ExitCode;
import gov.nist.secauto.metaschema.cli.processor.ExitStatus;
import gov.nist.secauto.metaschema.cli.processor.command.AbstractTerminalCommand;
import gov.nist.secauto.metaschema.cli.processor.command.ICommandExecutor;
import gov.nist.secauto.metaschema.core.metapath.StaticContext;
import gov.nist.secauto.metaschema.core.metapath.function.FunctionService;
import gov.nist.secauto.metaschema.core.metapath.function.IArgument;
import gov.nist.secauto.metaschema.core.metapath.function.IFunction;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

public class ListFunctionsSubcommand
    extends AbstractTerminalCommand {
  private static final Logger LOGGER = LogManager.getLogger(ListFunctionsSubcommand.class);

  @NonNull
  private static final String COMMAND = "list-functions";

  @Override
  public String getName() {
    return COMMAND;
  }

  @Override
  public String getDescription() {
    return "Get a listing of supported Metapath functions";
  }

  @Override
  public ICommandExecutor newExecutor(CallingContext callingContext, CommandLine cmdLine) {
    return ICommandExecutor.using(callingContext, cmdLine, this::executeCommand);
  }

  @SuppressWarnings({
      "PMD.OnlyOneReturn", // readability
      "unused"
  })
  protected ExitStatus executeCommand(
      @NonNull CallingContext callingContext,
      @NonNull CommandLine cmdLine) {

    Map<String, Map<String, List<IFunction>>> namespaceToNameToFunctionMap = FunctionService.getInstance().stream()
        .collect(Collectors.groupingBy(
            IFunction::getNamespace,
            Collectors.groupingBy(
                IFunction::getName,
                Collectors.toList())));

    Map<String, String> namespaceToPrefixMap = StaticContext.getWellKnownNamespaces().entrySet().stream()
        .collect(Collectors.toMap(entry -> entry.getValue().toASCIIString(), Map.Entry::getKey));

    List<String> namespaces = new ArrayList<>(namespaceToNameToFunctionMap.keySet());

    Collections.sort(namespaces);

    for (String namespace : namespaces) {
      String prefix = namespaceToPrefixMap.get(namespace);

      if (prefix == null) {
        LOGGER.atInfo().log("In namespace '{}':", namespace);
      } else {
        LOGGER.atInfo().log("In namespace '{}' as '{}':", namespace, prefix);
      }

      Map<String, List<IFunction>> namespacedFunctions = namespaceToNameToFunctionMap.get(namespace);

      List<String> names = new ArrayList<>(namespacedFunctions.keySet());
      Collections.sort(names);

      for (String name : names) {
        List<IFunction> functions = namespacedFunctions.get(name);
        Collections.sort(functions, Comparator.comparing(IFunction::arity));

        for (IFunction function : functions) {
          String functionRef = prefix == null
              ? String.format("Q{%s}%s", function.getNamespace(), function.getName())
              : String.format("%s:%s", prefix, function.getName());

          LOGGER.atInfo().log(String.format("%s(%s) as %s",
              functionRef,
              function.getArguments().isEmpty()
                  ? ""
                  : function.getArguments().stream().map(IArgument::toSignature)
                      .collect(Collectors.joining(","))
                      + (function.isArityUnbounded() ? ", ..." : ""),
              function.getResult().toSignature()));
        }
      }
    }
    return ExitCode.OK.exit();
  }
}
