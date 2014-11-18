package asf.dungeon.model.item;

import asf.dungeon.model.ModelId;
import asf.dungeon.model.token.Journal;
import asf.dungeon.model.token.Token;

/**
 * Created by Danny on 11/17/2014.
 */
public class EquipmentItem extends AbstractItem {

        public static enum Type{
                Weapon, Armor, Ring;
        }

        private Type type;
        private ModelId modelId;
        private String name;
        private String description;
        private String vagueName;
        private String vagueDescription;
        private int damage;
        private int armor;
        private int vitalityMod, strengthMod, agilityMod, luckMod;


        public EquipmentItem(Type type, ModelId modelId, String name, String description, String vagueName, String vagueDescription) {
                this.type = type;
                this.modelId = modelId;
                this.name = name;
                this.description = description;
                this.vagueName = vagueName;
                this.vagueDescription = vagueDescription;
        }

        public Type getType() {
                return type;
        }

        public int getDamageRating() {
                return damage;
        }


        public int getArmorRating() {
                return armor;
        }

        public int getVitalityMod() {
                return vitalityMod;
        }

        public int getStrengthMod() {
                return strengthMod;
        }

        public int getAgilityMod() {
                return agilityMod;
        }

        public int getLuckMod() {
                return luckMod;
        }

        @Override
        public ModelId getModelId() {
                return modelId;
        }

        @Override
        public String getName() {
                return name;
        }

        @Override
        public String getDescription() {
                return description;
        }

        @Override
        public String getVagueName() {
                return vagueName;
        }

        @Override
        public String getVagueDescription() {
                return vagueDescription;
        }

        @Override
        public boolean isIdentified(Token token) {Journal journal = token.get(Journal.class); return journal == null || journal.knows(this);}

        @Override
        public String toString() {
                if(type == Type.Weapon){
                        return String.format("%s (%s)", name, damage);
                }else if(type == Type.Armor){
                        return String.format("%s (%s)", name, armor);
                }else if(type == Type.Ring){
                        return String.format("%s (%s,%s)", name, damage, armor);
                }
                return String.format("%s (%s)", name, type);
        }

        public static EquipmentItem makeWeapon(String name, int damageRating){
                EquipmentItem equipmentItem = new EquipmentItem(Type.Weapon,ModelId.Potion,name, "A weapon", "Unidentified Weapon","A mysterious weapon, who knows what it will do once equipped?");
                equipmentItem.damage = damageRating;
                return equipmentItem;
        }

        public static EquipmentItem makeArmor(String name, int armorRating){
                EquipmentItem equipmentItem = new EquipmentItem(Type.Armor,ModelId.Potion,name, "Sturdy Armor", "Unidentified Armor","Mysterious armor, who knows what it will do once equipped?");
                equipmentItem.armor = armorRating;
                return equipmentItem;
        }

        public static EquipmentItem makeRing(String name){
                EquipmentItem equipmentItem = new EquipmentItem(Type.Ring,ModelId.Potion,name, "Shiny Ring", "Unidentified Ring","A Mysterious ring, who knows what it will do once equipped?");

                return equipmentItem;
        }
}
