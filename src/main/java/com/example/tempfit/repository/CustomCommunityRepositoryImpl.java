package com.example.tempfit.repository;

import com.example.tempfit.entity.Community;
import com.example.tempfit.entity.QCommunity;
import com.example.tempfit.entity.QCommunityStyle;
import com.example.tempfit.entity.TemperatureRange;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomCommunityRepositoryImpl implements CustomCommunityRepository {

    private final EntityManager em;

    @Override
    public List<Community> findTopCommunitiesByStyleAndTemp(String style, int temp, int limit) {
        JPAQueryFactory query = new JPAQueryFactory(em);
        QCommunity c = QCommunity.community;
        QCommunityStyle cs = QCommunityStyle.communityStyle;

        // 스타일별 boolean 필드명으로 동적 where 구성
        BooleanExpression styleCond = null;
        if ("캐주얼".equals(style))
            styleCond = cs.casual.isTrue();
        else if ("포멀".equals(style))
            styleCond = cs.formal.isTrue();
        else if ("스트리트".equals(style))
            styleCond = cs.street.isTrue();
        else if ("아웃도어".equals(style))
            styleCond = cs.outdoor.isTrue();

        // enum 활용해서 온도 구간 가져오기!
        TemperatureRange range = TemperatureRange.fromTemperature(temp);
        int minTemp = range.getMinTemp();
        int maxTemp = range.getMaxTemp();

        // QueryDSL 쿼리만 사용
        return query
                .selectFrom(c)
                .join(c.communityStyle, cs)
                .where(
                        styleCond,
                        c.temperature.goe(minTemp),
                        c.temperature.loe(maxTemp)
                )
                .orderBy(c.recommendCount.desc())
                .limit(limit)
                .fetch();
    }
}
