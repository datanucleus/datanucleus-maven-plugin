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
2007 Andy Jefferson - split out base class for all enhancer modes.
2011 Marco หงุ่ยตระกูล-Schulze - changed "@requiresDependencyResolution" to "compile"
    ...
**********************************************************************/
package org.datanucleus.maven;

import java.util.List;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * Goal to check the enhancement status of the provided classes.
 * @goal enhance-check
 * @phase process-classes
 * @requiresDependencyResolution compile
 * @description Checks the enhancement of the input classes.
 */
public class EnhancerCheckMojo extends AbstractEnhancerMojo
{
    /**
     * Method to add on any additional command line arguments for this mode of invoking the
     * DataNucleus Enhancer.
     * @param cl The current CommandLine
     * @param args Args that will be updated with anything appended here
     */
    protected void prepareModeSpecificCommandLineArguments(Commandline cl, List args)
    {
        // Use "checkonly" mode
        if (fork)
        {
            cl.createArg().setValue("-checkonly");
        }
        else
        {
            args.add("-checkonly");
        }
    }
}