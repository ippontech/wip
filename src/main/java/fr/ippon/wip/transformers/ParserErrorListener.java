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

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An error listener for the HTMLTransformer used to display custom error messages
 *
 * @author Anthony Luce
 * @author Quentin Thierry
 */
class ParserErrorListener implements ErrorListener
{

    private static final Logger LOG = Logger.getLogger(ParserErrorListener.class.getName());

    /**
     * Error message
     */
    public void error(TransformerException ex) throws TransformerException {
        LOG.log(Level.WARNING, "XSL parsing error: ", ex);
    }

    /**
     * Fatal error message
     */
    public void fatalError(TransformerException ex) throws TransformerException {
        LOG.log(Level.SEVERE, "XSL parsing fatal error: ", ex);
    }

    /**
     * Warning message
     */
    public void warning(TransformerException ex) throws TransformerException {
        LOG.log(Level.FINE, "XSL parsing warning: " + ex.getMessage());
    }
}
