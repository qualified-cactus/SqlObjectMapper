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
import sqlObjectMapper.annotations.MappedProperty;
import sqlObjectMapper.annotations.JoinMany;

import java.util.List;

public class BeansWithSingleKeyDto {

    public static class Entity4 implements IEntity4 {
        @MappedProperty(isId = true)
        private Integer col41;
        @JoinMany
        private List<Entity5> e5List;

        @Nullable
        @Override
        public Integer getCol41() {
            return col41;
        }

        public void setCol41(Integer col41) {
            this.col41 = col41;
        }

        @Nullable
        @Override
        public List<Entity5> getE5List() {
            return e5List;
        }

        public void setE5List(List<Entity5> e5List) {
            this.e5List = e5List;
        }
    }

    public static class Entity5 implements IEntity5 {
        @MappedProperty(isId = true)
        private Integer col51;
        @JoinMany
        private List<Entity6> e6List;
        @JoinMany
        private List<Entity7> e7List;

        @Nullable
        @Override
        public Integer getCol51() {
            return col51;
        }

        public void setCol51(Integer col51) {
            this.col51 = col51;
        }

        @Nullable
        @Override
        public List<Entity6> getE6List() {
            return e6List;
        }

        public void setE6List(List<Entity6> e6List) {
            this.e6List = e6List;
        }

        @Nullable
        @Override
        public List<Entity7> getE7List() {
            return e7List;
        }

        public void setE7List(List<Entity7> e7List) {
            this.e7List = e7List;
        }
    }

    public static class Entity6 implements IEntity6 {
        @MappedProperty(isId = true)
        private Integer col61;

        @Nullable
        @Override
        public Integer getCol61() {
            return col61;
        }

        public void setCol61(Integer col61) {
            this.col61 = col61;
        }
    }

    public static class Entity7 implements IEntity7 {
        @MappedProperty(isId = true)
        private Integer col71;

        @Nullable
        @Override
        public Integer getCol71() {
            return col71;
        }

        public void setCol71(Integer col71) {
            this.col71 = col71;
        }
    }
}
