package asf.dungeon.view;

import asf.dungeon.model.SfxId;
import asf.dungeon.utility.ObjectLongMap;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

/**
 *
 * Created by Daniel Strong on 12/7/2014.
 */
public class SfxManager implements Disposable{

        private final DungeonWorld world;
        private float masterVolume =1;
        private ObjectMap<Sound, ObjectLongMap<Object>> sourceIdMap;

        public SfxManager(DungeonWorld world) {
                this.world = world;
                int numSounds=0;
                for (String[] locations : world.assetMappings.getSoundLocations()) {
                        for (String location : locations) {
                                world.assetManager.load(location, Sound.class);
                                ++numSounds;
                        }
                }

                sourceIdMap = new ObjectMap<Sound, ObjectLongMap<Object>>(numSounds);
        }

        public void init(){
                // TODO: SfxManager uses assetManager.get() to access cached sounds, which is syncronized, shoudlnt be a major issue
                // but if there are sound performance issues, this is one place to look

                for (String[] locations : world.assetMappings.getSoundLocations()) {
                        for (String location : locations) {
                                Sound sound = world.assetManager.get(location, Sound.class);
                                sourceIdMap.put(sound, new ObjectLongMap<Object>(8));
                        }
                }

        }



        public void loop(SfxId sfx, Object source){
                String location = world.assetMappings.getRandomSoundLocation(sfx);
                Sound sound = world.assetManager.get(location, Sound.class);
                long id = sound.loop(masterVolume);
                sound.setPriority(id, 10);
                sourceIdMap.get(sound).put(source, id);
        }

        public void stopSound(SfxId sfx, Object source){
                String location = world.assetMappings.getRandomSoundLocation(sfx);
                Sound sound = world.assetManager.get(location, Sound.class);
                long id = sourceIdMap.get(sound).remove(source,0);
                sound.stop(id);

        }

        public void play(SfxId sfx, Object source){
                String location = world.assetMappings.getRandomSoundLocation(sfx);
                Sound sound = world.assetManager.get(location, Sound.class);
                long id = sound.play(masterVolume);
                sourceIdMap.get(sound).put(source, id);

        }

        public void play(SfxId sfx){
                String location = world.assetMappings.getRandomSoundLocation(sfx);
                Sound sound = world.assetManager.get(location, Sound.class);
                sound.play(masterVolume);

        }

        public void killSounds(){
                for (String[] locations : world.assetMappings.getSoundLocations()) {
                        for (String location : locations) {
                                Sound sound = world.assetManager.get(location, Sound.class);
                                sound.stop();

                        }
                }
        }

        public void setPaused(boolean paused){
                for (String[] locations : world.assetMappings.getSoundLocations()) {
                        for (String location : locations) {
                                Sound sound = world.assetManager.get(location, Sound.class);
                                if(paused){
                                        sound.pause();
                                }else{
                                        sound.resume();
                                }

                        }
                }
        }


        @Override
        public void dispose() {
                killSounds();
        }
}
