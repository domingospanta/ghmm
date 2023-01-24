package pt.feup.ghmm.metrics.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pt.feup.ghmm.core.response.ResponseMessage;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class MetricsStatisticsDto extends ResponseMessage {

    long totalMetrics;

    long maxRepoFiles;

    long minRepoFiles;

    long averageRepoFiles;

    long maxRepoAllContentsNumber;

    long minRepoAllContentsNumber;

    long averageRepoAllContentsNumber;

    long maxSize;

    long minSize;

    long averageSize;

    long countByMicroserviceMentionTrue;

    long countByMicroserviceMentionFalse;

    long countByDatabaseConnectionTrue;

    long countByDatabaseConnectionFalse;

    long countByDockerfileTrue;

    long countByDockerfileFalse;

    long countByRestfulTrue;

    long countByRestfulFalse;

    long countByMessagingTrue;

    long countByMessagingFalse;

    long countBySoapTrue;

    long countBySoapFalse;

    long countByLogsServiceTrue;

    long countByLogsServiceFalse;
}


