package com.qualifiedcactus.sqlObjectMapperBenchmark;


import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class SqlBenchmark {

    private ConfigurableApplicationContext appContext;
    private BenchmarkBean benchmarkBean;


    @Setup
    public void setUp() {
        var app = new SpringApplication(SqlObjectMapperBenchmark.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.setBannerMode(Banner.Mode.OFF);
        appContext = app.run();
        benchmarkBean = appContext.getBean(BenchmarkBean.class);
    }


    @TearDown
    public void tearDown() {
        appContext.stop();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void sql2oBenchmark(Blackhole blackhole) {
        blackhole.consume(
            benchmarkBean.sql2OBenchmark()
        );
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void springJpaBenchmark(Blackhole blackhole) {
        blackhole.consume(
            benchmarkBean.jpaBenchmark()
        );
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void plainSqlBenchmark(Blackhole blackhole) throws SQLException {
        blackhole.consume(
            benchmarkBean.plainSqlBenchmark()
        );
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void sqlObjectMapperBenchmark(Blackhole blackhole) throws SQLException {
        blackhole.consume(
            benchmarkBean.sqlObjectMapperBenchmark()
        );
    }

}
