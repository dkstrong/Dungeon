package asf.dungeon.model;

import asf.dungeon.model.floorgen.FloorMapGenerator;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.token.Attack;
import asf.dungeon.model.token.Command;
import asf.dungeon.model.token.Damage;
import asf.dungeon.model.token.Experience;
import asf.dungeon.model.token.FogMapping;
import asf.dungeon.model.token.Interactor;
import asf.dungeon.model.token.Inventory;
import asf.dungeon.model.token.Journal;
import asf.dungeon.model.token.Loot;
import asf.dungeon.model.token.Move;
import asf.dungeon.model.token.StatusEffects;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.logic.Logic;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by danny on 10/22/14.
 */
public class Dungeon {
        public final DungeonRand rand;
        private final MasterJournal masterJournal;
        private final FloorMapGenerator floorMapFactory;
        private final Map<Integer, FloorMap> floorMaps = new HashMap<Integer, FloorMap>(16);
        private Token localPlayerToken;
        private FloorMap currentFloorMap;
        private int nextTokenId = 0;

        private transient Listener listener;

        public Dungeon(DungeonRand rand, MasterJournal masterJournal, FloorMapGenerator floorMapFactory) {
                this.rand = rand;
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

        public FloorMap generateFloor(int floorIndex) {

                FloorMap floorMap = floorMaps.get(floorIndex);
                if (floorMap == null) {
                        //Gdx.app.log("Dungeon", "generateFloor: " + floorIndex);

                        floorMap = floorMapFactory.generate(this, floorIndex);

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

        /**
         * unlike the other newXXX methods, fm MAY be null, this allows for creating the local player and using his stats on generating the first floor
         *
         * be sure to manually call moveToken() after his stats are set to properly add him to the game and put him on a floor
         */
        public Token newPlayerCharacterToken(FloorMap fm, String name, ModelId modelId, Logic logic, Experience experience, int x, int y){
                Token t = new Token(this,  nextTokenId++, name, modelId);
                t.add(logic);
                t.add(new Command(t));
                t.add(new Interactor(t));
                t.add(new FogMapping(t));
                t.add(experience);
                t.add(new Journal(t));
                t.add(new Inventory.Character(t));
                t.add(new StatusEffects(t));
                t.add(new Damage(t));
                t.add(new Attack(t));
                t.add(new Move(t));

                t.getDamage().setDeathDuration(3f);
                t.getDamage().setDeathRemovalCountdown(10f);
                t.getExperience().setToken(t);
                t.getLogic().setToken(t);
                localPlayerToken = t;
                if(fm != null){
                        moveToken(t, fm, x, y, Direction.South);
                }
                return localPlayerToken;
        }

        public Token newCharacterToken(FloorMap fm, String name, ModelId modelId, Logic logic, Experience experience, int x, int y) {
                if(fm == null) throw new IllegalArgumentException("fm can not be null");
                Token t = new Token(this,  nextTokenId++, name, modelId);
                t.add(logic);
                t.add(new Command(t));
                //t.add(new FogMapping(t));
                //t.add(new Journal());
                t.add(experience);
                t.add(new Inventory.Character(t));
                t.add(new StatusEffects(t));
                t.add(new Damage(t));
                t.add(new Attack(t));
                t.add(new Move(t));

                t.getDamage().setDeathDuration(3f);
                t.getDamage().setDeathRemovalCountdown(10f);
                t.getExperience().setToken(t);
                t.getLogic().setToken(t);

                moveToken(t, fm, x,y,Direction.South);
                return t;
        }

        public Token newToken(Token token, FloorMap fm, int x, int y){
                if(fm == null) throw new IllegalArgumentException("fm can not be null");
                token.setId(nextTokenId++);
                moveToken(token, fm, x,y,Direction.South);
                return token;
        }

        public Token newCrateToken(FloorMap fm, String name, ModelId modelId, Item item, int x, int y) {
                if(fm == null) throw new IllegalArgumentException("fm can not be null");
                Token t = new Token(this,  nextTokenId++, name, modelId);
                t.add(new Inventory.Simple(t, item));
                t.add(new Damage(t));
                t.getDamage().setMaxHealth(1);
                t.getDamage().setDeathDuration(2.5f);
                t.getDamage().setDeathRemovalCountdown(.25f);
                moveToken(t, fm, x,y,Direction.South);
                return t;
        }

        public Token newLootToken(FloorMap fm, Item item, int x, int y) {
                if(fm == null) throw new IllegalArgumentException("fm can not be null");
                Token t = new Token(this,  nextTokenId++, item.getName(), item.getModelId());
                t.add(new Loot(t, item));

                moveToken(t, fm, x,y,Direction.South);

                return t;
        }

        public void removeToken(Token token) {
                FloorMap fm = token.getFloorMap();
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
                FloorMap oldFloorMap = token.getFloorMap();
                if(oldFloorMap == null){
                        token.teleport(newFloorMap ,x,y,direction);
                        newFloorMap.tokens.add(token);
                        if (listener != null && newFloorMap == currentFloorMap)
                                listener.onTokenAdded(token);


                        if(listener != null && token == localPlayerToken)
                                listener.onNewPlayerToken(localPlayerToken);


                        if(token == localPlayerToken)
                                setCurrentFloor(token.getFloorMap().index);

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
                                        setCurrentFloor(token.getFloorMap().index);
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

                FloorMap oldFloorMap = token.getFloorMap();
                if(oldFloorMap == null){
                        Pair stairLoc = newFloorMap.getLocationOfUpStairs();
                        token.teleport(newFloorMap ,stairLoc.x,stairLoc.y,Direction.South); // TODO: should be direction of stairs
                        newFloorMap.tokens.add(token);
                        if (listener != null && newFloorMap == currentFloorMap)
                                listener.onTokenAdded(token);

                        if(listener != null && token == localPlayerToken)
                                listener.onNewPlayerToken(localPlayerToken);

                        if(token == localPlayerToken)
                                setCurrentFloor(token.getFloorMap().index);

                }else{
                        boolean valid = oldFloorMap.tokens.removeValue(token, true);
                        if(valid){
                                boolean down = newFloorMap.index > oldFloorMap.index;
                                Pair stairLoc;
                                if (down) stairLoc = newFloorMap.getLocationOfUpStairs();
                                else stairLoc = newFloorMap.getLocationOfDownStairs();
                                token.teleport(newFloorMap ,stairLoc.x,stairLoc.y,Direction.South); // TODO: should be direction of stairs
                                newFloorMap.tokens.add(token);
                                if (listener != null){
                                        if (oldFloorMap == currentFloorMap) {
                                                listener.onTokenRemoved(token);
                                        } else if (newFloorMap == currentFloorMap) {
                                                listener.onTokenAdded(token);
                                        }
                                }
                                if(token == localPlayerToken){
                                        setCurrentFloor(token.getFloorMap().index);
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
