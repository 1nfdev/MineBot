package net.minecraft.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3i;

public enum EnumFacing implements IStringSerializable {
   DOWN(0, 1, -1, "down", EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.Y, new Vec3i(0, -1, 0)),
   UP(1, 0, -1, "up", EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.Y, new Vec3i(0, 1, 0)),
   NORTH(2, 3, 2, "north", EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.Z, new Vec3i(0, 0, -1)),
   SOUTH(3, 2, 0, "south", EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.Z, new Vec3i(0, 0, 1)),
   WEST(4, 5, 1, "west", EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.X, new Vec3i(-1, 0, 0)),
   EAST(5, 4, 3, "east", EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.X, new Vec3i(1, 0, 0));

   private final int field_176748_g;
   private final int field_176759_h;
   private final int field_176760_i;
   private final String field_176757_j;
   private final EnumFacing.Axis field_176758_k;
   private final EnumFacing.AxisDirection field_176755_l;
   private final Vec3i field_176756_m;
   private static final EnumFacing[] field_82609_l = new EnumFacing[6];
   private static final EnumFacing[] field_176754_o = new EnumFacing[4];
   private static final Map<String, EnumFacing> field_176761_p = Maps.<String, EnumFacing>newHashMap();

   private EnumFacing(int p_i46016_3_, int p_i46016_4_, int p_i46016_5_, String p_i46016_6_, EnumFacing.AxisDirection p_i46016_7_, EnumFacing.Axis p_i46016_8_, Vec3i p_i46016_9_) {
      this.field_176748_g = p_i46016_3_;
      this.field_176760_i = p_i46016_5_;
      this.field_176759_h = p_i46016_4_;
      this.field_176757_j = p_i46016_6_;
      this.field_176758_k = p_i46016_8_;
      this.field_176755_l = p_i46016_7_;
      this.field_176756_m = p_i46016_9_;
   }

   public int func_176745_a() {
      return this.field_176748_g;
   }

   public int func_176736_b() {
      return this.field_176760_i;
   }

   public EnumFacing.AxisDirection func_176743_c() {
      return this.field_176755_l;
   }

   public EnumFacing func_176734_d() {
      return func_82600_a(this.field_176759_h);
   }

   public EnumFacing func_176746_e() {
      switch(this) {
      case NORTH:
         return EAST;
      case EAST:
         return SOUTH;
      case SOUTH:
         return WEST;
      case WEST:
         return NORTH;
      default:
         throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
      }
   }

   public EnumFacing func_176735_f() {
      switch(this) {
      case NORTH:
         return WEST;
      case EAST:
         return NORTH;
      case SOUTH:
         return EAST;
      case WEST:
         return SOUTH;
      default:
         throw new IllegalStateException("Unable to get CCW facing of " + this);
      }
   }

   public int func_82601_c() {
      return this.field_176758_k == EnumFacing.Axis.X?this.field_176755_l.func_179524_a():0;
   }

   public int func_96559_d() {
      return this.field_176758_k == EnumFacing.Axis.Y?this.field_176755_l.func_179524_a():0;
   }

   public int func_82599_e() {
      return this.field_176758_k == EnumFacing.Axis.Z?this.field_176755_l.func_179524_a():0;
   }

   public String func_176742_j() {
      return this.field_176757_j;
   }

   public EnumFacing.Axis func_176740_k() {
      return this.field_176758_k;
   }

   public static EnumFacing func_82600_a(int p_82600_0_) {
      return field_82609_l[MathHelper.func_76130_a(p_82600_0_ % field_82609_l.length)];
   }

   public static EnumFacing func_176731_b(int p_176731_0_) {
      return field_176754_o[MathHelper.func_76130_a(p_176731_0_ % field_176754_o.length)];
   }

   public static EnumFacing func_176733_a(double p_176733_0_) {
      return func_176731_b(MathHelper.func_76128_c(p_176733_0_ / 90.0D + 0.5D) & 3);
   }

   public static EnumFacing func_176741_a(Random p_176741_0_) {
      return values()[p_176741_0_.nextInt(values().length)];
   }

