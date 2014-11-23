package asf.dungeon.model.item;

import asf.dungeon.model.M;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.token.Journal;
import asf.dungeon.model.token.Token;

/**
 * Created by Danny on 11/17/2014.
 */
public abstract class EquipmentItem implements Item {

        private ModelId modelId;
        private String name;
        private String description;
        private int complexity = 5;

        private int vitalityMod, strengthMod, agilityMod, intelligenceMod, luckMod;

        private boolean cursed;


        public EquipmentItem(ModelId modelId, String name, String description) {
                this.modelId = modelId;
                this.name = name;
                this.description = description;
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

        public void setComplexity(int complexity) {
                this.complexity = complexity;
        }

        /**
         *
         * @return how complex the item is / how long it takes to manually identify it.
         */
        public int getComplexity() {
                return complexity;
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

        public int getIntelligenceMod() { return intelligenceMod; }

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

        public void setName(String name) {
                this.name = name;
        }

        public void setDescription(String description) {
                this.description = description;
        }

        @Override
        public String getNameFromJournal(Token token) {
                boolean identified = isIdentified(token);
                String cursedMessage;
                if(isCursed() && (identified || token.getInventory().isEquipped(this))) cursedMessage = M.Cursed+" ";
                else cursedMessage="";

                if (identified) return cursedMessage+getName();
                return cursedMessage+getVagueName();
        }

        @Override
        public String getDescriptionFromJournal(Token token) {
                boolean identified = isIdentified(token);
                String cursedMessage;
                if(isCursed() && token.getInventory().isEquipped(this)) cursedMessage = "\n\n"+M.CursedEquippedDesc;
                else if(isCursed() && identified) cursedMessage = "\n\n"+M.CursedDesc;
                else cursedMessage="";

                if (isIdentified(token)) return getDescription()+cursedMessage;
                return getVagueDescription()+cursedMessage;
        }

        @Override
        public boolean isIdentified(Token token) {Journal journal = token.get(Journal.class); return journal == null || journal.knows(this);}
}
