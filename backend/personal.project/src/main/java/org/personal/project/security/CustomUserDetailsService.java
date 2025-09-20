package org.personal.project.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.personal.project.dto.MemberDTO;
import org.personal.project.entity.Member;
import org.personal.project.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 사용자 인증 정보 로드 클래스
 */
@RequiredArgsConstructor
@Service
@Log4j2
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    /**
     * MemberDTO의 User 클래스는 UserDetails 인터페이스를 구현한 구체적인 클래스
     *
     * @param username: 사용자가 입력한 아이디
     * @return 사용자가 로그인할 때 입력한 아이디를 기반으로 데이터베이스에서 사용자 정보를 찾아 반환
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("----------------loadUserByUsername----------------------");

        Optional<Member> findMember = memberRepository.getWithRoles(username);

        Member member = findMember.orElseThrow(
                () -> new UsernameNotFoundException("Not Found"));

        MemberDTO memberDTO = new MemberDTO(
                member.getEmail(),
                member.getPw(),
                member.getNickname(),
                member.isSocial(),
                member.getMemberRoleList().stream()
                        .map(memberRole -> memberRole.name()).collect(Collectors.toList()));

        log.info(memberDTO);

        return memberDTO;
    }

}
