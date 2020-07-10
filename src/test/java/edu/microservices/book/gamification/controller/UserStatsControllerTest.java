package edu.microservices.book.gamification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.microservices.book.gamification.domain.Badge;
import edu.microservices.book.gamification.domain.BadgeCard;
import edu.microservices.book.gamification.domain.GameStats;
import edu.microservices.book.gamification.service.GameService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * class summary
 * <p>
 * class detail description
 *
 * @author Siyi Lu
 * @since 2020/7/9
 */
@RunWith(SpringRunner.class)
@WebMvcTest(UserStatsController.class)
public class UserStatsControllerTest {

    @MockBean
    private GameService gameService;

    @Autowired
    private MockMvc mvc;

    private JacksonTester<GameStats> gameStatsJacksonTester;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        JacksonTester.initFields(this, new ObjectMapper());
    }

    @Test
    public void getStatsForUser() throws Exception{
        Long userId = 1L;
        int score = 1000;
        GameStats mockGameStats = new GameStats(userId, score, Collections.singletonList(Badge.GOLD_MULTIPLICATOR));

        given(gameService.retrieveStatsForUser(userId))
                .willReturn(mockGameStats);

        MockHttpServletResponse response = mvc.perform(
                get("/stats")
                        .param("userId", String.valueOf(userId))
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(gameStatsJacksonTester.write(mockGameStats).getJson());

    }
}