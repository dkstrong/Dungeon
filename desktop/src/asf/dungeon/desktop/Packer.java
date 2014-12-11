package asf.dungeon.desktop;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

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
                packerSettings.maxWidth = 512;
                packerSettings.maxHeight = 512;
                packerSettings.pot = true;
                packerSettings.square = true;
                TexturePacker.processIfModified(packerSettings, "../images/Menu", "../android/assets/Packs","Menu");

                TexturePacker.Settings packerSettings2 = new TexturePacker.Settings();
                packerSettings2.maxWidth = 1024;
                packerSettings2.maxHeight = 1024;
                packerSettings2.pot = true;
                packerSettings2.square = true;
                TexturePacker.processIfModified(packerSettings2, "../images/Game", "../android/assets/Packs","Game");

                TexturePacker.Settings packerSettings3 = new TexturePacker.Settings();
                packerSettings3.minWidth = 512;
                packerSettings3.maxHeight = 512;
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
}
