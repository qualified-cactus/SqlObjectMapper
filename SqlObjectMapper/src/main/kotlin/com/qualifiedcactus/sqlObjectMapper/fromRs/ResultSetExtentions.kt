package com.qualifiedcactus.sqlObjectMapper.fromRs

import com.qualifiedcactus.sqlObjectMapper.SqlObjectMapperException
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.reflect.KClass

/**
 * Get value of the first column of the first row, then close [ResultSet]
 * @return null if there is no rows
 * @throws SQLException from JDBC
 */
fun <T : Any> ResultSet.toScalar(): T? = ResultSetParser.parseToScalar(this)

/**
 * Get columns' values of the first row as a DTO of type [clazz], then close [ResultSet]
 * @return null if there is no rows
 * @throws SqlObjectMapperException on invalid mapping
 * @throws SQLException from JDBC
 */
fun <T : Any> ResultSet.toObject(clazz: KClass<T>): T? = ResultSetParser.parseToObject(this, clazz)

/**
 * Get columns' values of all rows as a list of DTOs of type [clazz], then close [ResultSet]
 * @throws SqlObjectMapperException on invalid mapping
 * @throws SQLException from JDBC
 */
fun <T : Any> ResultSet.toList(clazz: KClass<T>): List<T> = ResultSetParser.parseToList(this, clazz)