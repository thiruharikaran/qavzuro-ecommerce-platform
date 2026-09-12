package com.qavzuro.service;

/** Central registry of all permission codes used by the system. */
public final class PermissionCodes {
    private PermissionCodes() {}

    public static final String USER_VIEW = "USER_VIEW";
    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_DELETE = "USER_DELETE";
    public static final String ROLE_MANAGE = "ROLE_MANAGE";

    public static final String PRODUCT_VIEW = "PRODUCT_VIEW";
    public static final String PRODUCT_CREATE = "PRODUCT_CREATE";
    public static final String PRODUCT_UPDATE = "PRODUCT_UPDATE";
    public static final String PRODUCT_DELETE = "PRODUCT_DELETE";
    public static final String CATEGORY_MANAGE = "CATEGORY_MANAGE";

    public static final String ORDER_VIEW = "ORDER_VIEW";
    public static final String ORDER_UPDATE = "ORDER_UPDATE";
    public static final String ORDER_CANCEL = "ORDER_CANCEL";

    public static final String RETURN_VIEW = "RETURN_VIEW";
    public static final String RETURN_APPROVE = "RETURN_APPROVE";
    public static final String REFUND_PROCESS = "REFUND_PROCESS";

    public static final String INVENTORY_VIEW = "INVENTORY_VIEW";
    public static final String INVENTORY_UPDATE = "INVENTORY_UPDATE";

    public static final String COUPON_MANAGE = "COUPON_MANAGE";
    public static final String REVIEW_MODERATE = "REVIEW_MODERATE";

    public static final String WORKFORCE_VIEW = "WORKFORCE_VIEW";
    public static final String WORKFORCE_MANAGE = "WORKFORCE_MANAGE";
    public static final String WORKFORCE_TASK_ASSIGN = "WORKFORCE_TASK_ASSIGN";
    public static final String WORKFORCE_TASK_UPDATE_OWN = "WORKFORCE_TASK_UPDATE_OWN";

    public static final String AUDIT_VIEW = "AUDIT_VIEW";
    public static final String REPORT_VIEW = "REPORT_VIEW";
    public static final String SETTINGS_MANAGE = "SETTINGS_MANAGE";

    public static final String MASTER_ADMIN_ALL = "MASTER_ADMIN_ALL";

    public static java.util.List<String> all() {
        return java.util.List.of(
                USER_VIEW, USER_CREATE, USER_UPDATE, USER_DELETE, ROLE_MANAGE,
                PRODUCT_VIEW, PRODUCT_CREATE, PRODUCT_UPDATE, PRODUCT_DELETE, CATEGORY_MANAGE,
                ORDER_VIEW, ORDER_UPDATE, ORDER_CANCEL,
                RETURN_VIEW, RETURN_APPROVE, REFUND_PROCESS,
                INVENTORY_VIEW, INVENTORY_UPDATE,
                COUPON_MANAGE, REVIEW_MODERATE,
                WORKFORCE_VIEW, WORKFORCE_MANAGE, WORKFORCE_TASK_ASSIGN, WORKFORCE_TASK_UPDATE_OWN,
                AUDIT_VIEW, REPORT_VIEW, SETTINGS_MANAGE,
                MASTER_ADMIN_ALL
        );
    }
}
