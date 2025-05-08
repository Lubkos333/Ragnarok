package cz.ragnarok.ragnarok.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DataByDesignationDto {
    @JsonProperty("_id")
    private OidWrapper id;

    @JsonProperty("znění-dokument-id")
    private int zneniDokumentId;

    @JsonProperty("znění-base-id")
    private int zneniBaseId;

    @JsonProperty("akt-název-vyhlášený")
    private String aktNazevVyhlaseny;

    @JsonProperty("akt-typ-sbírky")
    private String aktTypSbirky;

    @JsonProperty("akt-označení")
    private String aktOznaceni;

    @JsonProperty("akt-plné-označení")
    private String aktPlneOznaceni;

    @JsonProperty("typ-aktu")
    private String typAktu;

    @JsonProperty("cis-esb-typ-znění-po")
    private String cisEsbTypZneniPo;

    @JsonProperty("znění-datum-účinnosti-od")
    private LocalDate zneniDatumUcinnostiOd;

    @JsonProperty("odkazován-v")
    private List<OdkazZneni> odkazovanV;

    @JsonProperty("odkazuje-na")
    private List<OdkazZneni> odkazujeNa;

    @JsonProperty("odkaz-stažení-pdf")
    private String odkazStazeniPdf;

    @JsonProperty("odkaz-stažení-docx")
    private String odkazStazeniDocx;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class OidWrapper {
        @JsonProperty("$oid")
        private String oid;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class OdkazZneni {
        @JsonProperty("znění-dokument-id")
        private int zneniDokumentId;
    }
}
