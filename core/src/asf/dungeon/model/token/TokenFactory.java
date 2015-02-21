package asf.dungeon.model.token;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.item.ArmorItem;
import asf.dungeon.model.item.BookItem;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.KeyItem;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.item.ScrollItem;
import asf.dungeon.model.item.WeaponItem;
import asf.dungeon.model.token.logic.Logic;
import asf.dungeon.model.token.quest.Quest;

/**
 * Created by Daniel Strong on 2/21/2015.
 */
public class TokenFactory {

        public static Token playerCharacterToken(Dungeon dungeon, String name, ModelId modelId, Logic logic){
                Token t = new Token(dungeon,  name, modelId);
                t.add(logic);
                t.add(new Command(t));
                t.add(new Interactor(t));
                t.add(new FogMapping(t));
                t.add(new Experience(t));
                t.add(new Journal(t));
                t.add(new CharacterInventory(t));
                t.add(new StatusEffects(t));
                t.add(new Damage(t));
                t.add(new Attack(t));
                t.add(new Move(t));

                t.experience.level = 1;
                t.experience.vitality = 20;
                t.experience.strength = 6;
                t.experience.agility = 3;
                t.experience.intelligence = 1;
                t.experience.luck = 1;

                t.damage.setDeathDuration(3f);
                t.damage.setDeathRemovalDuration(Float.NaN);
                t.experience.recalcStats();
                t.logic.setToken(t);


                t.inventory.setNumQuickSlots(3);

                ScrollItem potion = new ScrollItem(dungeon, ScrollItem.Type.Teleportation, 1);
                //potion.identifyItem(token);
                t.inventory.add(potion);
                //token.inventory.equip(potion);

                BookItem book = new BookItem(dungeon, BookItem.Type.Identify);
                t.inventory.add(book);
                book.identifyItem(t);

                book = new BookItem(dungeon, BookItem.Type.Identify);
                t.inventory.add(book);
                book.identifyItem(t);

                book = new BookItem(dungeon, BookItem.Type.RemoveCurse);
                t.inventory.add(book);
                //book.identifyItem(t);

                book = new BookItem(dungeon, BookItem.Type.RemoveCurse);
                t.inventory.add(book);
                //book.identifyItem(t);


                PotionItem health = new PotionItem(dungeon, PotionItem.Type.Speed, 4);
                health.identifyItem(t);
                t.inventory.add(health);

                PotionItem paralyze = new PotionItem(dungeon, PotionItem.Type.Health, 4);
                paralyze.identifyItem(t);
                t.inventory.add(paralyze);

                ArmorItem armor = new ArmorItem(dungeon, 1);
                armor.identifyItem(t);
                t.inventory.add(armor);
                t.inventory.equip(armor);

                t.inventory.add(new KeyItem(dungeon, KeyItem.Type.Silver));
                t.inventory.add(new KeyItem(dungeon,KeyItem.Type.Gold));
                t.inventory.add(new KeyItem(dungeon, KeyItem.Type.Red));

                if (modelId == ModelId.Knight) {
                        WeaponItem sword = new WeaponItem(dungeon, 3,2,1);

                        //sword.setAttackDuration(1);
                        t.inventory.add(sword);
                        t.inventory.equip(sword);
                        t.get(Journal.class).learn(sword);

                        WeaponItem bow = new WeaponItem(dungeon,  2,2,1, true,3,1);
                        t.inventory.add(bow);
                        t.get(Journal.class).learn(bow);
                        //t.inventory.equip(bow);

                        WeaponItem staff = new WeaponItem(dungeon, 3,2,1,false,3,1);
                        staff.identifyItem(t);
                        t.inventory.add(staff);
                } else if (modelId == ModelId.Archer) {
                        WeaponItem bow = new WeaponItem(dungeon,  2,2,1, true,3,1);
                        //bow.setCursed(true);
                        t.inventory.add(bow);
                        t.inventory.equip(bow);

                        WeaponItem sword = new WeaponItem(dungeon, 3,2,1);
                        sword.cursed = true;
                        t.inventory.add(sword);
                        t.get(Journal.class).learn(sword);

                        WeaponItem staff = new WeaponItem(dungeon, 3,2,1,false,3,1);
                        staff.identifyItem(t);
                        t.inventory.add(staff);
                } else if (modelId== ModelId.Mage) {
                        WeaponItem staff = new WeaponItem(dungeon, 3,2,1,false,3,1);
                        staff.identifyItem(t);

                        t.inventory.add(staff);
                        t.inventory.equip(staff);

                        WeaponItem sword = new WeaponItem(dungeon, 3,2,1);
                        t.inventory.add(sword);
                        sword.cursed = true;
                        sword.identifyItem(t);

                        WeaponItem bow = new WeaponItem(dungeon,  2,2,1, true,3,1);
                        t.inventory.add(bow);
                        //bow.identifyItem(t);

                } else if (modelId == ModelId.Priest) {
                        WeaponItem sword = new WeaponItem(dungeon, 3,2,1);
                        //sword.setAttackDuration(1);
                        t.inventory.add(sword);
                        t.inventory.equip(sword);
                        t.get(Journal.class).learn(sword);
                }



                return t;
        }

