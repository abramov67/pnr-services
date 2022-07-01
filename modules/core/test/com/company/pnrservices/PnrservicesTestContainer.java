package com.company.pnrservices;

import com.haulmont.cuba.testsupport.TestContainer;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.Arrays;

public class PnrservicesTestContainer extends TestContainer {

    public PnrservicesTestContainer() {
        super();
        dbUrl = "jdbc:postgresql://192.1.1.25:5432/yoda_test_service";
        dbUser = "testcuba_develop";
        dbPassword = "cuba_develop";
        appComponents = new ArrayList<>(Arrays.asList(
                "com.haulmont.cuba"
        ));
        appPropertiesFiles = Arrays.asList(
                "com/haulmont/cuba/testsupport/test-app.properties",
                "com/company/pnrservices/test-app.properties");
        autoConfigureDataSource();
    }

    

    public static class Common extends PnrservicesTestContainer {

        public static final PnrservicesTestContainer.Common INSTANCE = new PnrservicesTestContainer.Common();

        private static volatile boolean initialized;

        private Common() {
        }

        @Override
        public void before() throws Throwable {
            if (!initialized) {
                super.before();
                initialized = true;
            }
            setupContext();
        }
        

        @SuppressWarnings("RedundantThrows")
        @Override
        public void afterAll(ExtensionContext extensionContext) throws Exception {
            cleanupContext();
            // never stops - do not call super
        }
        
    }
}