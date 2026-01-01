package com.grillgauge.api.tasks;

import com.grillgauge.api.domain.repositorys.ReadingRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Scheduled task for cleaning up expired readings from the database. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReadingCleanupTask {

  private static final Logger LOG = LoggerFactory.getLogger(ReadingCleanupTask.class);

  private final ReadingRepository readingRepository;

  @Value("${reading.cleanup.batch-size:500}")
  private int batchSize;

  /** Scheduled task to delete expired readings from the database. */
  @Scheduled(fixedDelayString = "${reading.cleanup.delay:PT1H}")
  public void deleteExpiredReadings() {
    LOG.info("Starting expired readings cleanup task");
    int removedTotal = 0;
    while (true) {
      int removed = readingRepository.deleteExpiredBatch(Instant.now(), batchSize);
      removedTotal += removed;
      if (removed < batchSize) {
        break;
      }
    }
    if (removedTotal > 0) {
      LOG.info("Deleted {} expired readings", removedTotal);
    } else {
      LOG.info("No expired readings to delete");
    }
  }
}
