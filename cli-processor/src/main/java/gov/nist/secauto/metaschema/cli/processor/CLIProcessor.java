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

package gov.nist.secauto.metaschema.cli.processor;

import static org.fusesource.jansi.Ansi.ansi;

import gov.nist.secauto.metaschema.cli.processor.command.CommandService;
import gov.nist.secauto.metaschema.cli.processor.command.ExtraArgument;
import gov.nist.secauto.metaschema.cli.processor.command.ICommand;
import gov.nist.secauto.metaschema.cli.processor.command.ICommandExecutor;
import gov.nist.secauto.metaschema.core.util.IVersionInfo;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiPrintStream;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class CLIProcessor {
  private static final Logger LOGGER = LogManager.getLogger(CLIProcessor.class);

  @SuppressWarnings("null")
  @NonNull
  public static final Option HELP_OPTION = Option.builder("h")
      .longOpt("help")
      .desc("display this help message")
      .build();
  @SuppressWarnings("null")
  @NonNull
  public static final Option NO_COLOR_OPTION = Option.builder()
      .longOpt("no-color")
      .desc("do not colorize output")
      .build();
  @SuppressWarnings("null")
  @NonNull
  public static final Option QUIET_OPTION = Option.builder("q")
      .longOpt("quiet")
      .desc("minimize output to include only errors")
      .build();
  @SuppressWarnings("null")
  @NonNull
  public static final Option SHOW_STACK_TRACE_OPTION = Option.builder()
      .longOpt("show-stack-trace")
      .desc("display the stack trace associated with an error")
      .build();
  @SuppressWarnings("null")
  @NonNull
  public static final Option VERSION_OPTION = Option.builder()
      .longOpt("version")
      .desc("display the application version")
      .build();
  @SuppressWarnings("null")
  @NonNull
  public static final List<Option> OPTIONS = List.of(
      HELP_OPTION,
      NO_COLOR_OPTION,
      QUIET_OPTION,
      SHOW_STACK_TRACE_OPTION,
      VERSION_OPTION);

  public static final String COMMAND_VERSION = "http://csrc.nist.gov/ns/metaschema-java/cli/command-version";

  @NonNull
  private final List<ICommand> commands = new LinkedList<>();
  @NonNull
  private final String exec;
  @NonNull
  private final Map<String, IVersionInfo> versionInfos;

  public static void main(String... args) {
    System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
    CLIProcessor processor = new CLIProcessor("metaschema-cli");

    CommandService.getInstance().getCommands().stream().forEach(command -> {
      assert command != null;
      processor.addCommandHandler(command);
    });
    System.exit(processor.process(args).getExitCode().getStatusCode());
  }

  @SuppressWarnings("null")
  public CLIProcessor(@NonNull String exec) {
    this(exec, Map.of());
  }

  public CLIProcessor(@NonNull String exec, @NonNull Map<String, IVersionInfo> versionInfos) {
    this.exec = exec;
    this.versionInfos = versionInfos;
    AnsiConsole.systemInstall();
  }

  /**
   * Gets the command used to execute for use in help text.
   *
   * @return the command name
   */
  @NonNull
  public String getExec() {
    return exec;
  }

  /**
   * Retrieve the version information for this application.
   *
   * @return the versionInfo
   */
  @NonNull
  public Map<String, IVersionInfo> getVersionInfos() {
    return versionInfos;
  }

  public void addCommandHandler(@NonNull ICommand handler) {
    commands.add(handler);
  }

  /**
   * Process a set of CLIProcessor arguments.
   * <p>
   * process().getExitCode().getStatusCode()
   *
   * @param args
   *          the arguments to process
   * @return the exit status
   */
  @NonNull
  public ExitStatus process(String... args) {
    return parseCommand(args);
  }

  @NonNull
  private ExitStatus parseCommand(String... args) {
    List<String> commandArgs = Arrays.asList(args);
    assert commandArgs != null;
    CallingContext callingContext = new CallingContext(commandArgs);

    ExitStatus status;
    // the first two arguments should be the <command> and <operation>, where <type>
    // is the object type
    // the <operation> is performed against.
    if (commandArgs.isEmpty()) {
      status = ExitCode.INVALID_COMMAND.exit();
      callingContext.showHelp();
    } else {
      status = callingContext.processCommand();
    }
    return status;
  }

  protected final List<ICommand> getTopLevelCommands() {
    List<ICommand> retval = Collections.unmodifiableList(commands);
    assert retval != null;
    return retval;
  }

  private static void handleNoColor() {
    System.setProperty(AnsiConsole.JANSI_MODE, AnsiConsole.JANSI_MODE_STRIP);
    AnsiConsole.systemUninstall();
  }

  public static void handleQuiet() {
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false); // NOPMD not closable here
    Configuration config = ctx.getConfiguration();
    LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
    Level oldLevel = loggerConfig.getLevel();
    if (oldLevel.isLessSpecificThan(Level.ERROR)) {
      loggerConfig.setLevel(Level.ERROR);
      ctx.updateLoggers();
    }
  }

  protected void showVersion() {
    @SuppressWarnings("resource") PrintStream out = AnsiConsole.out(); // NOPMD - not owner
    getVersionInfos().values().stream().forEach(info -> {
      out.println(ansi()
          .bold().a(info.getName()).boldOff()
          .a(" ")
          .bold().a(info.getVersion()).boldOff()
          .a(" built at ")
          .bold().a(info.getBuildTimestamp()).boldOff()
          .a(" from branch ")
          .bold().a(info.getGitBranch()).boldOff()
          .a(" (")
          .bold().a(info.getGitCommit()).boldOff()
          .a(") at ")
          .bold().a(info.getGitOriginUrl()).boldOff()
          .reset());
    });
    out.flush();
  }

  // @SuppressWarnings("null")
  // @NonNull
  // public String[] getArgArray() {
  // return Stream.concat(options.stream(), extraArgs.stream()).toArray(size ->
  // new String[size]);
  // }

  public class CallingContext {
    @NonNull
    private final List<Option> options;
    @NonNull
    private final Deque<ICommand> calledCommands;
    @NonNull
    private final List<String> extraArgs;

    @SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW", justification = "Use of final fields")
    public CallingContext(@NonNull List<String> args) {
      Map<String, ICommand> topLevelCommandMap = getTopLevelCommands().stream()
          .collect(Collectors.toUnmodifiableMap(ICommand::getName, Function.identity()));

      List<Option> options = new LinkedList<>(OPTIONS);
      Deque<ICommand> calledCommands = new LinkedList<>();
      List<String> extraArgs = new LinkedList<>();

      boolean endArgs = false;
      for (String arg : args) {
        if (endArgs || arg.startsWith("-")) {
          extraArgs.add(arg);
        } else if ("--".equals(arg)) {
          endArgs = true;
        } else {
          ICommand command;
          if (calledCommands.isEmpty()) {
            command = topLevelCommandMap.get(arg);
          } else {
            command = calledCommands.getLast();
            command = command.getSubCommandByName(arg);
          }

          if (command == null) {
            extraArgs.add(arg);
            endArgs = true;
          } else {
            calledCommands.add(command);
          }
        }
      }

      if (LOGGER.isDebugEnabled()) {
        String commandChain = calledCommands.stream()
            .map(ICommand::getName)
            .collect(Collectors.joining(" -> "));
        LOGGER.debug("Processing command chain: {}", commandChain);
      }

      for (ICommand cmd : calledCommands) {
        options.addAll(cmd.gatherOptions());
      }

      options = Collections.unmodifiableList(options);
      extraArgs = Collections.unmodifiableList(extraArgs);

      assert options != null;
      assert extraArgs != null;

      this.options = options;
      this.calledCommands = calledCommands;
      this.extraArgs = extraArgs;
    }

    @NonNull
    public CLIProcessor getCLIProcessor() {
      return CLIProcessor.this;
    }

    @Nullable
    public ICommand getTargetCommand() {
      return calledCommands.peekLast();
    }

    @NonNull
    protected List<Option> getOptionsList() {
      return options;
    }

    @NonNull
    private Deque<ICommand> getCalledCommands() {
      return calledCommands;
    }

    @NonNull
    protected List<String> getExtraArgs() {
      return extraArgs;
    }

    protected Options toOptions() {
      Options retval = new Options();
      for (Option option : getOptionsList()) {
        retval.addOption(option);
      }
      return retval;
    }

    @SuppressWarnings("PMD.OnlyOneReturn") // readability
    @NonNull
    public ExitStatus processCommand() {
      CommandLineParser parser = new DefaultParser();
      CommandLine cmdLine;

      // this uses a two phase approach where:
      // phase 1: checks if help or version are used
      // phase 2: executes the command

      // phase 1
      ExitStatus retval = null;
      {
        try {
          Options phase1Options = new Options();
          phase1Options.addOption(HELP_OPTION);
          phase1Options.addOption(VERSION_OPTION);

          cmdLine = parser.parse(phase1Options, getExtraArgs().toArray(new String[0]), true);
        } catch (ParseException ex) {
          String msg = ex.getMessage();
          assert msg != null;
          return handleInvalidCommand(msg);
        }

        if (cmdLine.hasOption(VERSION_OPTION)) {
          showVersion();
          retval = ExitCode.OK.exit();
        } else if (cmdLine.hasOption(HELP_OPTION)) {
          showHelp();
          retval = ExitCode.OK.exit();
        }
      }

      if (retval == null) {
        // phase 2
        try {
          cmdLine = parser.parse(toOptions(), getExtraArgs().toArray(new String[0]));
        } catch (ParseException ex) {
          String msg = ex.getMessage();
          assert msg != null;
          return handleInvalidCommand(msg);
        }

        if (cmdLine.hasOption(NO_COLOR_OPTION)) {
          handleNoColor();
        }

        if (cmdLine.hasOption(QUIET_OPTION)) {
          handleQuiet();
        }
        retval = invokeCommand(cmdLine);
      }

      retval.generateMessage(cmdLine.hasOption(SHOW_STACK_TRACE_OPTION));
      return retval;
    }

    @SuppressWarnings({
        "PMD.OnlyOneReturn", // readability
        "PMD.AvoidCatchingGenericException" // needed here
    })
    protected ExitStatus invokeCommand(@NonNull CommandLine cmdLine) {
      ExitStatus retval;
      try {
        for (ICommand cmd : getCalledCommands()) {
          try {
            cmd.validateOptions(this, cmdLine);
          } catch (InvalidArgumentException ex) {
            String msg = ex.getMessage();
            assert msg != null;
            return handleInvalidCommand(msg);
          }
        }

        ICommand targetCommand = getTargetCommand();
        if (targetCommand == null) {
          retval = ExitCode.INVALID_COMMAND.exit();
        } else {
          ICommandExecutor executor = targetCommand.newExecutor(this, cmdLine);
          retval = executor.execute();
        }

        if (ExitCode.INVALID_COMMAND.equals(retval.getExitCode())) {
          showHelp();
        }
      } catch (RuntimeException ex) {
        retval = ExitCode.RUNTIME_ERROR
            .exitMessage(String.format("An uncaught runtime error occured. %s", ex.getLocalizedMessage()))
            .withThrowable(ex);
      }
      return retval;
    }

    @NonNull
    public ExitStatus handleInvalidCommand(
        @NonNull String message) {
      showHelp();

      ExitStatus retval = ExitCode.INVALID_COMMAND.exitMessage(message);
      retval.generateMessage(false);
      return retval;
    }

    /**
     * Callback for providing a help header.
     *
     * @return the header or {@code null}
     */
    @Nullable
    protected String buildHelpHeader() {
      // TODO: build a suitable header
      return null;
    }

    /**
     * Callback for providing a help footer.
     *
     * @param exec
     *          the executable name
     *
     * @return the footer or {@code null}
     */
    @NonNull
    private String buildHelpFooter() {

      ICommand targetCommand = getTargetCommand();
      Collection<ICommand> subCommands;
      if (targetCommand == null) {
        subCommands = getTopLevelCommands();
      } else {
        subCommands = targetCommand.getSubCommands();
      }

      String retval;
      if (subCommands.isEmpty()) {
        retval = "";
      } else {
        StringBuilder builder = new StringBuilder(64);
        builder
            .append(System.lineSeparator())
            .append("The following are available commands:")
            .append(System.lineSeparator());

        int length = subCommands.stream()
            .mapToInt(command -> command.getName().length())
            .max().orElse(0);

        for (ICommand command : subCommands) {
          builder.append(
              ansi()
                  .render(String.format("   @|bold %-" + length + "s|@ %s%n",
                      command.getName(),
                      command.getDescription())));
        }
        builder
            .append(System.lineSeparator())
            .append('\'')
            .append(getExec())
            .append(" <command> --help' will show help on that specific command.")
            .append(System.lineSeparator());
        retval = builder.toString();
        assert retval != null;
      }
      return retval;
    }

    /**
     * Get the CLI syntax.
     *
     * @return the CLI syntax to display in help output
     */
    protected String buildHelpCliSyntax() {

      StringBuilder builder = new StringBuilder(64);
      builder.append(getExec());

      Deque<ICommand> calledCommands = getCalledCommands();
      if (!calledCommands.isEmpty()) {
        builder.append(calledCommands.stream()
            .map(ICommand::getName)
            .collect(Collectors.joining(" ", " ", "")));
      }

      // output calling commands
      ICommand targetCommand = getTargetCommand();
      if (targetCommand == null) {
        builder.append(" <command>");
      } else {
        Collection<ICommand> subCommands = targetCommand.getSubCommands();

        if (!subCommands.isEmpty()) {
          builder.append(' ');
          if (!targetCommand.isSubCommandRequired()) {
            builder.append('[');
          }

          builder.append("<command>");

          if (!targetCommand.isSubCommandRequired()) {
            builder.append(']');
          }
        }
      }

      // output required options
      getOptionsList().stream()
          .filter(Option::isRequired)
          .forEach(option -> {
            builder
                .append(' ')
                .append(OptionUtils.toArgument(ObjectUtils.notNull(option)));
            if (option.hasArg()) {
              builder
                  .append('=')
                  .append(option.getArgName());
            }
          });

      // output non-required option placeholder
      builder.append(" [<options>]");

      // output extra arguments
      if (targetCommand != null) {
        // handle extra arguments
        for (ExtraArgument argument : targetCommand.getExtraArguments()) {
          builder.append(' ');
          if (!argument.isRequired()) {
            builder.append('[');
          }

          builder.append('<');
          builder.append(argument.getName());
          builder.append('>');

          if (argument.getNumber() > 1) {
            builder.append("...");
          }

          if (!argument.isRequired()) {
            builder.append(']');
          }
        }
      }

      String retval = builder.toString();
      assert retval != null;
      return retval;
    }

    public void showHelp() {

      HelpFormatter formatter = new HelpFormatter();
      formatter.setLongOptSeparator("=");

      AnsiPrintStream out = AnsiConsole.out();
      int terminalWidth = Math.max(out.getTerminalWidth(), 40);

      @SuppressWarnings("resource") PrintWriter writer = new PrintWriter( // NOPMD not owned
          out,
          true,
          StandardCharsets.UTF_8);
      formatter.printHelp(
          writer,
          terminalWidth,
          buildHelpCliSyntax(),
          buildHelpHeader(),
          toOptions(),
          HelpFormatter.DEFAULT_LEFT_PAD,
          HelpFormatter.DEFAULT_DESC_PAD,
          buildHelpFooter(),
          false);
      writer.flush();
    }
  }

}
