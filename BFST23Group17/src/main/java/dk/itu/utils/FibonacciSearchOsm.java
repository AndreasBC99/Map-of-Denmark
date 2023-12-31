package dk.itu.utils;

// Java program for Fibonacci Search

import dk.itu.model.OsmElement;

import java.util.List;

// Taken from https://www.geeksforgeeks.org/fibonacci-search/
public class FibonacciSearchOsm {
    // Utility function to find minimum
    // of two elements
    // Utility function to find minimum
    // of two elements
    private static int min(int x, int y)
    {
        return (x <= y) ? x : y;
    }

    /* Returns index of x if present, else returns -1 */
    public static int fibonaccianSearch(List<OsmElement> arr, long id) {
        /* Initialize fibonacci numbers */
        int fibMMm2 = 0; // (m-2)'th Fibonacci No.
        int fibMMm1 = 1; // (m-1)'th Fibonacci No.
        int fibM = fibMMm2 + fibMMm1; // m'th Fibonacci

        /* fibM is going to store the smallest Fibonacci Number greater than or equal to n */
        while (fibM < arr.size()) {
            fibMMm2 = fibMMm1;
            fibMMm1 = fibM;
            fibM = fibMMm2 + fibMMm1;
        }

        // Marks the eliminated range from front
        int offset = -1;

        /* while there are elements to be inspected.
        Note that we compare arr[fibMm2] with x.
        When fibM becomes 1, fibMm2 becomes 0 */
        while (fibM > 1) {
            // Check if fibMm2 is a valid location
            int i = min(offset + fibMMm2, arr.size() - 1);

            /* If x is greater than the value at
            index fibMm2, cut the subarray array
            from offset to i */
            if (arr.get(i).getId() < id) {
                fibM = fibMMm1;
                fibMMm1 = fibMMm2;
                fibMMm2 = fibM - fibMMm1;
                offset = i;
            }

            /* If x is less than the value at index
            fibMm2, cut the subarray after i+1 */
            else if (arr.get(i).getId() > id) {
                fibM = fibMMm2;
                fibMMm1 = fibMMm1 - fibMMm2;
                fibMMm2 = fibM - fibMMm1;
            }

            /* element found. return index */
            else
                return i;
        }

        try {
            /* comparing the last element with x */
            if (fibMMm1 == 1 && arr.get(arr.size() - 1).getId() == id)
                return arr.size() - 1;
        } catch (RuntimeException e) {
            return -1;
        }

        /*element not found. return -1 */
        return -1;
    }
}