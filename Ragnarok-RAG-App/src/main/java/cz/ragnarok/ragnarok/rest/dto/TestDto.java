package cz.ragnarok.ragnarok.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TestDto {
    private String question;
    private String answer;
    private String expectedAnswer;
    private String middleStep;
    private List<DocumentsResponseDto> documents;

}
