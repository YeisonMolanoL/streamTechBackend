package com.TechPulseInnovations.streamTech.core.router;

public class Router {
    public static class AccountRequestAPI{
        public static final String ROOT = "/account";
        public static final String CREATE = "/create";
        public static final String UPDATE = "/update";
        public static final String GET_BY_ID = "/{accountId}";
        public static final String SOFT_DELETE = "/{accountId}";
        public static final String GET_ALL = "/all";
        public static final String GET_AVAILABLE = "/available";
        public static final String GET_ALL_BY_TYPE = "/all/type";
    }

    public static class AccountSalesRequestAPI{
        public static final String ROOT = "/sale";
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
}
