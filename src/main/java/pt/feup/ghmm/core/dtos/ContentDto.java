package pt.feup.ghmm.core.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ContentDto {

    private long size;

    private String path;

    private String type;
}