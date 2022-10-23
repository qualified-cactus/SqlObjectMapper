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
import sqlObjectMapper.Nested;

public class BeansWithNestedDto {

    public static class Entity8 implements IEntity8 {
        @Column(valueConverter = IntToStringValueConverter.class)
        private String col81;
        @Nested
        private Entity8Nested1 nested;

        public Entity8() {
        }

        public Entity8(String col81, Entity8Nested1 nested) {
            this.col81 = col81;
            this.nested = nested;
        }

        @Nullable
        @Override
        public String getCol81() {
            return col81;
        }

        public void setCol81(String col81) {
            this.col81 = col81;
        }

        @Nullable
        @Override
        public Entity8Nested1 getNested() {
            return nested;
        }

        public void setNested(Entity8Nested1 nested) {
            this.nested = nested;
        }
    }

    public static class Entity8Nested1 implements IEntity8Nested1 {
        private Integer col82;
        @Nested
        private Entity8Nested2 nested;

        public Entity8Nested1() {
        }

        public Entity8Nested1(Integer col82, Entity8Nested2 nested) {
            this.col82 = col82;
            this.nested = nested;
        }

        @Nullable
        @Override
        public Integer getCol82() {
            return col82;
        }

        public void setCol82(Integer col82) {
            this.col82 = col82;
        }

        @Nullable
        @Override
        public Entity8Nested2 getNested() {
            return nested;
        }

        public void setNested(Entity8Nested2 nested) {
            this.nested = nested;
        }
    }
    public static class Entity8Nested2 implements IEntity8Nested2 {
        private Integer col83;

        public Entity8Nested2() {
        }

        public Entity8Nested2(Integer col83) {
            this.col83 = col83;
        }

        @Nullable
        @Override
        public Integer getCol83() {
            return col83;
        }

        public void setCol83(Integer col83) {
            this.col83 = col83;
        }
    }
}
