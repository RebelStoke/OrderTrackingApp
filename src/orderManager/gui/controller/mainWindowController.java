package orderManager.gui.controller;

import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import orderManager.be.Department;
import orderManager.be.IDepartment;
import orderManager.be.ProductionOrder;
import orderManager.gui.model.Model;
import orderManager.windowOpener;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class mainWindowController implements Initializable {

    public AnchorPane mainPane;
    public JFXButton departmentBtn;
    public JFXTreeTableView ordersTab;
    public Menu changeDepartmentMenuItem;
    public JFXSpinner spinner;
    public MenuBar menuBar;
    public ImageView logo;


    private ObservableList<ProductionOrder> observableOrders;
    private IDepartment chosenDepartment;
    private Model model;
    private Stage s;
    private RecursiveTreeItem<ProductionOrder> clickedOrder;
    private int clickedOrderIndex;
    private boolean selectionDone;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startSpinning();
        model = Model.getInstance();
        chosenDepartment = model.getDepartment();
        departmentBtn.setText(chosenDepartment.getName());
        loadDepartmentsItems();
        refresh();
        ordersTab.widthProperty().addListener((observable, oldValue, newValue) -> ordersTab.setStyle("-fx-font-size: " + newValue.doubleValue()/40));
    }

    public void cutDone()
    {
        LocalDate ld = LocalDate.now();
        observableOrders.removeIf(productionOrder -> productionOrder.getIndication().getText().equals("\u2713") && ChronoUnit.DAYS.between(productionOrder.getDelivery().getDeliveryTime().toLocalDate(), ld) > 7);
    }

    private void refresh() {
        ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
        s.scheduleAtFixedRate(() -> {
            try {
                observableOrders = (FXCollections.observableArrayList((List<ProductionOrder>) (List) model.getProductionOrders()));
                setIndication();
                cutDone();
                Platform.runLater(() -> {
                    prepareOrdersTable();
                    endSpinning();
                });
            } catch (SQLException | ParseException e) {
                e.printStackTrace();
            }
        }, 0, 5000, TimeUnit.MILLISECONDS);
    }


    private void startSpinning() {
        departmentBtn.setVisible(false);
        ordersTab.setVisible(false);
        menuBar.setVisible(false);
    }

    private void endSpinning() {
        departmentBtn.setVisible(true);
        ordersTab.setVisible(true);
        menuBar.setVisible(true);
        spinner.setVisible(false);
        logo.setVisible(false);
    }

    private void setIndication() throws ParseException {
        for (ProductionOrder p : observableOrders) {
            p.setIndication(chosenDepartment);
        }
    }

    private void prepareOrdersTable() {
        if (ordersTab.getColumns().isEmpty()) {

            JFXTreeTableColumn<ProductionOrder, Label> indication = new JFXTreeTableColumn<>("Progress");
            prepareColumn(indication, "indication", 70);

            JFXTreeTableColumn<ProductionOrder, String> orderNumber = new JFXTreeTableColumn<>("Order Number");
            prepareColumn(orderNumber, "orderNumber", 125);

            JFXTreeTableColumn<ProductionOrder, String> customerName = new JFXTreeTableColumn<>("Customer Name");
            prepareColumn(customerName, "customerName", 125);

            JFXTreeTableColumn<ProductionOrder, Date> deliveryDate = new JFXTreeTableColumn<>("Delivery Date");
            prepareColumn(deliveryDate, "deliveryDate", 145);

            ordersTab.getColumns().addAll(indication, orderNumber, customerName, deliveryDate);
        }
        setOrdersTable();
        s = (Stage) spinner.getScene().getWindow();
        s.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    private void setOrdersTable() {
        TreeItem<ProductionOrder> root = new RecursiveTreeItem<>(observableOrders, RecursiveTreeObject::getChildren);
        ordersTab.setRoot(root);
        ordersTab.setShowRoot(false);
        if (selectionDone)
            ordersTab.getSelectionModel().select(clickedOrderIndex);


    }


    private void prepareColumn(JFXTreeTableColumn colName, String attributeName, int colWidth) {
        colName.setCellValueFactory(new TreeItemPropertyValueFactory<>(attributeName));
        colName.setMinWidth(colWidth);
        colName.setStyle("-fx-alignment: CENTER");
    }

    public void doubleClickDetails(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getClickCount() == 2) {
            clickedOrder = (RecursiveTreeItem<ProductionOrder>) ordersTab.getSelectionModel().getSelectedItem();
            clickedOrderIndex = ordersTab.getSelectionModel().getSelectedIndex();
            selectionDone = true;
            model.setSelectedProductionOrder(clickedOrder.getValue());
            windowOpener wo = new windowOpener("gui/view/taskWindow.fxml", 804, 513, true);
            wo.passStage();
        } else if (mouseEvent.getClickCount() == 1) {
            selectionDone = true;
            clickedOrderIndex = ordersTab.getSelectionModel().getSelectedIndex();
        }
    }

    private void loadDepartmentsItems() {
        for (IDepartment d : model.getDepartments()) {
            if (!(d.getName().equals(chosenDepartment.getName()))) {
                MenuItem mi = new MenuItem();
                mi.setText(d.getName());
                mi.setOnAction(e -> {
                    IDepartment d1 = new Department(mi.getText());
                    Model.getInstance().setDepartment(d1);
                    Stage stage = (Stage) ordersTab.getScene().getWindow();
                    stage.close();
                    try {
                        new windowOpener("gui/view/mainWindow.fxml", 651, 496, true);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
                changeDepartmentMenuItem.getItems().add(mi);
            }
        }
    }

}