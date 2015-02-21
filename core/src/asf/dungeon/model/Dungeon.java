package asf.dungeon.model;

import asf.dungeon.model.floorgen.FloorMapGenerator;
import asf.dungeon.model.token.Stairs;
import asf.dungeon.model.token.Token;
import com.badlogic.gdx.utils.IntMap;

/**
 * Created by danny on 10/22/14.
 */
public class Dungeon {
        public final DungeonRand rand;
        public final M m;
        private final MasterJournal masterJournal;
        private final FloorMapGenerator floorMapFactory;
        private final IntMap<FloorMap> floorMaps = new IntMap<FloorMap>(2);
        private Token localPlayerToken;
        private FloorMap currentFloorMap;
        private int nextTokenId = 0;

        private transient Listener listener;

        public Dungeon(DungeonRand rand, MasterJournal masterJournal, FloorMapGenerator floorMapFactory) {
                this.rand = rand;
                m =new M(rand);
                this.masterJournal = masterJournal;
                this.floorMapFactory = floorMapFactory;
        }


        public MasterJournal getMasterJournal() {
                return masterJournal;
        }

        public Token getLocalPlayerToken() {
                return localPlayerToken;
        }

        public void update(float delta) {
                currentFloorMap.update(this, delta);
        }

        public int numFloormaps(){
                return floorMaps.size;
        }

        public FloorMap generateFloor(int floorIndex) {

                FloorMap floorMap = floorMaps.get(floorIndex);
                if (floorMap == null) {
                        //Gdx.app.log("Dungeon", "generateFloor: " + floorIndex);

                        floorMap = floorMapFactory.generate(this, null, floorIndex);

                        floorMaps.put(floorIndex, floorMap);
                }
                return floorMap;
        }


        public void setCurrentFloor(int floorIndex) {
                FloorMap newFloor = generateFloor(floorIndex);
                if (newFloor == currentFloorMap) {
                        return;
                }
                //Gdx.app.log("Dungeon", "setCurrentFloor: " + newFloor.index);
                FloorMap oldFloorMap = currentFloorMap;
                currentFloorMap = newFloor;

                if (listener == null)
                        return;
                listener.onFloorMapChanged(currentFloorMap);

                if (oldFloorMap != null) {
                        for (int i = 0; i < oldFloorMap.tokens.size; i++) {
                                listener.onTokenRemoved(oldFloorMap.tokens.items[i]);
                        }
                }

                for (int i = 0; i < currentFloorMap.tokens.size; i++) {
                        listener.onTokenAdded(currentFloorMap.tokens.items[i]);
                }

        }

        public void setListener(Listener listener) {
                this.listener = listener;
                if (listener == null || currentFloorMap == null)
                        return;

                listener.onFloorMapChanged(currentFloorMap);

                for (int i = 0; i < currentFloorMap.tokens.size; i++) {
                        listener.onTokenAdded(currentFloorMap.tokens.items[i]);
                }

                listener.onNewPlayerToken(localPlayerToken);


        }

        public FloorMap getCurrentFloopMap() {
                return currentFloorMap;
        }

        public FloorMap getFloorMap(int floorIndex) {
                return floorMaps.get(floorIndex);
        }

        public Token addPlayerToken(Token token, FloorMap fm){
                if(fm == null) throw new IllegalArgumentException("fm can not be null");
                if(localPlayerToken!= null) throw new IllegalArgumentException("already have local player token");
                token.setId(nextTokenId++);
                localPlayerToken = token;
                moveToken(token, fm);
                return token;
        }
        public Token addPlayerToken(Token token, FloorMap fm, int x, int y, Direction direction){
                if(fm == null) throw new IllegalArgumentException("fm can not be null");
                if(localPlayerToken!= null) throw new IllegalArgumentException("already have local player token");
                token.setId(nextTokenId++);
                localPlayerToken = token;
                moveToken(token, fm,x,y,direction);
                return token;
        }

        public Token addToken(Token token, FloorMap fm, int x, int y){
                if(fm == null) throw new IllegalArgumentException("fm can not be null");
                token.setId(nextTokenId++);
                moveToken(token, fm, x,y,token.direction);
                return token;
        }

