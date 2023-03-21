package pt.feup.ghmm.metrics.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pt.feup.ghmm.core.response.ResponseMessage;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class SearchRepoDto extends ResponseMessage {
    private String searchString;

    private String programmingLanguages;

    private int quantity;

    private boolean clearList;
}
