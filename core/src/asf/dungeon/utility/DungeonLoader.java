package asf.dungeon.utility;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.MasterJournal;
import asf.dungeon.model.PotionItem;
import asf.dungeon.model.factory.FloorMapGenerator;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.Input;

import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Danny on 11/13/2014.
 */
public class DungeonLoader {



        public DungeonLoader() {

        }

        public static Dungeon createDungeon(Dungeon.Listener listener, FloorMapGenerator floorMapGenerator){

                List<PotionItem.Type> colors= Arrays.asList(PotionItem.Type.values());
                Collections.shuffle(colors, MathUtils.random);
                PotionItem.Type[] potions = colors.toArray(new PotionItem.Type[colors.size()]);

                MasterJournal masterMasterJournal = new MasterJournal(potions);

                Dungeon dungeon = new Dungeon(listener, masterMasterJournal, floorMapGenerator);



                return dungeon;

        }

        // https://github.com/EsotericSoftware/kryo#unsafe-based-io

        public static Dungeon loadDungeon(Dungeon.Listener listener) {
                boolean isLocAvailable = Gdx.files.isLocalStorageAvailable();
                if(!isLocAvailable){
                        throw new IllegalStateException("could not load dungeon, local storage is not available.");
                }
                String locRoot = Gdx.files.getLocalStoragePath();
                System.out.println("loading dungeon from: "+locRoot+"sav\\dungeon.sav.bin");
                FileHandle fileHandle = Gdx.files.local("sav/dungeon.sav.bin");
                if(!fileHandle.exists()){
                        return null;
                }

                InputStream in = fileHandle.read();

                Kryo kryo = getKryo();
                Input input = new Input(in);
                Dungeon dungeon = kryo.readObject(input, Dungeon.class);
                input.close();

                try {
                        in.close();
                } catch (IOException e) {
                        e.printStackTrace();
                }

                dungeon.setListener(listener);


                return dungeon;
       }

        public static void saveDungeon(Dungeon dungeon) throws IOException{
                boolean isLocAvailable = Gdx.files.isLocalStorageAvailable();
                if(!isLocAvailable){
                        throw new IOException("could not save dungeon, local storage is not available.");
                }
                String locRoot = Gdx.files.getLocalStoragePath();
                System.out.println("saving dungeon to: "+locRoot+"sav\\dungeon.sav.bin");

                FileHandle fileHandle = Gdx.files.local("sav/dungeon.sav.bin");
                OutputStream out = fileHandle.write(false);

                Kryo kryo = getKryo();
                Output output = new Output(out);
                kryo.writeObject(output, dungeon);
                output.close();
                out.close();
        }

        private static Kryo getKryo(){
                Kryo kryo = new Kryo();
                //kryo.setRegistrationRequired(true);
                //kryo.register(Dungeon.class);


                return kryo;
        }
}
