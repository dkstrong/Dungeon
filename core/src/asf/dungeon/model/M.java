package asf.dungeon.model;

import asf.dungeon.model.item.ArmorItem;
import asf.dungeon.model.item.BookItem;
import asf.dungeon.model.item.KeyItem;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.item.RingItem;
import asf.dungeon.model.item.ScrollItem;
import asf.dungeon.model.item.WeaponItem;

/**
 * Created by Danny on 11/22/2014.
 */
public class M {
        public final transient String Cursed="Cursed";
        public final transient String CursedDesc = "This item is cursed.";

        private DungeonRand rand;

        public final String CursedEquippedDesc;


        public M(DungeonRand rand){
                this.rand = rand;

                CursedEquippedDesc = rand.choose(
                        "This item is cursed and you are powerless to remove it.",
                        "The curse on this equipment prevents you from removing it."
                );
        }

        public void generateNameDesc(KeyItem key){
                key.name =key.getType().name()+" Key";
                key.description = "This key can be used to open a locked "+ key.getType().name()+" door.";
                key.vagueName = key.name;
                key.vagueDescription = key.description;
        }

        public void generateNameDesc(PotionItem potion){
                potion.name = potion.getType().name()+" Potion";
                potion.description = "This is a "+potion.name+". Go ahead. Drink it.";
                potion.vagueName = potion.getColor().name()+" Potion";
                potion.vagueDescription = "A mysterious " + potion.getColor().name().toLowerCase() + " potion. The effects of drinking this are not known.";
        }

        public  void generateNameDesc(ScrollItem scroll){
                scroll.name = "Scroll of "+scroll.getType().name();
                scroll.description = "This is a "+scroll.name+". Use it well.";
                scroll.vagueName = "Unidentified Scroll";
                scroll.vagueDescription = "An unidentified scroll. Who knows what will happen when read out loud?";
        }

        public void generateNameDesc(BookItem book){
                book.name = "Tome of "+book.getType().name();
                book.description = "This is a "+book.name+". It can only be used once. Use it well.";
                book.vagueName = "Unidentified Tome";
                book.vagueDescription = "An unidentified Tome. Who knows what will happen once read out loud?";
        }

        public void generateNameDesc(WeaponItem weapon){
                if(weapon.isRanged()){
                        if(weapon.bow){
                                weapon.modelId = ModelId.Bow_01;
                                weapon.projectileFx = FxId.Arrow;
                                weapon.name = "Bow";
                                weapon.description ="A sturdy bow.";
                        }else{
                                weapon.modelId = ModelId.StaffLarge;
                                weapon.projectileFx = FxId.PlasmaBall;
                                weapon.name = "Staff";
                                weapon.description = "A magical staff.";
                        }
                }else{
                        weapon.modelId = ModelId.Sword_01;
                        weapon.name = "Sword";
                        weapon.description = "A basic sword.";
                }
                weapon.vagueName = "Unidentified Weapon";
                weapon.vagueDescription = "A mysterious weapon, who knows what it will do once equipped?";
        }

        public  void generateNameDesc(ArmorItem armor){
                armor.modelId = ModelId.SwordLarge;
                armor.name = "Armor";
                armor.description = "Some armor";
                armor.vagueName = "Unidentified Armor";
                armor.vagueDescription = "Mysterious armor, who knows what it will do once equipped?";
        }

        public void generateNameDesc(RingItem ring){
                ring.modelId = ModelId.SwordLarge;
                ring.name = "Ring";
                ring.description = "A ring";
                ring.vagueName = "Unidentified Ring";
                ring.vagueDescription = "A mysterious ring, who knows what it will do once equipped?";
        }

}
