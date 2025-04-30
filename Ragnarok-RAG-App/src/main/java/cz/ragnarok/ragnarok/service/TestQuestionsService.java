package cz.ragnarok.ragnarok.service;

import cz.ragnarok.ragnarok.rest.dto.FullTestDto;
import cz.ragnarok.ragnarok.rest.dto.FullTestLightDto;
import cz.ragnarok.ragnarok.rest.dto.TestDto;
import cz.ragnarok.ragnarok.rest.dto.TestLightDto;
import cz.ragnarok.ragnarok.rest.enums.FlowType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TestQuestionsService {

    @Autowired
    private TestFlowService flowService;

    private List<String> getQuestionsList() {
        return List.of(
                "Může si jeden z manželů vzít úvěr, aniž by s tím ten druhý souhlasil?",
                "Proč uzavřít předmanželskou smlouvu?",
                "Mají manželé nárok na stejný životní standard? Jak ho případně zajistit?",
                "Podle čeho se určuje výše výživného na dítě?",
                "Co je tzv. smluvený rozvod?",
                "Je střídavá péče po rozvodu pravidlem, nebo výjimkou?",
                "Co mám dělat, když ten druhý nesouhlasí? Jak nahradit souhlas druhého rodiče?",
                "Dostanu po rozvodu zpět finanční vnos poskytnutý na naše společné bydlení?",
                "Kdy mohu žádat o zvýšení výživného?"
        );
    }

    public List<TestDto> doQuestionsTest(FlowType flowType) {
        List<TestDto> results = new ArrayList<>();
        for (TestDto testDto: getTestDtoList()) {
            results.add(flowService.testFlow(testDto, flowType));
        }
        return results;
    }

    public FullTestDto doQuestionsFullTest() {
        return FullTestDto.builder()
                .classic(doQuestionsTest(FlowType.CLASSIC))
                .keyword(doQuestionsTest(FlowType.KEYWORDS))
                .paraphrase(doQuestionsTest(FlowType.PARAPHRASE))
                .build();
    }

    public FullTestLightDto doQuestionsFullTestLight() {
        return FullTestLightDto.builder()
                .classic(doQuestionsTest(FlowType.CLASSIC).stream().map(
                        testDto -> TestLightDto.builder()
                                .question(testDto.getQuestion())
                                .answer(testDto.getAnswer())
                                .expectedAnswer(testDto.getExpectedAnswer())
                                .build()
                ).toList())
                .keyword(doQuestionsTest(FlowType.KEYWORDS).stream().map(
                        testDto -> TestLightDto.builder()
                                .question(testDto.getQuestion())
                                .answer(testDto.getAnswer())
                                .expectedAnswer(testDto.getExpectedAnswer())
                                .build()
                ).toList())
                .paraphrase(doQuestionsTest(FlowType.PARAPHRASE).stream().map(
                        testDto -> TestLightDto.builder()
                                .question(testDto.getQuestion())
                                .answer(testDto.getAnswer())
                                .expectedAnswer(testDto.getExpectedAnswer())
                                .build()
                ).toList())
                .build();
    }

    private List<TestDto> getTestDtoList() {
        return List.of(
                TestDto.builder()
                        .question("Co je to SJM? Co do něj patří a co do něj naopak nepatří?")
                        .expectedAnswer("SJM je často užívaná zkratka pro společné jmění manželů. Jedná se o vše, co manželům náleží, má majetkovou hodnotu a není vyloučeno z právních poměrů. Do společného jmění manželů tak spadá vše, co nabyl jeden z manželů nebo čeho nabyli společně za trvání manželství. Jeho součástí je také zisk z toho, co náleží výhradně jednomu z manželů, např. výnos z nájmu nemovitosti. Patří do něj také podíl manžela v obchodní společnosti nebo družstvu, pokud se stal společníkem za dobu trvání manželství. Se vším ziskem během manželství však do společného jmění patří také dluhy převzaté za trvání manželství.\n" +
                                "\n" +
                                "Do společného jmění manželů ovšem nepatří majetek, který slouží k osobní potřebě jednoho z manželů, ani co jeden z manželů nabyl darem, děděním nebo odkazem v rámci pozůstalosti. Stejně tak finanční prostředky, které nabyl jeden z manželů jako náhradu nemajetkové újmy, a také to, co nabyl právním jednáním vztahujícím se k jeho výlučnému vlastnictví, nebo obnos, který jeden z manželů nabyl jako náhradu za poškození, zničení nebo ztrátu svého výhradního majetku.")
                        .build(),
                TestDto.builder()
                        .question("Může si jeden z manželů vzít úvěr, aniž by s tím ten druhý souhlasil?")
                        .expectedAnswer("Banky se na poskytování úvěru pouze jednomu z manželů bez souhlasu toho druhého dívají různě. Pokud úvěr přesahuje určitou částku, měly by vždy souhlas druhého manžela vyžadovat. Někdy to však jde i bez něho. Bankou poskytnutý úvěr za trvání manželství je tedy dluhem, který je součástí společného jmění manželů. V zásadě platí, že pokud vznikl dluh jednomu z manželů za trvání společného jmění, může se věřitel uspokojit z toho, co je ve společném jmění.\n" +
                                "\n" +
                                "Ochranu nevědomému manželovi nabízí občanský zákoník, neboť podle něho nejsou součástí společného jmění manželů dluhy převzaté jen jedním z manželů bez souhlasu druhého, aniž se jednalo o obstarávání každodenních nebo běžných potřeb rodiny. Jako určitou obranu v těchto situacích lze využít také včasné upozornění adresované věřiteli, ve kterém se nevědomý manžel vymezí proti dluhu, jakmile se o něm dozví.")
                        .build(),
                TestDto.builder()
                        .question("Proč uzavřít předmanželskou smlouvu?")
                        .expectedAnswer("Předmanželskou smlouvou si mohou snoubenci ujednat, co bude součástí společného jmění a co naopak jeho součástí nebude. Výhodou svědčící pro uzavření předmanželské smlouvy tak může být ochrana finančně lépe situovaného snoubence pro případ rozvodu manželství, ale stejně tak obrana proti uspokojení věřitele ze společného jmění manželů v případě dluhů vzniklých pouze jednomu z manželů. Pokud totiž snoubenci zúží společné jmění manželů před sňatkem, nelze dluh jednoho manžela později uspokojit z majetku druhého.\n" +
                                "\n" +
                                "Předmanželská smlouva je užitečná i za situace, kdy snoubenci nechtějí po svatbě mít zákonem předpokládané společné jmění manželů, ale přejí si, aby každý nabýval majetek do svého výlučného vlastnictví. Mohou si také například ujednat, že budou společný majetek nabývat pouze do spoluvlastnictví.")
                        .build(),
                TestDto.builder()
                        .question("Mají manželé nárok na stejný životní standard? Jak ho případně zajistit?")
                        .expectedAnswer("Manželé mají vůči sobě ze zákona navzájem vyživovací povinnost, která vzniká již uzavřením manželství a trvá až do jeho případného zániku. Hmotná a kulturní úroveň manželů by měla být po dobu trvání manželství tedy v zásadě stejná.\n" +
                                "\n" +
                                "Cílem výživného na potřeby oprávněného z manželů není pouze zajištění jeho výživy nebo úhrady výdajů spojených s chodem společné domácnosti. Výživné může druhý z manželů využít i pro úhrady za osobní nebo kulturní potřeby. Stejně tak může výživné spořit nebo ho libovolně investovat. V případě, že nastane situace, kdy jeden z manželů neplní vyživovací povinnost k druhému, může její rozsah určit soud na základě žaloby některého z nich.")
                        .build(),
                TestDto.builder()
                        .question("Podle čeho se určuje výše výživného na dítě?")
                        .expectedAnswer("Vodítkem pro stanovení výše výživného na dítě je životní úroveň srovnatelná s jeho rodiči. Podobně jako manželé, i dítě má dle zákona právo na stejnou životní úroveň, jakou mají jeho rodiče. Životní úroveň zde není vymezena jen jako uspokojování hmotných potřeb a zajišťování samotné výživy dítěte. Zahrnuje také oblékání, bydlení, zdravotní a sociální péči nebo úhrady za vzdělávání a kulturu. Při stanovení výše výživného se tak bere v potaz faktický příjem rodičů, rozsah jejich movitého a nemovitého majetku a způsob života, který vedou.\n" +
                                "\n" +
                                "Zohledňuje se i věk rodiče, jeho zdravotní stav, vzdělání, pracovní zkušenosti, sociální situovanost, také nepříznivá situace na trhu práce, nezaměstnanost v daném oboru nebo regionu apod. Přihlíží se rovněž k potenciálním příjmům, tedy zda povinný rodič pouze účelově nevyužívá svého potenciálu, který by mu umožnil lepší uplatnění na trhu práce a tím i možnost zajistit dítěti vyšší výživné a životní úroveň. Jako pomůcku používají soudy i tzv. „ministerskou tabulku“.")
                        .build()
                ,
                TestDto.builder()
                        .question("Co je tzv. smluvený rozvod?")
                        .expectedAnswer("Jak již název napovídá, smluvený rozvod (jinak také dohodnutý či nesporný) je vhodným institutem pro všechny manžele, kteří se na ukončení manželství dohodli a chtějí mít rozvodové řízení za sebou co nejrychleji. Tento typ rozvodu je možný pouze v případě, že manželství trvalo déle než rok a manželé spolu alespoň šest měsíců nežijí. Oba musí s rozvodem souhlasit a podat za tímto účelem společný návrh na rozvod. Případně může tento návrh podat pouze jeden z manželů a ten druhý se k němu připojí. Pokud mají manželé nezletilé děti, musí v řízení o výkon rodičovské odpovědnosti soud schválit jejich dohodu. Dále musí manželé pomocí dohody urovnat svoje majetkové poměry po rozvodu.\n" +
                                "\n" +
                                "Výhodou smluveného rozvodu je především to, že soud po splnění podmínek nezjišťuje existenci a příčiny rozvratu manželství. Soud manžele dlouze nevyslýchá, není proto nutné řešit osobní detaily manželství veřejně. Výsledkem jednání je pouze rozsudek se stručným odůvodněním.")
                        .build(),
                TestDto.builder()
                        .question("Je střídavá péče po rozvodu pravidlem, nebo výjimkou?")
                        .expectedAnswer("Současný postoj soudů je v případě porozvodové péče o nezletilé děti stále nakloněn střídavé péči. Tento názor se zakládá především na tom, že výlučná péče pouze jednoho z rodičů není v nejlepším zájmu dítěte. K dosažení všestranného vývoje dítěte, a tedy i v jeho nejlepším zájmu, je dostávat péči od obou rodičů stejnou měrou. Výjimky z tohoto pravidla sice existují, musí však být řádně odůvodněny na základě změny podstatných okolností.\n" +
                                "\n" +
                                "Při rozhodování, zda je střídavá péče pro dítě vhodná, soud postupuje v souladu s několika požadavky. Pokud jsou u všech osob, které o svěření dítěte do péče jeví upřímný zájem, splněna všechna kritéria ve stejné míře, je žádoucí dítě svěřit do společné či střídavé výchovy. Mezi kritéria, která musí obecné soudy vzít v potaz, patří zejména: existence pokrevního pouta mezi dítětem a osobou usilující o jeho svěření do péče; míra zachování identity dítěte a jeho rodinných vazeb v případě jeho svěření do péče té které osoby; schopnost osoby usilující o svěření dítěte do péče zajistit jeho vývoj a fyzické, vzdělávací, emocionální, materiální a jiné potřeby; a v neposlední řadě také přání dítěte.")
                        .build(),
                TestDto.builder()
                        .question("Co mám dělat, když ten druhý nesouhlasí? Jak nahradit souhlas druhého rodiče?")
                        .expectedAnswer("Situace, kdy se rodiče nemohou dohodnout na zásadních otázkách týkajících se jejich dítěte, mohou nastat v případě porozvodové péče, ale i ve funkčním manželském svazku. Rodiče mají vůči dítěti určitý stupeň odpovědnosti, kterou by měli vykonávat, pokud možno, ve shodě a v souladu se zájmy dítěte. Zákon zde rovněž dbá na ochranu dítěte, a proto umožňuje při takové patové situaci, aby o záležitosti rozhodl soud.\n" +
                                "\n" +
                                "Je nezbytné, aby se jednalo o významnou, nikoli běžnou záležitost, kde může být ohrožen zájem dítěte. Zákon předpokládá, že se může jednat především o rozhodování o léčebném zákroku, určení místa bydliště, volby vzdělání či povolání dítěte. Zároveň může jít i o věci, které se týkají samotné výchovy, změny jména a příjmení dítěte nebo o správu jeho jmění. Rodič, který byl z rozhodování vyloučen nebo si přeje, aby záležitost dítěte posoudil soud, může podat návrh. Na jeho základě se soud snaží odstranit neshodu rodičů tím, že nahradí souhlas jednoho z nich nebo že stanoví právní poměry, např. určí bydliště dítěte nebo základní školu, do které dítě nastoupí.")
                        .build(),
                TestDto.builder()
                        .question("Dostanu po rozvodu zpět finanční vnos poskytnutý na naše společné bydlení?")
                        .expectedAnswer("Jedním ze zákonných pravidel pro porozvodové vypořádání společného jmění je i předpoklad, že v jeho průběhu má každý z manželů právo žádat, aby mu bylo nahrazeno, co ze svého výhradního majetku vynaložil na společný majetek. Pokud tedy například jeden z manželů zdědil drahý obraz, tento obraz následně prodal a výtěžek z prodeje věnoval na rekonstrukci rodinného domu ve společném jmění, hovoříme o vnosu do společného jmění za trvání manželství.\n" +
                                "\n" +
                                "Během soudního vypořádání společného jmění v rámci rozvodu má manžel, který tento vnos poskytl, nárok na jeho vrácení. Nárok může být uplatněn pouze v rámci soudního řízení, ve kterém dochází k vypořádání zaniklého společného jmění manželů. Pokud se tak nestane, právo na vrácení vnosu zaniká.")
                        .build(),
                TestDto.builder()
                        .question("Kdy mohu žádat o zvýšení výživného?")
                        .expectedAnswer("Soud může změnit dohodu rodičů o výši výživného nebo předchozí rozhodnutí o výživném pro nezletilé dítě, pokud došlo k podstatné změně poměrů. Změna poměrů musí nastat ve skutečnostech, které byly podstatné pro předchozí rozhodnutí soudu v dané věci. V případě výživného na nezletilé dítě se tak předpokládá, že nastala nějaká závažná změna, která odůvodňuje zvýšení výživného.\n" +
                                "\n" +
                                "Podstatnou změnou v životě nezletilého dítěte, která by odůvodnila zvýšení jeho životních nákladů, a tedy i tomu odpovídajícího výživného, může být například přestup dítěte na vyšší stupeň vzdělávání, zvýšené náklady na dopravu do školy nebo zdravotní stav vyžadující léčebné procedury či medikaci. Stejně tak mohou změny nastat na straně povinného rodiče. Může se jednat o získání nového zaměstnání, přeřazení na lepší pracovní místo, a tedy i zvýšení platu nebo mzdy. Soud zváží, zda majetkové poměry povinného umožňují zvýšení výživného a zda na straně dítěte existuje důvodná potřeba k jeho vyplácení.")
                        .build()
        );
    }

}
