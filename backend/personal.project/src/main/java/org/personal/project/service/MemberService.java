package org.personal.project.service;

import org.personal.project.dto.MemberDTO;

public interface MemberService {

    MemberDTO getKakaoMember(String accessToken);

}
