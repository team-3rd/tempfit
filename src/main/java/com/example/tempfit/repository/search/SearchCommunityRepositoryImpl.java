package com.example.tempfit.repository.search;

import com.example.tempfit.entity.Community;
import com.example.tempfit.entity.QCommunity;
import com.example.tempfit.entity.QCommunityStyle;
import com.example.tempfit.entity.QCommunityTemp;
import com.example.tempfit.entity.QCommunityImage;
import com.example.tempfit.entity.TemperatureRange;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Transactional(readOnly = true) // 내부수정 불가
public class SearchCommunityRepositoryImpl
        extends QuerydslRepositorySupport 
        implements SearchCommunityRepository {

    public SearchCommunityRepositoryImpl(EntityManager em) {
        super(Community.class);
        setEntityManager(em);
    }

    // 오라클 테이블 연결하는 join 기능
    @Override
    public Page<Object[]> list(
            String type,
            String keyword,
            List<String> styleNames,
            TemperatureRange range,
            Pageable pageable) {

        QCommunity community = QCommunity.community;
        QCommunityStyle style     = QCommunityStyle.communityStyle;
        QCommunityImage image     = QCommunityImage.communityImage;
        QCommunityTemp temp       = QCommunityTemp.communityTemp;

        JPQLQuery<Tuple> query = from(community)
            .leftJoin(style).on(community.id.eq(style.id))
            .leftJoin(temp).on(community.id.eq(temp.id))
            .leftJoin(image).on(image.community.id.eq(community.id)
                    .and(image.isRep.isTrue()))
            .select(
                community.id,
                community.title,
                community.author,
                community.recommendCount,
                image.fileName,
                community.createdDate,
                style.casual,
                style.street,
                style.formal,
                style.outdoor,
                temp.dayAvgTemp,
                temp.nightAvgTemp
            )
            .distinct();
                .leftJoin(style).on(community.id.eq(style.id))
                .leftJoin(temp).on(community.id.eq(temp.id))
                .leftJoin(image).on(image.community.id.eq(community.id)
                        .and(image.isRep.isTrue()))
                .select(
                        community.id,
                        community.title,
                        community.author,
                        community.recommendCount,
                        image.fileName, 
                        community.createdDate,
                        style.casual,
                        style.street,
                        style.formal,
                        style.outdoor,
                        temp.dayAvgTemp,
                        temp.nightAvgTemp 
                )
                .distinct(); // 중복제거

        // 게시글 검색어 입력란 검색기능
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(community.id.gt(0L));

        // (1) 타입·키워드 검색
        if (type != null && keyword != null && !keyword.trim().isEmpty()) {
            BooleanBuilder tb = new BooleanBuilder();
            if (type.contains("t")) tb.or(community.title.containsIgnoreCase(keyword));
            if (type.contains("a")) tb.or(community.author.name.containsIgnoreCase(keyword));
            if (type.contains("c")) tb.or(community.content.containsIgnoreCase(keyword));
            builder.and(tb);
        }

        // (2) 스타일 필터
        // 스타일이 값이 있을경우 검색기능
        if (styleNames != null && !styleNames.isEmpty()) {
            BooleanBuilder sb = new BooleanBuilder();
            for (String s : styleNames) {
                switch (s.toUpperCase()) {
                    case "CASUAL":  sb.or(style.casual.isTrue());  break;
                    case "STREET":  sb.or(style.street.isTrue());  break;
                    case "FORMAL":  sb.or(style.formal.isTrue());  break;
                    case "OUTDOOR": sb.or(style.outdoor.isTrue()); break;
                    case "CASUAL":
                        sb.or(style.casual.isTrue());
                        break;
                    case "STREET":
                        sb.or(style.street.isTrue());
                        break;
                    case "FORMAL":
                        sb.or(style.formal.isTrue());
                        break;
                    case "OUTDOOR":
                        sb.or(style.outdoor.isTrue());
                        break; 
                }
            }
            builder.and(sb);
        }

        // (3) 온도 범위 필터링
        if (range != null) {
            BooleanBuilder tb2 = new BooleanBuilder();
            tb2.or(temp.dayAvgTemp.between(range.getMinTemp(), range.getMaxTemp()));
            tb2.or(temp.nightAvgTemp.between(range.getMinTemp(), range.getMaxTemp()));
            builder.and(tb2);
        }

        query.where(builder);

        // (4) 추천수 내림차순 정렬: 항상 우선 적용
        query.orderBy(new OrderSpecifier<>(Order.DESC,
            new PathBuilder<>(Community.class, "community")
                .getNumber("recommendCount", Integer.class)
        ));

        // (5) 페이지/페이징
        query.offset(pageable.getOffset());
        query.limit(pageable.getPageSize());

        // 결과 매핑
        List<Tuple> tuples = query.fetch();
        List<Object[]> results = tuples.stream()
            .map(t -> new Object[] {
                t.get(community.id),
                t.get(community.title),
                t.get(community.author),
                t.get(community.recommendCount),
                t.get(image.fileName),
                t.get(community.createdDate),
                t.get(style.casual),
                t.get(style.street),
                t.get(style.formal),
                t.get(style.outdoor),
                t.get(temp.dayAvgTemp),
                t.get(temp.nightAvgTemp)
            })
            .collect(Collectors.toList());
        query.where(builder);
        for (Sort.Order o : pageable.getSort()) { 
            Order dir = o.isAscending() ? Order.ASC : Order.DESC; // 정렬 방향
            PathBuilder<Community> path = new PathBuilder<>(Community.class, "community"); // 경로 객체 생성
            query.orderBy(new OrderSpecifier<>(dir,
                    path.getComparable(o.getProperty(), Comparable.class))); // 쿼리 반영
        }

        query.offset(pageable.getOffset()); 
        query.limit(pageable.getPageSize()); 

        List<Tuple> tuples = query.fetch(); // 쿼리 결과 담기
        List<Object[]> results = tuples.stream() // 쿼리 결과 stream으로 배열 자동 정렬
                .map(t -> new Object[] {
                        t.get(community.id),
                        t.get(community.title),
                        t.get(community.author),
                        t.get(community.recommendCount),
                        t.get(image.fileName),
                        t.get(community.createdDate),
                        t.get(style.casual),
                        t.get(style.street),
                        t.get(style.formal),
                        t.get(style.outdoor),
                        t.get(temp.dayAvgTemp), 
                        t.get(temp.nightAvgTemp) 
                })
                .collect(Collectors.toList());

        // 조인결과 개수 페이지로 반환
        long total = from(community)
            .leftJoin(style).on(community.id.eq(style.id))
            .leftJoin(temp).on(community.id.eq(temp.id))
            .leftJoin(image).on(image.community.id.eq(community.id)
                    .and(image.isRep.isTrue()))
            .where(builder)
            .fetchCount();

        return new PageImpl<>(results, pageable, total);
    }
    @Override
    public Object[] getCommunityByBno(Long id) {
        QCommunity community = QCommunity.community;
        QCommunityStyle style = QCommunityStyle.communityStyle;
        QCommunityImage image = QCommunityImage.communityImage;

        JPQLQuery<Tuple> q = from(community)
                .leftJoin(style).on(community.id.eq(style.id))
                .leftJoin(image).on(image.community.id.eq(community.id)
                        .and(image.isRep.isTrue()))
                .select(
                        community.id,
                        community.title,
                        community.author,
                        community.createdDate,
                        image.fileName,
                        style.casual,
                        style.street,
                        style.formal,
                        style.outdoor
                )
                .where(community.id.eq(id)); // 카테고리 검색 조건 하나

        Tuple t = q.fetchOne();
        if (t == null)
            return null;

        return new Object[] {
                t.get(community.id),
                t.get(community.title),
                t.get(community.author),
                t.get(community.createdDate),
                t.get(image.fileName),
                t.get(style.casual),
                t.get(style.street),
                t.get(style.formal),
                t.get(style.outdoor) 
        };
    }
}
