package github.tdonuk.stringhelper.util;

import java.io.File;
import java.nio.file.Path;

public final class AppUtils {
	public static final String VERSION = "v1.0.2";
	private AppUtils() {}
	
	public static Path getPluginPath() throws Exception {
		String userPath = System.getProperty("user.home");
		File file = new File(userPath + "/.StringHelpers");
		
		if(!file.exists()) file.mkdirs();
		if(!file.exists()) throw new Exception("Can not access to user dir");
		
		return file.toPath();
	}
	
	public static String getVersion() {
		return VERSION;
	}
}
