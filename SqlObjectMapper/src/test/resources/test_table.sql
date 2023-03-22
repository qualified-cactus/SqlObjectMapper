CREATE TABLE value_extractor_test(
    id INTEGER PRIMARY KEY,

    long_column BIGINT,
    int_column INTEGER,
    double_column NUMERIC(16,16),
    short_column INTEGER,
    boolean_column TINYINT,
    byte_column TINYINT,

    big_decimal_column NUMERIC(16,16),
    byte_array_column BLOB,
    string_column VARCHAR(50),

    time_column TIME,
    date_column DATE,
    datetime_column TIMESTAMP,

    offset_datetime_column TIMESTAMP,
    offset_time_column TIME,
    instant_column TIMESTAMP,
    zone_datetime_column TIMESTAMP,

    enum_column VARCHAR(50)
);

--INSERT INTO value_extractor_test VALUES (
--    1,
--
--    10,
--    10,
--    10.5,
--    10,
--    1,
--    10
--
--
--)