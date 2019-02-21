// Copyright (c) 2012 Health Market Science, Inc.

package com.streever.tools.stemshell.commands;

import java.util.ListIterator;

import jline.console.ConsoleReader;
import jline.console.history.History.Entry;

import org.apache.commons.cli.CommandLine;

import com.streever.tools.stemshell.Environment;
import com.streever.tools.stemshell.command.AbstractCommand;

public class HistoryCmd extends AbstractCommand {

    public HistoryCmd(String name) {
        super(name);
    }

    @Override
    public void execute(Environment env, CommandLine cmd, ConsoleReader reader) {
        jline.console.history.History history = reader.getHistory();
        ListIterator<Entry> it = history.entries();
        while(it.hasNext()){
            Entry entry = it.next();
            System.out.println(entry.value());
        }

    }

}
