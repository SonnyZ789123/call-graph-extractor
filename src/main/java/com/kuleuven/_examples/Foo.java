package com.kuleuven._examples;

public class Foo {
    public static int foo(int x) {
        System.out.println("\n-------- In foo! Parameter = " + x);

        if (x < 0) {
            System.out.println("L1: x < 0");

            if (x < -10) {
                System.out.println("L2: x < -10");

                if (x % 2 == 0) {
                    System.out.println("L3: x < -10 && even");
                    return -4;
                } else {
                    System.out.println("L3: x < -10 && odd");
                    return -3;
                }

            } else {
                System.out.println("L2: -10 <= x < 0");

                if (x == -5) {
                    System.out.println("L3: x == -5");
                    System.out.println("Raising an exception...");
                    throw new RuntimeException("Intentional error for CFG testing");
                } else {
                    System.out.println("L3: x != -5");
                    return -1;
                }
            }

        } else {
            System.out.println("L1: x >= 0");

            if (x > 10) {
                System.out.println("L2: x > 10");

                double y = Math.sin(x);
                if (y > 0.5) {
                    System.out.println("L3: sin(x) > 0.5");
                    return 101;
                } else {
                    System.out.println("L3: sin(x) <= 0.5");
                    return 102;
                }

            } else {
                System.out.println("L2: 0 <= x <= 10");

                float y = 3.14f;
                if (x + y < 256) {
                    System.out.println("L3: x + 3.14f < 256");
                    return 10;
                } else {
                    // JDart's constraint solver cannot invert this path
                    System.out.println("DON'T KNOW path");
                    return -1;
                }
            }
        }
    }
}
