package com.kuleuven.coverage;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream("../data/coverage.out"))) {

            @SuppressWarnings("unchecked")
            List<int[]> executionPaths = (List<int[]>) ois.readObject();

            for (int[] path : executionPaths) {
                System.out.println(Arrays.toString(path));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
