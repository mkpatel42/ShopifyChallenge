package net.falutin.meet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Holds the grid letters
 */
public class CellGrid implements Char2d {



    public static final int DIR_HORIZONTAL = 0;
    public static final int DIR_VERTICAL = 1;
    public static final int DIR_DIAG_UP = 2;
    public static final int DIR_DIAG_DOWN = 3;
    public static final int DIR_R_HORIZONTAL = 4;
    public static final int DIR_R_VERTICAL = 5;
    public static final int DIR_R_DIAG_UP = 6;
    public static final int DIR_R_DIAG_DOWN = 7;

    public static final int[] EASY_DIRECTIONS = {DIR_HORIZONTAL, DIR_VERTICAL};
    public static final int[] NORMAL_DIRECTIONS = {DIR_HORIZONTAL, DIR_VERTICAL, DIR_DIAG_UP, DIR_DIAG_DOWN};
    public static final int[] HARD_DIRECTIONS = {DIR_HORIZONTAL, DIR_VERTICAL, DIR_DIAG_UP, DIR_DIAG_DOWN, DIR_R_HORIZONTAL, DIR_R_VERTICAL, DIR_R_DIAG_UP, DIR_R_DIAG_DOWN};

    public static final char[] ALL_CHARS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    private final int width, height;
    private final char cells[];
    private final char permutedLetters[];
    private final Random random;
    private static final Character[] LETTERS = new Character[] {
            'A','A','A','A','A','A','A','A','A','B','B','C','C','D','D','D','D',
            'E','E','E','E','E','E','E','E','E','E','E','F','F','G','G','G','H','H',
            'I','I','I','I','I','I','I','I','J','K','L','L','L','L','M','M',
            'N','N','N','N','N','N','O','O','O','O','O','O','O','O','P','P','Q',
            'R','R','R','R','R','R','S','S','S','S','T','T','T','T','T','T','U','U','U','U',
            'V','V','W','W','X','Y','Y','Z','A','A','A','A','A','A','A','A','A','B','B','C','C','D','D','D','D',
            'E','E','E','E','E','E','E','E','E','E','E','F','F','G','G','G','H','H',
            'I','I','I','I','I','I','I','I','J','K','L','L','L','L','M','M',
            'N','N','N','N','N','N','O','O','O','O','O','O','O','O','P','P','Q',
            'R','R','R','R','R','R','S','S','S','S','T','T','T','T','T','T','U','U','U','U',
            'V','V','W','W','X','Y','Y','Z','A','A','A','A','A','A','A','A','A','B','B','C','C','D','D','D','D',
            'E','E','E','E','E','E','E','E','E','E','E','F','F','G','G','G','H','H',
            'I','I','I','I','I','I','I','I','J','K','L','L','L','L','M','M',
            'N','N','N','N','N','N','O','O','O','O','O','O','O','O','P','P','Q',
            'R','R','R','R','R','R','S','S','S','S','T','T','T','T','T','T','U','U','U','U',
            'V','V','W','W','X','Y','Y','Z'};

    public CellGrid (int width, int height) {
        this.width = width;
        this.height = height;
        cells = new char[width * height];
        random = new Random();
        permutedLetters = permuteLetters();
    }

    private char[] permuteLetters() {
        assert(LETTERS.length == 6 * 16);
        char[] pLetters = new char[LETTERS.length];
        LinkedList<Character> letterBag = new LinkedList<>();
        letterBag.addAll(Arrays.asList(LETTERS));
        // permute the letters
        for (int i = 0; i < LETTERS.length; i++) {
            pLetters[i] = letterBag.remove(random.nextInt(letterBag.size()));
        }
        return pLetters;
    }

