package com.amashchenko.maven.plugin.gitflow;

import com.google.common.base.Splitter;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;
import org.codehaus.plexus.configuration.PlexusConfiguration;

public abstract class CommandLineSettable {

    /**
     * Called by {@link org.eclipse.sisu.plexus.CompositeBeanHelper#setDefault(Object, Object, PlexusConfiguration)} during initialization
     *
     * @param cliConfig
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void set(String cliConfig) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        Map<String, String> values = Splitter.on(",").withKeyValueSeparator("=").split(cliConfig);
        for (Entry<String, String> e : values.entrySet()) {
            new PropertyDescriptor(e.getKey(), this.getClass()).getWriteMethod().invoke(this, e.getValue());
        }
    }
}
