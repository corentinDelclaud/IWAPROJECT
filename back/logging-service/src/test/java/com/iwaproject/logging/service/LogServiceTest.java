package com.iwaproject.logging.service;

import com.iwaproject.logging.model.LogEntry;
import com.iwaproject.logging.model.LogMessage;
import com.iwaproject.logging.repository.LogEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    @Mock
    private LogEntryRepository logEntryRepository;

    @InjectMocks
    private LogService logService;

    private LogMessage testLogMessage;

    @BeforeEach
    void setUp() {
        testLogMessage = LogMessage.builder()
            .serviceName("test-service")
            .logLevel("INFO")
            .message("Test log message")
            .timestamp(LocalDateTime.now())
            .threadName("main")
            .loggerName("com.test.Logger")
            .correlationId("test-123")
            .userId("user-456")
            .requestUri("/api/test")
            .requestMethod("GET")
            .responseStatus(200)
            .build();
    }

    @Test
    void testSaveLog() {
        // Arrange
        LogEntry expectedLogEntry = LogEntry.builder()
            .id(1L)
            .serviceName(testLogMessage.getServiceName())
            .logLevel(testLogMessage.getLogLevel())
            .message(testLogMessage.getMessage())
            .timestamp(testLogMessage.getTimestamp())
            .build();

        when(logEntryRepository.save(any(LogEntry.class))).thenReturn(expectedLogEntry);

        // Act
        LogEntry savedLog = logService.saveLog(testLogMessage);

        // Assert
        assertNotNull(savedLog);
        assertEquals(testLogMessage.getServiceName(), savedLog.getServiceName());
        assertEquals(testLogMessage.getLogLevel(), savedLog.getLogLevel());
        assertEquals(testLogMessage.getMessage(), savedLog.getMessage());
        verify(logEntryRepository, times(1)).save(any(LogEntry.class));
    }

    @Test
    void testSaveLogWithNullTimestamp() {
        // Arrange
        testLogMessage.setTimestamp(null);
        when(logEntryRepository.save(any(LogEntry.class))).thenReturn(any(LogEntry.class));

        // Act
        // LogEntry savedLog = logService.saveLog(testLogMessage);

        // Assert
        verify(logEntryRepository, times(1)).save(any(LogEntry.class));
    }

    @Test
    void testCountLogsByServiceAndLevel() {
        // Arrange
        String serviceName = "test-service";
        String logLevel = "ERROR";
        long expectedCount = 5L;

        when(logEntryRepository.countByServiceNameAndLogLevel(serviceName, logLevel))
            .thenReturn(expectedCount);

        // Act
        long count = logService.countLogsByServiceAndLevel(serviceName, logLevel);

        // Assert
        assertEquals(expectedCount, count);
        verify(logEntryRepository, times(1))
            .countByServiceNameAndLogLevel(serviceName, logLevel);
    }
}
