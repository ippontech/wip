/*
 *	Copyright 2010,2011 Ippon Technologies 
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

import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * The WIPTransformer interface defines the method transform that will be
 * implemented by the different transformers (HTML, CSS...)
 *
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public interface WIPTransformer {

    /**
     * This method will modify the original content returned by a http response.
     *
     * @param input the input string to rewrite
     * @return the transformed string
     * @throws TransformerException
     */
    public String transform(String input) throws SAXException, IOException, TransformerException;

}
