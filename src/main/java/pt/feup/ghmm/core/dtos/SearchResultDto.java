package pt.feup.ghmm.core.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SearchResultDto {

    @JsonProperty("total_count")
    private long totalCount;

    @JsonProperty("incomplete_results")
    private boolean incompleteResults;

    private List<ItemDto> items;
}
