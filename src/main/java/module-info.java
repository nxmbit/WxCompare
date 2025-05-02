module com.nxmbit.wxcompare {
    requires atlantafx.base;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.feather;
    requires org.kordamp.ikonli.material2;
    requires org.kordamp.ikonli.materialdesign;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires javafx.fxml;
    requires javafx.web;
    requires com.google.common;
    requires java.naming;

    opens com.nxmbit.wxcompare to javafx.fxml;
    exports com.nxmbit.wxcompare;
    exports com.nxmbit.wxcompare.controller;
    opens com.nxmbit.wxcompare.model to org.hibernate.orm.core;
    opens com.nxmbit.wxcompare.controller to javafx.fxml;
}