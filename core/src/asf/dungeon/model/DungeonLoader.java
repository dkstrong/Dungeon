package asf.dungeon.model;

import asf.dungeon.model.floorgen.BalanceTestFloorGen;
import asf.dungeon.model.floorgen.BinarySpaceGen;
import asf.dungeon.model.floorgen.CellularAutomataGen;
import asf.dungeon.model.floorgen.ConnectedRoomsGen;
import asf.dungeon.model.floorgen.DirectionalCaveHallGen;
import asf.dungeon.model.floorgen.FloorMapGenMultiplexer;
import asf.dungeon.model.floorgen.FloorMapGenerator;
import asf.dungeon.model.floorgen.MazeGen;
import asf.dungeon.model.floorgen.PreBuiltFloorGen;
import asf.dungeon.model.floorgen.RandomWalkGen;
import asf.dungeon.model.floorgen.Room;
import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.item.ArmorItem;
import asf.dungeon.model.item.BookItem;
import asf.dungeon.model.item.EquipmentItem;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.KeyItem;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.item.ScrollItem;
import asf.dungeon.model.item.WeaponItem;
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
import asf.dungeon.model.token.logic.FullAgroLogic;
import asf.dungeon.model.token.logic.LocalPlayerLogic;
import asf.dungeon.model.token.logic.Logic;
import asf.dungeon.utility.OutputAndroidFix;
import asf.dungeon.view.DungeonWorld;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
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

        public static Dungeon createDungeon(DungeonWorld.Settings settings){

                FloorMapGenerator floorMapGenerator;
                Logic playerLogic;
                DungeonRand dungeonRand = new DungeonRand(settings.random);

                List<PotionItem.Type> potionTypeValues= Arrays.asList(PotionItem.Type.values());
                Collections.shuffle(potionTypeValues, dungeonRand.random);
                PotionItem.Type[] potions = potionTypeValues.toArray(new PotionItem.Type[potionTypeValues.size()]);

                List<ScrollItem.Type> scrollTypeValues = Arrays.asList(ScrollItem.Type.values());
                Collections.shuffle(scrollTypeValues, dungeonRand.random);
                ScrollItem.Type[] scrolls = scrollTypeValues.toArray(new ScrollItem.Type[scrollTypeValues.size()]);

                List<BookItem.Type> bookTypeValues = Arrays.asList(BookItem.Type.values());
                Collections.shuffle(bookTypeValues, dungeonRand.random);
                BookItem.Type[] books = bookTypeValues.toArray(new BookItem.Type[bookTypeValues.size()]);

                MasterJournal masterMasterJournal = new MasterJournal(potions, scrolls, books);

                if(settings.balanceTest){
                        floorMapGenerator = new BalanceTestFloorGen();
                        playerLogic = new FullAgroLogic(0);
                }else{
                        floorMapGenerator = new FloorMapGenMultiplexer(new FloorMapGenerator[]{
                                new BinarySpaceGen(), new PreBuiltFloorGen(), new ConnectedRoomsGen(), new BinarySpaceGen(),
                                new DirectionalCaveHallGen(), new RandomWalkGen(), new CellularAutomataGen(),
                                new PreBuiltFloorGen(),
                                new ConnectedRoomsGen(),new MazeGen(7,4),new ConnectedRoomsGen(),new MazeGen(15,18)
                        },new FloorMapGenerator[]{
                                new ConnectedRoomsGen(), new MazeGen(10,10)
                        });
                        playerLogic = new LocalPlayerLogic(0);
                }





                Dungeon dungeon = new Dungeon(dungeonRand, masterMasterJournal, floorMapGenerator);

                // spawn player
                boolean spawn = true;

                if(spawn){
                        Token token = dungeon.newPlayerCharacterToken(null, "Player 1", settings.playerModel,
                                playerLogic,
                                new Experience(1, 20, 6, 3, 1,1),
                                0,0);

                        token.getDamage().setDeathRemovalCountdown(Float.NaN);

                        token.getInventory().setNumQuickSlots(1);
                        token.getInventory().add(new PotionItem(dungeon, PotionItem.Type.Health,2 ));
                        token.getInventory().add(new BookItem(dungeon, BookItem.Type.Map));

                        ScrollItem teleportScroll = new ScrollItem(dungeon, ScrollItem.Type.Teleportation, 5);
                        //teleportScroll.identifyItem(token);
                        token.getInventory().add(teleportScroll);

                        ScrollItem scroll = new ScrollItem(dungeon, ScrollItem.Type.Lightning, 5);
                        //scroll.identifyItem(token);
                        token.getInventory().add(scroll);

                        token.getInventory().equip(teleportScroll);

                        if(settings.playerModel == ModelId.Knight){
                                WeaponItem sword = new WeaponItem(ModelId.Sword,"Sword", 3);
                                sword.setCursed(true);
                                token.getInventory().add(sword);
                                token.getInventory().equip(sword);
                                token.get(Journal.class).learn(sword);
                        }else if(settings.playerModel == ModelId.Archer){
                                WeaponItem bow = new WeaponItem(ModelId.Sword,"Bow", 3);
                                bow.setRanged(true);
                                bow.setProjectileFx(FxId.Arrow);
                                token.getInventory().add(bow);
                                token.getInventory().equip(bow);
                                token.get(Journal.class).learn(bow);
                        }else if(settings.playerModel == ModelId.Mage){
                                WeaponItem staff = new WeaponItem(ModelId.Sword,"Staff", 3);
                                staff.setRanged(true);
                                staff.setProjectileFx(FxId.PlasmaBall);
                                token.getInventory().add(staff);
                                token.getInventory().equip(staff);
                                token.get(Journal.class).learn(staff);
                        }



                        ArmorItem armor = new ArmorItem(ModelId.Potion,"Armor",1);
                        token.getInventory().add(armor);
                        //token.getInventory().equip(armor);
                        //token.get(Journal.class).learn(armor);

                        dungeon.moveToken(token, dungeon.generateFloor(0));
                }else{
                        dungeon.setCurrentFloor(0);
                }




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
                        //ex.printStackTrace(); // this usually means the api was changed since the last file save. the file save is bassically useless
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

                kryo.register(com.badlogic.gdx.utils.IdentityMap.class);
                kryo.register(com.badlogic.gdx.utils.SnapshotArray.class);
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
                kryo.register(java.util.Random.class);
                kryo.register(java.util.concurrent.atomic.AtomicLong.class);

                kryo.register(Direction.class);
                kryo.register(Dungeon.class);
                kryo.register(FloorMap.class);
                kryo.register(asf.dungeon.model.DungeonRand.class);
                kryo.register(MasterJournal.class);
                kryo.register(ModelId.class);
                kryo.register(asf.dungeon.model.FxId.class);
                kryo.register(Pair.class);
                kryo.register(Pair[].class);
                kryo.register(Pair[][].class);
                kryo.register(Pathfinder.class);
                kryo.register(Pathfinder.PathingPolicy.class);
                kryo.register(Tile.class);
                kryo.register(asf.dungeon.model.Tile[].class);
                kryo.register(asf.dungeon.model.Tile[][].class);

                kryo.register(FloorMapGenerator.class);
                kryo.register(asf.dungeon.model.floorgen.FloorMapGenerator[].class);
                kryo.register(FloorMapGenMultiplexer.class);
                kryo.register(asf.dungeon.model.floorgen.BalanceTestFloorGen.class);
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
                kryo.register(EquipmentItem.class);
                kryo.register(EquipmentItem[].class);
                kryo.register(KeyItem.class);
                kryo.register(KeyItem.Type.class);
                kryo.register(PotionItem.class);
                kryo.register(PotionItem.Color.class);
                kryo.register(PotionItem.Type.class);
                kryo.register(PotionItem.Type[].class);
                kryo.register(ScrollItem.class);
                kryo.register(ScrollItem.Symbol.class);
                kryo.register(ScrollItem.Type.class);
                kryo.register(ScrollItem.Type[].class);
                kryo.register(BookItem.class);
                kryo.register(BookItem.Symbol.class);
                kryo.register(BookItem.Type.class);
                kryo.register(BookItem.Type[].class);
                kryo.register(asf.dungeon.model.item.WeaponItem.class);
                kryo.register(asf.dungeon.model.item.ArmorItem.class);

                kryo.register(Attack.class);
                kryo.register(Damage.class);
                kryo.register(Experience.class);
                kryo.register(FogMapping.class);
                kryo.register(Inventory.class);
                kryo.register(Inventory.Character.class);
                kryo.register(Inventory.Simple.class);
                kryo.register(Journal.class);
                kryo.register(Loot.class);
                kryo.register(Move.class);
                kryo.register(StatusEffects.class);
                kryo.register(StatusEffects.Effect.class);
                kryo.register(Command.class);
                kryo.register(Token.class);
                kryo.register(asf.dungeon.model.token.Token[].class);
                kryo.register(TokenComponent.class);
                kryo.register(asf.dungeon.model.token.TokenComponent[].class);

                kryo.register(Logic.class);
                kryo.register(LocalPlayerLogic.class);
                kryo.register(asf.dungeon.model.token.logic.FullAgroLogic.class);
                kryo.register(asf.dungeon.model.token.logic.fsm.FSMLogic.class);
                kryo.register(asf.dungeon.model.token.logic.fsm.Monster.class);


                return kryo;
        }
}
