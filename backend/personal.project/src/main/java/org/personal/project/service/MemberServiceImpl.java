package org.personal.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.personal.project.dto.member.MemberDTO;
import org.personal.project.dto.member.MemberModifyDTO;
import org.personal.project.entity.Member;
import org.personal.project.entity.MemberRole;
import org.personal.project.repository.MemberRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashMap;
import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
@Log4j2
public class MemberServiceImpl implements MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final RestTemplate kakaoRestTemplate;

    @Override
    public MemberDTO getKakaoMember(String accessToken) {

        String email = getEmailFromKakaoAccessToken(accessToken);
        log.info("email: " + email);

        Optional<Member> result = memberRepository.findById(email);

        //기존 회원
        if (result.isPresent()) {
            MemberDTO memberDTO = entityToDTO(result.get());
            return memberDTO;
        }

        //회원이 아니었다면 닉네임은 '소셜회원’으로 패스워드는 임의로 생성  
        Member socialMember = makeSocialMember(email);
        memberRepository.save(socialMember);

        return entityToDTO(socialMember);
    }

    @Override
    public void modifyMember(MemberModifyDTO memberModifyDTO) {
        Optional<Member> result = memberRepository.findById(memberModifyDTO.getEmail());

        Member member = result.orElseThrow();

        member.changePw(passwordEncoder.encode(memberModifyDTO.getPw()));
        member.changeNickname(memberModifyDTO.getNickname());

        memberRepository.save(member);
    }


    private String getEmailFromKakaoAccessToken(String accessToken) {

        String kakaoGetUserURL = "https://kapi.kakao.com/v2/user/me";

        if (accessToken == null) {
            throw new RuntimeException("Access Token is null");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-Type", "application/x-www-form-urlencoded");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        UriComponents uriBuilder = UriComponentsBuilder.fromUriString(kakaoGetUserURL).build();
        ResponseEntity<LinkedHashMap> response
                = kakaoRestTemplate.exchange(uriBuilder.toString(), HttpMethod.GET, entity, LinkedHashMap.class);

        log.info(response);

        LinkedHashMap<String, LinkedHashMap> bodyMap = response.getBody();

        log.info("------------------------------------");

        log.info(bodyMap);

        LinkedHashMap<String, String> kakaoAccount = bodyMap.get("kakao_account");

        log.info("kakaoAccount: " + kakaoAccount);
        return kakaoAccount.get("email");
    }

    /**
     * 10자리 임시 비밀번호를 무작위로 생성 메소드
     */
    private String makeTempPassword() {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < 10; i++) {
            sb.append((char) ((int) (Math.random() * 55) + 65));
        }

        return sb.toString();
    }


    private Member makeSocialMember(String email) {
        String tempPassword = makeTempPassword();
        log.info("tempPassword: {}", tempPassword);

        String nickname = "소셜회원";

        Member member = Member.builder()
                .email(email)
                .pw(passwordEncoder.encode(tempPassword))
                .nickname(nickname)
                .social(true)
                .build();

        member.addRole(MemberRole.USER);

        return member;
    }
}
