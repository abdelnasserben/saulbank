package com.dabel.constant;

public class App {
    public interface Endpoint {
        String BRANCH_LIST = "/branches";
        String BRANCH_ACCOUNTS = BRANCH_LIST + "/accounts";

        String CUSTOMER_LIST = "/customers";
        String CUSTOMER_ADD = CUSTOMER_LIST + "/add";
    }

    public interface View {
        String BRANCH_LIST = "branches";
        String BRANCH_ACCOUNTS = "branches-accounts";
        String DASHBOARD = "dashboard";

        String CUSTOMER_LIST = "customers";
        String CUSTOMER_ADD = "customers-add";
    }

    public interface MessageTag {
        String ERROR = "errorMessage";
        String SUCCESS = "successMessage";
    }

    public interface Menu {
        interface Bank {
            String MENU = "Bank";

            interface Branches {
                String SUB_MENU = "Branches";
                String ACCOUNTS = "Branch Accounts";
                String LIST = "Branches List";
            }
        }

        interface General {
            String MENU = "General";
            String DASHBOARD = "Dashboard";
        }

        interface Customer {
            String MENU = "Customers";
            String ADD = "New Customer";
            String LIST = "All Customers";
        }
    }
}
