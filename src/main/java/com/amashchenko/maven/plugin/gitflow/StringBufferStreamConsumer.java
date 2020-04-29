/*
 * Copyright 2014-2020 Aleksandr Mashchenko.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amashchenko.maven.plugin.gitflow;

import org.codehaus.plexus.util.cli.StreamConsumer;

public class StringBufferStreamConsumer implements StreamConsumer {
    private static final String LS = System.getProperty("line.separator");

    private final StringBuffer buffer;

    private final boolean printOut;

    public StringBufferStreamConsumer() {
        this(false);
    }

    public StringBufferStreamConsumer(boolean printOut) {
        this.buffer = new StringBuffer();
        this.printOut = printOut;
    }

    @Override
    public void consumeLine(String line) {
        if (printOut) {
            System.out.println(line);
        }

        buffer.append(line).append(LS);
    }

    public String getOutput() {
        return buffer.toString();
    }
}
