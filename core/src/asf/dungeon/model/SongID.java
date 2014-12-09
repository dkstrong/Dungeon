package asf.dungeon.model;

import asf.dungeon.MusicManager;

/**
* Created by Daniel Strong on 12/8/2014.
*/
public enum SongId implements MusicManager.SongInfo {
        MainTheme("Music/walking_with_poseidon.mp3", 1f),
        Arabesque("Music/Arabesque.mp3", 1f),
        RitualNorm("Music/ritual-norm_0.ogg", 1f);

        private transient final String assetLocation;
        private transient final float volume;

        SongId(String assetLocation, float volume) {
                this.assetLocation = assetLocation;
                this.volume = volume;
        }

        @Override
        public String getAssetLocation() {
                return assetLocation;
        }

        public float getVolume() {
                return volume;
        }
}
