package asf.dungeon.desktop;

import com.badlogic.gdx.tools.flame.FlameMain;

import javax.swing.UIManager;
import java.awt.EventQueue;

/**
 * Created by Danny on 11/21/2014.
 */
public class Flame {
        public static void main(String[] args){

                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if ("Nimbus".equals(info.getName())) {
                                try {
                                        UIManager.setLookAndFeel(info.getClassName());
                                } catch (Throwable ignored) {
                                }
                                break;
                        }
                }
                EventQueue.invokeLater(new Runnable() {
                        public void run() {
                                FlameMain flameMain = new FlameMain();
/*
                                Field assetManagerField = null;
                                try {
                                        assetManagerField = FlameMain.class.getDeclaredField("assetManager");
                                } catch (NoSuchFieldException e) {
                                        e.printStackTrace();
                                }
                                assetManagerField.setAccessible(true);
                                AssetManager assetManager = null;
                                try {
                                        assetManager = (AssetManager)assetManagerField.get(flameMain);
                                } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                }

                                assetManager.load("ParticleEffects/shock.png",Texture.class);
                                assetManager.finishLoading();
                                */
                        }
                });



        }
}
