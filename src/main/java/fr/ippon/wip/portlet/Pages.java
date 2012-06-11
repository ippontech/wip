package fr.ippon.wip.portlet;

public enum Pages {

	HEADER_CONFIG("header"),
	EXISTING_CONFIG("existingconfig"),
	SAVE_CONFIG("saveconfig"),
	SELECT_CONFIG("selectconfig", "/WEB-INF/jsp/"),
	
	GENERAL_SETTINGS("generalsettings"),
	AUTH("auth", "/WEB-INF/jsp/"),
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
