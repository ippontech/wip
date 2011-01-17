/*
 *	Copyright 2010,2011 Ippon Technologies 
 *  
 *	This file is part of Wip Portlet.
 *	Wip Portlet is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Wip Portlet is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with Wip Portlet.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.ippon.wip.transformers;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

/**
 * An error listener for the HTMLTransformer used to display custom error messages
 * @author Anthony Luce
 * @author Quentin Thierry
 *
 */
public class ParserErrorListener implements ErrorListener {

	/**
	 * Error message
	 */
	public void error(TransformerException arg0) throws TransformerException {
		System.out.print("[WIP] Parser error : " + arg0.getLocationAsString() + " : ");
		arg0.printStackTrace();
	}

	/**
	 * Fatal error message
	 */
	public void fatalError(TransformerException arg0) throws TransformerException {
		System.out.print("[WIP] Parser fatal : " + arg0.getLocationAsString() + " : ");
		arg0.printStackTrace();
	}

	/**
	 * Warning message
	 */
	public void warning(TransformerException arg0) throws TransformerException {
		System.out.print("[WIP] Parser warning : " + arg0.getLocationAsString() + " : ");
		arg0.printStackTrace();
	}

}
