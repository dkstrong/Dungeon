package asf.dungeon.model.factory;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Tile;
import com.badlogic.gdx.math.MathUtils;

/**
 *
 * floor is made by repeatidaly dividing the floor into 2 smaller parts
 *
 * http://www.roguebasin.com/index.php?title=Basic_BSP_Dungeon_generation
 *
 * Created by Danny on 11/4/2014.
 */
public class BinarySpaceGen implements FloorMapGenerator{


        private int minFloorWidth = 30;
        private int maxFloorWidth = 45;
        private int minFloorHeight = 30;
        private int maxFloorHeight = 45;


        private int numberOfSubdivisions = 4;
        private boolean makeDoors = true;


        @Override
        public FloorMap generate(Dungeon dungeon, int floorIndex) {


                Tile[][] tiles = generateTiles(floorIndex);
                UtFloorGen.printFloorTile(tiles);

                FloorMap floorMap = new FloorMap(floorIndex, tiles);

                UtFloorGen.spawnCharacters(dungeon, floorMap);
                UtFloorGen.spawnRandomCrates(dungeon, floorMap);

                return floorMap;
        }


        public Tile[][] generateTiles(int floorIndex){
                int floorWidth = MathUtils.random(minFloorWidth, maxFloorWidth);
                int floorHeight = MathUtils.random(minFloorHeight, maxFloorHeight);

                Tile[][] tiles = new Tile[floorWidth][floorHeight];

                for (int x = 0; x < tiles.length; x++){
                        for (int y = 0; y < tiles[0].length; y++){
                                //tiles[x][y] = Tile.makeWall();
                        }
                }


                RoomCell baseRoomCell = new RoomCell(0,0,floorWidth-1, floorHeight-1);

                for(int k=0;k< numberOfSubdivisions; k++){
                        subdivide(baseRoomCell);
                }

                fillRooms(tiles, baseRoomCell);
                fillTunnels(tiles, baseRoomCell.childCell1, baseRoomCell.childCell2);

                if (makeDoors)
                        fillRoomsWithDoors(tiles, baseRoomCell);

                //UtFloorGen.ensureEdgesAreWalls(tiles);
                UtFloorGen.placeUpStairs(tiles, floorIndex);
                UtFloorGen.placeDownStairs(tiles, floorIndex);
                return tiles;
        }

        private void subdivide(RoomCell baseCell){
                if(baseCell.isLeaf()){
                        baseCell.split();
                }else{
                        subdivide(baseCell.childCell1);
                        subdivide(baseCell.childCell2);
                }
        }


        private static class RoomCell {
                private RoomCell parentCell;
                private RoomCell childCell1;
                private RoomCell childCell2;
                private Room innerRoom;

                private int x1, y1, x2, y2;
                private boolean vertSplit;

                private RoomCell(int x1, int y1, int x2, int y2) {
                        this.x1 = x1;
                        this.y1 = y1;
                        this.x2 = x2;
                        this.y2 = y2;
                }

                private boolean isLeaf(){
                        return childCell1 == null && childCell2 == null;
                }

                private boolean hasLeafs(){
                        return childCell1.isLeaf() && childCell2.isLeaf();
                }

                private RoomCell getSisterCell(){
                        if(parentCell.childCell1 == this)
                                return parentCell.childCell2;
                        return parentCell.childCell1;
                }

                private RoomCell getEndLeaf(){
                        if(isLeaf())
                                return this;
                        return childCell1.getEndLeaf();
                }

                private void split(){
                        // look at how the parent was split to encourage its chldren to split in the other direction
                        // this should create layouts with less "squished" rooms that represent closets more than rooms
                        //vertSplit = MathUtils.randomBoolean(parentCell != null && parentCell.vertSplit ? .05f :.95f); //false; // MathUtils.randomBoolean()
                        vertSplit  = parentCell!= null && parentCell.vertSplit ? false : true;
                        float splitRatio = MathUtils.random(.35f, .65f);
                        if(vertSplit){
                                int newX2 = Math.round(MathUtils.lerp(x1,x2,splitRatio));
                                childCell1= new RoomCell(x1, y1, newX2, y2);
                                childCell1.parentCell =this;

                                childCell2 = new RoomCell(newX2, y1, x2, y2);
                                childCell2.parentCell = this;

                        }else{
                                int newY2 = Math.round(MathUtils.lerp(y1,y2,splitRatio));
                                childCell1= new RoomCell(x1, y1, x2, newY2);
                                childCell1.parentCell =this;

                                childCell2 = new RoomCell(x1, newY2, x2, y2);
                                childCell2.parentCell = this;

                        }

                }

                private Room getInnerRoom(){
                        if(innerRoom != null)
                                return innerRoom;

                        int innerX1= MathUtils.random(x1,Math.round(MathUtils.lerp(x1,x2, 0.2f)));
                        int innerX2 = MathUtils.random(Math.round(MathUtils.lerp(x1,x2, .75f)), x2);

                        int innerY1= MathUtils.random(y1,Math.round(MathUtils.lerp(y1,y2, 0.25f)));
                        int innerY2 = MathUtils.random(Math.round(MathUtils.lerp(y1,y2, .75f)), y2);

                        innerRoom = new Room(innerX1, innerY1, innerX2, innerY2);
                        return innerRoom;
                }
        }


        private void fillRooms(Tile[][] tiles,RoomCell roomCell){
                if(roomCell.isLeaf()){
                        Room.fillRoom(tiles, roomCell.getInnerRoom());
                }else{
                        fillRooms(tiles, roomCell.childCell1);
                        fillRooms(tiles, roomCell.childCell2);
                }

        }

        private void fillTunnels(Tile[][] tiles, RoomCell cell1, RoomCell cell2){

                Room.fillTunnel(tiles, cell1.getEndLeaf().getInnerRoom(), cell2.getEndLeaf().getInnerRoom(), false);
                if(cell1.childCell1 != null)
                        fillTunnels(tiles, cell1.childCell1, cell1.childCell2);
                if(cell2.childCell1 != null)
                        fillTunnels(tiles, cell2.childCell1, cell2.childCell2);


        }

        private void fillRoomsWithDoors(Tile[][] tiles,RoomCell roomCell){
                if(roomCell.isLeaf()){
                        Room.fillRoomWithDoors(tiles, roomCell.getInnerRoom());
                }else{
                        fillRoomsWithDoors(tiles, roomCell.childCell1);
                        fillRoomsWithDoors(tiles, roomCell.childCell2);
                }

        }







}
