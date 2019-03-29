package com.streever.tools.stemshell;

import com.streever.tools.stemshell.command.Command;

import java.util.Properties;
import java.util.Set;

public interface Context {

    Environment getEnvironment();
    void addCommand(Command cmd);

    Command getCommand(String name);

    Set<String> commandList();
    void setProperty(String key, String value);
    String getProperty(String key);

    Properties getProperties();
    void setValue(String key, Object value);
    Object getValue(String key);

    String getCurrentPrompt();
    void setCurrentPrompt(String prompt);
    String getDefaultPrompt();
    void setDefaultPrompt(String prompt);

}