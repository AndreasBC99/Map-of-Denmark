package dk.itu.datastructure;


public class AdNode {
    char character;
    boolean isEndOfWord;
    AdNode left, middle, right;

    public AdNode(char character) {
        this.character = character;
        this.isEndOfWord = false;
        this.left = null;
        this.middle = null;
        this.right = null;
    }
}
