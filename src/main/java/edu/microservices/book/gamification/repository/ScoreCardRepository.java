/******************************************************************************
 * Copyright (C) 2017 ShenZhen Powerdata Information Technology Co.,Ltd
 * All Rights Reserved.
 * 本软件为深圳博安达开发研制。未经本公司正式书面同意，其他任何个人、团体不得使用、
 * 复制、修改或发布本软件.
 *****************************************************************************/

package edu.microservices.book.gamification.repository;

import edu.microservices.book.gamification.domain.LeaderBoardRow;
import edu.microservices.book.gamification.domain.ScoreCard;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * class summary
 * <p>
 * class detail description
 *
 * @author Siyi Lu
 * @since 2020/7/7
 */
public interface ScoreCardRepository extends CrudRepository<ScoreCard, Long> {

    @Query("SELECT SUM (s.score) FROM edu.microservices.book.gamification.domain.ScoreCard s" +
            " WHERE s.userId = :userId GROUP BY s.userId"
    )
    int getTotalScoreForUser(@Param("userId") final Long userId);

    @Query("SELECT NEW edu.microservices.book.gamification.domain.LeaderBoardRow(s.userId, SUM(s.score)) " +
            " FROM edu.microservices.book.gamification.domain.ScoreCard s " +
            " GROUP BY s.userId ORDER BY SUM (s.score) DESC")
    List<LeaderBoardRow> findFirst10();

    List<ScoreCard> findByUserIdOrderByScoreTimestampDesc(final Long userId);

}
