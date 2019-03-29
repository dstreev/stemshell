// Copyright (c) 2012 P. Taylor Goetz (ptgoetz@gmail.com)

package com.streever.tools.stemshell.example;

import com.streever.tools.stemshell.AbstractShell;
import com.streever.tools.stemshell.BasicEnvironmentImpl;
import com.streever.tools.stemshell.Context;
import com.streever.tools.stemshell.Environment;
import com.streever.tools.stemshell.commands.Env;
import com.streever.tools.stemshell.commands.Exit;
import com.streever.tools.stemshell.commands.Help;
import com.streever.tools.stemshell.commands.HistoryCmd;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;

public class ExampleShell extends AbstractShell {

    public static void main(String[] args) throws Exception {
        System.out.println("StemShell example. Press [TAB] to list available commands.");
        new ExampleShell().run(args);
    }


    @Override
    public void initialize() throws Exception {

        Environment lclEnv = new BasicEnvironmentImpl();
        setEnv(lclEnv);

        Context ctx = lclEnv.createContext("example");

        ctx.addCommand(new Exit("exit"));
        ctx.addCommand(new Env("env"));
        ctx.addCommand(new Help("help", ctx));
        ctx.addCommand(new HistoryCmd("history"));
    }

    @Override
    protected void initParentContext(ConsoleReader reader) {
        super.initParentContext(reader);
        for (Completer completer: reader.getCompleters()) {
            reader.removeCompleter(completer);
        }

    }

    @Override
    public String getName() {
        return "stemshell-example";
    }

}
