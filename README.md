# ModUpgrade

## Deprecated Project Notice
I have not updated or otherwise maintained this project since early 2020, my first semester of undergrad. The code hasn’t been relevant in any of my technical projects since then, and I have no intention of ever using this project again. My program design skills, quality standards, language preferences, and other technical abilities have developed a lot since 2019, assisted by my interest in maintaining projects outside of academia. That being said, I have decided to keep my old projects public as I reflect on my own educational journey.

## Security Concerns
This program can download arbitrary Java jar files, but makes no effort to verify their authenticity. Since this program wasn't intended to be used directly by end users, it is the duty of developers to ensure their mod doesn't allow attackers access to distribute malicious updates to users. Also, mod developers who become evil could use this to push malicious updates of their own mod. Using modpack managers is probably more secure than something like this.

## Description

This is a command-line utility to enable auto-updating for Minecraft mods. Mods can download their own new versions, but cannot remove old versions since they are locked while the game is running. This utility will download the new mod, verify the mod ID and mcversion if they were supplied, move any conflicting mods (e.g. old versions) to an archive folder, and then add in the new mod. It should be launched at game shutdown, as it only tries for 60 seconds.

## Options

The arguments to supply are very simple. The only required argument is `get` followed by a valid URL.

    --modid	    Mod ID of the mod to install
    --mcversion Game version that Minecraft Forge is running on (enables nest)
    --path      Game profile directory
    --get       URL of replacement library
    --nest      Install new libraries in version folder (no args)
    --unnest    Install new libraries in mods folder (no args)
    
## License Statement

The following statements aren't guaranteed to provide accurate descriptions of licenses, permissions, and limitations. They have no legal value. It is still your responsibility to assure compliance with all terms of the GNU LGPL 3.0.

Overview: You could use this in your mod as a "shared library", assuming you do not make any modifications to ModUpgrade itself. If you make changes to ModUpgrade, your derivative product must be made available under the same license.

## Sample Implementation with Forge

This is a skeleton way of implementing ModUpgrader. Make sure your mod has an `mcmod.info` file with modid. See my repository for SpecializedArmor for an example of a full-fledged command system.

Please don't make anything that forces updates upon users or downloads anything without permission.

```java
public class MyModUpgradeHandler {

    private static Thread updater;

    public static void updateModToLatest() {
        updater = new Thread(new LaunchModUpgrade("https://example.com/my-latest-mod-binary.jar"));
        Runtime.getRuntime().addShutdownHook(updater);
    }
    
    public static void cancelUpdate() {
        if (updater != null) {
            Runtime.getRuntime().removeShutdownHook(updater);
        }
    }

    private static class LaunchModUpgrade implements Runnable {
    
        private String locationOfMod;
        
        private ModUpgrader(String locationOfMod) {
            this.locationOfMod = locationOfMod;
        }
    
        @Override
        public void run() {
            // mcversion is used for folder nesting (e.g. ./mods/1.12.2/install-location.jar)
            ProcessBuilder modupgrade = new ProcessBuilder("java", "-jar",
            Paths.get(System.getProperty("user.dir")).resolve("mods/ModUpgrade/modupgrade-1.0.jar").toString(), 
            "--get", locationOfMod, "--mcversion", "1.12.2",
            "--path", System.getProperty("user.dir"), "--modid", "mymodid");
            try {
                modupgrade.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
```
