// Copyright (c) 2012 P. Taylor Goetz (ptgoetz@gmail.com)

package com.streever.tools.stemshell.example;

import com.streever.tools.stemshell.AbstractShell;
import com.streever.tools.stemshell.BasicEnvironmentImpl;
import com.streever.tools.stemshell.Environment;
import com.streever.tools.stemshell.commands.Env;
import com.streever.tools.stemshell.commands.Exit;
import com.streever.tools.stemshell.commands.Help;
import com.streever.tools.stemshell.commands.HistoryCmd;
import jline.console.ConsoleReader;

public class ExampleShell extends AbstractShell {

    public static void main(String[] args) throws Exception{
        System.out.println("StemShell example. Press [TAB] to list available commands.");
        new ExampleShell().run(args);
    }
    

    @Override
    public void initialize() throws Exception {

        Environment lclEnv = new BasicEnvironmentImpl();
        setEnv(lclEnv);
        
        getEnv().addCommand(new Exit("exit"));
        getEnv().addCommand(new Env("env"));
        getEnv().addCommand(new Help("help", getEnv()));
        getEnv().addCommand(new HistoryCmd("history"));
        //env.setPrompt("stemshell%");
    }


    @Override
    protected boolean preProcessInitializationArguments(String[] arguments) {
        return Boolean.TRUE;
    }

    @Override
    protected boolean postProcessInitializationArguments(String[] arguments, ConsoleReader reader) {
        return Boolean.TRUE;
    }

    @Override
    public String getName() {
        return "stemshell-example";
    }

}
