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

package mappedResultSetTest;


import org.jetbrains.annotations.Nullable;
import sqlObjectMapper.Column;
import sqlObjectMapper.IgnoredProperty;
import sqlObjectMapper.LeftJoinedMany;
import sqlObjectMapper.LeftJoinedOne;

import java.util.List;

public class RecordsWithCompositeKeyDto {


    public record Entity1(
        @IgnoredProperty
        Integer ignored,
        @Column(isId = true)
        Integer col11,
        @Column(isId = true)
        Integer col12,
        String col13,
        @LeftJoinedOne
        Entity2 entity2
    ) implements IEntity1 {
        @Nullable
        @Override
        public Integer getIgnored() {
            return ignored;
        }

        @Nullable
        @Override
        public Integer getCol11() {
            return col11;
        }

        @Nullable
        @Override
        public Integer getCol12() {
            return col12;
        }

        @Nullable
        @Override
        public String getCol13() {
            return col13;
        }

        @Nullable
        @Override
        public IEntity2 getEntity2() {
            return entity2;
        }
    }

    public record Entity2(
        @Column(isId = true)
        Integer col21,
        @Column(isId = true)
        Integer col22,
        String col23,
        @LeftJoinedMany
        List<Entity3> entity3List
    ) implements IEntity2 {
        @Nullable
        @Override
        public Integer getCol21() {
            return col21;
        }

        @Nullable
        @Override
        public Integer getCol22() {
            return col22;
        }

        @Nullable
        @Override
        public String getCol23() {
            return col23;
        }

        @Nullable
        @Override
        public List<? extends IEntity3> getEntity3List() {
            return entity3List;
        }
    }

    public record Entity3(
        @Column(isId = true)
        Integer col31,
        @Column(isId = true)
        Integer col32,
        String col33
    ) implements IEntity3 {
        @Nullable
        @Override
        public Integer getCol31() {
            return col31;
        }

        @Nullable
        @Override
        public Integer getCol32() {
            return col32;
        }

        @Nullable
        @Override
        public String getCol33() {
            return col33;
        }
    }
}
