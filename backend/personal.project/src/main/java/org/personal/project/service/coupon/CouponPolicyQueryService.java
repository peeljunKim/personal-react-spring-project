package org.personal.project.service.coupon;

import lombok.RequiredArgsConstructor;
import org.personal.project.dto.coupon.response.CouponPolicyDetailResponse;
import org.personal.project.dto.coupon.response.CouponPolicySummaryResponse;
import org.personal.project.dto.page.PageRequestDTO;
import org.personal.project.dto.page.PageResponseDTO;
import org.personal.project.entity.coupon.CouponPolicy;
import org.personal.project.exception.CouponException;
import org.personal.project.repository.coupon.CouponPolicyRepository;
import org.personal.project.repository.coupon.CouponTargetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 쿠폰 정책 조회 처리
 */
@Service
@RequiredArgsConstructor
public class CouponPolicyQueryService {

    private final CouponPolicyRepository couponPolicyRepository;
    private final CouponTargetRepository couponTargetRepository;

    /**
     * 쿠폰 정책 목록 조회
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<CouponPolicySummaryResponse> getPolicies(PageRequestDTO pageRequestDTO) {
        Page<CouponPolicy> result = couponPolicyRepository.findAll(toPageable(pageRequestDTO));
        List<CouponPolicySummaryResponse> dtoList = result.getContent().stream()
                .map(CouponPolicyMapper::toSummaryResponse)
                .toList();

        return PageResponseDTO.<CouponPolicySummaryResponse>withAll()
                .dtoList(dtoList)
                .pageRequestDTO(pageRequestDTO)
                .totalCount(result.getTotalElements())
                .build();
    }

    /**
     * 쿠폰 정책 상세 조회
     */
    @Transactional(readOnly = true)
    public CouponPolicyDetailResponse getPolicy(Long policyId) {
        CouponPolicy policy = couponPolicyRepository.findById(policyId)
                .orElseThrow(() -> new CouponException("쿠폰 정책을 찾을 수 없습니다. policyId=" + policyId));
        return CouponPolicyMapper.toDetailResponse(
                policy,
                couponTargetRepository.findByPolicyPolicyId(policyId)
        );
    }

    /**
     * 페이지 요청 변환
     */
    private Pageable toPageable(PageRequestDTO pageRequestDTO) {
        int page = Math.max(pageRequestDTO.getPage(), 1) - 1;
        int size = Math.max(pageRequestDTO.getSize(), 1);
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "policyId"));
    }
}
