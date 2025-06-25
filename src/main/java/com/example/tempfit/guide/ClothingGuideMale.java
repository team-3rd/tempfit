package com.example.tempfit.guide;


import com.example.tempfit.entity.TemperatureRange;
import java.util.*;

public class ClothingGuideMale {

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
        imageUrlMap.put("민소매(얇은 가디건)", "/images/guide/top/male/sleeveless.png");
        imageUrlMap.put("반소매", "/images/guide/top/male/shortsleeve.png");
        imageUrlMap.put("헨리넥 반소매", "/images/guide/top/male/henryneck.png");
        imageUrlMap.put("피케/카라", "/images/guide/top/male/piqueshirt.png");
        imageUrlMap.put("얇은 니트/스웨터", "/images/guide/top/male/shortsleeveknit.png");
        imageUrlMap.put("반팔 셔츠", "/images/guide/top/male/shortsleeveshirts.png");
        imageUrlMap.put("린넨 셔츠", "/images/guide/top/male/linenshirts.png");
        imageUrlMap.put("긴소매", "/images/guide/top/male/longsleeve.png");
        imageUrlMap.put("셔츠", "/images/guide/top/male/shirt.png");
        imageUrlMap.put("가디건", "/images/guide/top/male/cardigan.png");
        imageUrlMap.put("니트/스웨터", "/images/guide/top/male/knitsweater.png");
        imageUrlMap.put("후드티", "/images/guide/top/male/hoodie.png");
        imageUrlMap.put("맨투맨", "/images/guide/top/male/sweatshirt.png");
        imageUrlMap.put("블레이저", "/images/guide/top/male/blazer.png");
        imageUrlMap.put("트렌치코트", "/images/guide/top/male/trench.png");
        imageUrlMap.put("가죽자켓", "/images/guide/top/male/bikerjacket.png");
        imageUrlMap.put("울코트", "/images/guide/top/male/woolencoat.png");
        imageUrlMap.put("패딩코트", "/images/guide/top/male/wintercoat.png");
        imageUrlMap.put("패딩", "/images/guide/top/male/pufferjacket.png");

        // 하의
        imageUrlMap.put("면반바지", "/images/guide/bottom/male/shorts.png");
        imageUrlMap.put("데님 반바지", "/images/guide/bottom/male/denimshorts.png");
        imageUrlMap.put("린넨 팬츠", "/images/guide/bottom/male/linenpants.png");
        imageUrlMap.put("얇은 데님", "/images/guide/bottom/male/lightdenim.png");
        imageUrlMap.put("얇은 슬랙스", "/images/guide/bottom/male/slacks.png");
        imageUrlMap.put("데님", "/images/guide/bottom/male/denim.png");
        imageUrlMap.put("코튼 팬츠", "/images/guide/bottom/male/chinopants.png");
        imageUrlMap.put("슬랙스", "/images/guide/bottom/male/slacks.png");
        imageUrlMap.put("스웨트팬츠", "/images/guide/bottom/male/sweatpants.png");
        imageUrlMap.put("두꺼운 데님", "/images/guide/bottom/male/heavydenim.png");
        imageUrlMap.put("울팬츠", "/images/guide/bottom/male/woolenpants.png");

