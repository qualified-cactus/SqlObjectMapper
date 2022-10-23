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

package mappedResultSetTest


// entity_1 }o--o| entity_2 |o--o{ entity_3
val schemaWithCompositeKey = """
    CREATE TABLE entity_2 (
        col_21 INTEGER NOT NULL,
        col_22 INTEGER NOT NULL,
        PRIMARY KEY(col_21, col_22),
        col_23 VARCHAR(50)
    );
    
    CREATE TABLE entity_1 (
        col_11 INTEGER NOT NULL,
        col_12 INTEGER NOT NULL,
        PRIMARY KEY(col_11, col_12),
        
        col_13 VARCHAR(50),
        
        col_14 INTEGER,
        col_15 INTEGER,
        FOREIGN KEY (col_14, col_15) REFERENCES entity_2(col_21, col_22)
    );
    
    CREATE TABLE entity_3 (
        col_31 INTEGER NOT NULL,
        col_32 INTEGER NOT NULL,
        PRIMARY KEY(col_31, col_32),
        
        col_33 VARCHAR(50),
        
        col_34 INTEGER,
        col_35 INTEGER,
        FOREIGN KEY (col_34, col_35) REFERENCES entity_2(col_21, col_22)
    );
    
""".trimIndent()

val schemaWithCompositeKeySeedData ="""
    INSERT INTO entity_2 VALUES (1,2,'entity 1');
    INSERT INTO entity_2 VALUES (2,3,'entity 2');
    INSERT INTO entity_2 VALUES (3,4,'entity 3');
    
    INSERT INTO entity_1 VALUES(1,2,'entity 1',1,2);
    INSERT INTO entity_1 VALUES(2,3,'entity 2',3,4);
    INSERT INTO entity_1 VALUES(3,4,'entity 3',2,3);
    INSERT INTO entity_1 VALUES(4,5,'entity 4',null,null);
    
    INSERT INTO entity_3 VALUES (1,2,'entity 1', 1,2);
    INSERT INTO entity_3 VALUES (2,3,'entity 2', 3,4);
    INSERT INTO entity_3 VALUES (4,5,'entity 3', 3,4);
""".trimIndent()

val testQuery1 = """
    SELECT * 
    FROM entity_1 e1
    LEFT JOIN entity_2 e2 ON (e1.col_14 = e2.col_21 AND e1.col_15 = e2.col_22)
    LEFT JOIN entity_3 e3 ON (e2.col_21 = e3.col_34 AND e2.col_22 = e3.col_35)
    ORDER BY e1.col_11 ASC, e2.col_21 ASC, e3.col_31 ASC
""".trimIndent()




interface IEntity1 {
    val ignored: Int?
    val col11: Int?
    val col12: Int?
    val col13: String?
    val entity2: IEntity2?
}

interface IEntity2 {
    val col21: Int?
    val col22: Int?
    val col23: String?
    val entity3List: MutableList<out IEntity3>?
}

interface IEntity3 {
    val col31: Int?
    val col32: Int?
    val col33: String?
}

val schemaWithSingleKey = """
    CREATE TABLE entity_4 (
        col_41 INTEGER PRIMARY KEY
    );
    CREATE TABLE entity_5 (
        col_51 INTEGER PRIMARY KEY,
        col_52 INTEGER REFERENCES entity_4(col_41)
    );
    CREATE TABLE entity_6 (
        col_61 INTEGER PRIMARY KEY,
        col_62 INTEGER REFERENCES entity_5(col_51)
    );
    CREATE TABLE entity_7 (
        col_71 INTEGER PRIMARY KEY,
        col_72 INTEGER REFERENCES entity_5(col_51)
    );
""".trimIndent()

val schemaWithSingleKeySeedData = """
    INSERT INTO entity_4 VALUES(1);
    INSERT INTO entity_4 VALUES(2);
    INSERT INTO entity_4 VALUES(3);
    
    INSERT INTO entity_5 VALUES(1, 1);
    INSERT INTO entity_5 VALUES(2, 1);
    INSERT INTO entity_5 VALUES(3, 3);
    
    INSERT INTO entity_6 VALUES(1, 2);
    INSERT INTO entity_6 VALUES(2, 2);
    INSERT INTO entity_6 VALUES(3, 2);
    
    INSERT INTO entity_7 VALUES(1, 2);
    INSERT INTO entity_7 VALUES(2, 2);
    INSERT INTO entity_7 VALUES(3, 2);
""".trimIndent()

interface IEntity4 {
    val col41: Int?
    val e5List: MutableList<out IEntity5>?
}

interface IEntity5 {
    val col51: Int?
    val e6List: MutableList<out IEntity6>?
    val e7List: MutableList<out IEntity7>?
}

interface IEntity6 {
    val col61: Int?
}

interface IEntity7 {
    val col71: Int?
}

val testQuery2 ="""
    SELECT *
    FROM entity_4 e4
    LEFT JOIN entity_5 e5 ON e4.col_41 = e5.col_52
    LEFT JOIN entity_6 e6 ON e5.col_51 = e6.col_62
    LEFT JOIN entity_7 e7 ON e5.col_51 = e7.col_72
    ORDER BY e4.col_41 ASC, e5.col_51 ASC, e6.col_61 ASC, e7.col_71 ASC
""".trimIndent()



val schemaForNestedTest ="""
    CREATE TABLE entity_8 (
        col_81 INTEGER PRIMARY KEY,
        col_82 INTEGER,
        col_83 INTEGER
    );
""".trimIndent()

val schemaForNestedTestSeedData ="""
    INSERT INTO entity_8 VALUES (1,2,3);
    INSERT INTO entity_8 VALUES (2,3,4);
    INSERT INTO entity_8 VALUES (3,4,5);
""".trimIndent()

val testQuery3 = """
    SELECT * FROM entity_8 ORDER BY col_81 ASC
""".trimIndent()

interface IEntity8 {
    val col81: String?
    val nested: IEntity8Nested1?
}

interface IEntity8Nested1 {
    val col82: Int?
    val nested: IEntity8Nested2?
}

interface IEntity8Nested2 {
    val col83: Int?
}

