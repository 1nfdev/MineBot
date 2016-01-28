package net.minecraft.item;

import java.util.Random;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.WeightedRandomChestContent;

public class ItemEnchantedBook extends Item {
   public boolean func_77616_k(ItemStack p_77616_1_) {
      return false;
   }

   public EnumRarity func_77613_e(ItemStack p_77613_1_) {
      return this.func_92110_g(p_77613_1_).func_74745_c() > 0?EnumRarity.UNCOMMON:super.func_77613_e(p_77613_1_);
   }

   public NBTTagList func_92110_g(ItemStack p_92110_1_) {
      NBTTagCompound nbttagcompound = p_92110_1_.func_77978_p();
      return nbttagcompound != null && nbttagcompound.func_150297_b("StoredEnchantments", 9)?(NBTTagList)nbttagcompound.func_74781_a("StoredEnchantments"):new NBTTagList();
   }

   public void func_92115_a(ItemStack p_92115_1_, EnchantmentData p_92115_2_) {
      NBTTagList nbttaglist = this.func_92110_g(p_92115_1_);
      boolean flag = true;

      for(int i = 0; i < nbttaglist.func_74745_c(); ++i) {
         NBTTagCompound nbttagcompound = nbttaglist.func_150305_b(i);
         if(nbttagcompound.func_74765_d("id") == p_92115_2_.field_76302_b.field_77352_x) {
            if(nbttagcompound.func_74765_d("lvl") < p_92115_2_.field_76303_c) {
               nbttagcompound.func_74777_a("lvl", (short)p_92115_2_.field_76303_c);
            }

            flag = false;
            break;
         }
      }

      if(flag) {
         NBTTagCompound nbttagcompound1 = new NBTTagCompound();
         nbttagcompound1.func_74777_a("id", (short)p_92115_2_.field_76302_b.field_77352_x);
         nbttagcompound1.func_74777_a("lvl", (short)p_92115_2_.field_76303_c);
         nbttaglist.func_74742_a(nbttagcompound1);
      }

      if(!p_92115_1_.func_77942_o()) {
         p_92115_1_.func_77982_d(new NBTTagCompound());
      }

      p_92115_1_.func_77978_p().func_74782_a("StoredEnchantments", nbttaglist);
   }

   public ItemStack func_92111_a(EnchantmentData p_92111_1_) {
      ItemStack itemstack = new ItemStack(this);
      this.func_92115_a(itemstack, p_92111_1_);
      return itemstack;
   }

   public WeightedRandomChestContent func_92114_b(Random p_92114_1_) {
      return this.func_92112_a(p_92114_1_, 1, 1, 1);
   }

   public WeightedRandomChestContent func_92112_a(Random p_92112_1_, int p_92112_2_, int p_92112_3_, int p_92112_4_) {
      ItemStack itemstack = new ItemStack(Items.field_151122_aG, 1, 0);
      EnchantmentHelper.func_77504_a(p_92112_1_, itemstack, 30);
      return new WeightedRandomChestContent(itemstack, p_92112_2_, p_92112_3_, p_92112_4_);
   }
}
