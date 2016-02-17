/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minebot.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import minebot.LookManager;
import minebot.Memory;
import minebot.MineBot;
import minebot.pathfinding.goals.GoalBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;

/**
 *
 * @author galdara
 */
public class CraftingTask extends ManagerTick {
    static ArrayList<CraftingTask> overallCraftingTasks = new ArrayList<CraftingTask>();
    ArrayList<CraftingTask> subCraftingTasks = new ArrayList<CraftingTask>();
    private Item currentlyCrafting = null;
    private int stackSize;
    private int alreadyHas;
    private CraftingTask(ItemStack craftStack) {
        this.currentlyCrafting = craftStack.getItem();
        this.stackSize = 0;
        buildTasks();
        increaseNeededAmount(craftStack.stackSize);
    }
    public static int map(int id, int width, int height, int craftingSize) {//shamelessly copied from Objectives
        int yPos = id / width;
        int xPos = id % width;
        int z = xPos + craftingSize * yPos;
        return z + 1;
    }
    /**
     * @param item
     * @return recipe for that item, or null if item has no recipe
     */
    public static IRecipe getRecipeFromItem(Item item) {
        List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
        for (IRecipe currRecipe : recipes) {
            if (currRecipe == null) {
                continue;
            }
            if (currRecipe.getRecipeOutput() == null) {
                continue;
            }
            if (currRecipe.getRecipeOutput().getItem() == null) {
                continue;//probably not all of these are necessary, but when I added all three it stopped a nullpointerexception somewhere in this function
            }
            if (currRecipe.getRecipeOutput().getItem().equals(item)) {
                if (isRecipeOkay(currRecipe)) {
                    return currRecipe;
                }
            }
        }
        return null;
    }
    public static boolean isRecipeOkay(IRecipe recipe) {
        if (recipe instanceof ShapedRecipes) {
            if (((ShapedRecipes) recipe).recipeItems.length > 1) {
                return true;
            }
            for (ItemStack stack : ((ShapedRecipes) recipe).recipeItems) {
                if (stack == null) {
                    continue;
                }
                if (stack.toString().toLowerCase().contains("block")) {
                    System.out.println("Not doing " + stack);
                    return false;
                }
            }
            return true;
        }
        if (recipe instanceof ShapelessRecipes) {
            if (((ShapelessRecipes) recipe).recipeItems.size() > 1) {
                return true;
            }
            for (ItemStack stack : ((ShapelessRecipes) recipe).recipeItems) {
                if (stack.toString().toLowerCase().contains("block")) {
                    System.out.println("Not doing " + stack);
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    public static CraftingTask getRequirementsFromItem(Item item) {
        List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
        IRecipe recipe = recipes.get(0);
        //recipe.
        return null;
    }
    public static boolean recipeNeedsCraftingTable(IRecipe recipe) {
        return (recipe instanceof ShapelessRecipes && recipe.getRecipeSize() > 4) || (recipe instanceof ShapedRecipes && (((ShapedRecipes) recipe).recipeHeight > 2 || ((ShapedRecipes) recipe).recipeWidth > 2));
    }
    ArrayList<int[]> plan = null;
    int tickNumber = 0;
    static int ticksBetweenClicks = 4;
    public void tickPlan() {
        GuiContainer contain = (GuiContainer) Minecraft.theMinecraft.currentScreen;
        if (tickNumber % ticksBetweenClicks == 0) {
            int index = tickNumber / ticksBetweenClicks;
            if (index >= plan.size()) {
                GuiScreen.sendChatMessage("Plan over");
                plan = null;
                tickNumber = -40;
                return;
            }
            if (index >= 0) {
                int[] click = plan.get(index);
                GuiScreen.sendChatMessage(index + " " + click[0] + " " + click[1] + " " + click[2] + " " + currentlyCrafting());
                contain.sketchyMouseClick(click[0], click[1], click[2]);
                System.out.println("Ticking plan");
            }
        }
        tickNumber++;
    }
    public boolean onTick1() {
        if (plan != null) {
            if (Minecraft.theMinecraft.currentScreen == null || !(Minecraft.theMinecraft.currentScreen instanceof GuiContainer)) {
                plan = null;
                tickNumber = 0;
                return true;
            }
            tickPlan();
            return true;
        }
        if (isDone()) {
            return false;
        }
        if (stackSize != 0) {
            System.out.println(currentlyCrafting() + " " + alreadyHas + " " + isDone());
        }
        boolean hasMaterials = actualDoCraft(1, false, true) != null;
        //System.out.println("materials " + this + " " + currentlyCrafting() + " " + hasMaterials);
        if (!hasMaterials) {
            return false;
        }
        boolean isCraftingTable = Minecraft.theMinecraft.currentScreen != null && Minecraft.theMinecraft.currentScreen instanceof GuiCrafting;
        if (isCraftingTable) {
            findOrCreateCraftingTask(new ItemStack(Item.getByNameOrId("minecraft:crafting_table"), 0)).clearAll();
        }
        if (!recipeNeedsCraftingTable(getRecipeFromItem(currentlyCrafting)) && !isCraftingTable) {
            craftAsManyAsICan(true);
            return true;//if this doesn't need a crafting table, return no matter what
        }
        //at this point we know that we need a crafting table
        if (isCraftingTable) {
            craftAsManyAsICan(false);
            return true;//since we are already in a crafting table, return so we don't run the code to get into a crafting table repeatedly
        }
        if (!recipeNeedsCraftingTable(getRecipeFromItem(currentlyCrafting))) {
            return false;
        }
        //at this point we know that we need a crafting table and we aren't in one at this moment
        BlockPos craftingTableLocation = Memory.closestOne("crafting_table");
        if (craftingTableLocation != null) {
            if (LookManager.couldIReach(craftingTableLocation)) {
                LookManager.lookAtBlock(craftingTableLocation, true);
                if (craftingTableLocation.equals(MineBot.whatAreYouLookingAt())) {
                    MineBot.currentPath = null;
                    MineBot.clearMovement();
                    Minecraft.theMinecraft.rightClickMouse();
                    findOrCreateCraftingTask(new ItemStack(Item.getByNameOrId("minecraft:crafting_table"), 0)).clearAll();
                }
                return true;
            } else {
                double diffX = craftingTableLocation.getX() + 0.5D - Minecraft.theMinecraft.thePlayer.posX;
                double diffY = craftingTableLocation.getY() + 0.5D - Minecraft.theMinecraft.thePlayer.posY;
                double diffZ = craftingTableLocation.getZ() + 0.5D - Minecraft.theMinecraft.thePlayer.posZ;
                double distXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
                if (distXZ < 50 && Math.abs(diffY) < 20) {
                    MineBot.goal = new GoalBlock(craftingTableLocation.up());
                    if (MineBot.currentPath == null && !MineBot.isPathFinding()) {
                        MineBot.findPathInNewThread(false);
                    }
                    return true;
                } else {
                    GuiScreen.sendChatMessage("too far away from closest crafting table (" + distXZ + " blocks), crafting another");
                }
            }
        }
        if (MineBot.whatAreYouLookingAt() != null && Block.getBlockFromName("crafting_table").equals(Minecraft.theMinecraft.theWorld.getBlockState(MineBot.whatAreYouLookingAt()).getBlock())) {
            MineBot.currentPath = null;
            MineBot.clearMovement();
            Minecraft.theMinecraft.rightClickMouse();
            findOrCreateCraftingTask(new ItemStack(Item.getByNameOrId("minecraft:crafting_table"), 0)).clearAll();
            return true;
        }
        //at this point we know that we need a crafting table and we aren't in one and there isn't one nearby
        if (putCraftingTableOnHotBar()) {
            findOrCreateCraftingTask(new ItemStack(Item.getByNameOrId("minecraft:crafting_table"), 0)).clearAll();
            System.out.println("Ready to place!");
            if (placeHeldBlockNearby()) {
                return true;
            }
            BlockPos player = Minecraft.theMinecraft.thePlayer.getPosition0();
            if (MineBot.isAir(player.down()) || MineBot.isAir(player.up())) {
                GuiScreen.sendChatMessage("Placing down");
                LookManager.lookAtBlock(Minecraft.theMinecraft.thePlayer.getPosition0().down(), true);
                MineBot.jumping = true;
                if (Minecraft.theMinecraft.thePlayer.getPosition0().down().equals(MineBot.whatAreYouLookingAt()) || Minecraft.theMinecraft.thePlayer.getPosition0().down().down().equals(MineBot.whatAreYouLookingAt())) {
                    Minecraft.theMinecraft.rightClickMouse();
                }
                return true;
            }
            /*
             LookManager.lookAtBlock(Minecraft.theMinecraft.thePlayer.getPosition0().down().north(), true);
             LookManager.beSketchy();
             MineBot.forward = new Random().nextBoolean();
             MineBot.backward = new Random().nextBoolean();
             MineBot.left = new Random().nextBoolean();
             MineBot.right = new Random().nextBoolean();
             MineBot.jumping = true;*/
            return true;
        }
        //at this point we know that we need a crafting table and we aren't in one and there isn't one nearby and we don't have one
        ensureCraftingDesired(Item.getByNameOrId("crafting_table"), 1);
        //at this point we know that we need a crafting table and we aren't in one and there isn't one nearby and we don't have one and we don't have the materials to make one
        //so just rip at this point
        return false;
    }
    public static boolean placeHeldBlockNearby() {
        BlockPos player = Minecraft.theMinecraft.thePlayer.getPosition0();
        for (int x = player.getX() - 3; x <= player.getX() + 3; x++) {
            for (int y = player.getY() - 2; y <= player.getY() + 1; y++) {
                for (int z = player.getZ() - 3; z <= player.getZ() + 3; z++) {
                    if (x == player.getX() && z == player.getZ()) {
                        continue;
                    }
                    BlockPos pos = new BlockPos(x, y, z);
                    if (Minecraft.theMinecraft.theWorld.getBlockState(pos).getBlock().equals(Block.getBlockFromName("crafting_table"))) {
                        Memory.scanBlock(pos);
                    }
                    if (MineBot.isAir(pos)) {
                        for (EnumFacing f : EnumFacing.values()) {
                            BlockPos placeAgainst = pos.offset(f);
                            if (!MineBot.isAir(placeAgainst) && Minecraft.theMinecraft.theWorld.getBlockState(placeAgainst).getBlock().isBlockNormalCube()) {
                                if (LookManager.couldIReach(placeAgainst, pos)) {
                                    double faceX = (pos.getX() + placeAgainst.getX() + 1.0D) * 0.5D;
                                    double faceY = (pos.getY() + placeAgainst.getY()) * 0.5D;
                                    double faceZ = (pos.getZ() + placeAgainst.getZ() + 1.0D) * 0.5D;
                                    if (LookManager.lookAtCoords(faceX, faceY, faceZ, true)) {
                                        Minecraft.theMinecraft.rightClickMouse();
                                    }
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    public static boolean putCraftingTableOnHotBar() {//shamelessly copied from MickeyMine.torch()
        EntityPlayerSP p = Minecraft.theMinecraft.thePlayer;
        ItemStack[] inv = p.inventory.mainInventory;
        for (int i = 0; i < 9; i++) {
            ItemStack item = inv[i];
            if (inv[i] == null) {
                continue;
            }
            if (Item.getByNameOrId("minecraft:crafting_table").equals(item.getItem())) {
                p.inventory.currentItem = i;
                return true;
            }
        }
        return false;
    }
    public void clearAll() {
        if (stackSize != 0) {
            decreaseNeededAmount(stackSize);
        }
    }
    /**
     *
     * @param inInventory
     * @return did I actually craft some
     */
    public boolean craftAsManyAsICan(boolean inInventory) {
        int amtCurrentlyCraftable = stackSize - alreadyHas;
        if (amtCurrentlyCraftable > 64) {
            amtCurrentlyCraftable = 64;
        }
        while (true) {
            Boolean b = actualDoCraft(amtCurrentlyCraftable, inInventory, false);
            if (b != null) {
                return b;
            }
            amtCurrentlyCraftable--;
            if (amtCurrentlyCraftable <= 0) {
                return false;
            }
        }
    }
    /**
     *
     * @param outputQuantity
     * @param inInventory
     * @return true if it was able to craft and did, null if it was unable to
     * craft because of a lack of input items, false for anything else
     * (including being unable to craft for other reasons)
     */
    public Boolean actualDoCraft(int outputQuantity, boolean inInventory, boolean justChecking) {
        IRecipe currentRecipe = getRecipeFromItem(currentlyCrafting);
        int outputVolume = currentRecipe.getRecipeOutput().stackSize;
        int inputQuantity = (int) Math.ceil(((double) outputQuantity) / ((double) outputVolume));
        if (currentRecipe instanceof ShapedRecipes) {
            ShapedRecipes shaped = (ShapedRecipes) currentRecipe;
            if (!inInventory || (inInventory && shaped.recipeHeight <= 2 && shaped.recipeWidth <= 2)) {
                int numNotNull = 0;
                for (ItemStack recipeItem : shaped.recipeItems) {
                    if (recipeItem != null) {
                        numNotNull++;
                    }
                }
                Item[] items = new Item[numNotNull];
                int[] positions = new int[items.length];
                int index = 0;
                for (int i = 0; i < shaped.recipeItems.length; i++) {
                    if (shaped.recipeItems[i] == null) {
                        continue;
                    }
                    items[index] = shaped.recipeItems[i].getItem();
                    positions[index] = map(i, shaped.recipeWidth, shaped.recipeHeight, inInventory ? 2 : 3);
                    index++;
                }
                return actualDoCraftOne(items, positions, inputQuantity, inInventory, justChecking);
            }
        }
        if (currentRecipe instanceof ShapelessRecipes) {
            ShapelessRecipes shapeless = (ShapelessRecipes) currentRecipe;
            if (!inInventory || (inInventory && shapeless.getRecipeSize() < 4)) {
                Item[] items = new Item[shapeless.getRecipeSize()];
                int[] positions = new int[items.length];
                for (int i = 0; i < items.length; i++) {
                    items[i] = shapeless.recipeItems.get(i).getItem();
                    positions[i] = i + 1;
                }
                return actualDoCraftOne(items, positions, inputQuantity, inInventory, justChecking);
            }
        }
        return justChecking ? null : false;
    }
    /**
     *
     * @param items
     * @param positions
     * @param amount
     * @param inv
     * @return true if it was able to craft and did, null if it was unable to
     * craft because of a lack of input items, false for anything else
     * (including being unable to craft for other reasons)
     */
    public Boolean actualDoCraftOne(Item[] items, int[] positions, int amount, boolean inv, boolean justChecking) {
        int[] amounts = new int[items.length];
        for (int i = 0; i < items.length; i++) {
            amounts[i] = amount;
        }
        int[] count = new int[items.length];
        for (int i = 0; i < Minecraft.theMinecraft.thePlayer.inventory.mainInventory.length; i++) {
            ItemStack in = Minecraft.theMinecraft.thePlayer.inventory.mainInventory[i];
            if (in == null) {
                continue;
            }
            Item item = in.getItem();
            int size = in.stackSize;
            for (int j = 0; j < items.length; j++) {
                if (items[j].equals(item)) {
                    int amountRemain = amounts[j] - count[j];
                    if (amountRemain >= size) {
                        count[j] += size;
                        size = 0;
                    } else {
                        count[j] += amountRemain;
                        size -= amountRemain;
                    }
                }
            }
        }
        for (int i = 0; i < count.length; i++) {
            if (count[i] != amounts[i]) {
                //GuiScreen.sendChatMessage("Not enough " + items[i], true);
                return null;
            }
        }
        if (justChecking) {
            return false;
        }
        if (inv) {
            if (Minecraft.theMinecraft.currentScreen == null || !(Minecraft.theMinecraft.currentScreen instanceof GuiInventory)) {
                System.out.println("Opening");
                MineBot.slowOpenInventory();
            }
            didIOpenMyInventory = true;
        } else {
            if (Minecraft.theMinecraft.currentScreen == null || !(Minecraft.theMinecraft.currentScreen instanceof GuiCrafting)) {
                GuiScreen.sendChatMessage("Not in crafting table", true);
                return false;
            } else {
                didIOpenMyInventory = true;
            }
        }
        GuiContainer contain = (GuiContainer) Minecraft.theMinecraft.currentScreen;
        for (int i = 1; i < (inv ? 5 : 10); i++) {
            if (contain.inventorySlots.inventorySlots.get(i).getHasStack()) {
                return false;
            }
        }
        GuiScreen.sendChatMessage("Crafting amount " + amount + " of " + currentlyCrafting(), true);
        plan = new ArrayList();
        tickNumber = -5;
        for (int i = inv ? 9 : 10; i < contain.inventorySlots.inventorySlots.size(); i++) {
            Slot slot = contain.inventorySlots.inventorySlots.get(i);
            if (!slot.getHasStack()) {
                continue;
            }
            ItemStack in = slot.getStack();
            if (in == null) {
                continue;
            }
            Item item = in.getItem();
            int size = in.stackSize;
            for (int j = 0; j < items.length; j++) {
                if (amounts[j] <= 0) {
                    continue;
                }
                if (items[j].equals(item)) {
                    leftClick(i);
                    if (size <= amounts[j]) {
                        leftClick(positions[j]);
                        amounts[j] -= size;
                        size = 0;
                    } else {
                        for (int k = 0; k < amounts[j]; k++) {
                            rightClick(positions[j]);
                        }
                        size -= amounts[j];
                        leftClick(i);
                        amounts[j] = 0;
                    }
                }
            }
        }
        GuiScreen.sendChatMessage("shift clicking " + contain.inventorySlots.inventorySlots.get(0).getStack(), true);
        shiftClick(0);
        for (int i = 0; i < amounts.length; i++) {
            if (amounts[i] > 0) {
                GuiScreen.sendChatMessage("Not enough " + i + " " + amounts[i] + " " + items[i] + " " + positions[i], true);//this detects if it didn't have enough, but you shouldn't call this function unless you have already made sure you have enough
            }
        }
        return true;
    }
    public void leftClick(int slot) {
        if (!plan.isEmpty()) {
            int[] last = plan.get(plan.size() - 1);
            if (last[0] == slot && last[1] == 0 && last[2] == 0) {
                plan.remove(plan.size() - 1);
                return;
            }
        }
        plan.add(new int[]{slot, 0, 0});
    }
    public void rightClick(int slot) {
        plan.add(new int[]{slot, 1, 0});
    }
    public void shiftClick(int slot) {
        plan.add(new int[]{slot, 0, 1});
    }
    static boolean didIOpenMyInventory = false;
    static boolean waitingToClose = false;
    static int TUC = 20;
    protected boolean onTick0() {
        MineBot.clearMovement();
        for (CraftingTask craftingTask : overallCraftingTasks) {
            if (craftingTask.plan != null) {
                System.out.println(craftingTask + " " + craftingTask.currentlyCrafting() + " " + craftingTask.plan);
                if (!craftingTask.onTick1()) {
                    didIOpenMyInventory = true;
                }
                return true;
            }
        }
        for (CraftingTask craftingTask : overallCraftingTasks) {
            if (craftingTask.onTick1()) {
                return false;
            }
        }
        if (didIOpenMyInventory) {
            waitingToClose = true;
            TUC = 3;
            didIOpenMyInventory = false;
        }
        if (waitingToClose) {
            TUC--;
            if (TUC <= 0) {
                GuiScreen.sendChatMessage("Closing screen!!!");
                Minecraft.theMinecraft.thePlayer.closeScreen();
                waitingToClose = false;
                TUC = 3;
            }
            return true;
        }
        return false;
    }
    public final void buildTasks() {
        IRecipe currentRecipe = getRecipeFromItem(currentlyCrafting);
        if (!(currentRecipe == null)) {
            if (currentRecipe instanceof ShapedRecipes) {
                ShapedRecipes shapedRecipe = (ShapedRecipes) currentRecipe;
                for (ItemStack input : shapedRecipe.recipeItems) {
                    if (input == null) {
                        continue;
                    }
                    IRecipe inputRecipe = getRecipeFromItem(input.getItem());
                    if (!(inputRecipe == null)) {
                        System.out.println("As a part of " + currentlyCrafting + ", getting " + input);
                        CraftingTask newTask = CraftingTask.findOrCreateCraftingTask(new ItemStack(input.getItem(), 0));
                        subCraftingTasks.add(newTask);
                        //newTask.execute();
                    }
                }
            } else if (currentRecipe instanceof ShapelessRecipes) {
                ShapelessRecipes shapelessRecipe = (ShapelessRecipes) currentRecipe;
                for (ItemStack input : shapelessRecipe.recipeItems) {
                    IRecipe inputRecipe = getRecipeFromItem(input.getItem());
                    if (!(inputRecipe == null)) {
                        System.out.println("As a part of " + currentlyCrafting + ", getting " + input);
                        CraftingTask newTask = CraftingTask.findOrCreateCraftingTask(new ItemStack(input.getItem(), 0));
                        subCraftingTasks.add(newTask);
                        //newTask.execute();
                    }
                }
            } else {
                throw new IllegalStateException("Current recipe isn't shapeless or shaped");
            }
        } else {
            throw new IllegalArgumentException("no recipe for this");
        }
    }
    public static CraftingTask findOrCreateCraftingTask(ItemStack itemStack) {
        //System.out.println("Getting a task for " + itemStack);
        for (CraftingTask selectedTask : overallCraftingTasks) {
            if (selectedTask.currentlyCrafting().getItem().equals(itemStack.getItem())) {
                if (itemStack.stackSize != 0) {
                    selectedTask.increaseNeededAmount(itemStack.stackSize);
                }
                return selectedTask;
            }
        }
        CraftingTask newTask = new CraftingTask(itemStack);
        overallCraftingTasks.add(newTask);
        return newTask;
    }
    public ItemStack currentlyCrafting() {
        return new ItemStack(currentlyCrafting, stackSize);
    }
    public final void increaseNeededAmount(int amount) {
        //GuiScreen.sendChatMessage(currentlyCrafting() + " inc " + amount);
        int stackSizeBefore = stackSize;
        stackSize += amount;
        IRecipe currentRecipe = getRecipeFromItem(currentlyCrafting);
        int outputVolume = currentRecipe.getRecipeOutput().stackSize;
        int inputQuantityBefore = (int) Math.ceil(((double) stackSizeBefore) / ((double) outputVolume));
        int inputQuantityNew = (int) Math.ceil(((double) stackSize) / ((double) outputVolume));
        int change = inputQuantityNew - inputQuantityBefore;
        if (change != 0) {
            /*for (CraftingTask craftingTask : subCraftingTasks) {
             GuiScreen.sendChatMessage("> inc sub " + craftingTask.currentlyCrafting() + " " + change);
             }*/
            for (CraftingTask craftingTask : subCraftingTasks) {
                craftingTask.increaseNeededAmount(change);
            }
        }
    }
    public void decreaseNeededAmount(int amount) {
        //GuiScreen.sendChatMessage(currentlyCrafting() + " dec " + amount);
        int stackSizeBefore = stackSize;
        stackSize -= amount;
        IRecipe currentRecipe = getRecipeFromItem(currentlyCrafting);
        int outputVolume = currentRecipe.getRecipeOutput().stackSize;
        int inputQuantityBefore = (int) Math.ceil(((double) stackSizeBefore) / ((double) outputVolume));
        int inputQuantityNew = (int) Math.ceil(((double) stackSize) / ((double) outputVolume));
        int change = inputQuantityBefore - inputQuantityNew;
        if (change != 0) {
            /*for (CraftingTask craftingTask : subCraftingTasks) {
             GuiScreen.sendChatMessage("> dec sub " + craftingTask.currentlyCrafting() + " " + change);
             }*/
            for (CraftingTask craftingTask : subCraftingTasks) {
                craftingTask.decreaseNeededAmount(change);
            }
        }
    }
    public static HashMap<Item, ArrayList<Tuple<Integer, Integer>>> getCurrentRecipeItems(IRecipe recipe) {
        HashMap<Item, ArrayList<Tuple<Integer, Integer>>> amountHasAndWhere = new HashMap();
        ArrayList<ItemStack> needsItemStackstmp = null;
        if (recipe instanceof ShapedRecipes) {
            ShapedRecipes shapedRecipe = (ShapedRecipes) recipe;
            needsItemStackstmp = new ArrayList<ItemStack>(Arrays.asList(shapedRecipe.recipeItems));
        } else if (recipe instanceof ShapelessRecipes) {
            ShapelessRecipes shapelessRecipe = (ShapelessRecipes) recipe;
            needsItemStackstmp = new ArrayList<ItemStack>(shapelessRecipe.recipeItems);
        } else {
            throw new IllegalStateException("recipe was not shaped or shapeless");
        }
        ArrayList<Item> needsItemStacks = new ArrayList();
        for (ItemStack stack : needsItemStackstmp) {
            Item item = stack.getItem();
            if (!needsItemStacks.contains(item)) {
                needsItemStacks.add(item);
            }
        }
        for (int i = 0; i < Minecraft.theMinecraft.thePlayer.inventory.getSizeInventory(); i++) {
            for (int j = 0; j < needsItemStacks.size(); j++) {
                if (Minecraft.theMinecraft.thePlayer.inventory.getStackInSlot(i) == null) {
                    continue;//prevents nullpointerexception if the stack is null
                }
                if (Minecraft.theMinecraft.thePlayer.inventory.getStackInSlot(i).getItem().equals(needsItemStacks.get(j))) {
                    if (!amountHasAndWhere.containsKey(needsItemStacks.get(j))) {
                        ArrayList<Tuple<Integer, Integer>> positionAmountArr = new ArrayList<Tuple<Integer, Integer>>();
                        positionAmountArr.add(new Tuple(Minecraft.theMinecraft.thePlayer.inventory.getStackInSlot(i).stackSize, i));
                        amountHasAndWhere.put(needsItemStacks.get(j), positionAmountArr);
                    } else {
                        amountHasAndWhere.get(needsItemStacks.get(j)).add(new Tuple(Minecraft.theMinecraft.thePlayer.inventory.getStackInSlot(i).stackSize, i));
                    }
                }
            }
        }
        return amountHasAndWhere;
    }
    public void calculateAlreadyHasAmount() {
        int count = 0;
        for (int i = 0; i < Minecraft.theMinecraft.thePlayer.inventory.getSizeInventory(); i++) {
            if (Minecraft.theMinecraft.thePlayer.inventory.getStackInSlot(i) == null) {
                continue;
            }
            if (Minecraft.theMinecraft.thePlayer.inventory.getStackInSlot(i).getItem().equals(currentlyCrafting)) {
                count += Minecraft.theMinecraft.thePlayer.inventory.getStackInSlot(i).stackSize;
            }
        }
        alreadyHas = count;
    }
    public int alreadyHas() {
        return alreadyHas;
    }
    public boolean isDone() {
        calculateAlreadyHasAmount();
        return stackSize <= alreadyHas;
    }
    public static boolean ensureCraftingDesired(Item item, int quantity) {
        CraftingTask craftingTableTask = CraftingTask.findOrCreateCraftingTask(new ItemStack(item, 0));
        //System.out.println(craftingTableTask.currentlyCrafting() + " " + quantity + " " + craftingTableTask.stackSize + " " + craftingTableTask.alreadyHas + " " + craftingTableTask.isDone());
        if (craftingTableTask.isDone() && craftingTableTask.alreadyHas >= quantity) {
            if (craftingTableTask.stackSize > 0) {
                craftingTableTask.decreaseNeededAmount(1);
            }
            return true;
        }
        if (craftingTableTask.stackSize < quantity) {
            craftingTableTask.increaseNeededAmount(quantity - craftingTableTask.stackSize);
        }
        return craftingTableTask.alreadyHas >= quantity;
    }
    public static Manager createInstance(Class c) {
        return new CraftingTask();
    }
    private CraftingTask() {
    }
    @Override
    protected void onCancel() {
        overallCraftingTasks.clear();
    }
    @Override
    protected void onStart() {
    }
    @Override
    protected boolean onEnabled(boolean enabled) {
        return true;
    }
}
