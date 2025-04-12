package cz.ragnarok.ragnarok.rest.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DocumentsResponseDto {
    private String text;
    private Map metadata;
}
