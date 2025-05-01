module com.nxmbit.wxcompare {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;


    opens com.nxmbit.wxcompare to javafx.fxml;
    exports com.nxmbit.wxcompare;
}