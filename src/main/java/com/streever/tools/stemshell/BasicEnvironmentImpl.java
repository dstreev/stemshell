// Copyright (c) 2012 P. Taylor Goetz (ptgoetz@gmail.com)

package com.streever.tools.stemshell;

import com.streever.tools.stemshell.command.Command;

import java.util.*;

public class BasicEnvironmentImpl implements Environment {

    private Boolean verbose = Boolean.FALSE;
    private Boolean debug = Boolean.FALSE;
    private Map<String, Context> contextMap = new TreeMap<String, Context>();

    @Override
    public Context createContext(String name) {
        if (contextMap.containsKey(name)) {
            return contextMap.get(name);
        } else {
            Context lclContext = new BasicContextImpl();
            lclContext.setDefaultPrompt(name);
            contextMap.put(name, lclContext);
            return lclContext;
        }
    }

    @Override
    public Context getContext(String name) {
        return contextMap.get(name);
    }

    public Boolean isVerbose() {
        return verbose;
    }

    @Override
    public void setVerbose(Boolean verbose) {
        this.verbose = verbose;
    }

    public Boolean isDebug() {
        return debug;
    }

    @Override
    public void setDebug(Boolean debug) {
        this.debug = debug;
    }
}
