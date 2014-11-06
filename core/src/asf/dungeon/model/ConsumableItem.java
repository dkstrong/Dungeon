package asf.dungeon.model;

/**
 * Created by Danny on 11/5/2014.
 */
public interface ConsumableItem extends Item {

        public void consume(CharacterToken token);
}