        // 신발
        imageUrlMap.put("샌들/슬리퍼", "/images/guide/shoes/male/sandal.png");
        imageUrlMap.put("크록스", "/images/guide/shoes/male/crocs.png");
        imageUrlMap.put("스니커즈", "/images/guide/shoes/male/canvasshoe.png");
        imageUrlMap.put("스포츠화", "/images/guide/shoes/male/athleticshoe.png");
        imageUrlMap.put("구두", "/images/guide/shoes/male/derby.png");
        imageUrlMap.put("부츠/워커", "/images/guide/shoes/male/chelseaboot.png");
        imageUrlMap.put("패딩/퍼", "/images/guide/shoes/male/paddedshoe.png");
    }

    private static final Map<TemperatureRange, GuideSet> guideMap = new HashMap<>();
    static {
        guideMap.put(TemperatureRange.VERY_HOT, // 28 ~ 100
                new GuideSet(
                        Arrays.asList("민소매(얇은 가디건)", "반소매","헨리넥 반소매", "피케/카라", "얇은 니트/스웨터", "반팔 셔츠", "린넨 셔츠"),
                        Arrays.asList("면반바지", "데님 반바지", "린넨 팬츠", "얇은 데님", "얇은 슬랙스"),
                        Arrays.asList("샌들/슬리퍼", "크록스","스니커즈")
                )
        );
        guideMap.put(TemperatureRange.HOT, // 23 ~ 27
                new GuideSet(
                        Arrays.asList("반소매", "헨리넥 반소매", "피케/카라", "얇은 니트/스웨터", "반팔 셔츠", "린넨 셔츠"),
                        Arrays.asList("얇은 데님", "얇은 슬랙스", "코튼 팬츠"),
                        Arrays.asList("스니커즈", "스포츠화")
                )
        );
        guideMap.put(TemperatureRange.WARM, // 20 ~ 22
                new GuideSet(
                        Arrays.asList("긴소매", "셔츠", "가디건", "얇은 니트/스웨터"),
                        Arrays.asList("데님", "코튼 팬츠", "슬랙스", "스웨트팬츠"),
                        Arrays.asList("스니커즈", "스포츠화", "구두")
                )
        );
        guideMap.put(TemperatureRange.MILD, // 17 ~ 19
                new GuideSet(
                        Arrays.asList("긴소매", "셔츠", "가디건", "니트/스웨터", "후드티", "맨투맨"),
                        Arrays.asList("데님", "코튼 팬츠", "슬랙스", "스웨트팬츠"),
                        Arrays.asList("스니커즈", "스포츠화", "구두", "부츠/워커")
                )
        );
        guideMap.put(TemperatureRange.COOL, // 12 ~ 16
                new GuideSet(
                        Arrays.asList("가디건", "니트/스웨터", "후드티", "맨투맨", "블레이저"),
                        Arrays.asList("데님", "코튼 팬츠", "슬랙스", "스웨트팬츠"),
                        Arrays.asList("스포츠화", "구두", "부츠/워커")
                )
        );
        guideMap.put(TemperatureRange.COLD, // 9 ~ 11
                new GuideSet(
                        Arrays.asList("트렌치코트", "가죽자켓"),
                        Arrays.asList("두꺼운 데님", "울팬츠"),
                        Arrays.asList("스포츠화", "구두", "부츠/워커")
                )
        );
        guideMap.put(TemperatureRange.VERY_COLD, // 5 ~ 8
                new GuideSet(
                        Arrays.asList("울코트", "패딩코트", "패딩"),
                        Arrays.asList("두꺼운 데님", "울팬츠"),
                        Arrays.asList("스포츠화", "구두", "부츠/워커", "패딩/퍼")
                )
        );
        guideMap.put(TemperatureRange.FREEZING, // 4 ~ -100
                new GuideSet(
                        Arrays.asList("울코트", "패딩코트", "패딩"),
                        Arrays.asList("두꺼운 데님"),
                        Arrays.asList("스포츠화", "구두", "부츠/워커", "패딩/퍼")
                )
        );
    }

    // 상의, 하의, 신발 각각 랜덤+이미지
    public static Map<String, Map<String, String>> getRandomClothingWithImage(TemperatureRange range) {
        GuideSet set = guideMap.get(range);
        Random random = new Random();
        Map<String, Map<String, String>> result = new LinkedHashMap<>();

        String top = set.tops.get(random.nextInt(set.tops.size()));
        String bottom = set.bottoms.get(random.nextInt(set.bottoms.size()));
        String shoes = set.shoes.get(random.nextInt(set.shoes.size()));

        result.put("top", Map.of(
                "name", top,
                "imageUrl", imageUrlMap.getOrDefault(top, "")
        ));
        result.put("bottom", Map.of(
                "name", bottom,
                "imageUrl", imageUrlMap.getOrDefault(bottom, "")
        ));
        result.put("shoes", Map.of(
                "name", shoes,
                "imageUrl", imageUrlMap.getOrDefault(shoes, "")
        ));

        return result;
    }
}
