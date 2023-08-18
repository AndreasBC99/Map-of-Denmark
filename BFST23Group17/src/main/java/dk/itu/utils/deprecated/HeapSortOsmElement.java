package dk.itu.utils.deprecated;

import dk.itu.model.OsmElement;

/*
 * Chosen HeapSort as it has a space complexity of O(1), whilst still an average time complexity of O(n log n)
 * Taken from https://www.geeksforgeeks.org/java-program-for-heap-sort/
 */
@Deprecated
public class HeapSortOsmElement {
    public static void sort(OsmElement[] arr, boolean onX, boolean onMin) {
        if (onMin) {
            sortMin(arr, onX);
        } else {
            sortMax(arr, onX);
        }
    }

    private static void sortMin(OsmElement[] arr, boolean onX) {
        int n = arr.length;

        // Build heap (rearrange array)
        for (int i = n / 2 - 1; i >= 0; i--)
            heapifyMin(arr, n, i, onX);

        // One by one extract an element from heap
        for (int i = n - 1; i > 0; i--) {
            // Move current root to end
            OsmElement temp = arr[0];
            arr[0] = arr[i];
            arr[i] = temp;

            // call max heapify on the reduced heap
            heapifyMin(arr, i, 0, onX);
        }
    }

    // To heapify a subtree rooted with node i which is
    // an index in arr[]. n is size of heap
    private static void heapifyMin(OsmElement[] arr, int n, int i, boolean onX)
    {
        int largest = i; // Initialize largest as root
        int l = 2 * i + 1; // left = 2*i + 1
        int r = 2 * i + 2; // right = 2*i + 2

        // If left child is larger than root
        if (l < n && (onX ? arr[l].getMinX() : arr[l].getMinY()) > (onX ? arr[largest].getMinX() : arr[largest].getMinY()))
            largest = l;

        // If right child is larger than largest so far
        if (r < n && (onX ? arr[r].getMinX() : arr[r].getMinY()) > (onX ? arr[largest].getMinX() : arr[largest].getMinY()))
            largest = r;

        // If largest is not root
        if (largest != i) {
            OsmElement swap = arr[i];
            arr[i] = arr[largest];
            arr[largest] = swap;

            // Recursively heapify the affected sub-tree
            heapifyMin(arr, n, largest, onX);
        }
    }

    private static void sortMax(OsmElement[] arr, boolean onX) {
        int n = arr.length;

        // Build heap (rearrange array)
        for (int i = n / 2 - 1; i >= 0; i--)
            heapifyMax(arr, n, i, onX);

        // One by one extract an element from heap
        for (int i = n - 1; i > 0; i--) {
            // Move current root to end
            OsmElement temp = arr[0];
            arr[0] = arr[i];
            arr[i] = temp;

            // call max heapify on the reduced heap
            heapifyMax(arr, i, 0, onX);
        }
    }

    // To heapify a subtree rooted with node i which is
    // an index in arr[]. n is size of heap
    private static void heapifyMax(OsmElement[] arr, int n, int i, boolean onX)
    {
        int largest = i; // Initialize largest as root
        int l = 2 * i + 1; // left = 2*i + 1
        int r = 2 * i + 2; // right = 2*i + 2

        // If left child is larger than root
        if (l < n && (onX ? arr[l].getMaxX() : arr[l].getMaxY()) > (onX ? arr[largest].getMaxX() : arr[largest].getMaxY()))
            largest = l;

        // If right child is larger than largest so far
        if (r < n && (onX ? arr[r].getMaxX() : arr[r].getMaxY()) > (onX ? arr[largest].getMaxX() : arr[largest].getMaxY()))
            largest = r;

        // If largest is not root
        if (largest != i) {
            OsmElement swap = arr[i];
            arr[i] = arr[largest];
            arr[largest] = swap;

            // Recursively heapify the affected sub-tree
            heapifyMax(arr, n, largest, onX);
        }
    }
}
