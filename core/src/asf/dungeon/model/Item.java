package asf.dungeon.model;

import asf.dungeon.model.token.Token;

import java.io.Serializable;

/**
 * Created by Danny on 11/5/2014.
 */
public interface Item  {

        public String getName();

        public String getDescription();

        public ModelId getModelId();

        /**
         * the name as it appears in this tokens "journal".
         * @param token
         * @return true name if this item is identified, obscure name if this item is not identified
         */
        public String getNameFromJournal(Token token);

        public String getDescriptionFromJournal(Token token);


        public interface Consumable extends Item{
                public void consume(Token token);
        }

}
