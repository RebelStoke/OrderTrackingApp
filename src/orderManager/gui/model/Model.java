package orderManager.gui.model;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import orderManager.be.IDepartment;
import orderManager.be.IProductionOrder;
import orderManager.be.IWorker;
import orderManager.be.ProductionOrder;
import orderManager.bll.mainLogicClass;
import orderManager.bll.mainLogicFacade;
import orderManager.dal.Properties.PropertyReader;

public class Model {

  private static Model model;
  private IDepartment department;
  private mainLogicFacade mlf;
  private ProductionOrder po;
  private PropertyReader pr;

  private Model() {
    try {
      mlf = new mainLogicClass();
      pr = new PropertyReader();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static Model getInstance() {
    if (model == null) {
      model = new Model();
    }
    return model;
  }

  public mainLogicFacade getManager() {
    return mlf;
  }

  public List<IProductionOrder> getProductionOrders() throws SQLException, ParseException {
    return mlf.getProducionOrdersByDepartment(department);
  }

  public List<IWorker> getWorkers() throws SQLException {
    return mlf.getWorkers();
  }

  public ProductionOrder getSelectedProductionOrder() {
    return po;
  }

  public void setSelectedProductionOrder(ProductionOrder po) {
    this.po = po;
  }

  public void changeStatus(IProductionOrder prodOrd) throws SQLException {
    mlf.changeStatus(prodOrd, department);
  }

  public IDepartment getDepartment() {
    if (department == null) {
      department = pr.read();
    }
    return department;
  }

  public void setDepartment(IDepartment department) {
    this.department = department;
    pr.write(department.getName());
  }

  public void readFile(String path) throws IOException, SQLException {
    mlf.readFile(path);
  }

  public List<IDepartment> getDepartments() {
    return mlf.getDepartments();
  }

}