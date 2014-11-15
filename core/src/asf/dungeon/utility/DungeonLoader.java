package asf.dungeon.utility;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.MasterJournal;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Pathfinder;
import asf.dungeon.model.Tile;
import asf.dungeon.model.factory.BinarySpaceGen;
import asf.dungeon.model.factory.CellularAutomataGen;
import asf.dungeon.model.factory.ConnectedRoomsGen;
import asf.dungeon.model.factory.DirectionalCaveHallGen;
import asf.dungeon.model.factory.FloorMapGenMultiplexer;
import asf.dungeon.model.factory.FloorMapGenerator;
import asf.dungeon.model.factory.MazeGen;
import asf.dungeon.model.factory.PreBuiltFloorGen;
import asf.dungeon.model.factory.RandomWalkGen;
import asf.dungeon.model.factory.Room;
import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.KeyItem;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.token.Attack;
import asf.dungeon.model.token.Command;
import asf.dungeon.model.token.Damage;
import asf.dungeon.model.token.Experience;
import asf.dungeon.model.token.FogMapping;
import asf.dungeon.model.token.Inventory;
import asf.dungeon.model.token.Journal;
import asf.dungeon.model.token.Loot;
import asf.dungeon.model.token.Move;
import asf.dungeon.model.token.StatusEffects;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.TokenComponent;
import asf.dungeon.model.token.logic.LogicProvider;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Danny on 11/13/2014.
 */
public class DungeonLoader {



        private DungeonLoader() {

        }

        public static Dungeon createDungeon( FloorMapGenerator floorMapGenerator){

                List<PotionItem.Type> colors= Arrays.asList(PotionItem.Type.values());
                Collections.shuffle(colors, MathUtils.random);
                PotionItem.Type[] potions = colors.toArray(new PotionItem.Type[colors.size()]);

                MasterJournal masterMasterJournal = new MasterJournal(potions);

                Dungeon dungeon = new Dungeon(masterMasterJournal, floorMapGenerator);

                return dungeon;

        }

        // https://github.com/EsotericSoftware/kryo

        /**
         * if null is supplied as the listener, then you'll need to set the listener manually
         * with dungeon.setListener()
         *
         * @param fileName
         * @return
         */
        public static Dungeon loadDungeon(String fileName) {
                boolean isLocAvailable = Gdx.files.isLocalStorageAvailable();
                if(!isLocAvailable){
                        return null;
                }
                Gdx.app.log("DungeonLoader", "loading dungeon from: "+Gdx.files.getLocalStoragePath()+"sav\\"+fileName+".sav");

                FileHandle fileHandle = Gdx.files.local("sav/"+fileName+".sav");
                if(!fileHandle.exists()){
                        return null;
                }

                InputStream in = fileHandle.read();
                Kryo kryo = getKryo();
                Input input = new Input(in);
                Dungeon dungeon;
                try{
                        dungeon = kryo.readObject(input, Dungeon.class);
                }catch(KryoException ex){
                        dungeon = null;
                        ex.printStackTrace(); // this usually means the api was changed since the last file save. the file save is bassically useless
                }
                input.close();
                try {
                        in.close();
                } catch (IOException e) {
                        e.printStackTrace();
                }

                return dungeon;
       }

        public static void saveDungeon(Dungeon dungeon, String fileName) throws IOException{
                boolean isLocAvailable = Gdx.files.isLocalStorageAvailable();
                if(!isLocAvailable){
                        throw new IOException("could not save dungeon, local storage is not available.");
                }
                Gdx.app.log("DungeonLoader", "saving dungeon to: "+Gdx.files.getLocalStoragePath()+"sav\\"+fileName+".sav");

                FileHandle fileHandle = Gdx.files.local("sav/"+fileName+".sav");

                OutputStream out = fileHandle.write(false);
                Kryo kryo = getKryo();
                OutputAndroidFix output = new OutputAndroidFix(out);
                kryo.writeObject(output, dungeon);
                output.close();
                out.close();
        }

        // http://developer.android.com/reference/java/lang/ref/SoftReference.html
        private static SoftReference<Kryo> kryoSoftReference;

        private static Kryo getKryo(){
                if(kryoSoftReference != null){
                        Kryo kryo = kryoSoftReference.get();
                        if(kryo != null){
                                return kryo;
                        }
                }
                Kryo kryo = new Kryo();
                kryoSoftReference = new SoftReference<Kryo>(kryo);
                kryo.setRegistrationRequired(true);

                kryo.register(Array.class);
                kryo.register(com.badlogic.gdx.utils.Array[].class);
                kryo.register(Array.ArrayIterable.class);
                kryo.register(Array.ArrayIterator.class);
                kryo.register(Object[].class);
                kryo.register(int[].class);
                kryo.register(int[][].class);
                kryo.register(byte[].class);
                kryo.register(byte[][].class);
                kryo.register(Float[].class);
                kryo.register(java.util.HashMap.class);

                kryo.register(Direction.class);
                kryo.register(Dungeon.class);
                kryo.register(FloorMap.class);

                kryo.register(MasterJournal.class);
                kryo.register(ModelId.class);
                kryo.register(Pair.class);
                kryo.register(Pair[].class);
                kryo.register(Pair[][].class);
                kryo.register(Pathfinder.class);
                kryo.register(Pathfinder.PathingPolicy.class);
                kryo.register(Tile.class);
                kryo.register(asf.dungeon.model.Tile[].class);
                kryo.register(asf.dungeon.model.Tile[][].class);

                kryo.register(FloorMapGenerator.class);
                kryo.register(asf.dungeon.model.factory.FloorMapGenerator[].class);
                kryo.register(FloorMapGenMultiplexer.class);
                kryo.register(BinarySpaceGen.class);
                kryo.register(CellularAutomataGen.class);
                kryo.register(ConnectedRoomsGen.class);
                kryo.register(DirectionalCaveHallGen.class);
                kryo.register(MazeGen.class);
                kryo.register(PreBuiltFloorGen.class);
                kryo.register(RandomWalkGen.class);
                kryo.register(Room.class);

                kryo.register(FogMap.class);
                kryo.register(FogState.class);
                kryo.register(asf.dungeon.model.fogmap.FogState[].class);
                kryo.register(asf.dungeon.model.fogmap.FogState[][].class);

                kryo.register(Item.class);
                kryo.register(Item[].class);
                kryo.register(KeyItem.class);
                kryo.register(KeyItem.Type.class);
                kryo.register(PotionItem.class);
                kryo.register(PotionItem.Color.class);
                kryo.register(PotionItem.Type.class);
                kryo.register(PotionItem.Type[].class);

                kryo.register(Attack.class);
                kryo.register(Damage.class);
                kryo.register(Experience.class);
                kryo.register(FogMapping.class);
                kryo.register(Inventory.class);
                kryo.register(Journal.class);
                kryo.register(Loot.class);
                kryo.register(Move.class);
                kryo.register(StatusEffects.class);
                kryo.register(StatusEffects.Effect.class);
                kryo.register(asf.dungeon.model.token.QuickSlot.class);
                kryo.register(Command.class);
                kryo.register(Token.class);
                kryo.register(asf.dungeon.model.token.Token[].class);
                kryo.register(TokenComponent.class);
                kryo.register(asf.dungeon.model.token.TokenComponent[].class);

                kryo.register(LogicProvider.class);
                kryo.register(asf.dungeon.model.token.logic.LocalPlayerLogicProvider.class);
                kryo.register(asf.dungeon.model.token.logic.SimpleLogicProvider.class);


                return kryo;
        }
}
