/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.soap.xfire.config;

import org.mule.config.spring.parsers.SingleElementDefinitionParser;
import org.mule.providers.soap.xfire.XFireConnector;

public class XfireElementDefinitionParser extends SingleElementDefinitionParser
{
    public XfireElementDefinitionParser()
    {
        super(XFireConnector.class, true);
        this.registerAttributeMapping("bindingProviderClass", "bindingProvider");
        this.registerAttributeMapping("clientTransportClass", "clientTransport");
        this.registerAttributeMapping("typeMappingRegistryClass", "typeMappingRegistry");
    }
}
