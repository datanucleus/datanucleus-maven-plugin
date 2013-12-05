/**********************************************************************
Copyright (c) 2005 Rahul Thakur and others. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Contributors:
2007 Andy Jefferson - migrated to DataNucleus, formatted, etc
2007 Andy Jefferson - support for "api", log4j, verbose
    ...
**********************************************************************/
package org.datanucleus.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Extensions of this class implement the
 * {@link #prepareModeSpecificCommandLineArguments(Commandline)} method and
 * provide <b>mode</b>-specific arguments to the SchemaTool invocation.
 * <p>
 * Following properties are at least required for the SchemaTool to execute:
 * <ul>
 * <li><code>javax.jdo.option.ConnectionDriverName</code></li>
 * <li><code>javax.jdo.option.ConnectionURL</code></li>
 * <li><code>javax.jdo.option.ConnectionUserName</code></li>
 * <li><code>javax.jdo.option.ConnectionPassword</code></li>
 * </ul>
 * <p>
 * SchemaTool properties can be specified in the POM configuration, or from
 * command line. In case of conflicts, property values specified from command
 * line take precedence.
 * <p>
 * An example DataNucleus-maven-plugin configuration can look like below:
 * <p>
 * <code>
 * <pre>
 *    &lt;plugin&gt;
 *      &lt;groupId&gt;org.datanucleus.maven&lt;/groupId&gt;
 *      &lt;artifactId&gt;datanucleus-maven-plugin&lt;/artifactId&gt;
 *      &lt;version&gt;${datanucleus.plugin.version}&lt;/version&gt;            
 *      &lt;configuration&gt;
 *        &lt;outputFile&gt;${project.build.directory}/schema.sql&lt;/outputFile&gt;
 *        &lt;toolProperties&gt;
 *          &lt;property&gt;
 *            &lt;name&gt;javax.jdo.option.ConnectionDriverName&lt;/name&gt;
 *            &lt;value&gt;org.hsqldb.jdbcDriver&lt;/value&gt;
 *          &lt;/property&gt;
 *          &lt;property&gt;
 *            &lt;name&gt;javax.jdo.option.ConnectionURL&lt;/name&gt;
 *            &lt;value&gt;jdbc:hsqldb:mem:continuum&lt;/value&gt;
 *          &lt;/property&gt;
 *          &lt;property&gt;
 *            &lt;name&gt;javax.jdo.option.ConnectionUserName&lt;/name&gt;
 *            &lt;value&gt;sa&lt;/value&gt;
 *          &lt;/property&gt;                
 *          &lt;property&gt;
 *            &lt;name&gt;javax.jdo.option.ConnectionPassword&lt;/name&gt;
 *            &lt;value&gt;&lt;/value&gt;
 *          &lt;/property&gt;
 *          &lt;property&gt;
 *            &lt;name&gt;datanucleus.autoCreateTables&lt;/name&gt;
 *            &lt;value&gt;true&lt;/value&gt;
 *          &lt;/property&gt;
 *        &lt;/toolProperties&gt;              
 *      &lt;/configuration&gt;            
 *    &lt;/plugin&gt;
 * </pre>
 * </code>
 */
public abstract class AbstractSchemaToolMojo extends AbstractDataNucleusMojo
{
    /** Qualified name for SchemaTool main class. */
    private static final String TOOL_NAME_SCHEMA_TOOL = "org.datanucleus.store.schema.SchemaTool";

    /**
     * @parameter expression="${props}" default-value=""
     */
    private String props;

    /**
     * Properties that will be passed to the SchemaTool's execution.
     * @parameter expression="${toolProperties}"
     */
    private Properties toolProperties;

    /**
     * File to which DDL SQL is written.
     * @parameter expression="${ddlFile}" default-value=""
     */
    protected String ddlFile;

    /**
     * @parameter expression="${completeDdl}" default-value="false"
     */
    protected boolean completeDdl;

    /**
     * @parameter expression="${includeAutoStart}" default-value="false"
     */
    protected boolean includeAutoStart;

    /**
     * {@inheritDoc}
     * @see org.datanucleus.maven.AbstractDataNucleusMojo#executeDataNucleusTool(java.util.List,
     *      java.net.URL, java.util.List)
     */
    protected void executeDataNucleusTool(List pluginArtifacts, List files)
    throws CommandLineException, MojoExecutionException
    {
        executeSchemaTool(pluginArtifacts, files);
    }

    /**
     * Generates Database schema using the list of JDO mappings and enhanced class files.
     * <p>
     * The list of class files is provided on the classpath by
     * {@link #getUniqueClasspathElements()}.
     * 
     * @param pluginArtifacts for creating classpath for DataNucleus tool execution.
     * @param files jdo mapping file list
     * @throws CommandLineException if there was an error invoking DataNucleus SchemaTool.
     * @throws MojoExecutionException
     */
    private void executeSchemaTool(List pluginArtifacts, List files)
    throws CommandLineException, MojoExecutionException
    {
        // Generate a set of CLASSPATH entries (avoiding dups)
        // Put plugin deps first so they are found before any project-specific artifacts
        List cpEntries = new ArrayList();
        for (Iterator it = pluginArtifacts.iterator(); it.hasNext();)
        {
            Artifact artifact = (Artifact) it.next();
            try
            {
                String artifactPath = artifact.getFile().getCanonicalPath();
                if (!cpEntries.contains(artifactPath))
                {
                    cpEntries.add(artifactPath);
                }
            }
            catch (IOException e)
            {
                throw new MojoExecutionException("Error while creating the canonical path for '" + artifact.getFile() + "'.", e);
            }
        }
        Iterator uniqueIter = getUniqueClasspathElements().iterator();
        while (uniqueIter.hasNext())
        {
            String entry = (String)uniqueIter.next();
            if (!cpEntries.contains(entry))
            {
                cpEntries.add(entry);
            }
        }

        // Set the CLASSPATH of the java process
        StringBuffer cpBuffer = new StringBuffer();
        for (Iterator it = cpEntries.iterator(); it.hasNext();)
        {
            cpBuffer.append((String) it.next());
            if (it.hasNext())
            {
                cpBuffer.append(File.pathSeparator);
            }
        }

        if (fork)
        {
            // Create a CommandLine for execution
            Commandline cl = new Commandline();
            cl.setExecutable("java");

            cl.createArgument().setValue("-cp");
            cl.createArgument().setValue(cpBuffer.toString());

            // Obtain list of system properties to apply (System props override POM props)
            Properties systemProperties = System.getProperties();
            if (toolProperties != null)
            {
                Set toolPropertyKeys = toolProperties.keySet();
                for (Iterator it = toolPropertyKeys.iterator(); it.hasNext();)
                {
                    String key = (String) it.next();
                    if (systemProperties.containsKey(key))
                    {
                        toolProperties.put(key, systemProperties.getProperty(key));
                        getLog().warn("Property '" + key + "' value specified in pom configuration will be overridden.");
                    }
                }

                for (Iterator it = toolPropertyKeys.iterator(); it.hasNext();)
                {
                    String key = (String) it.next();
                    String val = (null != toolProperties.getProperty(key) ? toolProperties.getProperty(key) : "");
                    cl.createArgument().setValue("-D" + key + "=" + val);
                }
            }

            // Logging - check for Log4j, else JDK1.4
            URL log4jURL = getLog4JConfiguration();
            if (log4jURL != null)
            {
                cl.createArgument().setValue("-Dlog4j.configuration=" + log4jURL);
            }
            else
            {
                URL jdkLogURL = getJdkLogConfiguration();
                if (jdkLogURL != null)
                {
                    cl.createArgument().setValue("-Djava.util.logging.config.file=" + jdkLogURL);
                }
            }

            cl.createArgument().setValue(TOOL_NAME_SCHEMA_TOOL);

            // allow extensions to prepare Mode specific arguments
            prepareModeSpecificCommandLineArguments(cl, null);

            if (verbose)
            {
                cl.createArgument().setValue("-v");
            }

            boolean usingPU = false;
            if (persistenceUnitName != null && persistenceUnitName.trim().length() > 0)
            {
                usingPU = true;
                cl.createArgument().setLine("-pu " + persistenceUnitName);
            }

            cl.createArgument().setLine("-api " + api);

            if (props != null)
            {
                cl.createArgument().setLine("-props " + props);
            }

            if (!usingPU)
            {
                for (Iterator it = files.iterator(); it.hasNext();)
                {
                    File file = (File) it.next();
                    cl.createArgument().setValue(file.getAbsolutePath());
                }
            }

            executeCommandLine(cl);
        }
        else
        {
            // Execute in the current JVM, so build up list of arguments to the method invoke
            List args = new ArrayList();

            // allow extensions to prepare Mode specific arguments
            prepareModeSpecificCommandLineArguments(null, args);

            if (verbose)
            {
                args.add("-v");
            }

            boolean usingPU = false;
            if (persistenceUnitName != null && persistenceUnitName.trim().length() > 0)
            {
                usingPU = true;
                args.add("-pu");
                args.add(persistenceUnitName);
            }

            args.add("-api");
            args.add(api);

            if (props != null)
            {
                args.add("-props");
                args.add(props);
            }

            if (!usingPU)
            {
                for (Iterator it = files.iterator(); it.hasNext();)
                {
                    File file = (File) it.next();
                    args.add(file.getAbsolutePath());
                }
            }

            executeInJvm(TOOL_NAME_SCHEMA_TOOL, args, cpEntries, false);
        }
    }

    /**
     * Template method that sets up arguments for the {@link SchemaTool} depending upon the <b>mode</b> invoked.
     * This is expected to be implemented by extensions.
     * @param cl {@link Commandline} instance to set up arguments for.
     * @param args Arguments list generated by this call (appended to)
     */
    protected abstract void prepareModeSpecificCommandLineArguments(Commandline cl, List args);

    /**
     * {@inheritDoc}
     * @see org.datanucleus.maven.AbstractDataNucleusMojo#getToolName()
     */
    protected String getToolName()
    {
        return TOOL_NAME_SCHEMA_TOOL;
    }
}