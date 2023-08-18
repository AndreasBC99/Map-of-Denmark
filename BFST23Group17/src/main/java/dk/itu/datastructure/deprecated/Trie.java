package dk.itu.datastructure.deprecated;

import java.util.ArrayList;
import java.util.List;

/**********************************************************************************************************************
Multi Way Trie (MWT) algorithm based on a depth-first search.

Methods:
 - insertAddress(String address)
    When the program is started the method takes all the addresses as strings and convert every character into chars
    that maps to the next character of the string.
    Time complexity: O(n), where n is the number of characters

 - suggestAddress(String prefix)
    When given a prefix the method uses the DFS method to get all the suggestions and instantiate a list
    of elements that contains the prefix and returns the list of suggestions
    Time complexity: O(n), where n is the number of elements

 - depthFirstSearch (AdNode node, String prefix, List<String> suggestions)
    Called from the method suggestAddresses that searches through the tree of nodes by DFS combines all characters from
    each path to become a suggestion.
    Time complexity: O(|V| + |E|), where |V| is the amount of vertices and |E| is the amount of edges
 **********************************************************************************************************************/

@Deprecated
public class Trie {
    private AdNodeTrie root;

    public Trie() {
        root = new AdNodeTrie();
    }

    /*Addresses gets inserted in the insert function
    *
    *Characters from the address are stored as nodes and will map to one another
    *
    */
    public void insertAddress(String address) {
        AdNodeTrie node = root;
        for (char c : address.toCharArray()) {
            node.children.putIfAbsent(c, new AdNodeTrie()); //If the specified key is not already
            // associated with a value associates it with the given value and returns null,
            // else returns the current value.
            node = node.children.get(c);

        }
        node.isEndOfWord = true;
    }

    /*
    *Return the list of suggestions based on the user input in the search bar
    */
    public List<String> suggestAddress(String prefix) {
        AdNodeTrie node = root;
        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                //Return one element in suggestion with an information string
                List<String> addressSuggestions = new ArrayList<>();
                addressSuggestions.add("No suggestions were found");
                return addressSuggestions;
            }
            node = node.children.get(c);
        }

        List<String> addressSuggestions = new ArrayList<>();
        if (node != null) {
            depthFirstSearch(node, prefix, addressSuggestions);
        }
        return addressSuggestions;
    }
    /*
    *Recursive depth-first search for finding suggestions
    */
    private void depthFirstSearch(AdNodeTrie node, String prefix, List<String> suggestions) {
        if (node.isEndOfWord) {
            suggestions.add(prefix);
        }
        for (char c : node.children.keySet()) {
            depthFirstSearch(node.children.get(c), prefix + c, suggestions);
        }
    }
}