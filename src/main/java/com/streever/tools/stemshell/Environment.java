// Copyright (c) 2012 P. Taylor Goetz (ptgoetz@gmail.com)

package com.streever.tools.stemshell;

import com.streever.tools.stemshell.command.Command;

import java.util.Properties;
import java.util.Set;

public interface Environment {

    Context createContext(String name);
    Context getContext(String name);
    
    Boolean isVerbose();
    void setVerbose(Boolean verbose);

    Boolean isDebug();
    void setDebug(Boolean debug);

}
