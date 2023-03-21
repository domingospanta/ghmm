package pt.feup.ghmm.metrics.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pt.feup.ghmm.core.response.ResponseMessage;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class CodeRepoDto extends ResponseMessage {
    private String owner;

    private String name;

    @NotBlank(message = "Url is mandatory")
    private String url;

    private boolean microservice;
}
