package cc.sdme.modupgrade;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtils {
	
	public static boolean setupPath(Path path) {
		
		File f = path.toFile();
		
		if (f.exists() && f.isDirectory()) {
			f = path.resolve("mods/modupgrade/temp/").toFile();
			return f.mkdirs();
		} else {
			throw new IllegalArgumentException("Root folder doesn't exist");
		}
		
		
	}
	
	public static File download(URL remote, Path...dest) {
		
		for (int i = 1; i < dest.length; i++) {
			dest[0] = dest[0].resolve(dest[i]);
		}
		
		File newFile = new File(dest[0].toUri());
		
		if (newFile.isDirectory()) {
			if (newFile.exists() || newFile.mkdirs()) {
				String fileName = remote.getPath();
		        try {
		            fileName = fileName.substring(fileName.lastIndexOf('/') + 1, fileName.length());
		        } catch (IndexOutOfBoundsException e) {
		            fileName = "unidentified-mod.jar";
		        }
		        newFile = new File(dest[0].resolve(fileName).toUri());
			} else {
				return null;
			}
		}
		
		try {
			ReadableByteChannel rbc = Channels.newChannel(remote.openStream());
			FileOutputStream fos = new FileOutputStream(newFile);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
			rbc.close();
			return newFile.exists() ? newFile : null;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return null;
		}
		
	}
	
	public static ModInfo extractModInfo(File mod) {
		try {
			
			if (mod.isDirectory()) return null;
			
			ModInfo modinfo = null;
			ZipFile zip = new ZipFile(mod);
			ZipEntry mcmodinfo = zip.getEntry("mcmod.info");
			
			if (mcmodinfo != null) {
			
				InputStream s = zip.getInputStream(mcmodinfo);
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				byte[] buf = new byte[1024];
				int length = 0;
				do {
					os.write(buf, 0, length);
					length = s.read(buf);
				} while (length != -1);
				
				s.close();
				
				String info = os.toString();
				modinfo = new ModInfo();
				
				modinfo.modid = getStringValue(info, "modid");
				modinfo.mcversion = getStringValue(info, "mcversion");
				
			}
			
			zip.close();
			return modinfo;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getStringValue(String json, String key) {
		int seek = json.indexOf(key);
		if (seek != -1) {
			seek += key.length() + 2;
			seek = json.indexOf("\"", seek);
				if (seek != -1) {
					seek++;
					return json.substring(seek, json.indexOf("\"", seek));
				}
		}
		return "";
	}
	
	public static boolean removeConflicts(Path folderPath, Path archivePath, String modid, String mcversion) {
		File folder = folderPath.toFile();
		File archive = archivePath.toFile();
		if (!(archive.exists() && archive.isDirectory() || archive.mkdirs())) return false;
		File[] files = folder.listFiles();
		if (files != null) {
			for (File f : files) {
				ModInfo info = extractModInfo(f);
				if (info != null && info.modid.equals(modid) && info.mcversion.equals(mcversion)) {
					if (!f.renameTo(archivePath.resolve(f.getName()).toFile())) return false;
				}
			}
		}
		return true;
	}
	
	public static class ModInfo {
		private String mcversion;
		private String modid;
		
		public String getMcVersion() {
			return mcversion;
		}
		
		public String getModid() {
			return modid;
		}
		
		@Override
		public String toString() {
			return "[" + modid + " for " + mcversion + "]";
		}
		
	}
	
}
