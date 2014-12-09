package asf.dungeon.model.item;

import asf.dungeon.model.token.CharacterInventory;
import asf.dungeon.model.token.Token;

/**
* Created by Danny on 11/15/2014.
*/
public interface ConsumableItem extends Item{

        public void consume(Token token, CharacterInventory.UseItemOutcome out);



        public interface TargetsTokens extends ConsumableItem{
                public void consume(Token token, Token targetToken, CharacterInventory.UseItemOutcome out);

                /**
                 * if this token can use this item to target the specified targetToken (not himself)
                 *
                 * if false then regular consume() will be used instead and the item wil be used on the user
                 * @param token
                 * @param targetToken
                 * @return
                 */
                public boolean canConsume(Token token, Token targetToken);
                /**
                 * returns true if this item is not meant to normally be targetted on to other items
                 *
                 * This is kind of a hack, some BookItems straight up do not target other items, but for consistency with PotionItems and BookItems
                 * I want to have all books by a "Type" of ScrollItem, which forces them to inherit this interface
                 * @return
                 */
                public boolean isPrimarilySelfConsume();
        }

        public interface TargetsItems extends ConsumableItem{
                public void consume(Token token, Item targetItem, CharacterInventory.UseItemOutcome out);

                /**
                 * if the token can use this item to target the specified targetItem (not the item itself)
                 *
                 * if false then regular consume() will be used instead and the item will be used on the user
                 * @param token
                 * @param targetItem
                 * @return
                 */
                public boolean canConsume(Token token, Item targetItem);

                /**
                 * returns true if this item is not meant to normally be targetted on to other items
                 *
                 * This is kind of a hack, some BookItems straight up do not target other items, but for consistency with PotionItems and ScrollItemss
                 * I want to have all books by a "Type" of BookItem, which forces them to inherit this interface
                 * @return
                 */
                public boolean isPrimarilySelfConsume();
        }
}
