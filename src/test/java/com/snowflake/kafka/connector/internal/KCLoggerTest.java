/*
 * Copyright (c) 2019 Snowflake Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.snowflake.kafka.connector.internal;

import com.snowflake.kafka.connector.Utils;
import java.util.UUID;

import org.apache.log4j.MDC;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import static com.snowflake.kafka.connector.Utils.MDC_CONN_CTX_KEY;

public class KCLoggerTest {
  // test constants
  private final String name = "test.logger.name";
  private final UUID kcGlobalInstanceId = UUID.randomUUID();

  // mock and test setup, inject logger into KCLogger
  @Mock(name = "logger")
  private Logger logger = Mockito.mock(Logger.class);

  @InjectMocks private KCLogger kcLogger = new KCLogger(this.name);

  @Before
  public void close() {
    this.kcLogger = new KCLogger(this.name);
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testAllLogMessages() {
    String msg = "super useful logging msg";
    String formatMsg = "super {} useful {} logging {} msg {}";
    String expectedFormattedMsg = "super wow useful wow! logging 1 msg yay";

    // note: utils.formatLogMessage should add the MDC context automatically
    this.testLogMessagesRunner(msg, Utils.formatLogMessage(msg));
    this.testLogMessagesWithFormattingRunner(
        formatMsg,
        Utils.formatLogMessage(expectedFormattedMsg),
        "wow",
        "wow!",
        1,
        "yay");
  }

  private void testLogMessagesRunner(String msg, String expectedMsg) {
    // info
    Mockito.when(logger.isInfoEnabled()).thenReturn(true);
    kcLogger.info(msg);

    Mockito.verify(logger, Mockito.times(1)).info(expectedMsg);

    // trace
    Mockito.when(logger.isTraceEnabled()).thenReturn(true);
    kcLogger.trace(msg);

    Mockito.verify(logger, Mockito.times(1)).trace(expectedMsg);

    // debug
    Mockito.when(logger.isDebugEnabled()).thenReturn(true);
    kcLogger.debug(msg);

    Mockito.verify(logger, Mockito.times(1)).debug(expectedMsg);

    // warn
    Mockito.when(logger.isWarnEnabled()).thenReturn(true);
    kcLogger.warn(msg);

    Mockito.verify(logger, Mockito.times(1)).warn(expectedMsg);

    // error
    Mockito.when(logger.isErrorEnabled()).thenReturn(true);
    kcLogger.error(msg);

    Mockito.verify(logger, Mockito.times(1)).error(expectedMsg);
  }

  private void testLogMessagesWithFormattingRunner(
      String formatMsg, String expectedFormattedMsg, Object... vars) {
    // info
    Mockito.when(logger.isInfoEnabled()).thenReturn(true);
    kcLogger.info(formatMsg, vars);

    Mockito.verify(logger, Mockito.times(1)).info(expectedFormattedMsg);

    // trace
    Mockito.when(logger.isTraceEnabled()).thenReturn(true);
    kcLogger.trace(formatMsg, vars);

    Mockito.verify(logger, Mockito.times(1)).trace(expectedFormattedMsg);

    // debug
    Mockito.when(logger.isDebugEnabled()).thenReturn(true);
    kcLogger.debug(formatMsg, vars);

    Mockito.verify(logger, Mockito.times(1)).debug(expectedFormattedMsg);

    // warn
    Mockito.when(logger.isWarnEnabled()).thenReturn(true);
    kcLogger.warn(formatMsg, vars);

    Mockito.verify(logger, Mockito.times(1)).warn(expectedFormattedMsg);

    // error
    Mockito.when(logger.isErrorEnabled()).thenReturn(true);
    kcLogger.error(formatMsg, vars);

    Mockito.verify(logger, Mockito.times(1)).error(expectedFormattedMsg);
  }
}
