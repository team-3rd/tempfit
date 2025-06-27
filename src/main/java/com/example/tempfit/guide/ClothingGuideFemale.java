package com.example.tempfit.guide;


import com.example.tempfit.entity.TemperatureRange;
import java.util.*;

public class ClothingGuideFemale {

    public static class GuideSet {
        public List<String> tops;
        public List<String> bottoms;
        public List<String> shoes;

        public GuideSet(List<String> tops, List<String> bottoms, List<String> shoes) {
            this.tops = tops;
            this.bottoms = bottoms;
            this.shoes = shoes;
        }
    }

    // 이미지 매핑 (항목명 -> 이미지URL)
    private static final Map<String, String> imageUrlMap = new HashMap<>();
    static {
        // 상의
        imageUrlMap.put("나시(얇은 가디건)", "/images/guide/top/female/camisole.png");
        imageUrlMap.put("반소매", "/images/guide/top/female/shortsleeve.png");
        imageUrlMap.put("헨리넥 반소매", "/images/guide/top/female/henryneck.png");
        imageUrlMap.put("반소매 블라우스", "/images/guide/top/female/shortsleeveblouse.png");
        imageUrlMap.put("피케/카라 원피스", "/images/guide/top/female/piqueonepiece.png");
        imageUrlMap.put("원피스", "/images/guide/top/female/sundress.png");
        imageUrlMap.put("동탄미시", "/images/guide/top/female/dongtanmissy.png");
        imageUrlMap.put("반팔 니트/스웨터", "/images/guide/top/female/shortsleeveknit.png");
        imageUrlMap.put("긴소매", "/images/guide/top/female/longsleeve.png");
        imageUrlMap.put("셔츠", "/images/guide/top/female/shirt.png");
        imageUrlMap.put("얇은 니트/스웨터", "/images/guide/top/female/thinknitsweater.png");
        imageUrlMap.put("얇은 가디건", "/images/guide/top/female/thincardigan.png");
        imageUrlMap.put("바람막이", "/images/guide/top/female/windbreaker.png");
        imageUrlMap.put("가디건", "/images/guide/top/female/cardigan.png");
        imageUrlMap.put("니트/스웨터", "/images/guide/top/female/knitsweater.png");
        imageUrlMap.put("후드티", "/images/guide/top/female/hoodie.png");
        imageUrlMap.put("블레이저", "/images/guide/top/female/blazer.png");
        imageUrlMap.put("트렌치코트", "/images/guide/top/female/trench.png");
        imageUrlMap.put("가죽자켓", "/images/guide/top/female/bikerjacket.png");
        imageUrlMap.put("울코트", "/images/guide/top/female/woolencoat.png");
        imageUrlMap.put("패딩", "/images/guide/top/female/pufferjacket.png");

        // 하의
        // imageUrlMap.put("돌핀 팬츠", "/images/guide/bottom/female/dolpinepants.png");
        imageUrlMap.put("반바지", "/images/guide/bottom/female/shorts.png");
        imageUrlMap.put("숏스커트", "/images/guide/bottom/female/tennisskirt.png");
        imageUrlMap.put("린넨 팬츠", "/images/guide/bottom/female/linenpants.png");
        imageUrlMap.put("얇은 데님", "/images/guide/bottom/female/lightdenim.png");
        imageUrlMap.put("얇은 슬랙스", "/images/guide/bottom/female/slacks.png");
        imageUrlMap.put("롱스커트", "/images/guide/bottom/female/chiffonskirt.png");
        imageUrlMap.put("데님", "/images/guide/bottom/female/denim.png");
        imageUrlMap.put("코튼 팬츠", "/images/guide/bottom/female/chinopants.png");
        imageUrlMap.put("슬랙스", "/images/guide/bottom/female/slacks.png");
        imageUrlMap.put("스웨트팬츠", "/images/guide/bottom/female/sweatpants.png");
        imageUrlMap.put("두꺼운 데님", "/images/guide/bottom/female/heavydenim.png");
        imageUrlMap.put("울팬츠", "/images/guide/bottom/female/woolenpants.png");

        // 신발
        imageUrlMap.put("샌들/슬리퍼", "/images/guide/shoes/female/sandal.png");
        imageUrlMap.put("크록스", "/images/guide/shoes/female/crocs.png");
        imageUrlMap.put("스니커즈", "/images/guide/shoes/female/canvasshoe.png");
        imageUrlMap.put("오픈토힐", "/images/guide/shoes/female/opentoeheel.png");
        imageUrlMap.put("스포츠화", "/images/guide/shoes/female/athleticshoe.png");
        imageUrlMap.put("구두", "/images/guide/shoes/female/dressshoe.png");
        imageUrlMap.put("앵클부츠", "/images/guide/shoes/female/ankleboot.png");
        imageUrlMap.put("부츠", "/images/guide/shoes/female/kneehigh.png");
        imageUrlMap.put("패딩/퍼", "/images/guide/shoes/female/furryboot.png");
    }

