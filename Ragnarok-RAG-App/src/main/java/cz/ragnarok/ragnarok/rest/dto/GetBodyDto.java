package cz.ragnarok.ragnarok.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class GetBodyDto {
    private List<String> ids;
    private Map<String, Object> where;
    private Map<String, Object> where_document;
    private Integer limit;
}
