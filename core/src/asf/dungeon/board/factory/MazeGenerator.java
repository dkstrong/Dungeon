package asf.dungeon.board.factory;



import asf.dungeon.board.FloorTile;

import java.util.Random;

/**
 * Created by danny on 10/26/14.
 */
public class MazeGenerator {
        private static boolean[][] blocks;
        private static boolean[][] visited;
        private static Random rand = new Random();

        public static FloorTile[][] generateTiles(int w, int h){

                boolean[][] generate = generate(w, h);
                FloorTile[][] tiles = new FloorTile[generate.length][generate[0].length];
                for (int i = 0; i < generate.length; i++) {
                        for (int i1 = 0; i1 < generate[i].length; i1++) {
                                if(generate[i][i1]){
                                        // wall
                                        tiles[i][i1] = new FloorTile(true, true);
                                }else{
                                        // floor
                                        tiles[i][i1] = new FloorTile(false, false);
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
