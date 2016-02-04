/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minebot.pathfinding.goals;

import net.minecraft.util.BlockPos;

/**
 *
 * @author leijurv
 */
public class GoalTwoBlocks implements Goal {
    final int x, y, z;
    public GoalTwoBlocks(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }
    public GoalTwoBlocks(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    @Override
    public boolean isInGoal(BlockPos pos) {
        return pos.getX() == this.x && (pos.getY() == this.y || pos.getY() == this.y - 1) && pos.getZ() == this.z;
    }
    @Override
    public double heuristic(BlockPos pos) {
        double xDiff = pos.getX() - this.x;
        double yDiff = pos.getY() - this.y;
        if (yDiff < 0) {
            yDiff++;
        }
        double zDiff = pos.getZ() - this.z;
        return GoalBlock.calculate(xDiff, yDiff, zDiff);
    }
    @Override
    public String toString() {
        return "Goal{x=" + x + ",y=" + y + ",z=" + z + "}";
    }
}
