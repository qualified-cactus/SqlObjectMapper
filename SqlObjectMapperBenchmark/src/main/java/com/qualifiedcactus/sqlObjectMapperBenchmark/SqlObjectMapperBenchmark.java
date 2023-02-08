package com.qualifiedcactus.sqlObjectMapperBenchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.sql2o.Sql2o;

import javax.sql.DataSource;

@SpringBootApplication
@Configuration
public class SqlObjectMapperBenchmark {

    @Autowired
    private DataSource dataSource;

    @Bean @Scope("singleton")
    public Sql2o sql2o() {
        return new Sql2o(dataSource);
    }



    public static void main(String[] args) throws Exception {

        var opt = new OptionsBuilder()
            .include(SqlBenchmark.class.getSimpleName())
            .forks(1)
            .warmupIterations(1)
            .measurementIterations(5)
            .build();
        new Runner(opt).run();

    }
}
