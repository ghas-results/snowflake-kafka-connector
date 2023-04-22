package com.snowflake.kafka.connector;

import static com.snowflake.kafka.connector.SnowflakeSinkConnectorConfig.BUFFER_COUNT_RECORDS;
import static com.snowflake.kafka.connector.SnowflakeSinkConnectorConfig.ERRORS_DEAD_LETTER_QUEUE_TOPIC_NAME_CONFIG;
import static com.snowflake.kafka.connector.SnowflakeSinkConnectorConfig.ERRORS_TOLERANCE_CONFIG;
import static com.snowflake.kafka.connector.SnowflakeSinkConnectorConfig.INGESTION_METHOD_OPT;

import com.snowflake.kafka.connector.dlq.InMemoryKafkaRecordErrorReporter;
import com.snowflake.kafka.connector.internal.SnowflakeConnectionService;
import com.snowflake.kafka.connector.internal.SnowflakeConnectionServiceV1;
import com.snowflake.kafka.connector.internal.TestUtils;
import com.snowflake.kafka.connector.internal.streaming.InMemorySinkTaskContext;
import com.snowflake.kafka.connector.internal.streaming.IngestionMethodConfig;
import com.snowflake.kafka.connector.internal.telemetry.SnowflakeTelemetryService;
import java.util.Collections;
import java.util.Map;
import net.snowflake.ingest.streaming.InsertValidationResponse;
import net.snowflake.ingest.streaming.SnowflakeStreamingIngestChannel;
import net.snowflake.ingest.streaming.SnowflakeStreamingIngestClient;
import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

/** Unit test for testing Snowflake Sink Task Behavior with Snowpipe Streaming */
public class SnowflakeSinkTaskStreamingTest {

  private String topicName;
  private static int partition = 0;
  private TopicPartition topicPartition;

  @Before
  public void setup() {
    topicName = TestUtils.randomTableName();
    topicPartition = new TopicPartition(topicName, partition);
  }

  @Test
  @Ignore
  public void testSinkTaskInvalidRecord_InMemoryDLQ() throws Exception {
    Map<String, String> config = TestUtils.getConfForStreaming();
    SnowflakeSinkConnectorConfig.setDefaultValues(config);
    config.put(BUFFER_COUNT_RECORDS, "1"); // override

    config.put(INGESTION_METHOD_OPT, IngestionMethodConfig.SNOWPIPE_STREAMING.toString());

    config.put(ERRORS_TOLERANCE_CONFIG, SnowflakeSinkConnectorConfig.ErrorTolerance.ALL.toString());
    config.put(ERRORS_DEAD_LETTER_QUEUE_TOPIC_NAME_CONFIG, "test_DLQ");

    InMemoryKafkaRecordErrorReporter errorReporter = new InMemoryKafkaRecordErrorReporter();

    SnowflakeConnectionService mockConnectionService =
        Mockito.mock(SnowflakeConnectionServiceV1.class);

    SnowflakeStreamingIngestClient mockStreamingClient =
        Mockito.mock(SnowflakeStreamingIngestClient.class);
    SnowflakeTelemetryService mockTelemetryService = Mockito.mock(SnowflakeTelemetryService.class);
    InMemorySinkTaskContext inMemorySinkTaskContext =
        new InMemorySinkTaskContext(Collections.singleton(topicPartition));

    SnowflakeStreamingIngestChannel mockStreamingChannel =
        Mockito.mock(SnowflakeStreamingIngestChannel.class);

    InsertValidationResponse validationResponse1 = new InsertValidationResponse();
    InsertValidationResponse.InsertError insertError =
        new InsertValidationResponse.InsertError("CONTENT", 0);
  }
}
