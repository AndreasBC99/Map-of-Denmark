package dk.itu.datastructure;
import java.util.ArrayList;
import java.util.List;


/**
 * The Ternary Search Tree (TST) implementation uses only nodes and references to create the tree structure,
 * and does not rely on any collections such as arrays or lists to store the characters or nodes.
 * The nodes themselves are stored in memory through object references,
 * where each node object contains a character,
 * a boolean value to indicate the end of a word,
 * and references to three child nodes: left, middle, and right.
 */

public class TernarySearchTree {

    private AdNode root;

    public TernarySearchTree() {
        root = null;
    }


    /**
     * The insert(String word) method inserts a string into the TST.
     * It uses a recursive approach to traverse the tree and insert the characters of the string.
     * The time complexity of this method is O(m), where m is the length of the string being inserted.
     */
    public void insert(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }

        root = insertHelper(root, word.toCharArray(), 0);
    }
    /**
     * The insertHelper(AdNode node, char[] word, int index) is a helper method that recursively inserts a character into the TST.
     * If the node is null, it creates a new node with the current character.
     * It then traverses the tree to find the correct position to insert the next character.
     * The time complexity of this method is O(m), where m is the length of the string being inserted.
     */

    private AdNode insertHelper(AdNode node, char[] word, int index) {
        if (node == null) {
            node = new AdNode(word[index]);
        }

        char c = word[index];

        if (c < node.character) {
            node.left = insertHelper(node.left, word, index);
        } else if (c > node.character) {
            node.right = insertHelper(node.right, word, index);
        } else {
            if (index < word.length - 1) {
                node.middle = insertHelper(node.middle, word, index + 1);
            } else {
                node.isEndOfWord = true;
            }
        }

        return node;
    }

    /**
     * The suggest(String prefix) method returns a list of suggestions for a given prefix.
     * It first finds the node corresponding to the prefix
     * and then collects all suggestions by traversing the middle subtree of that node.
     * The time complexity of this method depends on the length of the prefix and the number of suggestions found,
     * but in the worst case (no suggestions found), it is O(m), where m is the length of the prefix.
     */

    public List<String> suggest(String prefix) {
        List<String> suggestions = new ArrayList<>();

        if (prefix == null || prefix.isEmpty()) {
            return suggestions;
        }

        AdNode node = findNode(root, prefix.toCharArray(), 0);

        if (node != null) {
            if (node.isEndOfWord) {
                suggestions.add(prefix);
            }

            collectSuggestions(node.middle, new StringBuilder(prefix), suggestions);
        }

        return suggestions;
    }

    /**
     * The findNode(AdNode node, char[] word, int index) is a helper method that recursively finds the node corresponding to a given string.
     * It traverses the tree comparing each character of the string with the character in the current node.
     * The time complexity of this method is O(m), where m is the length of the string being searched.
     */

    private AdNode findNode(AdNode node, char[] word, int index) {
        if (node == null) {
            return null;
        }

        char c = word[index];

        if (c < node.character) {
            return findNode(node.left, word, index);
        } else if (c > node.character) {
            return findNode(node.right, word, index);
        } else {
            if (index == word.length - 1) {
                return node;
            } else {
                return findNode(node.middle, word, index + 1);
            }
        }
    }

    /**
     * This is a helper method that recursively collects suggestions from a given node.
     * The method first traverses the left subtree, then adds the node's character to the prefix
     * and checks if it is the end of a word.
     * The method then traverses the middle subtree with the updated prefix, and finally traverses the right subtree.
     * The time complexity of this method depends on the number of suggestions found,
     * but in the worst case (no suggestions found), it is O(n), where n is the number of nodes in the tree.
     */

    private void collectSuggestions(AdNode node, StringBuilder prefix, List<String> suggestions) {
        if (node == null) {
            return;
        }

        collectSuggestions(node.left, prefix, suggestions);

        if (node.isEndOfWord) {
            suggestions.add(prefix.toString() + node.character);
        }

        collectSuggestions(node.middle, prefix.append(node.character), suggestions);
        prefix.deleteCharAt(prefix.length() - 1);

        collectSuggestions(node.right, prefix, suggestions);
    }



}
