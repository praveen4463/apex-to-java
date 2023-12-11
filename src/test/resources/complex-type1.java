import java.util.List;
import java.util.Map;
/*
This file is generated and isn't the actual source code for this
managed global class.
This read-only file shows the class's global constructors,
methods, variables, and properties.
To enable code to compile, all methods return null.
*/
public class TerritoryGridController {
  private String territoryId;

public String getTerritoryId() {return territoryId;}

public void setTerritoryId(String territoryId) {this.territoryId = territoryId;}
  private List<Integer> atar;

public String getAtar() {return atar;}

public void setAtar(String atar) {this.atar = atar;}
  private List<String> ruleItems;

public String getRuleItems() {return ruleItems;}

public void setRuleItems(String ruleItems) {this.ruleItems = ruleItems;}
  private List<Map<String, Object>> expensePayload;
  public TerritoryGridController() {
  territoryId = "ShippingPostalCod";
}
class YearlyExpense {
  Double yearlyExpense = 0.0;
  Double yearlyHonorarium = 0.0;
}
public List<String> getTerritory2ModelList(String value) {
  System.debug('Input: '+value);
  ruleItems = new List<String>();
  ruleItems.add(value);
  return "SELECT Id, Name, DeveloperName, Description,               (SELECT Id, Name, Planning__c, Go_Live_Date__c                FROM Territory2s)                FROM Territory2Mode";
}
}
