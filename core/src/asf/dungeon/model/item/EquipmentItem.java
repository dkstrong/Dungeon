package asf.dungeon.model.item;

import asf.dungeon.model.M;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.token.Journal;
import asf.dungeon.model.token.Token;

/**
 * Created by Danny on 11/17/2014.
 */
public abstract class EquipmentItem implements Item {

        public ModelId modelId;
        public String name;
        public String description;
        public String vagueName, vagueDescription;
        /**
         *
         * @return how complex the item is / how long it takes to manually identify it.
         */
        public int complexity = 5;

        public int vitalityMod, strengthMod, agilityMod, intelligenceMod, luckMod;

        public int requiredStrength, requiredAgility, requiredIntelligence;

        /**
         * cursed equipment can not be unequipped
         * @return
         */
        public boolean cursed;

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
        public String getVagueDescription() {return vagueDescription; }

        @Override
        public String getNameFromJournal(Token token) {
                boolean identified = isIdentified(token);
                String cursedMessage;
                if(cursed && (identified || token.inventory.isEquipped(this))) cursedMessage = M.Cursed+" ";
                else cursedMessage="";

                if (identified) return cursedMessage+getName();
                return cursedMessage+getVagueName();
        }

        @Override
        public String getDescriptionFromJournal(Token token) {
                boolean identified = isIdentified(token);
                String cursedMessage;
                if(cursed && token.inventory.isEquipped(this)) cursedMessage = "\n\n"+M.CursedEquippedDesc;
                else if(cursed && identified) cursedMessage = "\n\n"+M.CursedDesc;
                else cursedMessage="";

                if (isIdentified(token)) return getDescription()+cursedMessage;
                return getVagueDescription()+cursedMessage;
        }

        @Override
        public void identifyItem(Token token){
                Journal journal = token.get(Journal.class);
                if(journal != null)
                        journal.learn(this);
        }

        @Override
        public boolean isIdentified(Token token) {
                Journal journal = token.get(Journal.class);
                return journal == null || journal.knows(this);
        }
}
