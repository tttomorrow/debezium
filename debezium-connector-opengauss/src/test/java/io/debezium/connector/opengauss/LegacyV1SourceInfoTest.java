/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.connector.opengauss;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.junit.Before;
import org.junit.Test;

import io.debezium.config.CommonConnectorConfig.Version;
import io.debezium.config.Configuration;
import io.debezium.doc.FixFor;
import io.debezium.relational.TableId;
import io.debezium.time.Conversions;

/**
 * @author Jiri Pechanec
 *
 */
public class LegacyV1SourceInfoTest {

    private SourceInfo source;

    @Before
    public void beforeEach() {
        source = new SourceInfo(new OpengaussConnectorConfig(
                Configuration.create()
                        .with(OpengaussConnectorConfig.SERVER_NAME, "serverX")
                        .with(OpengaussConnectorConfig.DATABASE_NAME, "serverX")
                        .with(OpengaussConnectorConfig.SOURCE_STRUCT_MAKER_VERSION, Version.V1)
                        .build()));
        source.update(Conversions.toInstantFromMicros(123_456_789L), new TableId("catalogNameX", "schemaNameX", "tableNameX"));
    }

    @Test
    public void versionIsPresent() {
        assertThat(source.struct().getString(SourceInfo.DEBEZIUM_VERSION_KEY)).isEqualTo(Module.version());
    }

    @Test
    public void connectorIsPresent() {
        assertThat(source.struct().getString(SourceInfo.DEBEZIUM_CONNECTOR_KEY)).isEqualTo(Module.name());
    }

    @Test
    @FixFor("DBZ-934")
    public void canHandleNullValues() {
        source.update(null, null, null, null, null);
    }

    @Test
    public void shouldHaveTimestamp() {
        assertThat(source.struct().getInt64(SourceInfo.TIMESTAMP_USEC_KEY)).isEqualTo(123_456_789L);
    }

    @Test
    public void schemaIsCorrect() {
        final Schema schema = SchemaBuilder.struct()
                .name("io.debezium.connector.postgresql.Source")
                .field("version", Schema.OPTIONAL_STRING_SCHEMA)
                .field("connector", Schema.OPTIONAL_STRING_SCHEMA)
                .field("name", Schema.STRING_SCHEMA)
                .field("db", Schema.STRING_SCHEMA)
                .field("ts_usec", Schema.OPTIONAL_INT64_SCHEMA)
                .field("txId", Schema.OPTIONAL_INT64_SCHEMA)
                .field("lsn", Schema.OPTIONAL_INT64_SCHEMA)
                .field("schema", Schema.OPTIONAL_STRING_SCHEMA)
                .field("table", Schema.OPTIONAL_STRING_SCHEMA)
                .field("snapshot", SchemaBuilder.bool().optional().defaultValue(false).build())
                .field("last_snapshot_record", Schema.OPTIONAL_BOOLEAN_SCHEMA)
                .field("xmin", Schema.OPTIONAL_INT64_SCHEMA)
                .build();

        assertThat(source.struct().schema()).isEqualTo(schema);
    }
}
