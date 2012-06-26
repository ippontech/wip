/*
 *	Copyright 2012 Ippon Technologies
 *
 *	This file is part of Web Integration Portlet (WIP).
 *	Web Integration Portlet (WIP) is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Web Integration Portlet (WIP) is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Lesser General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public License
 *	along with Web Integration Portlet (WIP).  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.ippon.wip.transformers;

import fr.ippon.wip.config.WIPConfiguration;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;

/**
 * A class used to process a get a XSL source from a WIPConfiguration object
 *
 * @author François Prot
 */
public class ClippingURIResolver implements URIResolver {
	
    private WIPConfiguration config;

    public ClippingURIResolver (WIPConfiguration config) {
        this.config = config;
    }
    
    public Source resolve(String href, String base) throws TransformerException {
    	return new StreamSource(new ByteArrayInputStream(config.getXsltClipping().getBytes()));
    }
}
