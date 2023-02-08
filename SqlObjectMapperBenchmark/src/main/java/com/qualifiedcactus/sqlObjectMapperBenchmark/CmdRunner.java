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
