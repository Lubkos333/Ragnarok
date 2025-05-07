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
public class FullTestLightDto {
    private List<TestLightDto> classic;
    private List<TestLightDto> paraphrase;
    private List<TestLightDto> keyword;
}
