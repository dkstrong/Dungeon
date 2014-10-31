package asf.dungeon.board.logic;

import asf.dungeon.board.CharacterToken;
import asf.dungeon.board.Direction;
import asf.dungeon.board.Dungeon;
import asf.dungeon.board.Token;
import asf.dungeon.board.pathfinder.Pair;
import asf.dungeon.view.DungeonWorld;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Created by danny on 10/26/14.
 */
public class LocalPlayerLogicProvider implements LogicProvider, InputProcessor {
        private DungeonWorld world;
        private final int id;
        private String name;
        private Dungeon dungeon;
        private CharacterToken token;

        public LocalPlayerLogicProvider(int id, String name) {
                this.id = id;
                this.name = name;
        }

        public void setWorld(DungeonWorld world){
                this.world = world;
        }

        public int getId() {
                return id;
        }

        public String getName() {
                return name;
        }

        @Override
        public void setToken(CharacterToken token) {
                this.token = token;
                this.dungeon = token.dungeon;
                token.setFogMappingEnabled(true);
        }

        @Override
        public void updateLogic(float delta) {


        }

        private final Vector3 temp = new Vector3();
        private final Pair tempMapCoords = new Pair();
        private boolean arrowKeysMove = false; // TODO: this setting should be stored on an app level object so it can be easily changed in the settings


        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if(world.isPaused())
                        return false;
                Ray ray = world.cam.getPickRay(screenX, screenY);
                if(arrowKeysMove){
                        Token targetToken = world.getToken(ray, token);
                        if(targetToken != null){
                                token.setMoveTokenTarget(targetToken);
                                world.selectionMark.mark(targetToken.getLocation());
                                return true;
                        }
                }else{
                        final float distance = -ray.origin.y / ray.direction.y;
                        temp.set(ray.direction).scl(distance).add(ray.origin);
                        world.getMapCoords(temp, tempMapCoords);
                        if(token.getFloorMap().getTile(tempMapCoords) != null){
                                token.setMoveTarget(tempMapCoords);
                                world.selectionMark.mark(tempMapCoords);
                                return true;
                        }
                }
                return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
                if(world.isPaused())
                        return false;
                Ray ray = world.cam.getPickRay(screenX, screenY);
                if(arrowKeysMove){
                        Token targetToken = world.getToken(ray, token);
                        if(targetToken != null){
                                token.setMoveTokenTarget(targetToken);
                                world.selectionMark.mark(targetToken.getLocation());
                                return true;
                        }
                }else{
                        final float distance = -ray.origin.y / ray.direction.y;
                        temp.set(ray.direction).scl(distance).add(ray.origin);
                        world.getMapCoords(temp, tempMapCoords);
                        if(token.getFloorMap().getTile(tempMapCoords) != null){
                                token.setMoveTarget(tempMapCoords);
                                world.selectionMark.mark(tempMapCoords);
                                return true;
                        }
                }
                return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                return false;
        }

        @Override
        public boolean keyDown(int keycode) {
                if(world.isPaused())
                        return false;
                if(keycode == Input.Keys.UP){
                        token.setMoveDir(Direction.North);
                        return true;
                }else if(keycode == Input.Keys.DOWN){
                        token.setMoveDir(Direction.South);
                        return true;
                }else if(keycode == Input.Keys.LEFT){
                        token.setMoveDir(Direction.West);
                        return true;
                }else if(keycode == Input.Keys.RIGHT){
                        token.setMoveDir(Direction.East);
                        return true;
                }
                return false;
        }

        @Override
        public boolean keyUp(int keycode) {
                if(world.isPaused())
                        return false;
                if(keycode == Input.Keys.UP && token.getMoveDir() == Direction.North){
                        token.setMoveDir(null);
                        return true;
                }else if(keycode == Input.Keys.DOWN && token.getMoveDir() == Direction.South){
                        token.setMoveDir(null);
                        return true;
                }else if(keycode == Input.Keys.LEFT && token.getMoveDir() == Direction.West){
                        token.setMoveDir(null);
                        return true;
                }else if(keycode == Input.Keys.RIGHT && token.getMoveDir() == Direction.East){
                        token.setMoveDir(null);
                        return true;
                }
                return false;
        }

        @Override
        public boolean keyTyped(char character) {
                return false;
        }



        @Override
        public boolean mouseMoved(int screenX, int screenY) {
                return false;
        }

        @Override
        public boolean scrolled(int amount) {
                return false;
        }
}
