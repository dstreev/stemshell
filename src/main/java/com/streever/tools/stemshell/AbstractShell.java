// Copyright (c) 2012 Health Market Science, Inc.

package com.streever.tools.stemshell;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jcabi.manifests.Manifests;
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

    protected static void logv(Environment env, String log){
        if(env.isVerbose()){
            System.out.println(log);
        }
    }

    protected static void log(Environment env, String log){
        System.out.println(log);
    }

    protected static void loge(Environment env, String log){
        System.err.println(log);
    }

    protected void preProcessInitializationArguments(String[] arguments) {
        //
    }

    protected void postProcessInitializationArguments(String[] arguments, ConsoleReader reader) {
         //
    }

    public final void run(String[] arguments) throws Exception {
        preProcessInitializationArguments(arguments);

        initialize();
        
        // if the subclass hasn't defined a prompt, do so for them.
        if(getEnv().getDefaultPrompt() == null){
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

        postProcessInitializationArguments(arguments, reader);

        acceptCommands(reader);

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
                iae.printStackTrace();
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public int processInput(String line, ConsoleReader reader) {
        // Deal with args that are in quotes and don't split them.
        String[] argv = line.split("\\s+(?=((\\\\[\\\\\"]|[^\\\\\"])*\"(\\\\[\\\\\"]|[^\\\\\"])*\")*(\\\\[\\\\\"]|[^\\\\\"])*$)");
        String cmdName = argv[0];
        int res = 0;
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
                    res = command.execute(env, cl, reader);
                }
                catch (Throwable e) {
                    loge(env,"Command failed with error: "
                            + e.getMessage());
                    if (cl.hasOption("v")) {
                        loge(env,e.getMessage());
                    }
                }
            }

        }
        else {
            if (cmdName != null && cmdName.length() > 0) {
                loge(env, cmdName + ": command not found");
            }
        }
        return res;
    }

    private void acceptCommands(ConsoleReader reader) throws IOException {
        String line;
        while ((line = reader.readLine(getEnv().getCurrentPrompt() + " ")) != null) {
            int res = processInput(line, reader);
            if (res != 0) {
                log(env, "Non-Zero Return Code: " + Integer.toString(res));
            }
        }
    }

    private static CommandLine parse(Command cmd, String[] args) {
        Options opts = cmd.getOptions();
        CommandLine retval = null;
        try {
            retval = parser.parse(opts, args);
        }
        catch (ParseException e) {
            System.err.println(e.getMessage());
        }
        return retval;
    }
    
    private Completer initCompleters(Environment env){
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
        }
        else if (!dir.exists()) {
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
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
        });

        return hist;

    }

    public abstract String getName();

}
