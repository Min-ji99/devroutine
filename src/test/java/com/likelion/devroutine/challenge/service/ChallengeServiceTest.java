package com.likelion.devroutine.challenge.service;

import com.likelion.devroutine.challenge.domain.Challenge;
import com.likelion.devroutine.challenge.dto.ChallengeCreateRequest;
import com.likelion.devroutine.challenge.dto.ChallengeDto;
import com.likelion.devroutine.challenge.dto.ChallengeModifiyRequest;
import com.likelion.devroutine.challenge.dto.ChallengeResponse;
import com.likelion.devroutine.challenge.enumerate.ResponseMessage;
import com.likelion.devroutine.challenge.exception.ChallengeNotFoundException;
import com.likelion.devroutine.challenge.exception.InProgressingChallengeException;
import com.likelion.devroutine.challenge.exception.InaccessibleChallengeException;
import com.likelion.devroutine.challenge.exception.InvalidPermissionException;
import com.likelion.devroutine.challenge.repository.ChallengeRepository;
import com.likelion.devroutine.hashtag.repository.ChallengeHashTagRepository;
import com.likelion.devroutine.hashtag.repository.HashTagRepository;
import com.likelion.devroutine.participant.repository.ParticipationRepository;
import com.likelion.devroutine.support.ChallengeFixture;
import com.likelion.devroutine.support.UserFixture;
import com.likelion.devroutine.user.domain.User;
import com.likelion.devroutine.user.exception.UserNotFoundException;
import com.likelion.devroutine.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

class ChallengeServiceTest {
    private ChallengeService challengeService;
    private ChallengeRepository challengeRepository = mock(ChallengeRepository.class);
    private ChallengeHashTagRepository challengeHashTagRepository = mock(ChallengeHashTagRepository.class);
    private HashTagRepository hashTagRepository = mock(HashTagRepository.class);
    private UserRepository userRepository = mock(UserRepository.class);
    private ParticipationRepository participationRepository =mock(ParticipationRepository.class);
    
    private User userFixture;
    private User mockUser;
    private Challenge challengeFixture;
    private Challenge mockChallenge;

    private final ChallengeCreateRequest challengeCreateRequest =
            new ChallengeCreateRequest("title", "description", true, null, "#test1 #test2", null, null);
    private final ChallengeModifiyRequest challengeModifyRequest=
            new ChallengeModifiyRequest("modify title", "modify description", true, null, "#test3");

    @BeforeEach
    void setup() {
        challengeService = new ChallengeService(
                challengeRepository, hashTagRepository,
                challengeHashTagRepository, userRepository, participationRepository);
        userFixture = UserFixture.getUser();
        challengeFixture = ChallengeFixture.getChallenge(userFixture);
        mockUser = mock(User.class);
        mockChallenge = mock(Challenge.class);
    }

    @Test
    @DisplayName("????????? ?????? ?????? ??????")
    void findByChallengeId_success() {
        Mockito.when(challengeRepository.findById(challengeFixture.getId())).thenReturn(Optional.of(challengeFixture));

        ChallengeDto challengeDto = challengeService.findByChallengeId(challengeFixture.getId());

        assertEquals(challengeDto.getTitle(), challengeFixture.getTitle());
        assertEquals(challengeDto.getAuthenticationType(), challengeFixture.getAuthenticationType());
    }

    @Test
    @DisplayName("????????? ?????? ?????? ?????? - ????????? ???????????? ?????? ???")
    void findByChallengeId_fail1() {
        Mockito.when(challengeRepository.findById(challengeFixture.getId())).thenThrow(new ChallengeNotFoundException());

        assertThrows(ChallengeNotFoundException.class, () -> challengeService.findByChallengeId(challengeFixture.getId()));
    }

    @Test
    @DisplayName("????????? ?????? ?????? ?????? - ????????? ????????? ????????? ??? ")
    void findByChallengeId_fail2() {
        Mockito.when(challengeRepository.findById(challengeFixture.getId())).thenThrow(new InaccessibleChallengeException());

        assertThrows(InaccessibleChallengeException.class, () -> challengeService.findByChallengeId(challengeFixture.getId()));
    }

