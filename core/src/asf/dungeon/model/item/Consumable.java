package asf.dungeon.model.item;

import asf.dungeon.model.token.Token;

/**
* Created by Danny on 11/15/2014.
*/
public interface Consumable extends Item{
        public void consume(Token token);
}
