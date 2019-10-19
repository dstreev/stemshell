// Copyright (c) 2012 Health Market Science, Inc.

package com.streever.tools.stemshell;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jcabi.manifests.Manifests;
import com.streever.tools.stemshell.command.CommandReturn;
import jline.console.ConsoleReader;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import jline.console.history.FileHistory;
import jline.console.history.History;

import org.apache.commons.cli.*;
import org.fusesource.jansi.AnsiConsole;

import com.streever.tools.stemshell.command.Command;

public abstract class AbstractShell implements Shell {
    private static CommandLineParser parser = new PosixParser();
    private Environment env = null; //new Environment();
    private String bannerResource = "/banner.txt";
//    private boolean silent = false;

    protected Environment getEnv() {
        return env;
    }

    protected void setEnv(Environment env) {
        this.env = env;
    }

    public String getBannerResource() {
        return bannerResource;
    }

    public void setBannerResource(String bannerResource) {
        this.bannerResource = bannerResource;
    }

//    public boolean isSilent() {
//        return silent;
//    }
//
//    public void setSilent(boolean silent) {
//        this.silent = silent;
//        getEnv().setSilent(silent);
//    }

    protected static void logv(Environment env, String log) {
        if (env.isVerbose()) {
            System.out.println(log);
        }
    }

    protected static void log(Environment env, String log) {
        System.out.println(log);
    }

    protected static void loge(Environment env, String log) {
        System.err.println(log);
    }

    protected abstract boolean preProcessInitializationArguments(String[] arguments);

    protected abstract boolean postProcessInitializationArguments(String[] arguments, ConsoleReader reader);

    public final void run(String[] arguments) throws Exception {
        if (!preProcessInitializationArguments(arguments)) {
            loge(env, "Initialization Issue");
            return;
        }

        initialize();

        // if the subclass hasn't defined a prompt, do so for them.
        if (getEnv().getDefaultPrompt() == null) {
            getEnv().setDefaultPrompt("$");
        }

        // banner
        if (!getEnv().isSilent()) {
            InputStream is = this.getClass().getResourceAsStream(getBannerResource());
            if (is != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ((line = br.readLine()) != null) {
                    // Replace Token.
                    log(env, substituteVariables(line));
                }
            }
        }

        // create reader and add completers
        ConsoleReader reader = new ConsoleReader();

        reader.addCompleter(initCompleters(env));
        // add history support
        reader.setHistory(initHistory());

        AnsiConsole.systemInstall();

        if (postProcessInitializationArguments(arguments, reader)) {
            acceptCommands(reader);
        } else {
            loge(env, "Initialization Issue.");
        }

    }

