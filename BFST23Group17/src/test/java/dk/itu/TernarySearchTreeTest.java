package dk.itu;

import dk.itu.datastructure.TernarySearchTree;
import dk.itu.model.MapModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TernarySearchTreeTest {
    private TernarySearchTree tst;
    @BeforeEach
    public void setup() {
        tst = new TernarySearchTree();

    }

    @Test
    public void testTSTSuggest() {
        List<String> exceptedList = new ArrayList<>();
        List<String> actualList = new ArrayList<>();
        tst.insert("Amagerbrogade 18 2300 København S");
        tst.insert("Amagerbrogade 20 2300 København S");
        tst.insert("Rued Langgaards Vej 7 2300 København S");
        tst.insert("Milanovej 2 2300 København S");
        tst.insert("Toldbodsgade 8 3790 Bornholm");
        actualList = tst.suggest("Amag");
        exceptedList.add("Amagerbrogade 18 2300 København S");
        exceptedList.add("Amagerbrogade 20 2300 København S");

        assertEquals(exceptedList, actualList);
    }

    @Test
    public void testInsertionTime() {
        long startTime = System.currentTimeMillis();
        for (int i = 0 ; i < 100000 ; i++) {
            tst.insert("Amagerbrogade 18 2300 København S");
            tst.insert("Amagerbrogade 20 2300 København S");
            tst.insert("Rued Langgaards Vej 7 2300 København S");
            tst.insert("Milanovej 2 2300 København S");
            tst.insert("Toldbodsgade 8 3790 Bornholm");
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        System.out.println("Insertion Time: " + totalTime + " ms");
    }

    @Test
    public void testSuggestionTime() {
        long startTime = System.currentTimeMillis();
        for (int i = 0 ; i < 100000 ; i++) {
            tst.insert("Amagerbrogade 18 2300 København S");
            tst.insert("Amagerbrogade 20 2300 København S");
            tst.insert("Rued Langgaards Vej 7 2300 København S");
            tst.insert("Milanovej 2 2300 København S");
            tst.insert("Toldbodsgade 8 3790 Bornholm");
        }
        List<String> suggestions = tst.suggest("R");

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        System.out.println(suggestions);
        System.out.println("Suggestion Time: " + totalTime + " ms");
    }



}
