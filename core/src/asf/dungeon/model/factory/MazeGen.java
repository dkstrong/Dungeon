package asf.dungeon.model.factory;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Tile;

import java.util.Random;

/**
 * Created by Danny on 11/4/2014.
 */
public class MazeGen implements FloorMapGenerator{

        private int width;
        private int height;

        public MazeGen(int width, int height) {
                this.width = width;
                this.height = height;
        }

        public void setSize(int width, int height){
                this.width = width;
                this.height = height;
        }

        public FloorMap generate(Dungeon dungeon, int floorIndex){
                Tile[][] tiles = MazeGen.generateTiles(width, height);
                // upper stairs is on bottom left
                if(floorIndex >0){
                        outerloop:
                        for(int x=0; x<tiles.length; x++){
                                for(int y=0; y<tiles[x].length; y++){
                                        if(!tiles[x][y].isBlockMovement() && !tiles[x][y].isBlockVision()){
                                                tiles[x][y] = Tile.makeStairs(floorIndex, floorIndex - 1);
                                                break outerloop;
                                        }
                                }
                        }
                }


                // lower stairs is on top right
                outerloop:
                for (int x = tiles.length - 1; x >= 0; x--) {
                        for (int y = tiles[x].length - 1; y >= 0; y--) {
                                if(!tiles[x][y].isBlockMovement() && !tiles[x][y].isBlockVision()){
                                        tiles[x][y] = Tile.makeStairs(floorIndex, floorIndex + 1);
                                        break outerloop;
                                }
                        }
                }

                FloorMap floorMap = new FloorMap(floorIndex,tiles);
                UtFloorGen.spawnCharacters(dungeon, floorMap);
                return floorMap;
        }

        private static boolean[][] blocks;
        private static boolean[][] visited;
        private static Random rand = new Random();

        public static Tile[][] generateTiles(int w, int h){

                boolean[][] generate = generate(w, h);
                Tile[][] tiles = new Tile[generate.length][generate[0].length];
                for (int i = 0; i < generate.length; i++) {
                        for (int i1 = 0; i1 < generate[i].length; i1++) {
                                if(generate[i][i1]){
                                        tiles[i][i1] = Tile.makeWall();
                                }else{
                                        tiles[i][i1] = Tile.makeFloor();
                                }
                        }
                }
                return tiles;
        }


        public static boolean[][] generate(int w, int h) {
                int bw = w * 2 + 1;
                int bh = h * 2 + 1;
                blocks = new boolean[bw][bh];
                visited = new boolean[w + 2][h + 2];
                for(int i = 0; i < blocks.length; i++) for(int j = 0; j < blocks[0].length; j++)
                        blocks[i][j] = true;
                for(int i = 0; i < visited.length; i++) for(int j = 0; j < visited[0].length; j++)
                        if(i == 0 || i == visited.length - 1 || j == 0 || j == visited[0].length - 1)
                                visited[i][j] = true;

                gen(0, 0);

                return blocks;
        }

        private static void gen(int xPos, int yPos) {
                int x = xPos * 2 + 1;
                int y = yPos * 2 + 1;
                blocks[x][y] = false;
                visited[xPos + 1][yPos + 1] = true;
                while(!visited[xPos][yPos + 1] || !visited[xPos + 2][yPos + 1] || !visited[xPos + 1][yPos + 2] || !visited[xPos + 1][yPos]) {
                        float num = rand.nextFloat();
                        if(num < 0.25F && !visited[xPos][yPos + 1]) {
                                blocks[x - 1][y] = false;
                                gen(xPos - 1, yPos);
                        }
                        else if(num < 0.5F && num >= 0.25F && !visited[xPos + 2][yPos + 1]) {
                                blocks[x + 1][y] = false;
                                gen(xPos + 1, yPos);
                        }
                        else if(num >= 0.5F && num < 0.75F && !visited[xPos + 1][yPos + 2]) {
                                blocks[x][y + 1] = false;
                                gen(xPos, yPos + 1);
                        }
                        else if(!visited[xPos + 1][yPos]) {
                                blocks[x][y - 1] = false;
                                gen(xPos, yPos - 1);
                        }
                }
        }
}
