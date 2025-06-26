package com.example.tempfit.repository.search;

import com.example.tempfit.entity.Community;
import com.example.tempfit.entity.QCommunity;
import com.example.tempfit.entity.QCommunityStyle;
import com.example.tempfit.entity.QCommunityTemp;
import com.example.tempfit.entity.QCommunityImage;
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
@Transactional(readOnly = true)
public class SearchCommunityRepositoryImpl
        extends QuerydslRepositorySupport
        implements SearchCommunityRepository {

    public SearchCommunityRepositoryImpl(EntityManager em) {
        super(Community.class);
        setEntityManager(em);
    }

    @Override
    public Page<Object[]> list(String type, String keyword, List<String> styleNames, Pageable pageable) {
        QCommunity community = QCommunity.community;
        QCommunityStyle style = QCommunityStyle.communityStyle;
        QCommunityImage image = QCommunityImage.communityImage;
        QCommunityTemp temp = QCommunityTemp.communityTemp;

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
                        image.fileName, // repImageUrl
                        community.createdDate,
                        style.casual,
                        style.street,
                        style.formal,
                        style.outdoor,
                        temp.dayAvgTemp,
                        temp.nightAvgTemp // 변경: style.sports → style.outdoor
                )
                .distinct();

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(community.id.gt(0L));

        if (type != null && keyword != null && !keyword.trim().isEmpty()) {
            BooleanBuilder tb = new BooleanBuilder();
            if (type.contains("t"))
                tb.or(community.title.containsIgnoreCase(keyword));
            if (type.contains("a"))
                tb.or(community.author.name.containsIgnoreCase(keyword));
            if (type.contains("c"))
                tb.or(
                        community.content.containsIgnoreCase(keyword));
            builder.and(tb);
        }

        if (styleNames != null && !styleNames.isEmpty()) {
            BooleanBuilder sb = new BooleanBuilder();
            for (String s : styleNames) {
                switch (s.toUpperCase()) {
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
                        break; // 변경
                }
            }
            builder.and(sb);
        }

        query.where(builder);

        for (Sort.Order o : pageable.getSort()) {
            Order dir = o.isAscending() ? Order.ASC : Order.DESC;
            PathBuilder<Community> path = new PathBuilder<>(Community.class, "community");
            query.orderBy(new OrderSpecifier<>(dir,
                    path.getComparable(o.getProperty(), Comparable.class)));
        }

        query.offset(pageable.getOffset());
        query.limit(pageable.getPageSize());

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
                        t.get(temp.dayAvgTemp), // 변경: t.get(style.sports) → t.get(style.outdoor)
                        t.get(temp.nightAvgTemp) // 변경: t.get(style.sports) → t.get(style.outdoor)
                })
                .collect(Collectors.toList());

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
                        style.outdoor // 변경
                )
                .where(community.id.eq(id));

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
                t.get(style.outdoor) // 변경
        };
    }
}
