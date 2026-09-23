package com.campustrade.enums;
public enum ProductStatusEnum {
    SELLING("出售中"), SOLD("已卖出"), TAKEN_DOWN("已下架");
    private final String desc;
    ProductStatusEnum(String d) { this.desc = d; }
    public String getDesc() { return desc; }
}
