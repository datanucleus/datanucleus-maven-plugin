/**********************************************************************
Copyright (c) 2007 Andy Jefferson and others. All rights reserved.
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
    ...
**********************************************************************/
package org.datanucleus.maven;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * Base for all enhancer-based Maven2 goals.
 */
public abstract class AbstractEnhancerMojo extends AbstractDataNucleusMojo
{
    private static final String TOOL_NAME_DATANUCLEUS_ENHANCER = "org.datanucleus.enhancer.DataNucleusEnhancer";

    /**
     * @parameter expression="${quiet}" default-value="false"
     */
    protected boolean quiet;

    /**
     * @parameter expression="${alwaysDetachable}" default-value="false"
     */
    protected boolean alwaysDetachable;

    /**
     * @parameter expression="${generatePK}" default-value="true"
     */
    protected boolean generatePK;

    /**
     * @parameter expression="${generateConstructor}" default-value="true"
     */
    protected boolean generateConstructor;

    /**
     * @parameter expression="${detachListener}" default-value="false"
     */
    protected boolean detachListener;

    /**
     * Method to execute the enhancer using the provided artifacts and input files.
     * @param pluginArtifacts Artifacts to use in CLASSPATH generation
     * @param files Input files
     */
    protected void executeDataNucleusTool(List pluginArtifacts, List files)
    throws CommandLineException, MojoExecutionException
    {
        enhance(pluginArtifacts, files);
    }

    /**
     * Run the DataNucleus Enhancer using the specified input data.
     * @param pluginArtifacts for creating classpath for execution.
     * @param files input file list
     * @throws CommandLineException if there was an error invoking the DataNucleus Enhancer.
     * @throws MojoExecutionException
     */
    protected void enhance(List pluginArtifacts, List files)
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

            // uncomment the following if you want to debug the enhancer
            // cl.addArguments(new String[]{"-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000"});

            cl.createArg().setValue("-cp");
            cl.createArg().setValue(cpBuffer.toString());

            // Logging - check for Log4j, else JDK1.4
            URL log4jURL = getLog4JConfiguration();
            if (log4jURL != null)
            {
                cl.createArg().setValue("-Dlog4j.configuration=" + log4jURL);
            }
            else
            {
                URL jdkLogURL = getJdkLogConfiguration();
                if (jdkLogURL != null)
                {
                    cl.createArg().setValue("-Djava.util.logging.config.file=" + jdkLogURL);
                }
            }

            cl.createArg().setValue(TOOL_NAME_DATANUCLEUS_ENHANCER);

            // allow extensions to prepare Mode specific arguments
            prepareModeSpecificCommandLineArguments(cl, null);

            if (quiet)
            {
                cl.createArg().setValue("-q");
            }
            else if (verbose)
            {
                cl.createArg().setValue("-v");
            }

            boolean usingPU = false;
            if (persistenceUnitName != null && persistenceUnitName.trim().length() > 0)
            {
                usingPU = true;
                cl.createArg().setLine("-pu " + persistenceUnitName);
            }

            cl.createArg().setLine("-api " + api);

            if (alwaysDetachable)
            {
                cl.createArg().setValue("-alwaysDetachable");
            }

            if (!generatePK)
            {
                cl.createArg().setLine("-generatePK false");
            }

            if (!generateConstructor)
            {
                cl.createArg().setLine("-generateConstructor false");
            }

            if (detachListener)
            {
                cl.createArg().setLine("-detachListener true");
            }

            if (!usingPU)
            {
                for (Iterator it = files.iterator(); it.hasNext();)
                {
                    File file = (File) it.next();
                    cl.createArg().setValue(file.getAbsolutePath());
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

            if (quiet)
            {
                args.add("-q");
            }
            else if (verbose)
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

            if (alwaysDetachable)
            {
                args.add("-alwaysDetachable");
            }

            if (!generatePK)
            {
                args.add("-generatePK");
                args.add("false");
            }

            if (!generateConstructor)
            {
                args.add("-generateConstructor");
                args.add("false");
            }

            if (detachListener)
            {
                args.add("-detachListener");
                args.add("true");
            }

            if (!usingPU)
            {
                for (Iterator it = files.iterator(); it.hasNext();)
                {
                    File file = (File) it.next();
                    args.add(file.getAbsolutePath());
                }
            }

            executeInJvm(TOOL_NAME_DATANUCLEUS_ENHANCER, args, cpEntries, quiet);
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
     * Accessor for the name of the class to be invoked.
     * @return Class name for the Enhancer.
     */
    protected String getToolName()
    {
        return TOOL_NAME_DATANUCLEUS_ENHANCER;
    }
}