    private static final Map<TemperatureRange, GuideSet> guideMap = new HashMap<>();
    static {
        guideMap.put(TemperatureRange.VERY_HOT, // 28 ~ 100
                new GuideSet(
                        Arrays.asList("나시(얇은 가디건)", "헨리넥 반소매", "반소매 블라우스","피케/카라 원피스", "원피스", "동탄미시", "반팔 니트/스웨터"),
                        Arrays.asList("돌핀 팬츠", "반바지", "숏스커트", "린넨 팬츠", "얇은 데님", "얇은 슬랙스"),
                        Arrays.asList("샌들/슬리퍼", "크록스", "스니커즈", "오픈토힐")
                )
        );
        guideMap.put(TemperatureRange.HOT, // 23 ~ 27
                new GuideSet(
                        Arrays.asList("헨리넥 반소매", "반소매 블라우스","피케/카라 원피스", "반팔 니트/스웨터"),
                        Arrays.asList("얇은 데님", "얇은 슬랙스", "코튼 팬츠"),
                        Arrays.asList("스니커즈", "스포츠화", "오픈토힐")
                )
        );
        guideMap.put(TemperatureRange.WARM, // 20 ~ 22
                new GuideSet(
                        Arrays.asList("긴소매", "셔츠", "얇은 가디건", "바람막이", "얇은 니트/스웨터", "후드티"),
                        Arrays.asList("데님", "코튼 팬츠", "슬랙스", "스웨트팬츠"),
                        Arrays.asList("스니커즈", "스포츠화", "구두")
                )
        );
        guideMap.put(TemperatureRange.MILD, // 17 ~ 19 얇은 옷 시작
                new GuideSet(
                        Arrays.asList("긴소매", "셔츠", "얇은 가디건", "바람막이","얇은 니트/스웨터", "후드티"),
                        Arrays.asList("데님", "코튼 팬츠", "슬랙스", "스웨트팬츠"),
                        Arrays.asList("스니커즈", "스포츠화", "구두", "앵클부츠")
                )
        );
        guideMap.put(TemperatureRange.COOL, // 12 ~ 16
                new GuideSet(
                        Arrays.asList("가디건", "니트/스웨터", "후드티", "블레이저"),
                        Arrays.asList("데님", "코튼 팬츠", "슬랙스", "스웨트팬츠"),
                        Arrays.asList("스포츠화", "구두", "앵클부츠")
                )
        );
        guideMap.put(TemperatureRange.COLD, // 9 ~ 11
                new GuideSet(
                        Arrays.asList("트렌치코트", "가죽자켓"),
                        Arrays.asList("두꺼운 데님", "울팬츠"),
                        Arrays.asList("스포츠화", "앵클부츠", "부츠")
                )
        );
        guideMap.put(TemperatureRange.VERY_COLD, // 5 ~ 8
                new GuideSet(
                        Arrays.asList("울코트", "패딩코트", "패딩"),
                        Arrays.asList("두꺼운 데님", "울팬츠"),
                        Arrays.asList("스포츠화", "앵클부츠", "부츠", "패딩/퍼")
                )
        );
        guideMap.put(TemperatureRange.FREEZING, // 4 ~ -100
                new GuideSet(
                        Arrays.asList("울코트", "패딩코트", "패딩"),
                        Arrays.asList("두꺼운 데님"),
                        Arrays.asList("스포츠화", "앵클부츠", "부츠", "패딩/퍼")
                )
        );
    }

    // 상의, 하의, 신발 각각 랜덤+이미지
    public static Map<String, Map<String, String>> getRandomClothingWithImage(TemperatureRange range) {
        GuideSet set = guideMap.get(range);
        Random random = new Random();
        Map<String, Map<String, String>> result = new LinkedHashMap<>();

        // 상의 먼저 뽑기
        String top = set.tops.get(random.nextInt(set.tops.size()));

        // "원피스류"라면 하의는 비우기
        Set<String> onePieceTops = Set.of("피케/카라 원피스", "원피스", "동탄미시");
        String bottom = "";
        String bottomImageUrl = "";
        if (!onePieceTops.contains(top)) {
        // 일반 상의라면 하의도 뽑기
        bottom = set.bottoms.get(random.nextInt(set.bottoms.size()));
        bottomImageUrl = imageUrlMap.getOrDefault(bottom, "");
        }

        // 신발 뽑기
        String shoes = set.shoes.get(random.nextInt(set.shoes.size()));

        result.put("top", Map.of(
                "name", top,
                "imageUrl", imageUrlMap.getOrDefault(top, "")
        ));
        result.put("bottom", Map.of(
                "name", bottom,
                "imageUrl", bottomImageUrl
        ));
        result.put("shoes", Map.of(
                "name", shoes,
                "imageUrl", imageUrlMap.getOrDefault(shoes, "")
        ));

        return result;
    }
}
