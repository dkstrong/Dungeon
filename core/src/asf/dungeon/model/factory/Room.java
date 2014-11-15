package asf.dungeon.model.factory;

import asf.dungeon.model.Tile;
import asf.dungeon.utility.UtMath;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by Danny on 11/4/2014.
 */
public class Room {

        int x1,y1,x2,y2;

        Room(int x1, int y1, int x2, int y2) {
                this.x1 = x1;
                this.y1 = y1;
                this.x2 = x2;
                this.y2 = y2;
        }



        void set(int x1, int y1, int x2, int y2) {
                this.x1 = x1;
                this.y1 = y1;
                this.x2 = x2;
                this.y2 = y2;
        }

        public int getWidth(){
                return x2-x1;
        }

        public int getHeight(){
                return y2-y1;
        }
        public int getCenterX(){
                return (x1+x2)/2;
        }

        int getCenterY(){
                return (y1+y2)/2;
        }

        boolean intersects(Room room){
                return (x1 <= room.x2 && x2 >= room.x1 && y1 <= room.y2 && y2 >= room.y1);
        }


        protected static void fillRoom(Tile[][] tiles, Room room){
                for(int x=room.x1; x<= room.x2; x++){
                        for(int y=room.y1; y<=room.y2; y++){
                                if(x == room.x1 || x== room.x2 || y==room.y1 || y==room.y2){
                                        tiles[x][y] = Tile.makeWall();
                                }else{
                                        tiles[x][y] = Tile.makeFloor();
                                }
                        }
                }

        }

        /**
         * to be called after fillRoom(), and usually also after fillTunnel()
         * goes over the room coordinates and places a door
         * where wall tiles should be that got replaced with a floor tile
         *
         * @param tiles
         * @param room
         */
        static void fillRoomWithDoors(Tile[][] tiles, Room room){
                // This is a lot of code for a seemingly simple thing. but it checks to ensure
                // that these scenarios do not happen
                // 1 - two doors placed next to eachother
                // 2- prevent the scenario of a tunnel going through a wall "long ways" causing the entire wall becoming a string of doors
                // 3 - prevent a door to be placed at the ends of double wide halls


                // top and bottom of room
                for(int y = room.y1; y<= room.y2; y+=room.getHeight()){
                        for (int x = room.x1; x <= room.x2; x++){
                                if(tiles[x][y] != null && tiles[x][y].isFloor()){
                                        if(UtFloorGen.countDoors(tiles, x, y) == 0 && UtFloorGen.isWall(tiles, x+1, y) && UtFloorGen.isWall(tiles, x-1, y)){
                                                tiles[x][y] = Tile.makeDoor();
                                        }
                                }
                        }
                }
                // left and right of room
                for(int x = room.x1; x<= room.x2; x+=room.getWidth()){
                        for (int y = room.y1; y <= room.y2; y++){
                                if(tiles[x][y] != null && tiles[x][y].isFloor()){
                                        if(UtFloorGen.countDoors(tiles, x, y) == 0 && UtFloorGen.isWall(tiles, x, y-1) && UtFloorGen.isWall(tiles, x, y+1)){
                                                tiles[x][y] = Tile.makeDoor();
                                        }


                                }
                        }
                }



        }

        /**
         *
         * @param tiles
         * @param room
         * @param prevRoom
         * @param randomLDirection if true the L shape direction will be randoml chosen, if false then the L shape will be chosen to try and make the smallest hallway
         */
        static void fillTunnel(Tile[][] tiles, Room room, Room prevRoom, boolean randomLDirection){
                int startX = room.getCenterX();
                int startY = room.getCenterY();
                int endX = prevRoom.getCenterX();
                int endY = prevRoom.getCenterY();

                if(startX == endX || startY == endY){
                        fillTunnel(tiles, startX, startY,endX, endY);
                }else{
                        // diagonal (convert in to a horizontal and vertical)
                        if(randomLDirection){
                                randomLDirection = MathUtils.randomBoolean();
                        }else{
                                float xRange = UtMath.range(room.getCenterX(), prevRoom.getCenterX());
                                float yRange = UtMath.range(room.getCenterY(), prevRoom.getCenterY());
                                randomLDirection = xRange > yRange;
                        }

                        if(randomLDirection){ // horizontal then vertical
                                fillTunnel(tiles, startX, startY,endX, startY);
                                fillTunnel(tiles, endX, startY,endX, endY);
                        }else{ // vertical then horizontal
                                fillTunnel(tiles, startX, startY, startX, endY);
                                fillTunnel(tiles, startX, endY,endX, endY);

                        }
                }

        }

        static void fillTunnel(Tile[][] tiles, int startX, int startY, int endX, int endY){
                if(startY==endY){
                        // horizontal
                        if(endX < startX){
                                int temp = startX;
                                startX = endX;
                                endX = temp;
                        }
                        int y = startY;
                        for(int x=startX; x<=endX; x++){
                                if(tiles[x][y+1] == null)
                                        tiles[x][y+1] = Tile.makeWall();
                                if(tiles[x][y-1] == null)
                                        tiles[x][y-1] = Tile.makeWall();
                                tiles[x][y] = Tile.makeFloor(); // make a floor for the hallway

                        }
                }else if(startX == endX){
                        // vertical
                        if(endY < startY){
                                int temp = startY;
                                startY = endY;
                                endY = temp;
                        }
                        int x = startX;
                        for(int y=startY; y<=endY; y++){
                                if(tiles[x+1][y] == null)
                                        tiles[x+1][y] = Tile.makeWall();
                                if(tiles[x-1][y] == null)
                                        tiles[x-1][y] = Tile.makeWall();
                                tiles[x][y] = Tile.makeFloor(); // make a floor for the hallway
                        }
                }else{
                        throw new IllegalArgumentException("must provide horizontal or vertical only coordinates");
                }

                if(tiles[startX+1][startY+1] == null)tiles[startX+1][startY+1] = Tile.makeWall();
                if(tiles[startX-1][startY+1] == null)tiles[startX-1][startY+1] = Tile.makeWall();
                if(tiles[startX+1][startY-1] == null)tiles[startX+1][startY-1] = Tile.makeWall();
                if(tiles[startX-1][startY-1] == null)tiles[startX-1][startY-1] = Tile.makeWall();
                if(tiles[endX+1][endY+1] == null)tiles[endX+1][endY+1] = Tile.makeWall();
                if(tiles[endX-1][endY+1] == null)tiles[endX-1][endY+1] = Tile.makeWall();
                if(tiles[endX+1][endY-1] == null)tiles[endX+1][endY-1] = Tile.makeWall();
                if(tiles[endX-1][endY-1] == null)tiles[endX-1][endY-1] = Tile.makeWall();
        }
}
