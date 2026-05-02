package com.techeer.carpool.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    POST_NOT_FOUND("POST_001", "게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    POST_FORBIDDEN("POST_002", "게시글 수정/삭제 권한이 없습니다.", HttpStatus.FORBIDDEN),
    COMMENT_NOT_FOUND("COMMENT_001", "댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    COMMENT_FORBIDDEN("COMMENT_002", "댓글 삭제 권한이 없습니다.", HttpStatus.FORBIDDEN),
    APPLICATION_NOT_FOUND("APPLICATION_001", "신청을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    APPLICATION_DUPLICATE("APPLICATION_002", "이미 신청한 게시글입니다.", HttpStatus.CONFLICT),
    APPLICATION_SELF("APPLICATION_003", "본인 게시글에는 신청할 수 없습니다.", HttpStatus.BAD_REQUEST),
    APPLICATION_FORBIDDEN("APPLICATION_004", "신청 수락/거절 권한이 없습니다.", HttpStatus.FORBIDDEN),
    APPLICATION_POST_FULL("APPLICATION_005", "정원이 가득 찬 게시글입니다.", HttpStatus.CONFLICT),
    APPLICATION_ALREADY_PROCESSED("APPLICATION_006", "이미 처리된 신청입니다.", HttpStatus.CONFLICT),
    APPLICATION_NOT_ACCEPTED("APPLICATION_007", "수락된 신청이 아닙니다.", HttpStatus.CONFLICT),
    APPLICATION_NOT_REJECTED("APPLICATION_008", "거절된 신청이 아닙니다.", HttpStatus.CONFLICT),
    INVALID_INPUT("COMMON_001", "잘못된 입력값입니다.", HttpStatus.BAD_REQUEST),

    EMAIL_DUPLICATE("AUTH_001", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    MEMBER_NOT_FOUND("AUTH_002", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_CREDENTIALS("AUTH_003", "이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("AUTH_004", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("AUTH_005", "만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),

    RIDE_NOT_FOUND("RIDE_001", "운행을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    RIDE_FORBIDDEN("RIDE_002", "운행 제어 권한이 없습니다.", HttpStatus.FORBIDDEN),
    RIDE_INVALID_STATUS("RIDE_003", "현재 상태에서 허용되지 않는 작업입니다.", HttpStatus.CONFLICT),
    RIDE_PASSENGER_NOT_FOUND("RIDE_004", "탑승자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    MEMBER_FORBIDDEN("MEMBER_001", "본인의 프로필만 조회할 수 있습니다.", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public HttpStatus getStatus() { return status; }
}
