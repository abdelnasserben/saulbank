package com.dabel.constant;

public class App {

    public static class Web {
        public static class View {

            public interface Branch {
                String ROOT = "branches";
            }

            public interface Card {
                String ROOT = "cards";
                String ADD = String.format("%s-add", ROOT);
                String DETAILS = String.format("%s-details", ROOT);
                String APPLICATION = String.format("%s-applications", ROOT);
                String APPLICATION_DETAILS = String.format("%s-details", APPLICATION);
            }

            public interface Cheque {
                String ROOT = "cheques";
            }

            public interface Common {
                String PAGE_404 = "404";
            }

            public interface Customer {
                String ROOT = "customers";
                String ADD = String.format("%s-add", ROOT);
                String DETAILS = String.format("%s-details", ROOT);
            }

            public interface Dashboard {
                String ROOT = "dashboard";
            }

            public interface Exchange {
                String ROOT = "exchanges";
                String INIT = String.format("%s-init", ROOT);
                String DETAILS = String.format("%s-details", ROOT);
            }

            public interface Loan {
                String ROOT = "loans";
                String DETAILS = String.format("%s-details", ROOT);
            }

            public interface Payment {
                String ROOT = "payments";
                String INIT = String.format("%s-init", ROOT);
                String DETAILS = String.format("%s-details", ROOT);
            }

            public interface Transaction {
                String ROOT = "transactions";
                String INIT = String.format("%s-init", ROOT);
                String DETAILS = String.format("%s-details", ROOT);
            }

            public interface ActivePage {
                String DASHBOARD = "dashboard";
                String CUSTOMERS = "customers";
                String TRANSACTIONS = "transactions";
                String LOANS = "loans";
                String ACCOUNTS = "accounts";
                String CARDS = "cards";
                String CHEQUES = "cheques";
                String SETTINGS = "settings";
            }
        }

        public static class Endpoint {

            private static final String APPROVE = "/approve";
            private static final String REJECT = "/reject";

            public interface Branch {
                String ROOT = "/branches";
            }

            public interface Card {
                String ROOT = "/cards";
                String APPLICATION = ROOT + "/application-requests";
                String APPLICATION_APPROVE = APPLICATION + "/approve";
                String APPLICATION_REJECT = APPLICATION + "/reject";
                String ACTIVATE = ROOT + "/activate";
                String DEACTIVATE = ROOT + "/deactivate";
            }

            public interface Cheque {
                String ROOT = "/cheques";
            }

            public interface Common {
                String PAGE_404 = "/404";
            }

            public interface Customer {
                String ROOT = "/customers";
                String ADD = ROOT + "/add";
            }

            public interface Exchange {
                String ROOT = Transaction.ROOT + "/exchanges";
                String INIT = ROOT + "/init";
                String APPROVE = ROOT + Endpoint.APPROVE;
                String REJECT = ROOT + Endpoint.REJECT;
            }

            public interface Loan {
                String ROOT = "/loans";
                String APPROVE = ROOT + Endpoint.APPROVE;
                String REJECT = ROOT + Endpoint.REJECT;
            }

            public interface Payment {
                String ROOT = Transaction.ROOT + "/payments";
                String INIT = ROOT + "/init";
                String APPROVE = ROOT + Endpoint.APPROVE;
                String REJECT = ROOT + Endpoint.REJECT;
            }

            public interface Transaction {
                String ROOT = "/transactions";
                String INIT = ROOT + "/init";
                String APPROVE = ROOT + Endpoint.APPROVE;
                String REJECT = ROOT + Endpoint.REJECT;
            }
        }

        public interface MessageTag {
            String SUCCESS = "successMessage";
            String ERROR = "errorMessage";
        }
    }

}
