// Copyright (c) 2012 P. Taylor Goetz (ptgoetz@gmail.com)

package com.streever.tools.stemshell;

import com.streever.tools.stemshell.command.Command;

import java.util.Properties;
import java.util.Set;

public interface Environment {
    
//    private String prompt = null;
//    private Properties props = new Properties();
//    private HashMap<String, Object> values = new HashMap<String, Object>();
    
//    public Environment(){
//    }
    
//    private HashMap<String, Command> commands = new HashMap<String, Command>();
    
    
    void addCommand(Command cmd);

    Command getCommand(String name);

    Set<String> commandList();
    void setProperty(String key, String value);
    String getProperty(String key);
    
    Properties getProperties();
    void setValue(String key, Object value);
    Object getValue(String key);
    String getPrompt();
    void setPrompt(String prompt);
}
