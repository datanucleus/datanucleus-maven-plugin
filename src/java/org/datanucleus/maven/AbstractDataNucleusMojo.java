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
    ...
**********************************************************************/
package org.datanucleus.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Convenience base class for DataNucleus Mojo extensions.
 */
public abstract class AbstractDataNucleusMojo extends AbstractMojo
{
    /**
     * @parameter expression="${metadataDirectory}" default-value="${project.build.outputDirectory}"
     * @required
     */
    protected File metadataDirectory;

    /**
     * @parameter expression="${metadataIncludes}" default-value="**\/*.jdo, **\/*.class"
     */
    protected String metadataIncludes;

    /**
     * @parameter expression="${metadataExcludes}"
     */
    protected String metadataExcludes;

    /**
     * @parameter expression="${project.compileClasspathElements}"
     * @required
     */
    private List classpathElements;

    /**
     * @parameter expression="${plugin.artifacts}"
     * @required
     */
    protected List pluginArtifacts;

    /**
     * @parameter expression="${log4jConfiguration}"
     */
    protected String log4jConfiguration;

    /**
     * @parameter expression="${jdkLogConfiguration}"
     */
    protected String jdkLogConfiguration;

    /**
     * @parameter expression="${verbose}" default-value="false"
     */
    protected boolean verbose;

    /**
     * @parameter expression="${fork}" default-value="true"
     */
    protected boolean fork;

    /**
     * @parameter expression="${persistenceUnitName}" default-value=""
     */
    protected String persistenceUnitName;

    /**
     * @parameter expression="${api}" default-value="JDO"
     */
    protected String api;

    /**
     * Method to execute a goal.
     * @throws MojoExecutionException
     */
    public void execute() throws MojoExecutionException
    {
        if (!metadataDirectory.exists())
        {
        	getLog().warn("No files to run DataNucleus tool '" + getToolName() + "'" +
        	    " since specified metadata directory '" + metadataDirectory.getAbsolutePath() + "'" +
        	    " is not available.");
        	return;
        }

        List files = findMetadataFiles();
        if (files.size() == 0)
        {
            getLog().warn("No files to run DataNucleus tool '" + getToolName() + "'");
            return;
        }

        getLog().debug("Metadata Directory is : " + metadataDirectory.getAbsolutePath());

        try
        {
            executeDataNucleusTool(pluginArtifacts, files);
        }
        catch (CommandLineException e)
        {
            throw new MojoExecutionException("Error while executing the DataNucleus tool '" + getToolName() + "'.", e);
        }
    }

    /**
     * Accessor for the Log4J configuration URL.
     * @return Log4J config URL (if provided)
     */
    protected URL getLog4JConfiguration()
    {
        if (log4jConfiguration != null)
        {
            URL log4jURL = this.getClass().getResource(log4jConfiguration);
            if (log4jURL == null && log4jConfiguration != null)
            {
                try
                {
                    log4jURL = new URL("file:" + log4jConfiguration);
                }
                catch (MalformedURLException mue)
                {
                    // TODO Throw exception?
                }
            }
            return log4jURL;
        }
        return null;
    }

    /**
     * Accessor for the JDK1.4 logging configuration URL.
     * @return JDK1.4 logging config URL (if provided)
     */
    protected URL getJdkLogConfiguration()
    {
        if (jdkLogConfiguration != null)
        {
            URL jdkLogURL = this.getClass().getResource(jdkLogConfiguration);
            if (jdkLogURL == null && jdkLogConfiguration != null)
            {
                try
                {
                    jdkLogURL = new URL("file:" + jdkLogConfiguration);
                }
                catch (MalformedURLException mue)
                {
                    // TODO Throw exception?
                }
            }
            return jdkLogURL;
        }
        return null;
    }

    /**
     * Locates and builds a list of all metadata files under the build output directory.
     * @throws MojoExecutionException
     */
    protected List findMetadataFiles() throws MojoExecutionException
    {
        List files;

        try
        {
            files = FileUtils.getFiles(metadataDirectory, metadataIncludes, metadataExcludes);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Error while scanning for metadata files in '"
                            + metadataDirectory.getAbsolutePath() + "'.", e);
        }