    @Test
    @DisplayName("????????? ?????? ??????")
    void createChallenge_success() {
        Mockito.when(userRepository.findByOauthId(mockUser.getOauthId())).thenReturn(Optional.of(mockUser));
        Mockito.when(challengeRepository.save(any())).thenReturn(mockChallenge);

        assertDoesNotThrow(() -> challengeService.createChallenge(mockUser.getOauthId(), challengeCreateRequest));
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ????????? ???????????? ?????? ???")
    void createChallenge_fail1() {
        Mockito.when(userRepository.findByOauthId(mockUser.getOauthId())).thenReturn(Optional.empty());
        Mockito.when(challengeRepository.save(any())).thenReturn(mockChallenge);

        assertThrows(UserNotFoundException.class,
                () -> challengeService.createChallenge(mockUser.getOauthId(), challengeCreateRequest));
    }

    @Test
    @DisplayName("????????? ?????? ??????")
    void modifyChallenge_success() {
        Mockito.when(userRepository.findByOauthId(mockUser.getOauthId())).thenReturn(Optional.of(mockUser));
        Mockito.when(challengeRepository.findById(mockChallenge.getId())).thenReturn(Optional.of(mockChallenge));
        Mockito.when(mockChallenge.getUser()).thenReturn(mockUser);
        Mockito.when(mockChallenge.getStartDate()).thenReturn(LocalDate.now().plusDays(2));

        ChallengeResponse challengeResponse=challengeService.modifyChallenge(mockUser.getOauthId(), mockChallenge.getId(), challengeModifyRequest);
        assertEquals(challengeResponse.getMessage(), ResponseMessage.CHALLENGE_MODIFY_SUCCESS.getMessage());
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ?????? ???????????? ??????")
    void modifyChallenge_fail1() {
        Mockito.when(userRepository.findByOauthId(mockUser.getOauthId())).thenReturn(Optional.empty());
        Mockito.when(challengeRepository.findById(mockChallenge.getId())).thenReturn(Optional.of(mockChallenge));

        assertThrows(UserNotFoundException.class,
                () -> challengeService.modifyChallenge(mockUser.getOauthId(), mockChallenge.getId(), challengeModifyRequest));
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ????????? ???????????? ??????")
    void modifyChallenge_fail2() {
        Mockito.when(userRepository.findByOauthId(mockUser.getOauthId())).thenReturn(Optional.of(mockUser));
        Mockito.when(challengeRepository.findById(mockChallenge.getId())).thenReturn(Optional.empty());

        assertThrows(ChallengeNotFoundException.class,
                () -> challengeService.modifyChallenge(mockUser.getOauthId(), mockChallenge.getId(), challengeModifyRequest));
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ????????? != ??????")
    void modifyChallenge_fail3(){
        Mockito.when(userRepository.findByOauthId(mockUser.getOauthId())).thenReturn(Optional.of(mockUser));
        Mockito.when(challengeRepository.findById(mockChallenge.getId())).thenReturn(Optional.of(mockChallenge));
        Mockito.when(mockChallenge.getUser()).thenReturn(userFixture);

        assertThrows(InvalidPermissionException.class,
                ()->challengeService.modifyChallenge(mockUser.getOauthId(), mockChallenge.getId(), challengeModifyRequest));
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ?????? ????????? ??????")
    void modifyChallenge_fail4(){
        Mockito.when(userRepository.findByOauthId(mockUser.getOauthId())).thenReturn(Optional.of(mockUser));
        Mockito.when(challengeRepository.findById(mockChallenge.getId())).thenReturn(Optional.of(mockChallenge));
        Mockito.when(mockChallenge.getUser()).thenReturn(mockUser);
        Mockito.when(mockChallenge.getStartDate()).thenReturn(LocalDate.now().minusDays(2));

        assertThrows(InProgressingChallengeException.class,
                ()->challengeService.modifyChallenge(mockUser.getOauthId(), mockChallenge.getId(), challengeModifyRequest));
    }

    @Test
    @DisplayName("????????? ?????? ??????")
    void deleteChallenge_success() {
        Mockito.when(userRepository.findByOauthId(mockUser.getOauthId())).thenReturn(Optional.of(mockUser));
        Mockito.when(challengeRepository.findById(mockChallenge.getId())).thenReturn(Optional.of(mockChallenge));
        Mockito.when(mockChallenge.getUser()).thenReturn(mockUser);
        Mockito.when(mockChallenge.getStartDate()).thenReturn(LocalDate.now().plusDays(2));

        ChallengeResponse challengeResponse=challengeService.deleteChallenge(mockUser.getOauthId(), mockChallenge.getId());
        assertEquals(challengeResponse.getMessage(), ResponseMessage.CHALLENGE_DELETE_SUCCESS.getMessage());
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ?????? ???????????? ??????")
    void deleteChallenge_fail1() {
        Mockito.when(userRepository.findByOauthId(mockUser.getOauthId())).thenReturn(Optional.empty());
        Mockito.when(challengeRepository.findById(mockChallenge.getId())).thenReturn(Optional.of(mockChallenge));

        assertThrows(UserNotFoundException.class,
                () -> challengeService.deleteChallenge(mockUser.getOauthId(), mockChallenge.getId()));
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ????????? ???????????? ??????")
    void deleteChallenge_fail2() {
        Mockito.when(userRepository.findByOauthId(mockUser.getOauthId())).thenReturn(Optional.of(mockUser));
        Mockito.when(challengeRepository.findById(mockChallenge.getId())).thenReturn(Optional.empty());

        assertThrows(ChallengeNotFoundException.class,
                () -> challengeService.deleteChallenge(mockUser.getOauthId(), mockChallenge.getId()));
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ????????? != ??????")
    void deleteChallenge_fail3(){
        Mockito.when(userRepository.findByOauthId(mockUser.getOauthId())).thenReturn(Optional.of(mockUser));
        Mockito.when(challengeRepository.findById(mockChallenge.getId())).thenReturn(Optional.of(mockChallenge));
        Mockito.when(mockChallenge.getUser()).thenReturn(userFixture);

        assertThrows(InvalidPermissionException.class,
                ()->challengeService.deleteChallenge(mockUser.getOauthId(), mockChallenge.getId()));
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ?????? ????????? ??????")
    void deleteChallenge_fail4() {
        Mockito.when(userRepository.findByOauthId(mockUser.getOauthId())).thenReturn(Optional.of(mockUser));
        Mockito.when(challengeRepository.findById(mockChallenge.getId())).thenReturn(Optional.of(mockChallenge));
        Mockito.when(mockChallenge.getUser()).thenReturn(mockUser);
        Mockito.when(mockChallenge.getStartDate()).thenReturn(LocalDate.now().minusDays(2));

        assertThrows(InProgressingChallengeException.class,
                () -> challengeService.deleteChallenge(mockUser.getOauthId(), mockChallenge.getId()));
    }
}