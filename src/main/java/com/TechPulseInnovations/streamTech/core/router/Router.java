package com.TechPulseInnovations.streamTech.core.router;

public class Router {
    public static class AccountRequestAPI{
        public static final String ROOT = "/account";
        public static final String CREATE = "/create";
        public static final String UPDATE = "/update";
        public static final String GET_BY_ID = "/{accountId}";
        public static final String SOFT_DELETE = "/{accountId}";
        public static final String GET_ALL = "/all";
        public static final String GET_AVAILABLE_BY_ACCOUNT = "/all/type";
        public static final String GET_AVAILABLE = "/available";
        public static final String GET_AVAILABLE_FILTER = "/available/filter";
        public static final String GET_ALL_BY_TYPE = "/all/type";
    }

    public static class ProfileSalesRequestAPI{
        public static final String ROOT = "/sale/profile";
        public static final String CREATE = "/create";
        public static final String UPDATE = "/{accountId}";
        public static final String GET_BY_ID = "/{accountId}";
        public static final String SOFT_DELETE = "/{accountId}";
    }

    public static class AccountTypeRequestAPI{
        public static final String ROOT = "/platform";
        public static final String CREATE = "/create";
        public static final String UPDATE = "/{accountId}";
        public static final String GET_BY_ID = "/{accountId}";
        public static final String SOFT_DELETE = "/{accountId}";
        public static final String GET_ALL = "/all";
        public static final String GET_ALL_DATA = "/all/data";
    }

    public static class ClientRequestAPI{
        public static final String ROOT = "/client";
        public static final String CREATE = "/create";
        public static final String UPDATE = "/update";
        public static final String GET_BY_ID = "/{accountId}";
        public static final String SOFT_DELETE = "/{accountId}";
        public static final String GET_ALL = "/all";
        public static final String GET_AVAILABLE = "/available";
        public static final String GET_ALL_BY_TYPE = "/all/type";
    }

    public static class AccountSaleRequestAPI{
        public static final String ROOT = "/sale/account";
        public static final String CREATE = "/create";
        public static final String UPDATE = "/update";
        public static final String GET_BY_ID = "/{accountId}";
        public static final String SOFT_DELETE = "/{accountId}";
        public static final String GET_ALL = "/all";
        public static final String GET_AVAILABLE = "/available";
        public static final String GET_ALL_BY_TYPE = "/all/type";
    }
}
