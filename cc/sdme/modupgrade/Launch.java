package cc.sdme.modupgrade;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * Install a mod from internet, remove old versions
 * @author Daniel P Tierney
 * @version 1.0
 */
public class Launch {

	public static boolean sustain = false;

    public static void main(String[] args) {
    	
        if (args.length > 0) {
            Installer installer = new Installer();
            for (int i = 0; i < args.length; i ++) {
                try {
                    if (args[i].equalsIgnoreCase("--get")) {
                        if (args[++i].substring(0, 2).equals("--")) throw new IllegalArgumentException("Option \"get\" requires a URL, but another option was found instead.");
                        installer.downloadFrom = new URL(args[i]);
                    } else if (args[i].equalsIgnoreCase("--path")) {
                        if (args[++i].substring(0, 2).equals("--")) throw new IllegalArgumentException("Option \"path\" requires a directory, but another option was found instead.");
                        installer.gameRoot = Paths.get(args[i]);
                    } else if (args[i].equalsIgnoreCase("--unnest")) {
                        installer.useVersionFolder = false;
                    } else if (args[i].equalsIgnoreCase("--nest")) {
                        installer.useVersionFolder = true;
                    } else if (args[i].equalsIgnoreCase("--mcversion")) {
                        installer.mcversion = args[++i];
                        installer.useVersionFolder = true;
                    } else if (args[i].equalsIgnoreCase("--modid")) {
                    	installer.modid = args[++i];
                    } else if (args[i].equalsIgnoreCase("--sustain")) {
                    	sustain = true;
                    } else {
                        System.out.println("Error on argument \"" + args[i] + "\", ignoring.");
                    }
                } catch (MalformedURLException e) {
                    System.out.println("Could not get from URL: " + args[i]);
                    sus(); return;
                } catch (IndexOutOfBoundsException e) {
                    System.out.println("Option \"" + args[--i].substring(2, args[i].length()) + "\" requires a value, but was passed none.");
                    sus(); return;
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                    sus(); return;
                }
            }

            FileUtils.setupPath(installer.gameRoot);
            
            if (installer.isReady()) {
            	try {
					if (installer.fetch()) {
						System.out.println("Download successful. Removing old versions...");
						for (int i = 0; i < 6; i++) {
							if (installer.removeConflicts()) {
								System.out.println("Eliminated conflicting files. Installing new version...");
								if (installer.install()) {
									System.out.println("Upgrade completed successfully.");
								} else {
									System.out.println("Installation failed for an unknown reason!");
								}
								break;
							} else {
								if (i != 5) {
									System.out.println("Couldn't remove old versions. Will try again in 10 seconds, and " + Integer.toString(5 - i) + " more tries.");
									try {
										Thread.sleep(10000);
									} catch (InterruptedException e) {
										e.printStackTrace();
										i = 5;
									}
								}
								if (i == 5) System.out.println("Couldn't remove old versions. Upgrade canceled to prevent mod ID conflicts.");
							}
						}
					}
				} catch (MismatchException e) {
					System.out.println(e.getMessage());
					System.out.println("Upgrade canceled.");
				}
            	
            } else {
            	System.out.println("You have not supplied enough arguments to proceed.");
            }
            
            
        } else {
        	
            InputStream stream = Launch.class.getResourceAsStream("/readme.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            try {
	            String line = reader.readLine();
	            while (line != null) {
	            	System.out.println(line);
	            	line = reader.readLine();
	            }
            } catch (IOException e){
            	e.printStackTrace();
            } finally {
            	try {
	            	reader.close();
	            	stream.close();
            	} catch (IOException e) {
            		System.out.println("There's no hope");
            	}
            }      
        }
        sus();
    }
    
    /**
     * Hold the process open for debugging
     */
    public static void sus() {
    	if (sustain) {
    		while(true);
    	}
    }

}