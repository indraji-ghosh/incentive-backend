package org.example.incentivebackend.common.constant;


public final class ApiConstants {

    private ApiConstants() {
    }

    public static final String API_V1 = "/api/v1";

    //    MASTER
    public static final String BUSINESS_HEAD = API_V1 + "/business-head";
    public static final String SECTOR = API_V1 + "/sector";
    public static final String SITE = API_V1 + "/site";
    public static final String ROUTE = API_V1 + "/route";
    public static final String CLIENT = API_V1 + "/client";
    public static final String DESIGNATION = API_V1 + "/designation";
    public static final String ESCORT = API_V1 + "/escort";
    public static final String SERVICE = API_V1 + "/service";
    public static final String COMMODITY = API_V1 + "/commodity";
    public static final String PRODUCT_CATEGORY = API_V1 + "/product-category";
    public static final String PRODUCT = API_V1 + "/product";
    public static final String USERS = API_V1 + "/users";
    public static final String INCHARGE = API_V1 + "/incharge";
    public static final String CONTRACTOR = API_V1 + "/contractor";
    public static final String AUTH = API_V1 + "/auth";

    public static final String RAKE = API_V1 + "/rake";
    public static final String TRANSIT = API_V1 + "/transit";

    public static final String EXPENSES = API_V1 + "/expenses";

    public static final String REQUISITIONS = API_V1 + "/requisitions";

    // Security
    public static final String PAGE = API_V1 + "/page";

    public static final String PERMISSION = API_V1 + "/permission";

    public static final String PAGEPERMISSION = API_V1 + "/page-permission";

    public static final String DESIGNATIONPERMISSION = API_V1 + "/designation-permission";

    // master association
    public static final String BHEADSITEASSOCIATON = API_V1 + "/bhead-site";

    public static final String SECTORSITE = API_V1 + "/sector-site";

    public static final String SITEINCHARGE = API_V1 + "/site-incharge";

    public static final String SECTORINCHARGE = API_V1 + "/sector-incharge";

    public static final String SITECOMMODITY = API_V1 + "/site-commodity";

    public static final String SITESERVICE = API_V1 + "/site-service";

    public static final String CLIENT_SITE_COMMODITY_CONTRACTOR = API_V1 + "/client-site-commodity-contractor";

    public static final String CLIENT_SITE_COMMODITY_SERVICE = API_V1 + "/client-site-commodity-service";

    //  Client Site Destination Cmdt Service Type
    public static final String CLIENT_SITE_DESTINATION = API_V1 + "/client-site-destination";

    // approval

    public static final String APPROVAL = API_V1 + "/approval" ;
}
