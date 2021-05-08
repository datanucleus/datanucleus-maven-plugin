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
 2016 Dan Haywood - https://github.com/datanucleus/datanucleus-maven-plugin/issues/5
**********************************************************************/
package org.datanucleus.maven;

import java.util.List;

import org.codehaus.plexus.util.cli.Commandline;

public abstract class AbstractEnhancerEnhanceMojo extends AbstractEnhancerMojo
{
    /**
     * @parameter property="targetDirectory" default-value=""
     */
    private String targetDirectory;

    /**
     * Method to add on any additional command line arguments for this mode of invoking the
     * DataNucleus Enhancer.
     * @param cl The current CommandLine
     * @param args Args that will be updated with anything appended here
     */
    protected void prepareModeSpecificCommandLineArguments(Commandline cl, List args)
    {
        if (targetDirectory != null && targetDirectory.trim().length() > 0)
        {
            // Output the enhanced classes to a different location
            if (fork)
            {
                cl.createArg().setValue("-d");
                cl.createArg().setValue(targetDirectory);
            }
            else
            {
                args.add("-d");
                args.add(targetDirectory);
            }
        }
    }

}