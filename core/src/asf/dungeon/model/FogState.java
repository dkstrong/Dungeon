package asf.dungeon.model;

/**
 * Created by danny on 10/27/14.
 */
public enum FogState {
        Visible(1), Visited(.55f), Dark(0f);

        @Deprecated
        public final float alpha;

        FogState(float alpha) {
                this.alpha = alpha;
        }
}
