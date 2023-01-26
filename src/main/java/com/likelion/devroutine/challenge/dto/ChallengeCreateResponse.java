package com.likelion.devroutine.challenge.dto;

import com.likelion.devroutine.challenge.domain.Challenge;
import com.likelion.devroutine.challenge.enumerate.AuthenticationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class ChallengeCreateResponse {
    private Long challengeId;
    private String title;
    private String description;
    private String vigibility;
    private AuthenticationType authenticationType;
    //해시태그 추가

    public static ChallengeCreateResponse toResponse(Challenge challenge) {
        return ChallengeCreateResponse.builder()
                .challengeId(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .vigibility(challenge.getIsVigibility()?"공개":"비공개")
                .authenticationType(challenge.getAuthenticationType())
                .build();
    }
}
