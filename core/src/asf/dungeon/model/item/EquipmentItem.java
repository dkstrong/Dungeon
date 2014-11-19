package asf.dungeon.model.item;

import asf.dungeon.model.ModelId;
import asf.dungeon.model.token.Journal;
import asf.dungeon.model.token.Token;

/**
 * Created by Danny on 11/17/2014.
 */
public class EquipmentItem implements Item {

        private ModelId modelId;
        private String name;
        private String description;
        private String vagueName;
        private String vagueDescription;

        private int vitalityMod, strengthMod, agilityMod, luckMod;

        private boolean cursed;


        public EquipmentItem(ModelId modelId, String name, String description, String vagueName, String vagueDescription) {
                this.modelId = modelId;
                this.name = name;
                this.description = description;
                this.vagueName = vagueName;
                this.vagueDescription = vagueDescription;
        }

        public void setCursed(boolean cursed) {
                this.cursed = cursed;
        }

        /**
         * cursed equipment can not be unequipped
         * @return
         */
        public boolean isCursed() {
                return cursed;
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
        public String getNameFromJournal(Token token) {
                if (isIdentified(token)) return getName();
                return getVagueName();
        }

        @Override
        public String getDescriptionFromJournal(Token token) {
                String cursedMessage;
                if(isCursed() && token.getInventory().isEquipped(this)){
                        cursedMessage = "\n\nThis item is cursed and you are powerless to remove it.";
                }else{
                        cursedMessage="";
                }
                if (isIdentified(token)) return getDescription()+cursedMessage;
                return getVagueDescription()+cursedMessage;
        }

        @Override
        public boolean isIdentified(Token token) {Journal journal = token.get(Journal.class); return journal == null || journal.knows(this);}
}
