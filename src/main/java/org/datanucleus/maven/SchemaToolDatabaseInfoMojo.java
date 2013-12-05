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
2007 Andy Jefferson - migrated to JPOX, formatted, etc
    ...
**********************************************************************/
package org.datanucleus.maven;

import java.util.List;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * Provides detailed information about the database - limits and datatypes support.
 * @goal schema-dbinfo
 * @requiresDependencyResolution
 */
public class SchemaToolDatabaseInfoMojo extends AbstractSchemaToolMojo
{
    private static final String OPERATION_MODE_DB_INFO = "-dbinfo";

    /**
     * {@inheritDoc}
     * 
     * @see org.datanucleus.maven.AbstractSchemaToolMojo#prepareModeSpecificCommandLineArguments(org.codehaus.plexus.util.cli.Commandline)
     */
    protected void prepareModeSpecificCommandLineArguments(Commandline cl, List args)
    {
        if (fork)
        {
            cl.createArgument().setValue(OPERATION_MODE_DB_INFO);
        }
        else
        { 
            args.add(OPERATION_MODE_DB_INFO);
        }
    }
}