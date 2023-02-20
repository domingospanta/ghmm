package pt.feup.ghmm.metrics.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.feup.ghmm.metrics.enums.ServiceType;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
public class Service {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Image is mandatory")
    private String image;

    @NotNull(message = "Database flag is mandatory")
    private ServiceType serviceType;
}
