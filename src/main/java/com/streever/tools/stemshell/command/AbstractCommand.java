// Copyright (c) 2012 P. Taylor Goetz (ptgoetz@gmail.com)

package com.streever.tools.stemshell.command;

import jline.console.completer.Completer;
import jline.console.completer.NullCompleter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

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
        Options opts =  new Options();
        opts.addOption("v", "verbose", false, "show verbose output");
        return opts;
    }
    
    public String getUsage(){
        return getName() + " [OPTION ...] [ARGS ...]";
    }
    
    protected static void logv(CommandLine cmd, String log){
        if(cmd.hasOption("v")){
            System.out.println(log);
        }
    }
    
    protected static void log(CommandLine cmd, String log){
            System.out.println(log);
    }

    public Completer getCompleter() {
        return this.completer;
    }
}
