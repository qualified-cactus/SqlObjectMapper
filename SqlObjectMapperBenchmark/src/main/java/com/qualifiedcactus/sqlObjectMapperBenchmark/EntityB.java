package com.qualifiedcactus.sqlObjectMapperBenchmark;

import jakarta.persistence.*;

@Entity
@Table(name = "table_b")
public class EntityB {
    @Id
    @Column(name = "column_4", nullable = false)
    private Long column4;
    @Column(name = "column_5", nullable = false)
    private String column5;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "a_id", referencedColumnName = "column_1")
    private EntityA entityA;

    public Long getColumn4() {
        return column4;
    }

    public void setColumn4(Long column4) {
        this.column4 = column4;
    }

    public String getColumn5() {
        return column5;
    }

    public void setColumn5(String column5) {
        this.column5 = column5;
    }

    public EntityA getEntityA() {
        return entityA;
    }

    public void setEntityA(EntityA entityA) {
        this.entityA = entityA;
    }

    public EntityB(Long column4, String column5, EntityA entityA) {
        this.column4 = column4;
        this.column5 = column5;
        this.entityA = entityA;
    }

    public EntityB() {
    }
}
