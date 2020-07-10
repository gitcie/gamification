package edu.microservices.book.gamification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.microservices.book.gamification.client.MultiplicationResultAttemptClient;
import edu.microservices.book.gamification.domain.Badge;
import edu.microservices.book.gamification.domain.BadgeCard;
import edu.microservices.book.gamification.domain.GameStats;
import edu.microservices.book.gamification.domain.ScoreCard;
import edu.microservices.book.gamification.repository.BadgeCardRepository;
import edu.microservices.book.gamification.repository.ScoreCardRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.json.JacksonTester;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * class summary
 * <p>
 * class detail description
 *
 * @author Siyi Lu
 * @since 2020/7/7
 */
public class GameServiceImplTest {

    private GameService gameService;

    @Mock
    private BadgeCardRepository badgeCardRepository;

    @Mock
    private ScoreCardRepository scoreCardRepository;

    @Mock
    private MultiplicationResultAttemptClient attemptClient;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        gameService = new GameServiceImpl(badgeCardRepository, scoreCardRepository, attemptClient);
    }

    @Test
    public void newAttemptForUserTest() {
        long userId = 1;
        long attemptId = 1;
        int score = 10;

        given(scoreCardRepository.getTotalScoreForUser(userId)).willReturn(10);

        List<ScoreCard> mockCards = createMockScoreCards(userId, attemptId);
        given(scoreCardRepository.findByUserIdOrderByScoreTimestampDesc(userId))
                .willReturn(mockCards);

        List<BadgeCard> mockBadgeCards = createMockBadgeCards(userId);
        given(badgeCardRepository.findByUserIdOrderByBadgeTimestampDesc(userId))
                .willReturn(Collections.emptyList());
        GameStats mockGameStats = new GameStats(userId, score,
                mockBadgeCards.stream()
                    .map(BadgeCard::getBadge)
                    .collect(Collectors.toList())
        );

        GameStats getStats = gameService.newAttemptForUser(userId, attemptId, true);

        assertThat(mockGameStats).isEqualTo(getStats);
    }

    private List<ScoreCard> createMockScoreCards(final Long userId, final Long attemptId){
        List<ScoreCard> mockCards = new ArrayList<>();
        mockCards.add(new ScoreCard(userId, attemptId));
        return mockCards;
    }

    private List<BadgeCard> createMockBadgeCards(final Long userId){
        List<BadgeCard> mockCards = new ArrayList<>();
        mockCards.add(new BadgeCard(userId, Badge.FIRST_WON));
        return mockCards;
    }

    @Test
    public void testForScoreBadgeTest() {
        long userId = 1L;
        long attemptId = 10L;
        int score = 100;

        BadgeCard firstWonBadgeCard = new BadgeCard(userId, Badge.FIRST_WON);
        given(scoreCardRepository.getTotalScoreForUser(userId)).willReturn(score);

        given(scoreCardRepository.findByUserIdOrderByScoreTimestampDesc(userId))
                .willReturn(createScoreCards(10, userId));

        given(badgeCardRepository.findByUserIdOrderByBadgeTimestampDesc(userId))
                .willReturn(Collections.singletonList(firstWonBadgeCard));
        GameStats stats = gameService.newAttemptForUser(userId, attemptId, true);

        assertThat(stats.getScore()).isEqualTo(ScoreCard.DEFAULT_SCORE);
        assertThat(stats.getBadges()).containsOnly(Badge.BRONZE_MULTIPLICATOR);
    }

    private List<ScoreCard> createScoreCards(int times, Long userId){
        return IntStream.range(0, times)
                .mapToObj(i -> new ScoreCard(userId, (long)i))
                .collect(Collectors.toList());
    }

    @Test
    public void testRetrieveUserBadgesCards() {
        int score = 1000;
        long userId = 1L;

        given(scoreCardRepository.getTotalScoreForUser(userId)).willReturn(score);

        BadgeCard badgeCard = new BadgeCard(userId, Badge.GOLD_MULTIPLICATOR);
        given(badgeCardRepository.findByUserIdOrderByBadgeTimestampDesc(userId))
                .willReturn(Collections.singletonList(badgeCard));

        GameStats stats = gameService.retrieveStatsForUser(userId);

        assertThat(stats.getScore()).isEqualTo(score);
        assertThat(stats.getBadges()).containsOnly(Badge.GOLD_MULTIPLICATOR);
    }
}