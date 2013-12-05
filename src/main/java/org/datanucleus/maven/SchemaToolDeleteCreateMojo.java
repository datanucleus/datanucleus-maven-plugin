/**********************************************************************
Copyright (c) 2012 Andy Jefferson and others. All rights reserved.
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

import java.util.List;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * Drop and create the Schema defined by the input files.
 * @goal schema-deletecreate
 * @requiresDependencyResolution runtime
 * @description Drops and creates the datastore Schema for the specified input files
 */
public class SchemaToolDeleteCreateMojo extends AbstractSchemaToolMojo
{
    private static final String OPERATION_MODE_DELETECREATE = "-deletecreate";

    /**
     * {@inheritDoc}
     * @see org.datanucleus.maven.AbstractSchemaToolMojo#prepareModeSpecificCommandLineArguments(org.codehaus.plexus.util.cli.Commandline)
     */
    protected void prepareModeSpecificCommandLineArguments(Commandline cl, List args)
    {
        if (fork)
        {
            cl.createArgument().setValue(OPERATION_MODE_DELETECREATE);
            if (ddlFile != null && ddlFile.trim().length() > 0)
            {
                cl.createArgument().setValue("-ddlFile");
                cl.createArgument().setValue(ddlFile);
            }
            if (completeDdl)
            {
                cl.createArgument().setValue("-completeDdl");
            }
            if (includeAutoStart)
            {
                cl.createArgument().setValue("-includeAutoStart");
            }
        }
        else
        {
            args.add(OPERATION_MODE_DELETECREATE);
            if (ddlFile != null && ddlFile.trim().length() > 0)
            {
                args.add("-ddlFile");
                args.add(ddlFile);
            }
            if (completeDdl)
            {
                args.add("-completeDdl");
            }
            if (includeAutoStart)
            {
                args.add("-includeAutoStart");
            }
        }
    }
}