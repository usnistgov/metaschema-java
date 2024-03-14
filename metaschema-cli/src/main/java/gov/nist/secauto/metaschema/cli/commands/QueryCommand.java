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

package gov.nist.secauto.metaschema.cli.commands;

import gov.nist.secauto.metaschema.cli.processor.CLIProcessor.CallingContext;
import gov.nist.secauto.metaschema.cli.processor.ExitCode;
import gov.nist.secauto.metaschema.cli.processor.ExitStatus;
import gov.nist.secauto.metaschema.cli.processor.InvalidArgumentException;
import gov.nist.secauto.metaschema.cli.processor.command.AbstractTerminalCommand;
import gov.nist.secauto.metaschema.cli.processor.command.DefaultExtraArgument;
import gov.nist.secauto.metaschema.cli.processor.command.ExtraArgument;
import gov.nist.secauto.metaschema.cli.processor.command.ICommandExecutor;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.model.xml.ModuleLoader;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.IBoundLoader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public class QueryCommand
    extends AbstractTerminalCommand {
  private static final Logger LOGGER = LogManager.getLogger(QueryCommand.class);
  @NonNull
  private static final String COMMAND = "query";
  @NonNull
  public static final Option CONTENT_OPTION = ObjectUtils.notNull(
      Option.builder("i")
          .hasArg()
          .argName("FILE")
          //.required()
          .desc("metaschema content instance resource")
          .build());
  @NonNull
  private static final List<ExtraArgument> EXTRA_ARGUMENTS = ObjectUtils.notNull(List.of(
      new DefaultExtraArgument("metapath", true)));

  @Override
  public String getName() {
    return COMMAND;
  }

  @Override
  public String getDescription() {
    return "Execute a Metapath query, optionally with a module and a related instance.";
  }

  @Override
  public Collection<? extends Option> gatherOptions() {
    Collection<? extends Option> orig = super.gatherOptions();

    List<Option> retval = new ArrayList<>(orig.size() + 1);
    retval.addAll(orig);
    retval.add(MetaschemaCommandSupport.METASCHEMA_OPTION);
    retval.add(CONTENT_OPTION);

    return CollectionUtil.unmodifiableCollection(retval);
  }

  @Override
  public List<ExtraArgument> getExtraArguments() {
    return EXTRA_ARGUMENTS;
  }

  @Override
  public void validateOptions(CallingContext callingContext, CommandLine cmdLine) throws InvalidArgumentException {
    super.validateOptions(callingContext, cmdLine);

    String metaschemaName = cmdLine.getOptionValue(MetaschemaCommandSupport.METASCHEMA_OPTION);
    Path metaschema = Paths.get(metaschemaName);
    if (!Files.exists(metaschema)) {
      throw new InvalidArgumentException("The provided module '" + metaschema + "' does not exist.");
    }
    if (!Files.isReadable(metaschema)) {
      throw new InvalidArgumentException("The provided module '" + metaschema + "' is not readable.");
    }

    String contentInstanceName = cmdLine.getOptionValue(CONTENT_OPTION);
    Path contentInstance = Paths.get(contentInstanceName);
    if (!Files.exists(contentInstance)) {
      throw new InvalidArgumentException("The provided content instance '" + metaschema + "' does not exist.");
    }
    if (!Files.isReadable(contentInstance)) {
      throw new InvalidArgumentException("The provided content instance '" + metaschema + "' is not readable.");
    }
  }

  @Override
  public ICommandExecutor newExecutor(CallingContext callingContext, CommandLine commandLine) {
    return ICommandExecutor.using(callingContext, commandLine, this::executeCommand);
  }

  @SuppressWarnings({
      "PMD.OnlyOneReturn", // readability
      "unused"
  })
  protected ExitStatus executeCommand(
      @NonNull CallingContext callingContext,
      @NonNull CommandLine cmdLine) {
    List<String> extraArgs = cmdLine.getArgList();

    String metpath = extraArgs.get(0);

    String metaschemaName = cmdLine.getOptionValue(MetaschemaCommandSupport.METASCHEMA_OPTION);
    Path metaschema = Paths.get(metaschemaName);

    String contentInstanceName = cmdLine.getOptionValue(CONTENT_OPTION);
    Path contentInstance = Paths.get(contentInstanceName);

    IBindingContext bindingContext = IBindingContext.instance();

    try {
      // load the metaschema module
      ModuleLoader moduleLoader = new ModuleLoader();
      moduleLoader.allowEntityResolution();
      IModule module = moduleLoader.load(metaschema);

      // generate class bindings and register them with the binding context
      Path classPath = Files.createTempDirectory("metaschema-classes-");
      bindingContext.registerModule(module, classPath);
    } catch (IOException | MetaschemaException ex) {
      return ExitCode.PROCESSING_ERROR.exit().withThrowable(ex); // NOPMD readability
    }

    // load the content
    IBoundLoader contentLoader = bindingContext.newBoundLoader();

    IDocumentNodeItem contentObject;
    try {
      contentObject = contentLoader.loadAsNodeItem(contentInstance);
    } catch (IOException ex) {
      return ExitCode.IO_ERROR.exit().withThrowable(ex);
    }

    // executing the query against the content
    ISequence<?> sequence = MetapathExpression.compile(metpath).evaluate(contentObject);
    LOGGER.info(sequence);

    return ExitCode.OK.exit();
  }
}
