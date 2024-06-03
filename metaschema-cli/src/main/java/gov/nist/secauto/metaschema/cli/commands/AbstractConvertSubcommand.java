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
import gov.nist.secauto.metaschema.cli.processor.OptionUtils;
import gov.nist.secauto.metaschema.cli.processor.command.AbstractCommandExecutor;
import gov.nist.secauto.metaschema.cli.processor.command.AbstractTerminalCommand;
import gov.nist.secauto.metaschema.cli.processor.command.DefaultExtraArgument;
import gov.nist.secauto.metaschema.cli.processor.command.ExtraArgument;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.core.util.UriUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.io.IBoundLoader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractConvertSubcommand
    extends AbstractTerminalCommand {
  private static final Logger LOGGER = LogManager.getLogger(AbstractConvertSubcommand.class);

  @NonNull
  private static final String COMMAND = "convert";
  @NonNull
  private static final List<ExtraArgument> EXTRA_ARGUMENTS = ObjectUtils.notNull(List.of(
      new DefaultExtraArgument("source-file-or-URL", true),
      new DefaultExtraArgument("destination-file", false)));

  @NonNull
  private static final Option OVERWRITE_OPTION = ObjectUtils.notNull(
      Option.builder()
          .longOpt("overwrite")
          .desc("overwrite the destination if it exists")
          .build());
  @NonNull
  private static final Option TO_OPTION = ObjectUtils.notNull(
      Option.builder()
          .longOpt("to")
          .required()
          .hasArg().argName("FORMAT")
          .desc("convert to format: xml, json, or yaml")
          .build());

  @Override
  public String getName() {
    return COMMAND;
  }

  @Override
  public Collection<? extends Option> gatherOptions() {
    return ObjectUtils.notNull(List.of(
        OVERWRITE_OPTION,
        TO_OPTION));
  }

  @Override
  public List<ExtraArgument> getExtraArguments() {
    return EXTRA_ARGUMENTS;
  }

  @SuppressWarnings("PMD.PreserveStackTrace") // intended
  @Override
  public void validateOptions(CallingContext callingContext, CommandLine cmdLine) throws InvalidArgumentException {

    try {
      String toFormatText = cmdLine.getOptionValue(TO_OPTION);
      Format.valueOf(toFormatText.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      InvalidArgumentException newEx = new InvalidArgumentException(
          String.format("Invalid '%s' argument. The format must be one of: %s.",
              OptionUtils.toArgument(TO_OPTION),
              Format.names().stream()
                  .collect(CustomCollectors.joiningWithOxfordComma("and"))));
      newEx.setOption(TO_OPTION);
      newEx.addSuppressed(ex);
      throw newEx;
    }

    List<String> extraArgs = cmdLine.getArgList();
    if (extraArgs.isEmpty() || extraArgs.size() > 2) {
      throw new InvalidArgumentException("Illegal number of arguments.");
    }
  }

  protected abstract static class AbstractConversionCommandExecutor
      extends AbstractCommandExecutor {

    public AbstractConversionCommandExecutor(
        @NonNull CallingContext callingContext,
        @NonNull CommandLine commandLine) {
      super(callingContext, commandLine);
    }

    @NonNull
    protected abstract IBindingContext getBindingContext();

    @NonNull
    protected abstract Class<?> getLoadedClass();

    @SuppressWarnings({
        "PMD.OnlyOneReturn", // readability
        "PMD.CyclomaticComplexity", "PMD.CognitiveComplexity" // reasonable
    })
    @Override
    public ExitStatus execute() {
      CommandLine cmdLine = getCommandLine();

      List<String> extraArgs = cmdLine.getArgList();

      Path destination = null;
      if (extraArgs.size() > 1) {
        destination = Paths.get(extraArgs.get(1)).toAbsolutePath();
      }

      if (destination != null) {
        if (Files.exists(destination)) {
          if (!cmdLine.hasOption(OVERWRITE_OPTION)) {
            return ExitCode.INVALID_ARGUMENTS.exitMessage(
                String.format("The provided destination '%s' already exists and the '%s' option was not provided.",
                    destination,
                    OptionUtils.toArgument(OVERWRITE_OPTION)));
          }
          if (!Files.isWritable(destination)) {
            return ExitCode.IO_ERROR.exitMessage(
                "The provided destination '" + destination + "' is not writable.");
          }
        } else {
          Path parent = destination.getParent();
          if (parent != null) {
            try {
              Files.createDirectories(parent);
            } catch (IOException ex) {
              return ExitCode.INVALID_TARGET.exit().withThrowable(ex); // NOPMD readability
            }
          }
        }
      }

      String sourceName = extraArgs.get(0);
      URI source;
      URI cwd = Paths.get("").toAbsolutePath().toUri();
      try {
        source = UriUtils.toUri(sourceName, cwd);
      } catch (URISyntaxException ex) {
        return ExitCode.IO_ERROR.exitMessage("Cannot load source '%s' as it is not a valid file or URI.")
            .withThrowable(ex);
      }
      assert source != null;

      String toFormatText = cmdLine.getOptionValue(TO_OPTION);
      Format toFormat = Format.valueOf(toFormatText.toUpperCase(Locale.ROOT));

      IBindingContext bindingContext = getBindingContext();
      try {
        IBoundLoader loader = bindingContext.newBoundLoader();
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info("Converting '{}'.", source);
        }

        if (destination == null) {
          loader.convert(source, ObjectUtils.notNull(System.out), toFormat, getLoadedClass());
        } else {
          loader.convert(source, destination, toFormat, getLoadedClass());
        }
      } catch (IOException | IllegalArgumentException ex) {
        return ExitCode.PROCESSING_ERROR.exit().withThrowable(ex); // NOPMD readability
      }
      if (destination != null && LOGGER.isInfoEnabled()) {
        LOGGER.info("Generated {} file: {}", toFormat.toString(), destination);
      }
      return ExitCode.OK.exit();
    }

  }
}
