package asf.dungeon.model.item;

import asf.dungeon.model.token.Inventory;
import asf.dungeon.model.token.Token;

/**
* Created by Danny on 11/15/2014.
*/
public interface ConsumableItem extends Item{

        public void consume(Token token, Inventory.Character.UseItemOutcome out);



        public interface TargetsTokens extends ConsumableItem{
                public void consume(Token token, Token targetToken, Inventory.Character.UseItemOutcome out);

                /**
                 * if this token can use this item to target the specified targetToken (not himself)
                 *
                 * if false then regular consume() will be used instead and the item wil be used on the user
                 * @param token
                 * @param targetToken
                 * @return
                 */
                public boolean canConsume(Token token, Token targetToken);
        }

        public interface TargetsItems extends ConsumableItem{
                public void consume(Token token, Item targetItem, Inventory.Character.UseItemOutcome out);

                /**
                 * if the token can use this item to target the specified targetItem (not the item itself)
                 *
                 * if false then regular consume() will be used instead and the item will be used on the user
                 * @param token
                 * @param targetItem
                 * @return
                 */
                public boolean canConsume(Token token, Item targetItem);
        }
}
