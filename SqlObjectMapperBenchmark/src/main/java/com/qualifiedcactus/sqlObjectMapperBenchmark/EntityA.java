package com.qualifiedcactus.sqlObjectMapperBenchmark;

import com.qualifiedcactus.sqlObjectMapper.fromRs.RsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "table_a")
public class EntityA {
    @Id
    @Column(name = "column_1", nullable = false)
    private Long column1;
    @Column(name = "column_2", nullable = false)
    private String column2;
    @Column(name = "column_3", nullable = false)
    private String column3;

    public Long getColumn1() {
        return column1;
    }

    public void setColumn1(Long column1) {
        this.column1 = column1;
    }

    public String getColumn2() {
        return column2;
    }

    public void setColumn2(String column2) {
        this.column2 = column2;
    }

    public String getColumn3() {
        return column3;
    }

    public void setColumn3(String column3) {
        this.column3 = column3;
    }

    public EntityA(Long column1, String column2, String column3) {
        this.column1 = column1;
        this.column2 = column2;
        this.column3 = column3;
    }

    @RsConstructor
    public EntityA() {
    }
}
