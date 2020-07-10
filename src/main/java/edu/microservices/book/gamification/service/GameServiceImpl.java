/******************************************************************************
 * Copyright (C) 2017 ShenZhen Powerdata Information Technology Co.,Ltd
 * All Rights Reserved.
 * 本软件为深圳博安达开发研制。未经本公司正式书面同意，其他任何个人、团体不得使用、
 * 复制、修改或发布本软件.
 *****************************************************************************/

package edu.microservices.book.gamification.service;

import edu.microservices.book.gamification.client.MultiplicationResultAttemptClient;
import edu.microservices.book.gamification.client.dto.MultiplicationResultAttempt;
import edu.microservices.book.gamification.domain.Badge;
import edu.microservices.book.gamification.domain.BadgeCard;
import edu.microservices.book.gamification.domain.GameStats;
import edu.microservices.book.gamification.domain.ScoreCard;
import edu.microservices.book.gamification.repository.BadgeCardRepository;
import edu.microservices.book.gamification.repository.ScoreCardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * class summary
 * <p>
 * class detail description
 *
 * @author Siyi Lu
 * @since 2020/7/7
 */
@Service
@Slf4j
public class GameServiceImpl implements GameService {

    private BadgeCardRepository badgeCardRepository;

    private ScoreCardRepository scoreCardRepository;

    private MultiplicationResultAttemptClient attemptClient;

    @Autowired
    public GameServiceImpl(
            final BadgeCardRepository badgeCardRepository,
            final ScoreCardRepository scoreCardRepository,
            final MultiplicationResultAttemptClient attemptClient
    ){
        this.badgeCardRepository = badgeCardRepository;
        this.scoreCardRepository = scoreCardRepository;
        this.attemptClient = attemptClient;
    }

    @Override
    public GameStats newAttemptForUser(Long userId, Long attemptId, boolean correct) {
        if (correct) {
            ScoreCard scoreCard = new ScoreCard(userId, attemptId);
            scoreCardRepository.save(scoreCard);
            log.info("User with id {} scroed {} points for attempt id {} ", userId, scoreCard.getScore(), attemptId);
            List<BadgeCard> badgeCards = processBadges(userId, attemptId);
            return new GameStats(userId, scoreCard.getScore(),
                    badgeCards.stream()
                            .map(BadgeCard::getBadge)
                            .collect(Collectors.toList())
            );
        }
        return GameStats.emptyStats(userId);
    }

    @Override
    public GameStats retrieveStatsForUser(Long userId) {
        int score = scoreCardRepository.getTotalScoreForUser(userId);
        List<BadgeCard> badgeCards = badgeCardRepository.findByUserIdOrderByBadgeTimestampDesc(userId);
        return new GameStats(userId, score, badgeCards.stream()
                .map(BadgeCard::getBadge)
                .collect(Collectors.toList())
        );
    }

    private List<BadgeCard> processBadges(final Long userId, final Long attemptId){
        List<BadgeCard> badgeCards = new ArrayList<>();
        int totalScore = scoreCardRepository.getTotalScoreForUser(userId);
        log.info("New score for user {} is {}", userId, totalScore);
        List<ScoreCard> scoreCardList = scoreCardRepository.findByUserIdOrderByScoreTimestampDesc(userId);
        final List<BadgeCard> badgeCardList = badgeCardRepository.findByUserIdOrderByBadgeTimestampDesc(userId);

        Stream.of(Badge.BRONZE_MULTIPLICATOR, Badge.SILVER_MULTIPLICATOR, Badge.GOLD_MULTIPLICATOR)
                .collect(Collectors.toList())
                .forEach(badge -> {
                    checkAndGiveBadgeBasedOnScore(badgeCardList,
                            badge, totalScore, badge.getScore(), userId
                    ).ifPresent(badgeCards::add);
                });

        if (scoreCardList.size() == 1 && !containsBadge(badgeCardList, Badge.FIRST_WON)) {
            BadgeCard firstWonBadge = giveBadgeToUser(Badge.FIRST_WON, userId);
            badgeCards.add(firstWonBadge);
        }
        MultiplicationResultAttempt resultAttempt = attemptClient.retrieveMultiplicationResultAttemptById(attemptId);
        if(!containsBadge(badgeCardList, Badge.LUCKY_NUMBER) &&
                (Badge.LUCKY_NUMBER.getScore() == resultAttempt.getMultiplicationFactorA() ||
                        Badge.LUCKY_NUMBER .getScore()== resultAttempt.getMultiplicationFactorB())
        ){
            BadgeCard badgeCard = giveBadgeToUser(Badge.LUCKY_NUMBER, userId);
            badgeCards.add(badgeCard);
        }
        return badgeCards;
    }

    private Optional<BadgeCard> checkAndGiveBadgeBasedOnScore(
            final List<BadgeCard> badgeCards,
            final Badge badge,
            final int score, final int scoreThreshold, final Long userId){
        if (score >= scoreThreshold && !containsBadge(badgeCards, badge)) {
            return Optional.of(giveBadgeToUser(badge, userId));
        } else {
          return Optional.empty();
        }
    }

    private boolean containsBadge(final List<BadgeCard> badgeCards, final Badge badge) {
        return badgeCards.stream().anyMatch(b -> b.getBadge().equals(badge));
    }

    private BadgeCard giveBadgeToUser(final Badge badge, final Long userId){
        BadgeCard badgeCard = new BadgeCard(userId, badge);
        badgeCardRepository.save(badgeCard);
        log.info("User with id {} won a new badge: {}", userId, badge);
        return badgeCard;
    }
}
