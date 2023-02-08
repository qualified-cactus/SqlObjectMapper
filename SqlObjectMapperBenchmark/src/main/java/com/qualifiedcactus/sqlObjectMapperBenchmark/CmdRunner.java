package com.qualifiedcactus.sqlObjectMapperBenchmark;

import jakarta.persistence.EntityManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Component
public class CmdRunner implements CommandLineRunner {

    private final EntityManager entityManager;
    private final EntityARepository aRepo;
    private final EntityBRepository bRepo;
    private final DataSource dataSource;

    public CmdRunner(EntityManager entityManager, EntityARepository aRepo, EntityBRepository bRepo, DataSource dataSource) {
        this.entityManager = entityManager;
        this.aRepo = aRepo;
        this.bRepo = bRepo;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        seedData();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void seedData() {

        for (int i = 0; i < 100; i++) {
            var a = new EntityA(
                (long) i, "foo", "bar"
            );
            a = aRepo.save(a);
            for (int j = 0; j < 10; j++) {
                var b = new EntityB(
                    (long)j, "foo", a
                );
                b = bRepo.save(b);
            }
        }
    }
}
