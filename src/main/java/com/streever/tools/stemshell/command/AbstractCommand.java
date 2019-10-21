package com.streever.tools.stemshell.command;

import com.streever.tools.stemshell.Environment;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.completer.NullCompleter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public abstract class AbstractCommand implements Command{
    private String name;

    protected Completer completer = new NullCompleter();

    public AbstractCommand(String name){
        this.name = name;
    }

    public String getHelpHeader() {
        return "Options:";
    }

    public String gethelpFooter() {
        return null;
    }

    public String getName() {
        return name;
    }

    public Options getOptions() {
        Options options =  new Options();

        options.addOption("v", "verbose", false, "show verbose output");

//        Option bufferOption = Option.builder("b").required(false)
//                .argName("buffer")
//                .desc("Buffer Output")
//                .hasArg(false)
//                .longOpt("buffer")
//                .build();
//        options.addOption(bufferOption);

        return options;
    }
    
    protected void processCommandLine(CommandLine commandLine) {
        // TODO: Handle Verbose here
        
//        if (commandLine.hasOption("buffer")) {
//            setBufferOutput(true);
//        } else {
//            setBufferOutput(false);
//        }
    }

    public String getUsage(){
        return getName() + " [OPTION ...] [ARGS ...]";
    }
    
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

    protected static void logd(Environment env, String log){
        if(env.isDebug()){
            System.out.println(log);
        }
    }

    public Completer getCompleter() {
        return this.completer;
    }
    
    @Override
    public CommandReturn execute(Environment env, CommandLine cmd, ConsoleReader reader, boolean buffer) {
        CommandReturn cr = null;
        if (buffer) {
            PrintStream old = null;
            try {
                // Change Output to ByteArrayOutputStream
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                old = System.out;
                System.setOut(ps);

                cr = implementation(env, cmd, reader);

                System.out.flush();

                cr.setBufferedOutputStream(baos);
                try {
                    baos.flush();
                } catch (IOException io) {
                    io.printStackTrace();
                }
            } finally {
                // Revert Buffered Output
                System.setOut(old);
            }
        } else {
            cr = implementation(env, cmd, reader);
        }

        return cr;
    }

    @Override
    public abstract CommandReturn implementation(Environment env, CommandLine cmd, ConsoleReader reader);

}
