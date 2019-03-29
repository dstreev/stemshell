// Copyright (c) 2012 P. Taylor Goetz (ptgoetz@gmail.com)

package com.streever.tools.stemshell.commands;

import com.streever.tools.stemshell.Context;
import jline.console.ConsoleReader;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.Completer;
import jline.console.completer.NullCompleter;
import jline.console.completer.StringsCompleter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;

import com.streever.tools.stemshell.Environment;
import com.streever.tools.stemshell.command.AbstractCommand;
import com.streever.tools.stemshell.command.Command;

public class Help extends AbstractCommand {
    private Context context;

    public Help(String name, Context context) {
        super(name);
        this.context = context;
        
        StringsCompleter strCompleter = new StringsCompleter(this.context.commandList());
        NullCompleter nullCompleter = new NullCompleter();
        Completer completer = new AggregateCompleter(strCompleter, nullCompleter);
        
        this.completer = completer;
        
    }
    

    public void execute(Context ctx, CommandLine cmd, ConsoleReader reader) {
        if (cmd.getArgs().length == 0) {
            for (String str : ctx.commandList()) {
                log(ctx.getEnvironment(), str);
            }
        } else {
            Command command = ctx.getCommand(cmd.getArgs()[0]);
            logv(ctx.getEnvironment(), "Get Help for command: " + command.getName() + "(" + command.getClass().getName() + ")");
            printHelp(command);
        }

    }
    
    private void printHelp(Command cmd){
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp(cmd.getUsage(), cmd.getHelpHeader(), cmd.getOptions(), cmd.gethelpFooter());
    }
}
