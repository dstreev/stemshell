// Copyright (c) 2012 P. Taylor Goetz (ptgoetz@gmail.com)

package com.streever.tools.stemshell.commands;

import java.util.Properties;

import jline.console.ConsoleReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.streever.tools.stemshell.Environment;
import com.streever.tools.stemshell.command.AbstractCommand;

public class Env extends AbstractCommand {

    public Env(String name) {
        super(name);
    }

    public int execute(Environment env, CommandLine cmd, ConsoleReader reader) {
        if (cmd.hasOption("l") || !cmd.hasOption("s")) {
            Properties props = env.getProperties();
            log(env, "Local Properties:");
            for (Object key : props.keySet()) {
                log(env, "\t" + key + "=" + props.get(key));
            }
        }
        if (cmd.hasOption("s")) {
            log(env, "System Properties:");
            Properties props = System.getProperties();
            for (Object key : props.keySet()) {
                log(env, "\t" + key + "=" + props.get(key));
            }
        }

        return 0;
    }

    @Override
    public Options getOptions() {
        Options opts = super.getOptions();
        opts.addOption("s", "system", false, "list system properties.");
        opts.addOption("l", "local", false, "list local properties.");
        return opts;
    }

}
