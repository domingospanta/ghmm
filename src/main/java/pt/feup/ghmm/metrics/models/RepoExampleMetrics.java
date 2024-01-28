package pt.feup.ghmm.metrics.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pt.feup.ghmm.repo.models.RepoExample;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
@Entity
public class RepoExampleMetrics extends CodeRepoMetrics{

    @OneToOne(cascade = CascadeType.REMOVE)
    private RepoExample repoExample;

    private double sizeWeight;

    private double sizeAggregatedWeight;

    private double filesWeight;

    private double filesAggregatedWeight;

    private double allContentsNumberWeight;

    private double allContentsAggregatedNumberWeight;

}
