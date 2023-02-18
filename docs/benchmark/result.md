# Benchmark Result

## Environment

- CPU: Intel(R) Xeon(R) Platinum 8372HC CPU @ 3.40GHz
- Memory: 8G
- Jdk: 1.8.0_362
- Tool: JMH

Test config : see class `io/github/treetops/benchmark/common/AverageTimeBenchmarkTemplate.java`

## BatchSize: 100

```
Benchmark                                                    Mode  Cnt        Score        Error  Units
i.g.t.b.breastcancer.GeneratedPredictorBenchmark.group       avgt   10    41839.248 ±   1021.564  ns/op
i.g.t.b.breastcancer.SimplePredictorBenchmark.group          avgt   10   496211.590 ± 144021.236  ns/op
i.g.t.b.californiahousing.GeneratedPredictorBenchmark.group  avgt   10    47484.875 ±   6165.425  ns/op
i.g.t.b.californiahousing.SimplePredictorBenchmark.group     avgt   10   681600.553 ± 257224.470  ns/op
i.g.t.b.diabetes.GeneratedPredictorBenchmark.group           avgt   10    42794.777 ±   4549.512  ns/op
i.g.t.b.diabetes.SimplePredictorBenchmark.group              avgt   10   465262.379 ± 193065.665  ns/op
i.g.t.b.wine.GeneratedPredictorBenchmark.group               avgt   10   111460.641 ±  12352.069  ns/op
i.g.t.b.wine.SimplePredictorBenchmark.group                  avgt   10  1200014.729 ± 283487.105  ns/op
```

## BatchSize: 500

```
Benchmark                                                    Mode  Cnt        Score         Error  Units
i.g.t.b.breastcancer.GeneratedPredictorBenchmark.group       avgt   10   208786.500 ±    7699.946  ns/op
i.g.t.b.breastcancer.SimplePredictorBenchmark.group          avgt   10  1994982.258 ±  403898.299  ns/op
i.g.t.b.californiahousing.GeneratedPredictorBenchmark.group  avgt   10   222625.586 ±    1881.976  ns/op
i.g.t.b.californiahousing.SimplePredictorBenchmark.group     avgt   10  3409444.662 ± 1336838.246  ns/op
i.g.t.b.diabetes.GeneratedPredictorBenchmark.group           avgt   10   147066.124 ±   15336.055  ns/op
i.g.t.b.diabetes.SimplePredictorBenchmark.group              avgt   10  2023224.922 ±  498936.117  ns/op
i.g.t.b.wine.GeneratedPredictorBenchmark.group               avgt   10   517825.494 ±   85232.860  ns/op
i.g.t.b.wine.SimplePredictorBenchmark.group                  avgt   10  5981068.592 ± 1483154.756  ns/op
```


## Graph

```python
from pyecharts.charts import Bar
from pyecharts import options as opts
from pyecharts.globals import ThemeType
from pyecharts.render import make_snapshot

bar = (
    Bar(init_opts=opts.InitOpts(theme=ThemeType.WESTEROS))
    .add_xaxis(["bc/100", "ch/100", "db/100", "wn/100","bc/500", "ch/500", "db/500", "wn/500"])
    .add_yaxis("asm",    [41,  47,  42,  111,  208,  222,  147,  517])
    .add_yaxis("simple", [496, 681, 465, 1200, 1994, 3409, 2023, 5981])
    .set_global_opts(title_opts=opts.TitleOpts(title="Average Latency (us)", subtitle="model_name/batch_size"))
)
bar.render()
```