/*
 MIT License

 Copyright (c) 2022 qualified-cactus

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package example;

import sqlObjectMapper.annotationProcessing.bean.BeanMappingProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import sqlObjectMapper.*;
import sqlObjectMapper.annotations.IgnoredProperty;
import sqlObjectMapper.annotations.JoinMany;
import sqlObjectMapper.annotations.MappedProperty;
import utils.TestUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JavaExampleTest {

    private static Connection connection;
    private static QueryExecutor queryExecutor;

    @BeforeAll
    public static void initConn() throws SQLException {
        queryExecutor = new QueryExecutor(new BeanMappingProvider());
        connection = TestUtils.createConn();
        try (var statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE entity_1 (
                    column_1 INTEGER PRIMARY KEY,
                    column_2 INTEGER,
                    column_3 INTEGER
                );
                
                CREATE TABLE entity_2 (
                    column_1 INTEGER PRIMARY KEY,
                    column_2 INTEGER,
                    column_3 INTEGER REFERENCES entity_1(column_1)
                );
                """.stripIndent()
            );
            statement.execute("""
                INSERT INTO entity_1 VALUES (1,2,3);
                INSERT INTO entity_1 VALUES (2,3,4);
                INSERT INTO entity_1 VALUES (3,4,5);
                
                INSERT INTO entity_2 VALUES (1,2,1);
                INSERT INTO entity_2 VALUES (2,3,1);
                INSERT INTO entity_2 VALUES (3,3,1);
                """.stripIndent()
            );
        }
    }
    @AfterAll
    public static void closeConn() throws SQLException {
        connection.close();
    }

    public static class ParamDto {
        private Integer param1;
        private Integer param2;

        public ParamDto() {
        }

        public ParamDto(Integer param1, Integer param2) {
            this.param1 = param1;
            this.param2 = param2;
        }

        public Integer getParam1() {
            return param1;
        }

        public void setParam1(Integer param1) {
            this.param1 = param1;
        }

        public Integer getParam2() {
            return param2;
        }

        public void setParam2(Integer param2) {
            this.param2 = param2;
        }
    }

    public static class Entity1 {
        @MappedProperty(isId = true)
        private Integer column1;
        private Integer column2;
        private Integer column3;
        @JoinMany
        private List<Entity2> entity2List;
        @IgnoredProperty
        private String ignoredProperty = "foo";

        public Integer getColumn1() {
            return column1;
        }

        public void setColumn1(Integer column1) {
            this.column1 = column1;
        }

        public Integer getColumn2() {
            return column2;
        }

        public void setColumn2(Integer column2) {
            this.column2 = column2;
        }

        public Integer getColumn3() {
            return column3;
        }

        public void setColumn3(Integer column3) {
            this.column3 = column3;
        }

        public List<Entity2> getEntity2List() {
            return entity2List;
        }

        public void setEntity2List(List<Entity2> entity2List) {
            this.entity2List = entity2List;
        }

        public String getIgnoredProperty() {
            return ignoredProperty;
        }

        public void setIgnoredProperty(String ignoredProperty) {
            this.ignoredProperty = ignoredProperty;
        }
    }

    public static class Entity2 {
        @MappedProperty(isId = true, name = "c1")
        private Integer column1;
        @MappedProperty(name = "c2")
        private Integer column2;

        public Integer getColumn1() {
            return column1;
        }

        public void setColumn1(Integer column1) {
            this.column1 = column1;
        }

        public Integer getColumn2() {
            return column2;
        }

        public void setColumn2(Integer column2) {
            this.column2 = column2;
        }
    }


    @Test
    public void toListExample() {
        final String sql = """
            SELECT
                e1.column_1, e1.column_2, e1.column_3,
                e2.column_1 c1, e2.column_2 c2
            FROM entity_1 e1
            LEFT JOIN entity_2 e2
                ON e1.column_1 = e2.column_3
            WHERE e1.column_1 <> :param_1 AND (e2.column_1 > :param_2 OR e2.column_1 IS NULL)
            ORDER BY e1.column_1 ASC, e2.column_1 ASC
            """.stripIndent();
        List<Entity1> entity1List = queryExecutor.queryForList(connection, sql, new ParamDto(2, 1), Entity1.class);

        assertEquals(2, entity1List.size());

        assertEquals(1, entity1List.get(0).getColumn1());
        assertEquals(2, entity1List.get(0).getEntity2List().size());

        assertEquals(3, entity1List.get(1).getColumn1());
        assertEquals(0, entity1List.get(1).getEntity2List().size());
    }

    @Test
    public void toObjectExample() {
        final String sql = """
            SELECT column_1 c1, column_2 c2 FROM entity_2 WHERE column_1 = :param_1 AND column_2 = :param_2
            """.stripIndent();

        final var entity2 = queryExecutor.queryForObject(connection, sql, new ParamDto(2,3), Entity2.class);
        assertEquals(2, entity2.column1);
    }

    @Test
    public void toScalarExample() {
        final String sql = """
            SELECT COUNT(*) FROM entity_2
            """.stripIndent();
        final Long count = queryExecutor.queryForScalar(connection, sql, null);
        assertEquals(3, count);


    }

}
