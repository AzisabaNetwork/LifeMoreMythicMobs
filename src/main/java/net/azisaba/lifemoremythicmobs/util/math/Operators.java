package net.azisaba.lifemoremythicmobs.util.math;

import net.objecthunter.exp4j.operator.Operator;

public final class Operators {
   public static final int PRECEDENCE_LESS_THAN = 499;
   public static final int PRECEDENCE_LESS_THAN_EQ = 499;
   public static final int PRECEDENCE_GREATER_THAN = 499;
   public static final int PRECEDENCE_GREATER_THAN_EQ = 499;
   public static final int PRECEDENCE_EQ = 499;
   public static final Operator lessThan = new Operator("<", 2, true, 499) {
      public double apply(double[] values) {
         return values[0] < values[1] ? 1.0 : 0.0;
      }
   };
   public static final Operator lessThanEq = new Operator("<=", 2, true, 499) {
      public double apply(double[] values) {
         return values[0] <= values[1] ? 1.0 : 0.0;
      }
   };
   public static final Operator greaterThan = new Operator(">", 2, true, 499) {
      public double apply(double[] values) {
         return values[0] > values[1] ? 1.0 : 0.0;
      }
   };
   public static final Operator greaterThanEq = new Operator(">=", 2, true, 499) {
      public double apply(double[] values) {
         return values[0] >= values[1] ? 1.0 : 0.0;
      }
   };
   public static final Operator equals = new Operator("=", 2, true, 499) {
      public double apply(double[] values) {
         return values[0] == values[1] ? 1.0 : 0.0;
      }
   };
   public static final Operator[] operators = new Operator[]{lessThan, lessThanEq, greaterThan, greaterThanEq, equals};
}
