package org.personal.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.personal.project.dto.coupon.request.CouponPolicyCreateRequest;
import org.personal.project.dto.coupon.request.CouponPolicyUpdateRequest;
import org.personal.project.dto.coupon.response.CouponPolicyCreateResponse;
import org.personal.project.dto.coupon.response.CouponPolicyDetailResponse;
import org.personal.project.dto.coupon.response.CouponPolicyStatusResponse;
import org.personal.project.dto.coupon.response.CouponPolicySummaryResponse;
import org.personal.project.dto.coupon.response.MemberCouponResponse;
import org.personal.project.dto.page.PageRequestDTO;
import org.personal.project.dto.page.PageResponseDTO;
import org.personal.project.entity.coupon.MemberCouponStatus;
import org.personal.project.service.coupon.CouponPolicyCommandService;
import org.personal.project.service.coupon.CouponPolicyQueryService;
import org.personal.project.service.coupon.MemberCouponQueryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 쿠폰 API
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/coupons")
public class CouponAdminController {

    private final CouponPolicyCommandService couponPolicyCommandService;
    private final CouponPolicyQueryService couponPolicyQueryService;
    private final MemberCouponQueryService memberCouponQueryService;

    /**
     * 쿠폰 정책 목록 조회
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @GetMapping
    public PageResponseDTO<CouponPolicySummaryResponse> getPolicies(PageRequestDTO pageRequestDTO) {
        return couponPolicyQueryService.getPolicies(pageRequestDTO);
    }

    /**
     * 사용자 쿠폰 운영 조회
     * <p>
     * memberId/status/policyId 조합으로 동적 조회 가능
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @GetMapping("/member-coupons")
    public PageResponseDTO<MemberCouponResponse> getMemberCoupons(
            @RequestParam(required = false) String memberId,
            @RequestParam(required = false) MemberCouponStatus status,
            @RequestParam(required = false) Long policyId,
            PageRequestDTO pageRequestDTO
    ) {
        return memberCouponQueryService.getAdminMemberCoupons(memberId, status, policyId, pageRequestDTO);
    }

    /**
     * 쿠폰 정책 상세 조회
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @GetMapping("/{policyId}")
    public CouponPolicyDetailResponse getPolicy(@PathVariable Long policyId) {
        return couponPolicyQueryService.getPolicy(policyId);
    }

    /**
     * 쿠폰 정책 생성
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PostMapping
    public CouponPolicyCreateResponse createPolicy(@Valid @RequestBody CouponPolicyCreateRequest request) {
        log.info("관리자 쿠폰 정책 생성 요청 name={}, issueType={}, applyScope={}",
                request.name(), request.issueType(), request.applyScope());

        CouponPolicyCreateResponse response = couponPolicyCommandService.createPolicy(request);

        log.info("관리자 쿠폰 정책 생성 완료 policyId={}, status={}", response.policyId(), response.status());
        return response;
    }

    /**
     * 쿠폰 정책 수정
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PutMapping("/{policyId}")
    public CouponPolicyDetailResponse updatePolicy(
            @PathVariable Long policyId,
            @Valid @RequestBody CouponPolicyUpdateRequest request
    ) {
        log.info("관리자 쿠폰 정책 수정 요청 policyId={}, name={}", policyId, request.name());
        return couponPolicyCommandService.updatePolicy(policyId, request);
    }

    /**
     * 쿠폰 정책 활성화
     * DRAFT -> ACTIVE
     * 쿠폰 상태 변화?를 하나로 합칠 수 있는데 현재 상황을 유지하는게 더 좋은 것 같아서 유지
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PatchMapping("/{policyId}/activate")
    public CouponPolicyStatusResponse activate(@PathVariable Long policyId) {
        return couponPolicyCommandService.activate(policyId);
    }

    /**
     * 쿠폰 정책 일시 중지
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PatchMapping("/{policyId}/pause")
    public CouponPolicyStatusResponse pause(@PathVariable Long policyId) {
        return couponPolicyCommandService.pause(policyId);
    }

    /**
     * 쿠폰 정책 재개
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PatchMapping("/{policyId}/resume")
    public CouponPolicyStatusResponse resume(@PathVariable Long policyId) {
        return couponPolicyCommandService.resume(policyId);
    }

    /**
     * 쿠폰 발급 종료
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PatchMapping("/{policyId}/close-issue")
    public CouponPolicyStatusResponse closeIssue(@PathVariable Long policyId) {
        return couponPolicyCommandService.closeIssue(policyId);
    }

    /**
     * 쿠폰 정책 취소
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PatchMapping("/{policyId}/cancel")
    public CouponPolicyStatusResponse cancel(@PathVariable Long policyId) {
        return couponPolicyCommandService.cancel(policyId);
    }
}