    public static String substituteVariables(String template) {
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(template);
        // StringBuilder cannot be used here because Matcher expects StringBuffer
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String matchStr = matcher.group(1);
//            System.out.println("Found variable: " + matchStr);
            try {
                String replacement = Manifests.read(matchStr);
                if (replacement != null) {
//                    System.out.println("Replacement Value: " + replacement);
                    // quote to work properly with $ and {,} signs
                    matcher.appendReplacement(buffer, replacement != null ? Matcher.quoteReplacement(replacement) : "null");
                } else {
//                    System.out.println("No replacement found for: " + matchStr);
                }
            } catch (IllegalArgumentException iae) {
                //iae.printStackTrace();
                // Couldn't locate MANIFEST Entry.
                // Silently continue. Usually happens in IDE->run.
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public CommandReturn processInput(String line, ConsoleReader reader) {
        CommandReturn cr = CommandReturn.GOOD;
        // Check for Pipelining.
        // Pipelining are used to string commands together.
        // https://en.wikipedia.org/wiki/Pipeline_%28Unix%29
        /*
        This pipelining works a bit different in the sense the downstream function does
        NOT take a stream.  So we need to iterate through the previous functions output
        and repeatively call the next function in the pipeline.

        At this time, the pipeline only support 1 redirect.
         */
        if (line.contains("|")) {
            String[] commands = line.split("\\|");
            if (commands.length > 2) {
                loge(env, "Currently, only supporting 1 pipeline redirect");
                cr = CommandReturn.BAD;
            } else {
                for (int i = 0; i < commands.length; i++) {
                    if (i > 0) {
                        try {
                            BufferedReader bufferedReader = new BufferedReader(new StringReader(new String(cr.getBufferedOutputStream().toByteArray())));
                            while ((line = bufferedReader.readLine()) != null) {
                                // Check line for spaces.  If it has them, quote it.
                                String adjustedLine = null;
                                if (line.contains(" ")) {
                                    adjustedLine = "\"" + line + "\"";
                                } else {
                                    adjustedLine = line;
                                }
                                String pipedCommand = commands[i].trim() + " " + adjustedLine;
                                if (i == commands.length - 1) {
                                    // Last command in pipeline
                                    logv(env, "Pipeline Command: " + pipedCommand);
                                    processCommand(pipedCommand, reader, false);
                                } else {
                                    loge(env, "Currently, only supporting 1 pipeline redirect");
                                }
                            }
                        } catch (IOException io) {
                            io.printStackTrace();
                        }
                    } else {
                        // First Cycle of Pipeline
                        cr = processCommand(commands[i], reader, true);
                    }
                }
            }
        } else {
            cr = processCommand(line, reader, false);
        }

        return cr;
    }

    protected CommandReturn processCommand(String line, ConsoleReader reader, boolean buffer) {
        // Deal with args that are in quotes and don't split them.
//        String[] argv = line.split("\\s+(?=((\\\\[\\\\\"]|[^\\\\\"])*\"(\\\\[\\\\\"]|[^\\\\\"])*\")*(\\\\[\\\\\"]|[^\\\\\"])*$)");
//        String[] argv = line.split("[^\\s\\\"']+|\\\"([^\\\"]*)\\\"|'([^']*)'");
//        String cmdName = argv[0];

        List<String> matchList = new ArrayList<String>();
        Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
        Matcher regexMatcher = regex.matcher(line);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                // Add double-quoted string without the quotes
                matchList.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                // Add single-quoted string without the quotes
                matchList.add(regexMatcher.group(2));
            } else {
                // Add unquoted word
                matchList.add(regexMatcher.group());
            }
        }

        String[] argv = new String[matchList.size()];
        matchList.toArray(argv);

        CommandReturn cr = null;

        if (matchList.size() == 0) {
            cr = CommandReturn.BAD;
            return cr;
        }
        
        String cmdName = argv[0];

        cr = CommandReturn.GOOD;

        Command command = env.getCommand(cmdName);
        if (command != null) {
//            if (getEnv().isVerbose()) {
//                System.out.println("Running: " + command.getName() + " ("
//                        + command.getClass().getName() + ")");
//            }
            String[] cmdArgs = null;
            if (argv.length > 1) {
                cmdArgs = Arrays.copyOfRange(argv, 1, argv.length);
            }
            CommandLine cl = parse(command, cmdArgs);
            if (cl != null) {
                try {
                    cr = command.execute(env, cl, reader, buffer);
                    if (cr.isError()) {
                        loge(env, cr.getSummary());
                    }
                } catch (Throwable e) {
                    loge(env, "Command failed with error: "
                            + e.getMessage());
                    if (cl.hasOption("v")) {
                        loge(env, e.getMessage());
                    }
                }
            }

        } else {
            if (cmdName != null && cmdName.length() > 0) {
                loge(env, cmdName + ": command not found");
            }
        }
        return cr;
    }

    private void acceptCommands(ConsoleReader reader) throws IOException {
        String line;
        while ((line = reader.readLine(getEnv().getCurrentPrompt() + " ")) != null) {
            CommandReturn cr = processInput(line, reader);
//            if (cr.isError()) {
//                loge(env, cr.getSummary());
//            }
        }
    }

    private static CommandLine parse(Command cmd, String[] args) {
        Options opts = cmd.getOptions();
        CommandLine retval = null;
        try {
            retval = parser.parse(opts, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
        }
        return retval;
    }

    private Completer initCompleters(Environment env) {
        // create completers
        ArrayList<Completer> completers = new ArrayList<Completer>();
        for (String cmdName : env.commandList()) {
            // command name
            StringsCompleter sc = new StringsCompleter(cmdName);

            ArrayList<Completer> cmdCompleters = new ArrayList<Completer>();
            // add a completer for the command name
            cmdCompleters.add(sc);
            // add the completer for the command
            cmdCompleters.add(env.getCommand(cmdName).getCompleter());
            // add a terminator for the command
            // cmdCompleters.add(new NullCompleter());

            ArgumentCompleter ac = new ArgumentCompleter(cmdCompleters);
            completers.add(ac);
        }

        AggregateCompleter aggComp = new AggregateCompleter(completers);

        return aggComp;
    }

    private History initHistory() throws IOException {
        File dir = new File(System.getProperty("user.home"), "."
                + this.getName());
        if (dir.exists() && dir.isFile()) {
            throw new IllegalStateException(
                    "Default configuration file exists and is not a directory: "
                            + dir.getAbsolutePath());
        } else if (!dir.exists()) {
            dir.mkdir();
        }
        // directory created, touch history file
        File histFile = new File(dir, "history");
        if (!histFile.exists()) {
            if (!histFile.createNewFile()) {
                throw new IllegalStateException(
                        "Unable to create history file: "
                                + histFile.getAbsolutePath());
            }
        }

        final FileHistory hist = new FileHistory(histFile);

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                try {
                    hist.flush();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        });

        return hist;

    }

    public abstract String getName();

}
