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

import sqlObjectMapper.annotations.*


class KotlinDataClassDtoCompositeKey {
    data class Entity1(
        @IgnoredProperty
        override val ignored: Int? = null,
        @MappedProperty(isId = true)
        override val col11: Int,
        @MappedProperty(isId = true)
        override val col12: Int,
        override val col13: String,
        @JoinOne
        override val entity2: Entity2?
    ) : IEntity1

    data class Entity2(
        @MappedProperty(isId = true)
        override val col21: Int,
        @MappedProperty(isId = true)
        override val col22: Int,
        override val col23: String,
        @JoinMany
        override val entity3List: MutableList<Entity3>
    ) : IEntity2

    data class Entity3(
        @MappedProperty(isId = true)
        override val col31: Int,
        @MappedProperty(isId = true)
        override val col32: Int,
        override val col33: String
    ) : IEntity3
}

class KotlinDataClassDtoSingleKey {

    data class Entity4(
        @MappedProperty(isId = true)
        override val col41: Int,
        @JoinMany
        override val e5List: MutableList<Entity5>?
    ) : IEntity4

    data class Entity5(
        @MappedProperty(isId = true)
        override val col51: Int,
        @JoinMany
        override val e6List: MutableList<Entity6>?,
        @JoinMany
        override val e7List: MutableList<Entity7>?
    ) : IEntity5

    data class Entity6(
        @MappedProperty(isId = true)
        override val col61: Int,
    ) : IEntity6

    data class Entity7(
        @MappedProperty(isId = true)
        override val col71: Int,
    ) : IEntity7
}


class KotlinDataClassWithNested {

    data class Entity8(
        @MappedProperty(valueConverter = IntToStringValueConverter::class)
        override val col81: String?,
        @Nested
        override val nested: Entity8Nested1?
    ) : IEntity8

    data class Entity8Nested1(
        override val col82: Int?,
        @Nested
        override val nested: Entity8Nested2?
    ) : IEntity8Nested1

    data class Entity8Nested2(
        override val col83: Int?
    ) : IEntity8Nested2

}