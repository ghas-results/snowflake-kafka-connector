package com.snowflake.kafka.connector;

import com.snowflake.kafka.connector.internal.SnowflakeErrors;
import com.snowflake.kafka.connector.internal.TestUtils;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.MDC;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class UtilsTest {
  @Rule public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @Test
  public void testGetTopicToTableMap() {
    // no map
    Map<String, String> config = new HashMap<>();
    Map<String, String> result = SnowflakeSinkTask.getTopicToTableMap(config);
    assert result.isEmpty();

    // has map
    config.put(SnowflakeSinkConnectorConfig.TOPICS_TABLES_MAP, "aaa:bbb," + "ccc:ddd");
    result = SnowflakeSinkTask.getTopicToTableMap(config);
    assert result.size() == 2;
    assert result.containsKey("aaa");
    assert result.get("aaa").equals("bbb");
    assert result.containsKey("ccc");
    assert result.get("ccc").equals("ddd");

    // has map, but invalid data
    config.put(SnowflakeSinkConnectorConfig.TOPICS_TABLES_MAP, "12321");
    result = SnowflakeSinkTask.getTopicToTableMap(config);
    assert result.isEmpty();
  }

  @Test
  public void testObjectIdentifier() {
    String name = "DATABASE.SCHEMA.TABLE";
    assert !Utils.isValidSnowflakeObjectIdentifier(name);
    String name1 = "table!@#$%^;()";
    assert !Utils.isValidSnowflakeObjectIdentifier(name1);
  }

  @Test
  public void testVersionChecker() {
    assert Utils.checkConnectorVersion();
  }

  @Test
  public void testParseTopicToTable() {
    TestUtils.assertError(SnowflakeErrors.ERROR_0021, () -> Utils.parseTopicToTableMap("adsadas"));

    TestUtils.assertError(
        SnowflakeErrors.ERROR_0021, () -> Utils.parseTopicToTableMap("abc:@123,bvd:adsa"));
  }

  @Test
  public void testTableName() {
    Map<String, String> topic2table = Utils.parseTopicToTableMap("ab@cd:abcd, 1234:_1234");

    assert Utils.tableName("ab@cd", topic2table).equals("abcd");
    assert Utils.tableName("1234", topic2table).equals("_1234");

    TestUtils.assertError(SnowflakeErrors.ERROR_0020, () -> Utils.tableName("", topic2table));
    TestUtils.assertError(SnowflakeErrors.ERROR_0020, () -> Utils.tableName(null, topic2table));

    String topic = "bc*def";
    assert Utils.tableName(topic, topic2table).equals("bc_def_" + Math.abs(topic.hashCode()));

    topic = "12345";
    assert Utils.tableName(topic, topic2table).equals("_12345_" + Math.abs(topic.hashCode()));
  }

  @Test
  public void testTableFullName() {
    assert Utils.isValidSnowflakeTableName("_1342dfsaf$");
    assert Utils.isValidSnowflakeTableName("dad._1342dfsaf$");
    assert Utils.isValidSnowflakeTableName("adsa123._gdgsdf._1342dfsaf$");
    assert !Utils.isValidSnowflakeTableName("_13)42dfsaf$");
    assert !Utils.isValidSnowflakeTableName("_13.42dfsaf$");
    assert !Utils.isValidSnowflakeTableName("_1342.df.sa.f$");
  }

  @Test
  public void testConvertAppName() {
    HashMap<String, String> config = new HashMap<String, String>();

    config.put(SnowflakeSinkConnectorConfig.NAME, "_aA1");
    Utils.convertAppName(config);
    assert config.get(SnowflakeSinkConnectorConfig.NAME).equals("_aA1");

    config.put(SnowflakeSinkConnectorConfig.NAME, "-_aA1");
    Utils.convertAppName(config);
    assert config.get(SnowflakeSinkConnectorConfig.NAME).equals("___aA1_44483871");

    config.put(SnowflakeSinkConnectorConfig.NAME, "_aA1-");
    Utils.convertAppName(config);
    assert config.get(SnowflakeSinkConnectorConfig.NAME).equals("_aA1__90688251");

    config.put(SnowflakeSinkConnectorConfig.NAME, "testApp.snowflake-connector");
    Utils.convertAppName(config);
    assert config
        .get(SnowflakeSinkConnectorConfig.NAME)
        .equals("testApp_snowflake_connector_36242259");
  }

  @Test
  public void testIsValidSnowflakeApplicationName() {
    assert Utils.isValidSnowflakeApplicationName("-_aA1");
    assert Utils.isValidSnowflakeApplicationName("aA_1-");
    assert !Utils.isValidSnowflakeApplicationName("1aA_-");
    assert !Utils.isValidSnowflakeApplicationName("_1.a$");
    assert !Utils.isValidSnowflakeApplicationName("(1.f$-_");
  }

  @Test
  public void testSetJDBCLoggingDir() {
    String defaultTmpDir = System.getProperty(Utils.JAVA_IO_TMPDIR);

    Utils.setJDBCLoggingDirectory();
    assert !System.getProperty(Utils.JAVA_IO_TMPDIR).isEmpty();

    environmentVariables.set(
        SnowflakeSinkConnectorConfig.SNOWFLAKE_JDBC_LOG_DIR, "/dummy_dir_not_exist");
    Utils.setJDBCLoggingDirectory();
    assert !System.getProperty(Utils.JAVA_IO_TMPDIR).equals("/dummy_dir_not_exist");

    environmentVariables.set(SnowflakeSinkConnectorConfig.SNOWFLAKE_JDBC_LOG_DIR, "/usr");
    Utils.setJDBCLoggingDirectory();
    assert System.getProperty(Utils.JAVA_IO_TMPDIR).equals("/usr");

    environmentVariables.set(SnowflakeSinkConnectorConfig.SNOWFLAKE_JDBC_LOG_DIR, defaultTmpDir);
    Utils.setJDBCLoggingDirectory();
    assert System.getProperty(Utils.JAVA_IO_TMPDIR).equals(defaultTmpDir);
  }

  @Test
  public void testLogMessageBasic() {
    // no variable
    String expected = Utils.SF_LOG_TAG + " test message";

    assert Utils.formatLogMessage("test message").equals(expected);

    // 1 variable
    expected = Utils.SF_LOG_TAG + " 1 test message";

    assert Utils.formatLogMessage("{} test message", 1).equals(expected);
  }

  @Test
  public void testLogMessageNulls() {
    // nulls
    String expected = Utils.SF_LOG_TAG + " null test message";
    assert Utils.formatLogMessage("{} test message", (String) null).equals(expected);

    expected = Utils.SF_LOG_TAG + " some string test null message null";
    assert Utils.formatLogMessage("{} test {} message {}", "some string", null, null)
        .equals(expected);
  }

  @Test
  public void testLogMessageMultiLines() {
    // 2 variables
    String expected = Utils.SF_LOG_TAG + " 1 test message\n" + "2 test message";

    System.out.println(Utils.formatLogMessage("{} test message\n{} test message", 1, 2));

    assert Utils.formatLogMessage("{} test message\n{} test message", 1, 2).equals(expected);

    // 3 variables
    expected = Utils.SF_LOG_TAG + " 1 test message\n" + "2 test message\n" + "3 test message";

    assert Utils.formatLogMessage("{} test message\n{} test message\n{} test " + "message", 1, 2, 3)
        .equals(expected);

    // 4 variables
    expected =
        Utils.SF_LOG_TAG
            + " 1 test message\n"
            + "2 test message\n"
            + "3 test message\n"
            + "4 test message";

    assert Utils.formatLogMessage(
            "{} test message\n{} test message\n{} test " + "message\n{} test message", 1, 2, 3, 4)
        .equals(expected);
  }

  @Test
  public void testFormatLogMessage() {
    // setup
    String mdcContext = "test context";
    String msg = "example message";
    String expected = Utils.SF_LOG_TAG + " " + mdcContext + msg;

    // test
    MDC.put(Utils.MDC_CONN_CTX_KEY, mdcContext);
    String result = Utils.formatLogMessage(msg);

    // verify
    assert result.equals(expected);
  }

  @Test
  public void testFormatLogMessageWithVars() {
    // setup
    String mdcContext = "test context";
    String msg = "example message with {} vars{}";
    String var1 = "fun";
    String var2 = "!";
    String expected = Utils.SF_LOG_TAG + " " + mdcContext + "example message with fun vars!";

    // test
    MDC.put(Utils.MDC_CONN_CTX_KEY, mdcContext);
    String result = Utils.formatLogMessage(msg, var1, var2);

    // verify
    assert result.equals(expected);
  }
}
