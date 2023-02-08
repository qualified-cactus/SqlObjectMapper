package com.qualifiedcactus.sqlObjectMapperBenchmark;

import com.qualifiedcactus.sqlObjectMapper.NpStatements;
import com.qualifiedcactus.sqlObjectMapper.fromRs.ResultSetParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;

@Component
public class BenchmarkBean {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private Sql2o sql2o;

    @Autowired
    private EntityARepository aRepository;

    public Object sql2OBenchmark() {
        try (var conn = sql2o.open()) {
            return conn.createQuery("SELECT * FROM table_a")
                .executeAndFetch(EntityADto.class);
        }
    }

    public Object jpaBenchmark() {
        return aRepository.findAll();
    }

    public Object sqlObjectMapperBenchmark() throws SQLException {
        try (var conn = dataSource.getConnection()) {
            return NpStatements.prepareNpStatement(conn, "SELECT * FROM table_a")
                .useExecuteQuery(rs-> ResultSetParser.parseToList(rs, EntityA.class));
        }
    }

    public Object plainSqlBenchmark() throws SQLException {
        var result = new ArrayList<EntityA>();

        try (var conn = dataSource.getConnection()) {
            try (var stmt = conn.prepareStatement("SELECT * FROM table_a")) {
                try (var rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        result.add(new EntityA(
                            (Long)rs.getObject(1),
                            (String)rs.getObject(2),
                            (String)rs.getObject(3)
                        ));
                    }
                }
            }
        }
        return result;
    }
}
