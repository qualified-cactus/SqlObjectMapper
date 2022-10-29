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

import sqlObjectMapper.annotations.MappedProperty;
import sqlObjectMapper.annotations.IgnoredProperty;
import sqlObjectMapper.annotations.JoinMany;
import sqlObjectMapper.annotations.JoinOne;

import java.util.List;

public class BeansWithCompositeKeyDto {

    public static class Entity1 implements IEntity1{
        @IgnoredProperty
        private Integer ignored;
        @MappedProperty(isId = true)
        private Integer col11;
        @MappedProperty(isId = true)
        private Integer col12;
        private String col13;
        @JoinOne
        private Entity2 entity2;

        public Integer getIgnored() {
            return ignored;
        }

        public void setIgnored(Integer ignored) {
            this.ignored = ignored;
        }

        public Integer getCol11() {
            return col11;
        }

        public void setCol11(Integer col11) {
            this.col11 = col11;
        }

        public Integer getCol12() {
            return col12;
        }

        public void setCol12(Integer col12) {
            this.col12 = col12;
        }

        public String getCol13() {
            return col13;
        }

        public void setCol13(String col13) {
            this.col13 = col13;
        }

        public Entity2 getEntity2() {
            return entity2;
        }

        public void setEntity2(Entity2 entity2) {
            this.entity2 = entity2;
        }
    }

    public static class Entity2 implements IEntity2 {
        @MappedProperty(isId = true)
        private Integer col21;
        @MappedProperty(isId = true)
        private Integer col22;
        private String col23;
        @JoinMany
        private List<Entity3> entity3List;

        public Integer getCol21() {
            return col21;
        }

        public void setCol21(Integer col21) {
            this.col21 = col21;
        }

        public Integer getCol22() {
            return col22;
        }

        public void setCol22(Integer col22) {
            this.col22 = col22;
        }

        public String getCol23() {
            return col23;
        }

        public void setCol23(String col23) {
            this.col23 = col23;
        }

        public List<Entity3> getEntity3List() {
            return entity3List;
        }

        public void setEntity3List(List<Entity3> entity3List) {
            this.entity3List = entity3List;
        }
    }

    public static class Entity3 implements IEntity3 {
        @MappedProperty(isId = true)
        private Integer col31;
        @MappedProperty(isId = true)
        private Integer col32;
        private String col33;

        public Integer getCol31() {
            return col31;
        }

        public void setCol31(Integer col31) {
            this.col31 = col31;
        }

        public Integer getCol32() {
            return col32;
        }

        public void setCol32(Integer col32) {
            this.col32 = col32;
        }

        public String getCol33() {
            return col33;
        }

        public void setCol33(String col33) {
            this.col33 = col33;
        }
    }
}
