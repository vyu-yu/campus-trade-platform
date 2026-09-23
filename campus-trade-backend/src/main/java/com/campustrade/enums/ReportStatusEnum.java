package com.campustrade.enums;
public enum ReportStatusEnum {
    PENDING("待处理"), RESOLVED("已处理"), DISMISSED("已驳回");
    private final String desc;
    ReportStatusEnum(String d) { this.desc = d; }
    public String getDesc() { return desc; }
}
