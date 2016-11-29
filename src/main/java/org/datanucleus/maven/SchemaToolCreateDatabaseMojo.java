/**********************************************************************
Copyright (c) 2014 Andy Jefferson and others. All rights reserved.
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
 * Generates the database specified by the catalogName/schemaName parameters.
 * @goal schema-createdatabase
 * @requiresDependencyResolution runtime
 * @description Creates the database for the specified catalogName/schemaName.
 */
public class SchemaToolCreateDatabaseMojo extends AbstractSchemaToolMojo
{
    private static final String OPERATION_MODE_CREATE = "-createDatabase";

    /**
     * @parameter expression="${classpath}" default-value="${project.compileClasspathElements}"
     * @required
     */
    private List classpathElements;

    @Override
    List getClasspathElements() {
        return classpathElements;
    }




    /**
     * {@inheritDoc}
     * @see org.datanucleus.maven.AbstractSchemaToolMojo#prepareModeSpecificCommandLineArguments(org.codehaus.plexus.util.cli.Commandline, java.util.List)
     */
    protected void prepareModeSpecificCommandLineArguments(Commandline cl, List args)
    {
        if (fork)
        {
            cl.createArg().setValue(OPERATION_MODE_CREATE);

            if (catalogName != null && !catalogName.isEmpty())
            {
                cl.createArg().setLine("-catalog " + catalogName);
            }
            if (schemaName != null && !schemaName.isEmpty())
            {
                cl.createArg().setLine("-schema " + schemaName);
            }
        }
        else
        {
            args.add(OPERATION_MODE_CREATE);

            if (catalogName != null && !catalogName.isEmpty())
            {
                args.add("-catalog");
                args.add(catalogName);
            }
            if (schemaName != null && !schemaName.isEmpty())
            {
                args.add("-schema");
                args.add(schemaName);
            }
        }
    }
}