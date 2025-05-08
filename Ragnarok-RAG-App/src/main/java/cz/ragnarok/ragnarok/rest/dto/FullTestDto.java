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
public class FullTestDto {
    private List<TestDto> classic;
    private List<TestDto> paraphrase;
    private List<TestDto> keyword;
}
