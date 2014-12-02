package asf.dungeon.model.item;

/**
 * Created by Danny on 11/18/2014.
 */
public interface StackableItem extends Item{

        public void stack(StackableItem other);

        public StackableItem unStack(int numCharges);

        public int getCharges();

        public boolean canStackWith(StackableItem other);


}
