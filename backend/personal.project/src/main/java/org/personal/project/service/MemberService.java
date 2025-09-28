package org.personal.project.service;

import org.personal.project.dto.member.MemberDTO;
import org.personal.project.dto.member.MemberModifyDTO;
import org.personal.project.entity.Member;

import java.util.stream.Collectors;

public interface MemberService {

    MemberDTO getKakaoMember(String accessToken);

    void modifyMember(MemberModifyDTO memberModifyDTO);

    default MemberDTO entityToDTO(Member member) {
        MemberDTO dto = new MemberDTO(
                member.getEmail(),
                member.getPw(),
                member.getNickname(),
                member.isSocial(),
                member.getMemberRoleList().stream()
                        .map(memberRole -> memberRole.name()).collect(Collectors.toList()));
        return dto;
    }
}
