package com.deepan.bettervillagers;

import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class PrintWalkNodeEvaluator {
    public static void main(String[] args) {
        System.out.println("Methods in WalkNodeEvaluator:");
        for (Method m : WalkNodeEvaluator.class.getDeclaredMethods()) {
            if (m.getName().equals("evaluateBlockPathType") || m.getName().equals("getBlockPathType") || m.getName().equals("getPathType")) {
                System.out.print(m.getName() + "(");
                Parameter[] params = m.getParameters();
                for (int i = 0; i < params.length; i++) {
                    System.out.print(params[i].getType().getSimpleName());
                    if (i < params.length - 1) System.out.print(", ");
                }
                System.out.println(") -> " + m.getReturnType().getSimpleName());
            }
        }
    }
}
