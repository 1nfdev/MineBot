/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minebot.pathfinding.goals;

import java.util.Arrays;
import net.minecraft.util.BlockPos;

/**
 *
 * @author leijurv
 */
public class GoalRunAway implements Goal {
    public final BlockPos[] from;
    final double distanceSq;
    public GoalRunAway(double distance, BlockPos... from) {
        if (from.length == 0) {
            throw new IllegalArgumentException();
        }
        this.from = from;
        this.distanceSq = distance * distance;
    }
    @Override
    public boolean isInGoal(BlockPos pos) {
        for (BlockPos p : from) {
            int diffX = pos.getX() - p.getX();
            int diffZ = pos.getZ() - p.getZ();
            double distSq = diffX * diffX + diffZ * diffZ;
            if (distSq < distanceSq) {
                return false;
            }
        }
        return true;
    }
    @Override
    public double heuristic(BlockPos pos) {//mostly copied from GoalBlock
        double min = Double.MAX_VALUE;
        for (BlockPos p : from) {
            double h = GoalXZ.calculate(p.getX() - pos.getX(), p.getZ() - pos.getZ());
            if (h < min) {
                min = h;
            }
        }
        return -min;
    }
    @Override
    public String toString() {
        return "GoalRunAwayFrom[" + Arrays.asList(from) + "]";
    }
}
