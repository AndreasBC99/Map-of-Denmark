package dk.itu.datastructure.deprecated;

import java.util.HashMap;
import java.util.Map;

@Deprecated
class AdNodeTrie {
    public Map<Character, AdNodeTrie> children;

    public boolean isEndOfWord;


    public AdNodeTrie() {
        isEndOfWord = false;
        children = new HashMap<Character, AdNodeTrie>();

    }
}