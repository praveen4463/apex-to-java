/*
This file is generated and isn't the actual source code for this
managed global class.
This read-only file shows the class's global constructors,
methods, variables, and properties.
To enable code to compile, all methods return null.
*/
global class TerritoryGridController
{
    global String territoryId {get;set;}
    global List<Integer> atar {get;set;}
    global List<String> ruleItems {get;set;}
    List<Map<String, Sobject>> expensePayload;
    global TerritoryGridController()
    {
        territoryId = 'ShippingPostalCode';

    }
    class YearlyExpense {
        Double yearlyExpense = 0.0;
        Double yearlyHonorarium = 0.0;
    }

     global List<String> getTerritory2ModelList(String value)
    {
        System.debug('Input: ' + value);
         ruleItems = new List<String>();
         ruleItems.add(value);
        return [SELECT Id, Name, DeveloperName, Description,
               (SELECT Id, Name, Planning__c, Go_Live_Date__c
                FROM Territory2s)
                FROM Territory2Model];

    }

}