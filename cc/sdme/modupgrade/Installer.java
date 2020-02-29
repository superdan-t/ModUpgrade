package cc.sdme.modupgrade;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *  3-step installer:
 *  1. Fetch
 *  2. Remove conflicts
 *  3. Install
 */
public class Installer {

    public URL downloadFrom;
    public Path gameRoot = Paths.get(System.getProperty("user.dir"));
    public boolean useVersionFolder = false;
    public String mcversion = null;
    public String modid = null;
    public File tempFile;
    
    public boolean isReady() {
    	return downloadFrom != null;
    }
    
    public boolean fetch() throws MismatchException {
    	tempFile = FileUtils.download(downloadFrom, gameRoot.resolve("mods/modupgrade/temp/"));
    	FileUtils.ModInfo info = FileUtils.extractModInfo(tempFile);
    	if (mcversion != null && !info.getMcVersion().equals(mcversion)) {
    		tempFile.delete();
    		throw new MismatchException("The target version is " + mcversion + ", but the new file has version " + info.getMcVersion());
    	}
    	if (modid != null && !info.getModid().equals(modid)) {
    		tempFile.delete();
    		throw new MismatchException("The target mod ID is " + modid + ", but the new file has mod ID " + info.getModid());
    	}
    	return tempFile != null;
    }
    
    public boolean removeConflicts() {
    	if (mcversion == null || modid == null) {
    		FileUtils.ModInfo tempInfo = FileUtils.extractModInfo(tempFile);
    		mcversion = tempInfo.getMcVersion();
    		modid = tempInfo.getModid();
    	}
    	if (!FileUtils.removeConflicts(gameRoot.resolve("mods/"), gameRoot.resolve("mods/modupgrade/old/" + mcversion + "/"), modid, mcversion)) return false;
    	return FileUtils.removeConflicts(gameRoot.resolve("mods/" + mcversion + "/"), gameRoot.resolve("mods/modupgrade/old/" + mcversion + "/"), modid, mcversion);
    }
    
    public boolean install() {
    	Path path = gameRoot.resolve("mods/");
    	if (useVersionFolder) path = path.resolve(mcversion + "/");
    	if (path.toFile().exists() || path.toFile().mkdirs()) {
    		path = path.resolve(tempFile.getName());
    		return tempFile.renameTo(path.toFile());
    	} else {
    		return false;
    	}
    }
    


}
