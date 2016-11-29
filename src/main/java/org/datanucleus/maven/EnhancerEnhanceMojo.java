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

/**
 * Goal to enhance the provided classes as per the input file definition.
 *
 * @goal enhance
 * @phase process-classes
 * @requiresDependencyResolution compile
 * @description Enhances the input classes.
 */
public class EnhancerEnhanceMojo extends AbstractEnhancerEnhanceMojo {

    /**
     * @parameter expression="${classpath}" default-value="${project.compileClasspathElements}"
     * @required
     */
    private List classpathElements;

    @Override
    List getClasspathElements() {
        return classpathElements;
    }


}