   public String toString() {
      return this.field_176757_j;
   }

   public String func_176610_l() {
      return this.field_176757_j;
   }

   public static EnumFacing func_181076_a(EnumFacing.AxisDirection p_181076_0_, EnumFacing.Axis p_181076_1_) {
      for(EnumFacing enumfacing : values()) {
         if(enumfacing.func_176743_c() == p_181076_0_ && enumfacing.func_176740_k() == p_181076_1_) {
            return enumfacing;
         }
      }

      throw new IllegalArgumentException("No such direction: " + p_181076_0_ + " " + p_181076_1_);
   }

   static {
      for(EnumFacing enumfacing : values()) {
         field_82609_l[enumfacing.field_176748_g] = enumfacing;
         if(enumfacing.func_176740_k().func_176722_c()) {
            field_176754_o[enumfacing.field_176760_i] = enumfacing;
         }

         field_176761_p.put(enumfacing.func_176742_j().toLowerCase(), enumfacing);
      }

   }

   public static enum Axis implements Predicate<EnumFacing>, IStringSerializable {
      X("x", EnumFacing.Plane.HORIZONTAL),
      Y("y", EnumFacing.Plane.VERTICAL),
      Z("z", EnumFacing.Plane.HORIZONTAL);

      private static final Map<String, EnumFacing.Axis> field_176725_d = Maps.<String, EnumFacing.Axis>newHashMap();
      private final String field_176726_e;
      private final EnumFacing.Plane field_176723_f;

      private Axis(String p_i46015_3_, EnumFacing.Plane p_i46015_4_) {
         this.field_176726_e = p_i46015_3_;
         this.field_176723_f = p_i46015_4_;
      }

      public String func_176719_a() {
         return this.field_176726_e;
      }

      public boolean func_176720_b() {
         return this.field_176723_f == EnumFacing.Plane.VERTICAL;
      }

      public boolean func_176722_c() {
         return this.field_176723_f == EnumFacing.Plane.HORIZONTAL;
      }

      public String toString() {
         return this.field_176726_e;
      }

      public boolean apply(EnumFacing p_apply_1_) {
         return p_apply_1_ != null && p_apply_1_.func_176740_k() == this;
      }

      public EnumFacing.Plane func_176716_d() {
         return this.field_176723_f;
      }

      public String func_176610_l() {
         return this.field_176726_e;
      }

      static {
         for(EnumFacing.Axis enumfacing$axis : values()) {
            field_176725_d.put(enumfacing$axis.func_176719_a().toLowerCase(), enumfacing$axis);
         }

      }
   }

   public static enum AxisDirection {
      POSITIVE(1, "Towards positive"),
      NEGATIVE(-1, "Towards negative");

      private final int field_179528_c;
      private final String field_179525_d;

      private AxisDirection(int p_i46014_3_, String p_i46014_4_) {
         this.field_179528_c = p_i46014_3_;
         this.field_179525_d = p_i46014_4_;
      }

      public int func_179524_a() {
         return this.field_179528_c;
      }

      public String toString() {
         return this.field_179525_d;
      }
   }

   public static enum Plane implements Predicate<EnumFacing>, Iterable<EnumFacing> {
      HORIZONTAL,
      VERTICAL;

      public EnumFacing[] func_179516_a() {
         switch(this) {
         case HORIZONTAL:
            return new EnumFacing[]{EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};
         case VERTICAL:
            return new EnumFacing[]{EnumFacing.UP, EnumFacing.DOWN};
         default:
            throw new Error("Someone\'s been tampering with the universe!");
         }
      }

      public EnumFacing func_179518_a(Random p_179518_1_) {
         EnumFacing[] aenumfacing = this.func_179516_a();
         return aenumfacing[p_179518_1_.nextInt(aenumfacing.length)];
      }

      public boolean apply(EnumFacing p_apply_1_) {
         return p_apply_1_ != null && p_apply_1_.func_176740_k().func_176716_d() == this;
      }

      public Iterator<EnumFacing> iterator() {
         return Iterators.<EnumFacing>forArray(this.func_179516_a());
      }
   }
}
