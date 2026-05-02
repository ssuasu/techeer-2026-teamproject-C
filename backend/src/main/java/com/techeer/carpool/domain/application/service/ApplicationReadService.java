@Transactional(readOnly = true)
public ApplicationResponse getApplication(Long applicationId, Long requesterId) {
    Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new CarpoolException(ErrorCode.APPLICATION_NOT_FOUND));

    if (!application.getApplicantId().equals(requesterId)) {
        throw new CarpoolException(ErrorCode.APPLICATION_FORBIDDEN);
    }

    String nickname = memberRepository.findById(application.getApplicantId())
            .map(Member::getNickname)
            .orElse("알 수 없음");

    return ApplicationResponse.of(application, nickname);
}