package asf.dungeon.model;

import asf.dungeon.model.floorgen.FloorMapGenMultiplexer;
import asf.dungeon.model.floorgen.FloorMapGenerator;
import asf.dungeon.model.floorgen.cave.CellularAutomataGen;
import asf.dungeon.model.floorgen.cave.DirectionalCaveHallGen;
import asf.dungeon.model.floorgen.cave.MazeGen;
import asf.dungeon.model.floorgen.cave.RandomWalkGen;
import asf.dungeon.model.floorgen.prebuilt.BalanceTestFloorGen;
import asf.dungeon.model.floorgen.prebuilt.PreBuiltFloorGen;
import asf.dungeon.model.floorgen.prebuilt.TestAssetsFloorGen;
import asf.dungeon.model.floorgen.prebuilt.TutorialFloorGen;
import asf.dungeon.model.floorgen.room.BinarySpaceGen;
import asf.dungeon.model.floorgen.room.ConnectedRoomsGen;
import asf.dungeon.model.floorgen.room.Room;
import asf.dungeon.model.floorgen.room.ZeldaGen;
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
import asf.dungeon.model.token.CharacterInventory;
import asf.dungeon.model.token.Command;
import asf.dungeon.model.token.CrateInventory;
import asf.dungeon.model.token.Damage;
import asf.dungeon.model.token.Experience;
import asf.dungeon.model.token.FogMapping;
import asf.dungeon.model.token.Inventory;
import asf.dungeon.model.token.Journal;
import asf.dungeon.model.token.Loot;
import asf.dungeon.model.token.Move;
import asf.dungeon.model.token.StatusEffect;
import asf.dungeon.model.token.StatusEffects;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.TokenComponent;
import asf.dungeon.model.token.logic.LocalPlayerLogic;
import asf.dungeon.model.token.logic.Logic;
import asf.dungeon.model.token.logic.fsm.FsmLogic;
import asf.dungeon.model.token.logic.fsm.Monster;
import asf.dungeon.model.token.quest.Quest;
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
 * Created by Daniel Strong on 11/13/2014.
 */
public class DungeonLoader {

        private DungeonLoader() {

        }

