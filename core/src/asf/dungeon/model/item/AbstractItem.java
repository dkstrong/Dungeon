package asf.dungeon.model.item;

import asf.dungeon.model.token.Token;

/**
 * Created by Danny on 11/17/2014.
 */
public abstract class AbstractItem implements Item{



        @Override
        public String getNameFromJournal(Token token) {
                if (isIdentified(token)) return getName();
                return getVagueName();
        }

        @Override
        public String getDescriptionFromJournal(Token token) {
                if (isIdentified(token)) return getDescription();
                return getVagueDescription();
        }

}
