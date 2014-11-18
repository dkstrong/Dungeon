package asf.dungeon.model.item;

import asf.dungeon.model.ModelId;
import asf.dungeon.model.token.Token;

/**
 * Created by Danny on 11/5/2014.
 */
public interface Item  {

        public String getName();

        public String getDescription();

        public String getVagueName();

        public String getVagueDescription();

        public ModelId getModelId();

        /**
         * the name as it appears in this tokens "journal".
         * @param token
         * @return true name if this item is identified, obscure name if this item is not identified
         */
        public String getNameFromJournal(Token token);

        public String getDescriptionFromJournal(Token token);

        public boolean isIdentified(Token token);




}
