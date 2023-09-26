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

import gov.nist.secauto.metaschema.cli.processor.CLIProcessor;
import gov.nist.secauto.metaschema.cli.processor.CLIProcessor.CallingContext;
import gov.nist.secauto.metaschema.cli.processor.ExitCode;
import gov.nist.secauto.metaschema.cli.processor.ExitStatus;
import gov.nist.secauto.metaschema.cli.processor.InvalidArgumentException;
import gov.nist.secauto.metaschema.cli.processor.OptionUtils;
import gov.nist.secauto.metaschema.cli.processor.command.AbstractCommandExecutor;
import gov.nist.secauto.metaschema.cli.processor.command.AbstractTerminalCommand;
import gov.nist.secauto.metaschema.cli.processor.command.DefaultExtraArgument;
import gov.nist.secauto.metaschema.cli.processor.command.ExtraArgument;
import gov.nist.secauto.metaschema.cli.util.LoggingValidationHandler;
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraintSet;
import gov.nist.secauto.metaschema.core.model.validation.IValidationResult;
import gov.nist.secauto.metaschema.core.model.xml.ConstraintLoader;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.IBindingContext.IValidationSchemaProvider;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.io.IBoundLoader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractValidateContentCommand
    extends AbstractTerminalCommand {
  private static final Logger LOGGER = LogManager.getLogger(AbstractValidateContentCommand.class);
  @NonNull
  private static final String COMMAND = "validate";
  @NonNull
  private static final List<ExtraArgument> EXTRA_ARGUMENTS = ObjectUtils.notNull(List.of(
      new DefaultExtraArgument("file to validate", true)));

  @NonNull
  private static final Option AS_OPTION = ObjectUtils.notNull(
      Option.builder()
          .longOpt("as")
          .hasArg()
          .argName("FORMAT")
          .desc("source format: xml, json, or yaml")
          .build());
  @NonNull
  private static final Option CONSTRAINTS_OPTION = ObjectUtils.notNull(
      Option.builder("c")
          .hasArg()
          .argName("FILE")
          .desc("additional constraint definitions")
          .build());

  @Override
  public String getName() {
    return COMMAND;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<? extends Option> gatherOptions() {
    return List.of(
        AS_OPTION,
        CONSTRAINTS_OPTION);
  }

  @Override
  public List<ExtraArgument> getExtraArguments() {
    return EXTRA_ARGUMENTS;
  }

  @SuppressWarnings("PMD.PreserveStackTrace") // intended
  @Override
  public void validateOptions(CallingContext callingContext, CommandLine cmdLine) throws InvalidArgumentException {
    if (cmdLine.hasOption(CONSTRAINTS_OPTION)) {
      String[] args = cmdLine.getOptionValues(CONSTRAINTS_OPTION);
      for (String arg : args) {
        Path constraint = Paths.get(arg);
        if (!Files.exists(constraint)) {
          throw new InvalidArgumentException(
              "The provided external constraint file '" + constraint + "' does not exist.");
        }
        if (!Files.isRegularFile(constraint)) {
          throw new InvalidArgumentException(
              "The provided external constraint file '" + constraint + "' is not a file.");
        }
        if (!Files.isReadable(constraint)) {
          throw new InvalidArgumentException(
              "The provided external constraint file '" + constraint + "' is not readable.");
        }
      }
    }

    List<String> extraArgs = cmdLine.getArgList();
    if (extraArgs.size() != 1) {
      throw new InvalidArgumentException("The source to validate must be provided.");
    }

    Path source = Paths.get(extraArgs.get(0));
    if (!Files.exists(source)) {
      throw new InvalidArgumentException("The provided source file '" + source + "' does not exist.");
    }
    if (!Files.isReadable(source)) {
      throw new InvalidArgumentException("The provided source file '" + source + "' is not readable.");
    }

    if (cmdLine.hasOption(AS_OPTION)) {
      try {
        String toFormatText = cmdLine.getOptionValue(AS_OPTION);
        Format.valueOf(toFormatText.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException ex) {
        InvalidArgumentException newEx = new InvalidArgumentException(
            String.format("Invalid '%s' argument. The format must be one of: %s.",
                OptionUtils.toArgument(AS_OPTION),
                Arrays.asList(Format.values()).stream()
                    .map(format -> format.name())
                    .collect(CustomCollectors.joiningWithOxfordComma("and"))));
        newEx.addSuppressed(ex);
        throw newEx;
      }
    }
  }

  protected abstract class AbstractValidationCommandExecutor
      extends AbstractCommandExecutor
      implements IValidationSchemaProvider {

    public AbstractValidationCommandExecutor(
        @NonNull CallingContext callingContext,
        @NonNull CommandLine commandLine) {
      super(callingContext, commandLine);
    }

    @NonNull
    protected abstract IBindingContext getBindingContext(@NonNull Set<IConstraintSet> constraintSets)
        throws MetaschemaException, IOException;

    @SuppressWarnings("PMD.OnlyOneReturn") // readability
    @Override
    public ExitStatus execute() {
      CommandLine cmdLine = getCommandLine();

      Set<IConstraintSet> constraintSets;
      if (cmdLine.hasOption(CONSTRAINTS_OPTION)) {
        ConstraintLoader constraintLoader = new ConstraintLoader();
        constraintSets = new LinkedHashSet<>();
        String[] args = cmdLine.getOptionValues(CONSTRAINTS_OPTION);
        for (String arg : args) {
          Path constraintPath = Paths.get(arg);
          assert constraintPath != null;
          try {
            constraintSets.add(constraintLoader.load(constraintPath));
          } catch (IOException | MetaschemaException ex) {
            return ExitCode.IO_ERROR.exitMessage("Unable to load constraint set '" + arg + "'.").withThrowable(ex);
          }
        }
      } else {
        constraintSets = CollectionUtil.emptySet();
      }
      IBindingContext bindingContext;
      try {
        bindingContext = getBindingContext(constraintSets);
      } catch (IOException | MetaschemaException ex) {
        return ExitCode.PROCESSING_ERROR
            .exitMessage("Unable to get binding context. " + ex.getMessage())
            .withThrowable(ex);
      }

      IBoundLoader loader = bindingContext.newBoundLoader();

      List<String> extraArgs = cmdLine.getArgList();
      @SuppressWarnings("null") Path source = resolvePathAgainstCWD(Paths.get(extraArgs.get(0)));
      assert source != null;

      Format asFormat;
      if (cmdLine.hasOption(AS_OPTION)) {
        try {
          String toFormatText = cmdLine.getOptionValue(AS_OPTION);
          asFormat = Format.valueOf(toFormatText.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
          return ExitCode.IO_ERROR
              .exitMessage("Invalid '--as' argument. The format must be one of: "
                  + Arrays.stream(Format.values())
                      .map(format -> format.name())
                      .collect(CustomCollectors.joiningWithOxfordComma("or")))
              .withThrowable(ex);
        }
      } else {
        // attempt to determine the format
        try {
          asFormat = loader.detectFormat(source);
        } catch (FileNotFoundException ex) {
          // this case was already checked for
          return ExitCode.IO_ERROR.exitMessage("The provided source file '" + source + "' does not exist.");
        } catch (IOException ex) {
          return ExitCode.PROCESSING_ERROR.exit().withThrowable(ex);
        } catch (IllegalArgumentException ex) {
          return ExitCode.IO_ERROR.exitMessage(
              "Source file has unrecognizable format. Use '--as' to specify the format. The format must be one of: "
                  + Arrays.stream(Format.values())
                      .map(format -> format.name())
                      .collect(CustomCollectors.joiningWithOxfordComma("or")));
        }
      }

      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Validating '{}' as {}.", source, asFormat.name());
      }

      IValidationResult validationResult;
      try {
        validationResult = bindingContext.validate(source, asFormat, this);
      } catch (IOException | SAXException ex) {
        return ExitCode.PROCESSING_ERROR.exit().withThrowable(ex);
      }

      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Validation identified the following in file '{}'.", source);
      }

      LoggingValidationHandler.instance().handleValidationResults(validationResult);

      if (validationResult.isPassing() && !cmdLine.hasOption(CLIProcessor.QUIET_OPTION) && LOGGER.isInfoEnabled()) {
        LOGGER.info("The file '{}' is valid.", source);
      }

      return (validationResult.isPassing() ? ExitCode.OK : ExitCode.FAIL).exit();
    }

  }
}
