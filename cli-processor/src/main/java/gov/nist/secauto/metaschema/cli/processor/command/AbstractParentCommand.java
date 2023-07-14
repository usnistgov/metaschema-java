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

package gov.nist.secauto.metaschema.cli.processor.command;

import gov.nist.secauto.metaschema.cli.processor.CLIProcessor.CallingContext;
import gov.nist.secauto.metaschema.cli.processor.ExitCode;
import gov.nist.secauto.metaschema.cli.processor.ExitStatus;

import org.apache.commons.cli.CommandLine;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractParentCommand implements ICommand {
  @NonNull
  private final Map<String, ICommand> commandToSubcommandHandlerMap;
  private final boolean subCommandRequired;

  @SuppressWarnings("null")
  protected AbstractParentCommand(boolean subCommandRequired) {
    this.commandToSubcommandHandlerMap = Collections.synchronizedMap(new LinkedHashMap<>());
    this.subCommandRequired = subCommandRequired;
  }

  protected void addCommandHandler(ICommand handler) {
    String commandName = handler.getName();
    this.commandToSubcommandHandlerMap.put(commandName, handler);
  }

  @Override
  public ICommand getSubCommandByName(String name) {
    return commandToSubcommandHandlerMap.get(name);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<ICommand> getSubCommands() {
    return Collections.unmodifiableCollection(commandToSubcommandHandlerMap.values());
  }

  @Override
  public boolean isSubCommandRequired() {
    return subCommandRequired;
  }

  @Override
  public ICommandExecutor newExecutor(CallingContext callingContext, CommandLine cmdLine) {
    return ICommandExecutor.using(callingContext, cmdLine, this::executeCommand);
  }

  @NonNull
  protected ExitStatus executeCommand(
      @NonNull CallingContext callingContext,
      @NonNull CommandLine commandLine) {
    callingContext.showHelp();
    ExitStatus status;
    if (isSubCommandRequired()) {
      status = ExitCode.INVALID_COMMAND
          .exitMessage("Please use one of the following sub-commands: " +
              getSubCommands().stream()
                  .map(command -> command.getName())
                  .collect(Collectors.joining(", ")));
    } else {
      status = ExitCode.OK.exit();
    }
    return status;
  }

}
