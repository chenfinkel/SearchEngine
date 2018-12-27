package sample;

import java.util.List;

// Java implementation of recursive Binary Search
class BinarySearch {
    // Returns index of x if it is present in arr[l..
    // r], else return -1
    public String binarySearch(List<String> arr, int l, int r, String term)
    {
        if (r >= l) {
            int mid = l + (r - l) / 2;
            String postingTerm = arr.get(mid).split("~")[0];
            // If the element is present at the
            // middle itself
            if (postingTerm.equals(term))
                return arr.get(mid);

            // If element is smaller than mid, then
            // it can only be present in left subarray
            if (postingTerm.compareToIgnoreCase(term) > 0)
                return binarySearch(arr, l, mid - 1, term);

            // Else the element can only be present
            // in right subarray
            return binarySearch(arr, mid + 1, r, term);
        }

        // We reach here when element is not present
        // in array
        return null;
    }
}
/* This code is contributed by Rajat Mishra */