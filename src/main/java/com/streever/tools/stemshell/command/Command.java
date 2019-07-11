// Copyright (c) 2012 P. Taylor Goetz (ptgoetz@gmail.com)

package com.streever.tools.stemshell.command;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.streever.tools.stemshell.Environment;

public interface Command {

    String getHelpHeader();
    String gethelpFooter();
    String getUsage();
    
    String getName();
    
    CommandReturn execute(Environment env, CommandLine cmd, ConsoleReader reader);
    
    Options getOptions();
    
    Completer getCompleter();
}