    public void randomize(InputStream inputStream) {
//        for (int i = cells.length - 1; i >= 0; i--) {
//            cells[i] = getRandomChar(i);
//        }
        List<String> words = new ArrayList<>();
        try {
           words = readWords(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Grid grid = new Grid(10,10);
        for (int i = 0; i < 6; i++) {
            placeWord(words.get(i),grid);
        }
        writeRandomChars(grid);
        int k =0;
        for (int i = 0; i < 10 ; i++) {
            for (int j = 0; j < 10; j++) {
                cells[k] = grid.getCharAt(new GridCoordinate(i,j));
                k++;
            }
            System.out.print("\n");
        }
    }

    private List<String> readWords(InputStream inputStream) throws IOException {

        List<String> wordData = new ArrayList<>();
        byte[] buffer = new byte[0];
        try {
            buffer = new byte[inputStream.available()];
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (inputStream.read(buffer) != -1);
        String jsontext = new String(buffer);
        String[] words = jsontext.split("\n");
        for (int i = 0; i < words.length; i++) {
            if(words[i].length()>2 && words[i].length()<11)
            wordData.add(words[i]);
        }



        return wordData;
    }

    protected void placeWord(String word,Grid grid)
    {

        boolean isValidPlacement = false;  // Is the word able to be placed at this direction & starting position?
        int direction = 0;
        GridCoordinate startingPosition = new GridCoordinate(0, 0);
        while(!isValidPlacement)
        {
            direction = new Random().nextInt(HARD_DIRECTIONS.length - 1);
            startingPosition = generateRandomCoordinate(grid);
            isValidPlacement = GridUtils.willWordPrint(startingPosition, direction, word, grid);
        }
        Grid result = GridUtils.printWord(startingPosition, direction, word, grid);
        for (int i = 0; i < result.getXSize() ; i++) {
            for (int j = 0; j < result.getYSize(); j++) {
                System.out.print(result.getCharAt(new GridCoordinate(i,j))+ " ");
            }

            System.out.print("\n");

        }

    }

    /**
     * Generates a GridCoordinate at random, within the grid's constraints.
     * @return
     */
    protected GridCoordinate generateRandomCoordinate(Grid grid)
    {
        int x = new Random().nextInt(grid.getXSize() - 1);
        int y = new Random().nextInt(grid.getYSize() - 1);
        return new GridCoordinate(x, y);
    }

    private char getRandomChar(int cellIndex) {
        // return permutedLetters[random.nextInt(permutedLetters.length)];
        // choose from a different bag for each cell
        int ncells = 100;
        return permutedLetters[ncells * random.nextInt(LETTERS.length / ncells) + cellIndex];
    }

    public char get(int idx) {
        return cells[idx];
    }

    @Override
    public char get(int row, int col) {
        return cells[width * row + col];
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    public String toString () {
        return new String(cells);
    }

    public void setCells (String cellString) {
        System.arraycopy(cellString.toCharArray(), 0, cells, 0, width * height);
    }

    /**
     * Write random "noise" characters to any grid coordinates which are blank.
     * The characters written depend on the difficulty of the puzzle.
     * @param grid
     * @return
     */
    protected Grid writeRandomChars(Grid grid)
    {
        char [] charPool = ALL_CHARS;
        grid = placeCharsFromCharPool(grid, charPool);
        return grid;
    }

    /**
     * Iterate over the grid, placing chars from the char pool into empty
     * spaces at random.
     * @param grid
     * @param charPool
     * @return
     */
    private Grid placeCharsFromCharPool(Grid grid, char[] charPool)
    {
        int char_pool_index = new Random().nextInt(charPool.length - 1);
        char c = charPool[char_pool_index];
        for(int i = 0; i < grid.getYSize(); i++)
        {
            for(int j = 0; j < grid.getXSize(); j++)
            {
                GridCoordinate gc = new GridCoordinate(j,i);
                if(grid.getCharAt(gc) == Grid.BLANK_CHAR)
                {
                    grid.setCharAt(gc, c);
                    char_pool_index = new Random().nextInt(charPool.length - 1);
                    c = charPool[char_pool_index];
                }
            }
        }
        return grid;
    }

}
