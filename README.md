# Treetops

Fast LightGBM tree model interference Java library which is based on ASM dynamic code generation framework.

## Get Started

```java
    Predictor predictor = TreePredictorFactory.newInstance("model_v0", modelFilePath);
    predictor.predict(features);
```

## Core Idea

For example, let's say there is a tree config in a lightGBM model:

```
Tree=0
num_leaves=4
num_cat=0
split_feature=1 2 2
split_gain=0.568011 0.483606 0.45669
threshold=0.73144941452196321 0.90708366268745222 0.85551601478390116
decision_type=2 2 2
left_child=1 -1 -2
right_child=2 -3 -4
leaf_value=0.49510661266514339 0.50645382200299838 0.50688948369558862 0.49040602357823876
leaf_weight=326 114 39 21
leaf_count=326 114 39 21
internal_value=0.498415 0.496366 0.503957
internal_weight=0 365 135
internal_count=500 365 135
is_linear=0
shrinkage=1
```

The output of the decision value by this tree is based on every internal and leaf node's split strategy and value.

According to the config, we can see there are three internal nodes and four leave nodes, and if we store the tree in a tree-based data structure, in order to do the decision, we need to iterator from the tree root to the leave. There would be lots of memory access and function calls during the prediction process in a large tree. 

**<u>What treetops mainly do is translate the model file into a hardcode class instead of storing it in a tree-based data structure, and that's the core idea of treetops.</u>**

We can see the generated prediction method of this tree by treetops:

```
private tree_0([D)D
  ... ...
    IFEQ L0
    GOTO L1
   L0
   FRAME APPEND [D]
    DLOAD 2
    LDC 0.7314494145219632
    DCMPG
    IFGE L2
    GOTO L1
   L1

   ... ...

   L8
   FRAME SAME
    LDC 0.49040602357823876
    DRETURN
    MAXSTACK = 4
    MAXLOCALS = 4
```

the corresponding java code is:

```java
    private double tree_0(double[] var1) {
        double var2 = var1[1];
        if (var2 == var2 && !(var2 < 0.7314494145219632D)) {
            var2 = var1[2];
            return var2 == var2 && !(var2 < 0.8555160147839012D) ? 0.49040602357823876D : 0.5064538220029984D;
        } else {
            var2 = var1[2];
            return var2 == var2 && !(var2 < 0.9070836626874522D) ? 0.5068894836955886D : 0.4951066126651434D;
        }
    }
```

## Performance

Test Model: NycTaxi ( 100 Trees )

Environment: Intel(R) Xeon(R) Gold 6148 CPU @ 2.40GHz,  2 Core

Workflow: 20,000 times prediction warm-up, then test 100 times prediction elapsed time.

Result: 
```
SimplePredictor (tree-based data structure) : 738,283 ns
Treetops GeneratedPredictor : 60,620 ns
```