        return files;
    }

    /**
     * <p>
     * Return the set of classpath elements, ensuring that {@link #metadataDirectory}
     * location is first, and that no entry is duplicated in the classpath.
     * </p>
     * <p>
     * The ability of the user to specify an alternate {@link #metadataDirectory} location
     * facilitates the need for this. <br>
     * Example: Users that want to DataNucleusEnhance their test classes.
     * </p>
     * 
     * @return the list of unique classpath elements.
     */
    protected List getUniqueClasspathElements()
    {
        List ret = new ArrayList();
        ret.add(this.metadataDirectory.getAbsolutePath());
        Iterator it = classpathElements.iterator();
        while (it.hasNext())
        {
            String pathelem = (String) it.next();
            if (!ret.contains(new File(pathelem).getAbsolutePath()))
            {
                ret.add(pathelem);
            }
        }
        return ret;
    }

    /**
     * Template method expected to be implemented by extensions. This acts as hook to invoke custom DataNucleus tool.
     * @param pluginArtifacts
     * @param log4jProperties
     * @param files
     */
    protected abstract void executeDataNucleusTool(List pluginArtifacts, List files)
    throws CommandLineException, MojoExecutionException;

    /**
     * Returns the DataNucleus tool name being invoked by this plugin's execution.
     * @return DataNucleus tool/utility name being invoked.
     */
    protected abstract String getToolName();

    /**
     * Method to execute a command line.
     * @param cl CommandLine
     * @throws CommandLineException Thrown if an error occurs invoking the command line
     * @throws MojoExecutionException Thrown if the command line executes but with error return code
     */
    protected void executeCommandLine(Commandline cl)
    throws CommandLineException, MojoExecutionException
    {
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        getLog().debug("Executing command line:");
        getLog().debug(cl.toString());
        int exitCode = CommandLineUtils.executeCommandLine(cl, stdout, stderr);

        getLog().debug("Exit code: " + exitCode);
        getLog().debug("--------------------");
        getLog().debug(" Standard output from the DataNucleus tool " + getToolName() + " :");
        getLog().debug("--------------------");
        getLog().info(stdout.getOutput());
        getLog().debug("--------------------");
        String stream = stderr.getOutput();
        if (stream.trim().length() > 0)
        {
            getLog().error("--------------------");
            getLog().error(" Standard error from the DataNucleus tool + " + getToolName() + " :");
            getLog().error("--------------------");
            getLog().error(stderr.getOutput());
            getLog().error("--------------------");
        }

        if (exitCode != 0)
        {
            throw new MojoExecutionException("The DataNucleus tool " + getToolName() + " exited with a non-null exit code.");
        }
    }

    protected void executeInJvm(String className, List args, List cpEntries, boolean quiet)
    throws MojoExecutionException
    {
        try
        {
            URL[] urls = new URL[cpEntries.size()];
            int urlIdx=0;
            for (Iterator it = cpEntries.iterator(); it.hasNext(); )
            {
                String n  = (String) it.next();
                try
                {
                    if (!quiet && verbose)
                    {
                        getLog().info("  CP: " + n);
                    }
                    urls[urlIdx++] = new File(n).toURI().toURL();
                }
                catch (Exception e)
                {
                    throw new MojoExecutionException("Cannot convert to url: " + n, e );
                }
            }

            URLClassLoader loader = new URLClassLoader(urls, null);
            Class c = loader.loadClass(className);
            Method m = c.getMethod("main", new Class[] { String[].class });
            ClassLoader tl = Thread.currentThread().getContextClassLoader();
            String oldProp = System.getProperty("log4j.configuration");
            try
            {
                Thread.currentThread().setContextClassLoader(loader);

                URL log4jURL = getLog4JConfiguration();
                if (log4jURL != null)
                {
                    System.setProperty("log4j.configuration", log4jURL.toString());
                }
                else
                {
                    URL jdkLogURL = getJdkLogConfiguration();
                    if (jdkLogURL != null)
                    {
                        System.setProperty("java.util.logging.config.file", jdkLogURL.toString());
                    }
                }

                m.invoke(null, new Object[] {(String[])args.toArray(new String[args.size()])});
            }
            finally
            {
                Thread.currentThread().setContextClassLoader(tl);
                if (oldProp != null)
                {
                    System.setProperty("log4j.configuration", oldProp);
                }
                else
                {
                    System.getProperties().remove("log4j.configuration");
                }
            }
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Error executing DataNucleus tool " + getToolName(), e);
        }
    }
}