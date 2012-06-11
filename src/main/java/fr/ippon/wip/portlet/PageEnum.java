package fr.ippon.wip.portlet;

public enum PageEnum {

	HEADER("header"),
	
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

	private static final String DEFAULT_EXTENSION = ".jsp";

	private static final String DEFAULT_PATH = "/WEB-INF/jsp/config/";

	PageEnum(String name) {
		this(name, DEFAULT_PATH, DEFAULT_EXTENSION);
	}

	PageEnum(String fileName, String path) {
		this(fileName, path, DEFAULT_EXTENSION);
	}

	PageEnum(String fileName, String parentDirectory, String extension) {
		this.fileName = fileName;
		this.parentDirectory = parentDirectory;
		this.extension = extension;
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
		return parentDirectory + fileName + extension;
	}
}