        public void removeToken(Token token) {
                FloorMap fm = token.floorMap;
                if(fm == null){
                        if(token == localPlayerToken)
                                localPlayerToken = null;
                        return;
                }
                boolean valid = fm.tokens.removeValue(token, true);
                if(valid){
                        if(fm == currentFloorMap && listener != null)
                                listener.onTokenRemoved(token);

                        if(token == localPlayerToken){
                                localPlayerToken = null;
                                if(listener != null)
                                        listener.onNewPlayerToken(null);
                        }
                }else{
                        throw new IllegalStateException("token was not on this floor");
                }

        }

        /**
         * moves token to the specified floor, coordinates, and direction
         * @param token
         * @param newFloorMap
         * @param x
         * @param y
         * @param direction
         */
        public void moveToken(Token token, FloorMap newFloorMap, int x, int y, Direction direction){
                FloorMap oldFloorMap = token.floorMap;
                if(oldFloorMap == null){
                        token.teleport(newFloorMap ,x,y,direction);
                        newFloorMap.tokens.add(token);
                        if (listener != null && newFloorMap == currentFloorMap)
                                listener.onTokenAdded(token);


                        if(listener != null && token == localPlayerToken)
                                listener.onNewPlayerToken(localPlayerToken);


                        if(token == localPlayerToken)
                                setCurrentFloor(token.floorMap.index);

                }else{
                        if(oldFloorMap == newFloorMap){
                                // if moving within the same floor, no point in needlessly removing and readding to the token list
                                token.teleport(oldFloorMap,x,y,direction);
                                return;
                        }

                        boolean valid = oldFloorMap.tokens.removeValue(token, true);
                        if(valid){
                                token.teleport(newFloorMap ,x,y,direction);
                                newFloorMap.tokens.add(token);
                                if (listener != null && oldFloorMap != newFloorMap){
                                        if (oldFloorMap == currentFloorMap) {
                                                listener.onTokenRemoved(token);
                                        } else if (newFloorMap == currentFloorMap) {
                                                listener.onTokenAdded(token);
                                        }
                                }
                                if(token == localPlayerToken){
                                        setCurrentFloor(token.floorMap.index);
                                }
                        }else{
                                throw new IllegalStateException("token was not on a valid floor");
                        }
                }
        }

        /**
         * moves token to this floor, uses the stairs location to determine coordinates
         * @param token
         * @param newFloorMap
         */
        public void moveToken(Token token, FloorMap newFloorMap) {

                FloorMap oldFloorMap = token.floorMap;
                if(oldFloorMap == null){
                        Stairs stairs = newFloorMap.getStairsUp();
                        token.teleport(newFloorMap ,stairs.getLocation().x,stairs.getLocation().y,stairs.token.direction);
                        newFloorMap.tokens.add(token);
                        if (listener != null && newFloorMap == currentFloorMap)
                                listener.onTokenAdded(token);

                        if(listener != null && token == localPlayerToken)
                                listener.onNewPlayerToken(localPlayerToken);

                        if(token == localPlayerToken)
                                setCurrentFloor(token.floorMap.index);

                }else{
                        boolean valid = oldFloorMap.tokens.removeValue(token, true);
                        if(valid){
                                boolean down = newFloorMap.index > oldFloorMap.index;
                                Stairs stairs = down ? newFloorMap.getStairsUp() : newFloorMap.getStairsDown();
                                token.teleport(newFloorMap ,stairs.getLocation().x,stairs.getLocation().y, down ? stairs.token.direction : stairs.token.direction.opposite());
                                newFloorMap.tokens.add(token);
                                if (listener != null){
                                        if (oldFloorMap == currentFloorMap) {
                                                listener.onTokenRemoved(token);
                                        } else if (newFloorMap == currentFloorMap) {
                                                listener.onTokenAdded(token);
                                        }
                                }
                                if(token == localPlayerToken){
                                        setCurrentFloor(token.floorMap.index);
                                }
                        }else{
                                throw new IllegalStateException("token was not on a valid floor");
                        }
                }

        }


        public interface Listener {

                public void onNewPlayerToken(Token playerToken);

                public void onFloorMapChanged(FloorMap newFloorMap);

                public void onTokenAdded(Token token);

                public void onTokenRemoved(Token token);

        }


}
