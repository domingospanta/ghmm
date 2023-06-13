package pt.feup.ghmm.integration.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AllContentDto {

    private String url;

    private List<ContentDto> tree;
}
