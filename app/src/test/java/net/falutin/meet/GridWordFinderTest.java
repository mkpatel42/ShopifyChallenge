package net.falutin.meet;

import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static junit.framework.Assert.*;

/**
 * Tests for the GridWordFinder
 */
public class GridWordFinderTest {

    @Test
    public void testFindWords () throws IOException {
        LetterTree tree = LetterTreeTest.readLetterTree(false);
        TestGrid grid = new TestGrid("ABCDEFGHIJKLMNOP");
        GridWordFinder finder = new GridWordFinder(tree);
        Set<String> words = finder.findWords(grid);
        for (String word : new String[] { "fie", "fin", "fink", "fino",
                "glop", "ink", "knife", "lop", "mink", "nim",
                "plonk", "pol"}) {
            assertTrue (word + " not found", words.contains(word));
        }
        assertEquals (12, words.size());
        finder = new GridWordFinder(tree, 4);
        words = finder.findWords(grid);
        assertEquals (6, words.size());
        for (String word : new String[] { "fink", "fino", "glop", "knife", "mink", "plonk"}) {
            assertTrue (words.contains(word));
        }
    }

    @Test
    public void testFindWordsForAppStore () throws IOException {
        LetterTree tree = LetterTreeTest.readLetterTree(false);
        TestGrid grid = new TestGrid("MRABSYLHDRNEMEDE");
        GridWordFinder finder = new GridWordFinder(tree);
        Set<String> words = finder.findWords(grid);
        assertEquals (95, words.size());
        System.out.println(words);
        int score = 0;
        for (String word : words) {
            score += WordSearch.fibonacci(word.length()-2);
        }
        assertEquals(240, score);
    }

    /**
     * In live testing, WordSearch reported that there were 11 words, but actually found 14
     * because Q is not being treated as QU
     * @throws IOException
     */
    @Test
    public void testFindWordsBug() throws IOException {
        LetterTree tree = LetterTreeTest.readLetterTree(false);
        TestGrid grid = new TestGrid("AQOWAIIMGUAIILOA");
        GridWordFinder finder = new GridWordFinder(tree);
        Set<String> words = finder.findWords(grid);
        assertEquals (14, words.size());
        System.out.println(words);
    }

    class TestGrid implements Char2d {
        char[] grid = new char[16];

        public TestGrid(String s) {
            grid = s.substring(0, 16).toCharArray();
        }

        @Override
        public int width() {
            return 4;
        }

        @Override
        public int height() {
            return 4;
        }

        @Override
        public char get(int row, int col) {
            return grid[row * 4 + col];
        }
    }
}
