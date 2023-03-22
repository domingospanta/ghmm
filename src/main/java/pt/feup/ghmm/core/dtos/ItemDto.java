package pt.feup.ghmm.core.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ItemDto {
    private String name;

    private String path;

    private String url;

    private double score;
}
