package asf.dungeon.model.token;

/**
 * Created by Danny on 11/11/2014.
 */
public interface TokenComponent {

        /**
         * return true if this component consumes the update and does not let the lower comonents update
         * @param delta
         * @return
         */
        public boolean update(float delta);

}
