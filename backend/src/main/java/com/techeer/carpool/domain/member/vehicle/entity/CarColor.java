package com.techeer.carpool.domain.member.vehicle.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CarColor {
    WHITE("흰색", "#FFFFFF"),
    BLACK("검정", "#000000"),
    GRAY("회색", "#808080"),
    SILVER("은색", "#C0C0C0"),
    RED("빨강", "#FF0000"),
    BLUE("파랑", "#0000FF"),
    BROWN("갈색", "#8B4513");

    private final String label;
    private final String hexCode;
}
