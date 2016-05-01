/*
 * Copyright (c) 2016 Rafael Bedia
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Original Author: Rafael Bedia
 *
 */
package psm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a log file for permissions and consolidates them into a policy file.
 *
 * @author Rafael Bedia
 */
public class Parser {

    private static final Logger LOG = Logger.getLogger(Parser.class.getName());

    public static void main(String[] args) {
        Parser main = new Parser();
        Codebases codebases = main.readCodebases(System.in);
        main.writeCodebases(codebases, System.out);
    }

    private Codebases readCodebases(InputStream in) {
        Pattern pattern = Pattern.compile(
                "grant codeBase \"(.*)\" \\{(permission .*)\\};");
        Codebases codebases = new Codebases();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    String codebase = matcher.group(1);
                    String permission = matcher.group(2);
                    codebases.add(codebase, permission);
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        return codebases;
    }

    private void writeCodebases(Codebases codebases, PrintStream out) {
        Map<String, String> properties = getProperties();
        for (Map.Entry<String, Permissions> entrySet : codebases.entrySet()) {
            String codebase = entrySet.getKey();
            codebase = makeReplacement(properties, codebase);
            Permissions permissions = entrySet.getValue();
            out.println("grant codeBase \"" + codebase + "\" {");
            for (String permission : permissions) {
                permission = makeReplacement(properties, permission);
                out.println("    " + permission);
            }
            out.println("};");
            out.println();
        }
    }

    private String makeReplacement(Map<String, String> properties, String input) {
        for (Map.Entry<String, String> entrySet : properties.entrySet()) {
            String key = entrySet.getKey();
            String value = entrySet.getValue();
            input = input.replace(key, value);
        }
        return input;
    }

    private Map<String, String> getProperties() {
        Map<String, String> replacements = new TreeMap<>();
        String[] properties = {
            "user.home",
            "java.home",
            "java.io.tmpdir",
            "jboss.home.dir"
        };
        for (String property : properties) {
            String value = System.getProperty(property);
            if (value != null) {
                replacements.put(value, "${" + property + "}");
            }
        }
        return replacements;
    }

    private static class Codebases extends HashMap<String, Permissions> {

        public void add(String codebase, String permission) {
            Permissions permissions = get(codebase);
            if (permissions == null) {
                permissions = new Permissions();
                put(codebase, permissions);
            }
            permissions.add(permission);
        }
    }

    private static class Permissions extends HashSet<String> {
    }
}
