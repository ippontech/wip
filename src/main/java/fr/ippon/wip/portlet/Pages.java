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

package fr.ippon.wip.portlet;

public enum Pages {

	SELECT_CONFIG("selectconfig", "/WEB-INF/jsp/"),
	AUTH("auth", "/WEB-INF/jsp/"),

	EXISTING_CONFIG("existingconfig"),
	SAVE_CONFIG("saveconfig"),
	GENERAL_SETTINGS("generalsettings"),
	CACHING("caching"),
	CLIPPING("clipping"),
	CSS_REWRITING("cssrewriting"),
	HTML_REWRITING("htmlrewriting"),
	JS_REWRITING("jsrewriting"),
	LTPA_AUTH("ltpaauth");
		
	private final String fileName;

	private final String parentDirectory;

	private final String extension;
	
	private final String path;

	private static final String DEFAULT_EXTENSION = ".jsp";

	private static final String DEFAULT_PATH = "/WEB-INF/jsp/config/";

	Pages(String name) {
		this(name, DEFAULT_PATH, DEFAULT_EXTENSION);
	}

	Pages(String fileName, String path) {
		this(fileName, path, DEFAULT_EXTENSION);
	}

	Pages(String fileName, String parentDirectory, String extension) {
		this.fileName = fileName;
		this.parentDirectory = parentDirectory;
		this.extension = extension;
		this.path = parentDirectory + fileName + extension;
	}

	public String getExtension() {
		return extension;
	}

	public String getFileName() {
		return fileName;
	}

	public String getParentDirectory() {
		return parentDirectory;
	}

	public String getPath() {
		return path;
	}
}
