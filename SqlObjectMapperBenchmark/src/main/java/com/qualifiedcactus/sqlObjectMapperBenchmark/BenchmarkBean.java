/*
 * MIT License
 *
 * Copyright (c) 2023 qualified-cactus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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
