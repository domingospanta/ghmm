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

    float microserviceMentionPercentage;

    long countByDatabaseConnectionTrue;

    long countByDatabaseConnectionFalse;

    float databaseConnectionPercentage;

    long countByDockerfileTrue;

    long countByDockerfileFalse;

    float dockerfilePercentage;

    long countByRestfulTrue;

    long countByRestfulFalse;

    float restfulPercentage;

    long countByMessagingTrue;

    long countByMessagingFalse;

    float messagingPercentage;

    long countBySoapTrue;

    long countBySoapFalse;

    float soapPercentage;

    long countByLogsServiceTrue;

    long countByLogsServiceFalse;

    float logsServicePercentage;
}


