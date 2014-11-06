package asf.dungeon.model;

/**
 * Created by Danny on 11/5/2014.
 */
public interface Item {

        public String getName();

        public String getDescription();

        public ModelId getModelId();

        /**
         * the name as it appears in this tokens "journal".
         * @param token
         * @return true name if this item is identified, obscure name if this item is not identified
         */
        public String getNameFromJournal(CharacterToken token);

        public String getDescriptionFromJournal(CharacterToken token);



}