        public static Token characterToken(Dungeon dungeon, String name, ModelId modelId, Logic logic, Experience experience){
                Token t = new Token(dungeon, name, modelId);
                t.add(logic);
                t.add(new Command(t));
                //t.add(new FogMapping(t));
                //t.add(new Journal());
                t.add(experience);
                t.add(new CharacterInventory(t));
                t.add(new StatusEffects(t));
                t.add(new Damage(t));
                t.add(new Attack(t));
                t.add(new Move(t));

                t.move.setPicksUpItems(false);
                t.damage.setDeathDuration(3f);
                t.damage.setDeathRemovalDuration(10f);
                t.experience.setToken(t);
                t.experience.recalcStats();
                if(t.logic!=null)t.logic.setToken(t);

                if(modelId == ModelId.Archer){
                        WeaponItem weapon = new WeaponItem(dungeon,  2,2,1, true,3,1);
                        t.inventory.add(weapon);
                        t.inventory.equip(weapon);
                }
                return t;
        }

        public static Token trapCharacterToken(Dungeon dungeon, String name, ModelId modelId, Logic logic, Experience experience){
                Token t = new Token(dungeon, name, modelId);
                t.add(new MonsterTrap(t));
                t.add(logic);
                t.add(new Command(t));
                //t.add(new FogMapping(t));
                //t.add(new Journal());
                t.add(experience);
                t.add(new CharacterInventory(t));
                t.add(new StatusEffects(t));
                t.add(new Damage(t));
                t.add(new Attack(t));
                t.add(new Move(t));

                t.move.setPicksUpItems(false);
                t.damage.setDeathDuration(3f);
                t.damage.setDeathRemovalDuration(10f);
                t.experience.recalcStats();
                if(t.logic!=null)t.logic.setToken(t);
                return t;
        }

        public static Token questCharacterToken(Dungeon dungeon, String name, ModelId modelId, Logic logic, Quest quest){
                if(quest == null) throw new IllegalArgumentException("quest can not be null");
                Token t = new Token(dungeon, name, modelId);
                t.add(logic);
                t.add(new Command(t));
                t.add(quest);
                t.add(new CharacterInventory(t));
                t.add(new StatusEffects(t));
                t.add(new Damage(t));
                t.add(new Move(t));
                t.move.setPicksUpItems(false);
                t.damage.setMaxHealth(4);
                t.damage.setHealth(2);
                t.damage.setAttackable(false);
                t.damage.setDeathRemovalDuration(Float.NaN);
                if(t.logic != null) t.logic.setToken(t);
                return t;
        }

        public static Token crate(Dungeon dungeon, ModelId modelId, Item item){
                Token t = new Token(dungeon, modelId.name(), modelId);
                t.add(new CrateInventory(t, item));
                t.add(new Damage(t));
                t.damage.setMaxHealth(1);
                t.damage.setDeathDuration(1.75f);
                t.damage.setDeathRemovalDuration(0f);
                return t;
        }

        public static Token loot(Dungeon dungeon, Item item){
                Token t = new Token(dungeon, item.getAbbrName(), item.getModelId());
                t.add(new Loot(t, item));
                return t;
        }






}
