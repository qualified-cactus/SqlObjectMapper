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

package com.qualifiedcactus.sqlObjectMapper.fromRs

import java.util.*
import kotlin.math.ceil

/**
 * Use to contain paged content. Almost similar to Spring Data's `Page<T>`
 */
class RsPage<T:Any>(
    val content: List<T>,
    /**
     * A page number, starting from 0
     */
    val page: Int,
    /**
     * Size of a page
     */
    val size: Int,
    val totalElements: Long
) {

    /**
     * Create an empty page
     */
    constructor() : this(Collections.emptyList(), 0, 0, 0)


    val first: Boolean = page == 0
    val last: Boolean = (page + 1) * size >= totalElements
    val totalPages: Long = ceil(totalElements.toDouble() / size).toLong()

    fun <TargetType:Any> map(mapFunction: (element: T)->TargetType): RsPage<TargetType> {
        return RsPage(
            content.map(mapFunction),
            page,
            size,
            totalElements
        )
    }


}