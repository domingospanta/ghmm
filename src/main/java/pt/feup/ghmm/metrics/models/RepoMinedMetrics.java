package pt.feup.ghmm.metrics.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
@Entity
public class RepoMinedMetrics extends CodeRepoMetrics{

    @OneToOne(cascade = CascadeType.REMOVE)
    private RepoMined repoMined;

}
