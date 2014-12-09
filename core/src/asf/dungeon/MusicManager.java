package asf.dungeon;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

/**
 * A music manager that is more or less a virtual jukebox. Music is loaded and disposed one at a time
 * as needed to play the desired songs.
 * <p/>
 * use setPlayList() then playNextSong() to begin playing the playlist songs in random order.
 * <p/>
 * at any time playSong() can be called to override the playlist,
 * once that song is finished the jukebox will return to the playlist
 * <p/>
 * If playSong(song,true) is called, then that override song will be played continuously
 * and never return to the playlist until playSong() or playNextSong() is called.
 * <p/>
 * to pause the music (eg for a pause screen) use setPaused()
 * <p/>
 * to disable music (eg for user settings) use setMusicEnabled(). When music is disabled all music
 * will be unloaded from memory. this is useful because playSong() and setPlaylist() etc can still
 * be used when the music is disabled, so if the music is enabled again during the game the jukebox
 * knows what song to start playing.
 * <p/>
 * <p/>
 * Created by Daniel Strong on 12/6/2014.
 */
public class MusicManager implements Music.OnCompletionListener, Disposable {

        public interface SongInfo {
                public String getAssetLocation();

                public float getVolume();
        }

        private boolean musicEnabled = true;
        private boolean paused = false;
        private float masterVolume = 1f;
        private SongInfo[] playlist;

        private SongInfo currentSong;
        private Music currentMusic;
        private SongInfo nextSong;
        private boolean loopNextSong;



        public void update(float delta){

        }

        /**
         * set a list of songs that the jukebox should play at random once the current song
         * is finished playing. if no song is currently playing then you need to call playNextSong()
         * to start hearing music.
         * @param songs
         */
        public void setPlaylist(SongInfo... songs) {
                playlist = songs;
        }
/*
        public void update(float delta) {

                if(Gdx.input.isKeyJustPressed(Input.Keys.UP)){
                        playSong(Song.MainTheme);
                }else if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)){
                        playSong(Song.MainTheme1);
                }else if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT)){
                        playSong(Song.MainTheme2);
                }else if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)){
                        playSong(Song.MainTheme3);
                }else if(Gdx.input.isKeyJustPressed(Input.Keys.N)){
                        playNextSong();
                }else if(Gdx.input.isKeyJustPressed(Input.Keys.P)){
                        setPaused(!isPaused());
                }else if(Gdx.input.isKeyJustPressed(Input.Keys.E)){
                        setMusicEnabled(!isMusicEnabled());
                }
        }
*/
        @Override
        public void onCompletion(Music music) {
                if (music == currentMusic) {
                        playNextSong();
                }

        }

        /**
         * choses another song by random from the playlist and play it
         */
        public void playNextSong() {
                if (playlist == null) return;
                for (int tries = 0; tries < 10; ++tries) {
                        nextSong = playlist[MathUtils.random.nextInt(playlist.length)];
                        if (nextSong != currentSong) break;
                }

                playSong(nextSong);
        }

        /**
         * Stops playing the current song
         */
        public void stopSong(){
                playSong(null);
        }

        /**
         * plays the specified song once (does not need to be on the playlist),
         * after song is finished playing then the jukebox returns to picking songs
         * from the playlist
         *
         * @param song
         */
        public void playSong(SongInfo song) {
                playSong(song, false);
        }

        /**
         * plays the specified song once (does not need to be on the playlist)
         * if loop is true then the song will loop endlessly until a playSong() or playNextSong()
         * is called again.
         *
         * @param song
         * @param loop
         */
        public void playSong(SongInfo song, boolean loop) {
                if (!musicEnabled) {
                        currentSong = song;
                        nextSong = null;
                        loopNextSong = loop;
                        return;
                }

                nextSong = song;

                if (nextSong != currentSong) {
                        if (currentMusic != null) {
                                currentMusic.stop();
                                currentMusic.dispose();
                                currentMusic = null;
                        }

                        if (nextSong != null) {
                                currentMusic = Gdx.audio.newMusic(Gdx.files.internal(nextSong.getAssetLocation()));
                                currentMusic.setOnCompletionListener(this);
                                currentMusic.setLooping(loop);
                                currentMusic.setVolume(nextSong.getVolume() * masterVolume);
                                if (!paused)
                                        currentMusic.play();
                        }

                } else if (currentSong != null) {
                        // playing the current song, start it over
                        currentMusic.stop();
                        currentMusic.setLooping(loop);
                        if (!paused)
                                currentMusic.play();
                }
                currentSong = nextSong;
                nextSong = null;
                loopNextSong = false;

        }

        public float getMasterVolume() {
                return masterVolume;
        }

        /**
         * all music that is played will be scaled by this value. To mute/disable music
         * setMusicEnable(false) should be used isntead as it will unload the music
         * from memory.
         * @param masterVolume how loud the music should be, [0,1]
         */
        public void setMasterVolume(float masterVolume) {
                if (currentMusic != null) {
                        currentMusic.setVolume(currentSong.getVolume() / this.masterVolume * masterVolume);
                }
                this.masterVolume = masterVolume;
        }

        public boolean isMusicEnabled() {
                return musicEnabled;
        }

        /**
         * enables and disables the user preference of having music. When music is disabled
         * songs will still be selected just not played. once enabled the currentSong will
         * begin playing
         *
         * @param musicEnabled
         */
        public void setMusicEnabled(boolean musicEnabled) {
                if (this.musicEnabled == musicEnabled) return;
                this.musicEnabled = musicEnabled;
                if (musicEnabled) {
                        nextSong = currentSong;
                        currentSong = null;
                        playSong(nextSong, loopNextSong);
                } else {
                        if (currentMusic != null) {
                                currentMusic.stop();
                                currentMusic.dispose();
                                currentMusic = null;
                        }

                        if (nextSong != null) {
                                currentSong = nextSong;
                                nextSong = null;
                        }

                }
        }

        public boolean isPaused() {
                return paused;
        }

        /**
         * pauses the music player, the current song will stay in memory
         * when unpaused it will resume playing the song from the point it paused.
         * <p/>
         * if a new song is played while the music player is paused, then the song will
         * be selected, but it will not start playing until setPaused(false) is called.
         *
         * @param paused
         */
        public void setPaused(boolean paused) {
                if (this.paused == paused) return;
                this.paused = paused;
                if (!this.musicEnabled) return;

                if (this.paused) {
                        if (currentMusic != null) {
                                currentMusic.pause();
                        }
                } else {
                        if (currentMusic != null) {
                                currentMusic.play();
                        } else if (currentSong != null) {
                                // song was changed during pause, need to start it from the beginning
                                nextSong = currentSong;
                                currentSong = null;
                                playSong(nextSong, loopNextSong);
                        }

                }

        }

        @Override
        public void dispose() {
                if (currentMusic != null){
                        currentMusic.stop();
                        currentMusic.dispose();
                        currentMusic = null;
                }
        }


}
