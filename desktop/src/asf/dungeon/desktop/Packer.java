package asf.dungeon.desktop;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 *
 *
 * Use the gradle task "packImages" to run this program.
 *
 * Created by Daniel Strong on 12/10/2014.
 */
public class Packer {
        public static void main (String[] arg) {
                // https://github.com/libgdx/libgdx/wiki/Texture-packer#settings
                TexturePacker.Settings packerSettings = new TexturePacker.Settings();
                packerSettings.minWidth = 512;
                packerSettings.minHeight = 512;
                packerSettings.maxWidth = 512;
                packerSettings.maxHeight = 512;
                packerSettings.paddingX = 1;
                packerSettings.paddingY = 1;
                TexturePacker.processIfModified(packerSettings, "../images/GameSkin", "../android/assets/Packs","GameSkin");

                 copy("../images/GameSkin","../android/assets/Packs",".fnt",".json");

                TexturePacker.Settings packerSettings2 = new TexturePacker.Settings();
                packerSettings2.minWidth = 512;
                packerSettings2.minHeight = 512;
                packerSettings2.maxWidth = 512;
                packerSettings2.maxHeight = 512;
                packerSettings2.edgePadding = false;
                packerSettings2.paddingX = 0;
                packerSettings2.paddingY=0;
                packerSettings2.pot = true;
                packerSettings2.square = true;
                packerSettings2.combineSubdirectories = true;
                TexturePacker.processIfModified(packerSettings2, "../images/Game", "../android/assets/Packs","Game");

                TexturePacker.Settings packerSettings3 = new TexturePacker.Settings();
                packerSettings3.minWidth = 512;
                packerSettings3.minHeight = 512;
                packerSettings3.maxWidth = 512;
                packerSettings3.maxHeight = 512;
                packerSettings3.paddingX = 0;
                packerSettings3.paddingY = 0;
                packerSettings3.edgePadding = false;
                packerSettings3.combineSubdirectories = true;
                packerSettings3.square = true;
                packerSettings3.grid = true;
                TexturePacker.processIfModified(packerSettings3, "../images/Particle", "../android/assets/ParticleEffects","Particle");


        }

        private static void copy(String src, String dst, final String... extensions){
                System.out.println("Copying");
                File srcDir = new File(src);

                File[] files = srcDir.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                                for (String extension : extensions)
                                        if(name.endsWith(extension))
                                                return true;
                                return false;
                        }
                });

                for (File file : files) {
                        File newLoc = new File(dst+"\\"+file.getName());
                        System.out.println("Writing: "+dst+"\\"+file.getName());
                        try {
                                Files.copy(file.toPath(), newLoc.toPath(),
                                        StandardCopyOption.REPLACE_EXISTING,
                                        StandardCopyOption.COPY_ATTRIBUTES);
                        } catch (IOException e) {
                                e.printStackTrace();
                                System.exit(1);
                        }

                }


        }
}