        public static Dungeon createDungeon(DungeonWorld.Settings settings) {

                FloorMapGenerator floorMapGenerator;
                Logic playerLogic;
                DungeonRand dungeonRand = new DungeonRand(settings.random);

                List<PotionItem.Type> potionTypeValues = Arrays.asList(PotionItem.Type.values());
                Collections.shuffle(potionTypeValues, dungeonRand.random);
                PotionItem.Type[] potions = potionTypeValues.toArray(new PotionItem.Type[potionTypeValues.size()]);

                List<ScrollItem.Type> scrollTypeValues = Arrays.asList(ScrollItem.Type.values());
                Collections.shuffle(scrollTypeValues, dungeonRand.random);
                ScrollItem.Type[] scrolls = scrollTypeValues.toArray(new ScrollItem.Type[scrollTypeValues.size()]);

                List<BookItem.Type> bookTypeValues = Arrays.asList(BookItem.Type.values());
                Collections.shuffle(bookTypeValues, dungeonRand.random);
                BookItem.Type[] books = bookTypeValues.toArray(new BookItem.Type[bookTypeValues.size()]);

                MasterJournal masterMasterJournal = new MasterJournal(potions, scrolls, books);

                if (settings.balanceTest) {
                        floorMapGenerator = new BalanceTestFloorGen();
                        //playerLogic = new FullAgroLogic(0);
                        playerLogic = new FsmLogic(0, null, Monster.Sleep);
                } else {

                        floorMapGenerator = new FloorMapGenMultiplexer(new FloorMapGenerator[]{
                                new ConnectedRoomsGen(),new TutorialFloorGen(),new MazeGen(15, 19),
                                new ConnectedRoomsGen(), new CellularAutomataGen(),  new TestAssetsFloorGen(),
                                new CellularAutomataGen(), new RandomWalkGen(), new CellularAutomataGen(),
                                new PreBuiltFloorGen(),
                                new ConnectedRoomsGen(), new MazeGen(7, 4)
                        }, new FloorMapGenerator[]{
                                new ConnectedRoomsGen(), new MazeGen(10, 10)
                        });
                        playerLogic = new LocalPlayerLogic(0);
                }


                Dungeon dungeon = new Dungeon(dungeonRand, masterMasterJournal, floorMapGenerator);

                // spawn player
                boolean spawn = true;

                if (spawn) {
                        Token token = dungeon.newPlayerCharacterToken(null, "Player 1", settings.playerModel,
                                playerLogic,
                                new Experience(1, 20, 6, 3, 1, 1),
                                0, 0);

                        //token.statusEffects.addStatusEffect(StatusEffects.Effect.Blind);
                        token.inventory.setNumQuickSlots(3);

                        //token.inventory.add(new KeyItem(KeyItem.Type.Silver));
                        //token.inventory.add(new KeyItem(KeyItem.Type.Gold));
                        //token.inventory.add(new KeyItem(KeyItem.Type.Red));

                        ScrollItem potion = new ScrollItem(dungeon, ScrollItem.Type.Teleportation, 4);
                        potion.identifyItem(token);
                        token.inventory.add(potion);
                        //token.inventory.equip(potion);

                        BookItem book = new BookItem(dungeon, BookItem.Type.MagicMapping);
                        token.inventory.add(book);

                        book = new BookItem(dungeon, BookItem.Type.MagicMapping);
                        token.inventory.add(book);

                        PotionItem health = new PotionItem(dungeon, PotionItem.Type.Speed, 4);
                        health.identifyItem(token);
                        token.inventory.add(health);

                        PotionItem paralyze = new PotionItem(dungeon, PotionItem.Type.Paralyze, 4);
                        paralyze.identifyItem(token);
                        token.inventory.add(paralyze);

                        ArmorItem armor = new ArmorItem(ModelId.SwordLarge, "Simple Armor", 1);
                        armor.identifyItem(token);
                        token.inventory.add(armor);
                        token.inventory.equip(armor);

                        if (settings.playerModel == ModelId.Knight) {
                                WeaponItem sword = new WeaponItem(ModelId.Sword_01, "Sword", 3);
                                //sword.setAttackDuration(1);
                                token.inventory.add(sword);
                                token.inventory.equip(sword);
                                token.get(Journal.class).learn(sword);

                                WeaponItem bow = new WeaponItem(ModelId.Bow_01, "Bow", 2, FxId.Arrow);
                                bow.setRangedStats(3, 1);
                                token.inventory.add(bow);
                                token.get(Journal.class).learn(bow);
                                //token.inventory.equip(bow);

                                WeaponItem staff = new WeaponItem(ModelId.SwordLarge, "Staff", 3, FxId.PlasmaBall);
                                staff.identifyItem(token);
                                token.inventory.add(staff);
                        } else if (settings.playerModel == ModelId.Archer) {
                                WeaponItem bow = new WeaponItem(ModelId.Bow_01, "Bow", 2, 2f, 1f,FxId.Arrow , 3, 2f);
                                //bow.setCursed(true);
                                token.inventory.add(bow);
                                token.inventory.equip(bow);

                                WeaponItem sword = new WeaponItem(ModelId.Sword_01, "Sword", 3);
                                token.inventory.add(sword);
                                token.get(Journal.class).learn(sword);

                                WeaponItem staff = new WeaponItem(ModelId.SwordLarge, "Staff", 3, FxId.PlasmaBall);
                                staff.identifyItem(token);
                                token.inventory.add(staff);
                        } else if (settings.playerModel == ModelId.Mage) {
                                WeaponItem staff = new WeaponItem(ModelId.Bow_01, "Staff", 3, FxId.PlasmaBall);
                                staff.identifyItem(token);
                                token.inventory.add(staff);
                                token.inventory.equip(staff);

                                WeaponItem sword = new WeaponItem(ModelId.Sword_01, "Sword", 3);
                                token.inventory.add(sword);
                                token.get(Journal.class).learn(sword);

                                WeaponItem bow = new WeaponItem(ModelId.Bow_01, "Bow", 2, FxId.Arrow);
                                bow.setRangedStats(3, 1);
                                token.inventory.add(bow);
                                token.get(Journal.class).learn(bow);

                        } else if (settings.playerModel == ModelId.Priest) {
                                WeaponItem sword = new WeaponItem(ModelId.SwordLarge, "Sword", 3);
                                //sword.setAttackDuration(1);
                                token.inventory.add(sword);
                                token.inventory.equip(sword);
                                token.get(Journal.class).learn(sword);
                        }


                        dungeon.moveToken(token, dungeon.generateFloor(0));
                } else {
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
                if (!isLocAvailable) {
                        return null;
                }
                Gdx.app.log("DungeonLoader", "loading dungeon from: " + Gdx.files.getLocalStoragePath() + "sav\\" + fileName + ".sav");

                FileHandle fileHandle = Gdx.files.local("sav/" + fileName + ".sav");
                if (!fileHandle.exists()) {
                        return null;
                }

                InputStream in = fileHandle.read();
                Kryo kryo = getKryo();
                Input input = new Input(in);
                Dungeon dungeon;
                try {
                        dungeon = kryo.readObject(input, Dungeon.class);
                } catch (KryoException ex) {
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

        public static void saveDungeon(Dungeon dungeon, String fileName) throws IOException {
                boolean isLocAvailable = Gdx.files.isLocalStorageAvailable();
                if (!isLocAvailable) {
                        throw new IOException("could not save dungeon, local storage is not available.");
                }
                Gdx.app.log("DungeonLoader", "saving dungeon to: " + Gdx.files.getLocalStoragePath() + "sav\\" + fileName + ".sav");

                FileHandle fileHandle = Gdx.files.local("sav/" + fileName + ".sav");

                OutputStream out = fileHandle.write(false);
                Kryo kryo = getKryo();
                OutputAndroidFix output = new OutputAndroidFix(out);
                kryo.writeObject(output, dungeon);
                output.close();
                out.close();
        }

        // http://developer.android.com/reference/java/lang/ref/SoftReference.html
        private static SoftReference<Kryo> kryoSoftReference;

        private static Kryo getKryo() {
                if (kryoSoftReference != null) {
                        Kryo kryo = kryoSoftReference.get();
                        if (kryo != null) {
                                return kryo;
                        }
                }
                Kryo kryo = new Kryo();
                kryoSoftReference = new SoftReference<Kryo>(kryo);
                kryo.setRegistrationRequired(true);

                kryo.register(com.badlogic.gdx.utils.ObjectIntMap.class);
                kryo.register(com.badlogic.gdx.utils.IdentityMap.class);
                kryo.register(com.badlogic.gdx.utils.IdentityMap.Entry.class);
                kryo.register(com.badlogic.gdx.utils.IdentityMap.Entries.class);
                kryo.register(com.badlogic.gdx.utils.SnapshotArray.class);
                kryo.register(com.badlogic.gdx.utils.IntMap.class);
                kryo.register(Array.class);
                kryo.register(com.badlogic.gdx.utils.Array[].class);
                kryo.register(Array.ArrayIterable.class);
                kryo.register(Array.ArrayIterator.class);
                kryo.register(com.badlogic.gdx.math.Vector2.class);
                kryo.register(com.badlogic.gdx.utils.FloatArray.class);
                kryo.register(com.badlogic.gdx.utils.FloatArray[].class);
                kryo.register(Object[].class);
                kryo.register(int[].class);
                //kryo.register(int[][].class);
                kryo.register(byte[].class);
                kryo.register(byte[][].class);
                kryo.register(float[].class);
                kryo.register(java.util.HashMap.class);
                kryo.register(java.util.Random.class);
                kryo.register(java.util.concurrent.atomic.AtomicLong.class);

                kryo.register(Direction.class);
                kryo.register(Dungeon.class);
                kryo.register(FloorMap.class);
                kryo.register(asf.dungeon.model.DungeonRand.class);
                kryo.register(MasterJournal.class);
                kryo.register(ModelId.class);
                kryo.register(asf.dungeon.model.SfxId.class);
                kryo.register(asf.dungeon.model.FxId.class);
                kryo.register(asf.dungeon.model.SongId.class);
                kryo.register(Pair.class);
                kryo.register(Pair[].class);
                kryo.register(Pair[][].class);
                kryo.register(Pathfinder.class);
                kryo.register(Pathfinder.PathingPolicy.class);
                kryo.register(Tile.class);
                kryo.register(Symbol.class);
                kryo.register(asf.dungeon.model.Tile[].class);
                kryo.register(asf.dungeon.model.Tile[][].class);
                kryo.register(asf.dungeon.model.Sector.class);

                kryo.register(Attack.class);
                kryo.register(Damage.class);
                kryo.register(Experience.class);
                kryo.register(FogMapping.class);
                kryo.register(asf.dungeon.model.token.Fountain.class);
                kryo.register(asf.dungeon.model.token.Torch.class);
                kryo.register(asf.dungeon.model.token.SpikeTrap.class);
                kryo.register(asf.dungeon.model.token.Interactor.class);
                kryo.register(asf.dungeon.model.token.Stairs.class);
                kryo.register(asf.dungeon.model.token.Boulder.class);
                kryo.register(Inventory.class);
                kryo.register(CharacterInventory.class);
                kryo.register(CrateInventory.class);
                kryo.register(Journal.class);
                kryo.register(Loot.class);
                kryo.register(Move.class);
                kryo.register(StatusEffects.class);
                kryo.register(StatusEffect.class);
                kryo.register(Command.class);
                kryo.register(Token.class);
                kryo.register(asf.dungeon.model.token.Token[].class);
                kryo.register(TokenComponent.class);
                kryo.register(asf.dungeon.model.token.TokenComponent[].class);

                kryo.register(asf.dungeon.model.token.puzzle.CombinationDoorPuzzle.class);

                kryo.register(Quest.class);
                kryo.register(asf.dungeon.model.token.quest.PotionQuest.class);
                kryo.register(asf.dungeon.model.token.quest.FountainQuest.class);
                kryo.register(asf.dungeon.model.token.quest.TorchQuest.class);
                kryo.register(asf.dungeon.model.token.quest.SignPostQuest.class);

                kryo.register(FogMap.class);
                kryo.register(FogState.class);
                kryo.register(asf.dungeon.model.fogmap.FogState[].class);
                kryo.register(asf.dungeon.model.fogmap.FogState[][].class);

                kryo.register(Logic.class);
                kryo.register(LocalPlayerLogic.class);
                kryo.register(asf.dungeon.model.token.logic.FullAgroLogic.class);
                kryo.register(FsmLogic.class);
                kryo.register(asf.dungeon.model.token.logic.fsm.Monster.class);
                kryo.register(asf.dungeon.model.token.logic.fsm.QuestNPC.class);





                kryo.register(Item.class);
                kryo.register(Item[].class);
                kryo.register(EquipmentItem.class);
                kryo.register(EquipmentItem[].class);
                kryo.register(KeyItem.class);
                kryo.register(KeyItem.Type.class);
                kryo.register(asf.dungeon.model.item.QuickItem.class);
                kryo.register(asf.dungeon.model.item.QuickItem[].class);
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

                kryo.register(FloorMapGenerator.class);
                kryo.register(asf.dungeon.model.floorgen.FloorMapGenerator[].class);
                kryo.register(FloorMapGenMultiplexer.class);
                kryo.register(BalanceTestFloorGen.class);
                kryo.register(BinarySpaceGen.class);
                kryo.register(CellularAutomataGen.class);
                kryo.register(ConnectedRoomsGen.class);
                kryo.register(DirectionalCaveHallGen.class);
                kryo.register(MazeGen.class);
                kryo.register(PreBuiltFloorGen.class);
                kryo.register(RandomWalkGen.class);
                kryo.register(Room.class);
                kryo.register(asf.dungeon.model.floorgen.room.Doorway.class);
                kryo.register(asf.dungeon.model.floorgen.room.Doorway[].class);
                kryo.register(ZeldaGen.class);
                kryo.register(TestAssetsFloorGen.class);
                kryo.register(asf.dungeon.model.floorgen.prebuilt.TutorialFloorGen.class);


                return kryo;
        }
}
