global class ComprehensiveApexClass implements Database.Batchable<SObject>, Schedulable  {

    // Primitive Data Types
    public Integer integerVariable {get;set;}
    public String stringVariable {get;set;}
    public Boolean booleanVariable {get;set;}
    public Double doubleVariable {get;set;}
    public Date dateVariable {get;set;}

    // Collection Data Types
    public List<String> stringList;
    public Set<Integer> integerSet;
    public Map<String, Integer> stringIntegerMap;
    public enum Season {WINTER, SPRING, SUMMER, FALL}


    //GET SET
    private string myProperty;

    // Getter method for myProperty
    public String getMyProperty() {
        return myProperty;
    }

    // Setter method for myProperty
    public void setMyProperty(String value) {
        myProperty = value;
    }


    // Custom Object
    public CustomObject__c customObjectVariable;

    // Apex Class Instantiation
    public AnotherApexClass anotherApexClassInstance;

    // Constants
    public static final string CONSTANT_STRING = 'This is a constant string';

    // Define the endpoint URL
    private static final String ENDPOINT_URL = 'https://api.example.com/data';


    // Replace with a valid Account Id
    Id accountId = '001XXXXXXXXXXXXXXX';


    // Constructor
    public ComprehensiveApexClass() {
        // Initialize variables in the constructor if needed
        integerVariable = 10;
        stringVariable = 'Hello, Salesforce!';
        booleanVariable = true;
        doubleVariable = 3.14;
        dateVariable = Date.today();

        stringList = new List<String>{'Apple', 'Banana', 'Orange'};
        integerSet = new Set<Integer>{1, 2, 3};
        stringIntegerMap = new Map<String, Integer>{'One' => 1, 'Two' => 2, 'Three' => 3};

        customObjectVariable = new CustomObject__c();
        customObjectVariable.Name = 'Example';

        anotherApexClassInstance = new AnotherApexClass();
        anotherApexClassInstance.methodInAnotherClass();


        List<String> fieldsToQuery = new List<String>{'Id', 'Name', 'CreatedDate'};
        Map<String, String> conditions = new Map<String, String>{
            'Name' => 'Test Account',
            'Industry' => 'Technology'
        };

        List<Account> queriedAccounts = (List<Account>)new QueryFramework().dynamicQuery('Account', fieldsToQuery, conditions);


        Integer resultAdd = new CalculationClass().add(5, 3); // 8
        System.debug('resultAdd :::::' + resultAdd);

        Integer resultSubtract = new CalculationClass().subtract(10, 4); // 6
        System.debug('resultSubtract :::::' + resultSubtract);

        Integer resultMultiply = new CalculationClass().multiply(2, 6); // 12
        System.debug('resultMultiply :::::' + resultMultiply);

        Decimal resultDivide = new CalculationClass().divide(15, 3); // 5.0
        System.debug('resultDivide :::::' + resultDivide);

        ComprehensiveApexClass.integrationMethod();


        String searchQuery = 'Test';
        List<List<SObject>> searchResults = ComprehensiveApexClass.performSOSLSearch(searchQuery);

        // Accessing results for each object
        List<Account> accountResults = (List<Account>)searchResults[0];
        list<Contact> contactResults = (list<Contact>)searchResults[1];
        List<Opportunity> opportunityResults = (List<Opportunity>)searchResults[2];


        // Sending single email code call
        String toAddress = 'recipient@example.com';
        String subject = 'Test Email';
        String body = 'Hello, this is a test email from Salesforce!';

        ComprehensiveApexClass.sendEmail(toAddress, subject, body);


        //XML Parsing
        String xmlString = '<root><element1>Value1</element1><element2>Value2</element2></root>';
        ComprehensiveApexClass.parseXml(xmlString);


        //JSON Parsing
        String jsonString = '{"key1": "value1", "key2": 42, "key3": true}';
        ComprehensiveApexClass.parseJson(jsonString);

        // Example: Insert a file related to an Account
        FileUploader fileUploader = new FileUploader();
        fileUploader.insertFile('ExampleFile.txt', Blob.valueOf('File content here'), accountId);
    }


    public Season getSouthernHemisphereSeason(Season northernHemisphereSeason) {
        if (northernHemisphereSeason == Season.SUMMER) return northernHemisphereSeason;

        return null;
    }


    global Database.QueryLocator start(Database.BatchableContext BC) {
        // Define the query to retrieve records to process
        String query = 'SELECT Id, Name FROM Account WHERE CreatedDate >= 2023-01-01T00:00:00Z';
        return Database.getQueryLocator(query);
    }

    global void execute(Database.BatchableContext BC, List<Account> scope) {
        // Process each record in the batch
        for (Account acc : scope) {
            acc.Description = 'Processed by Batch Apex';
        }

        // Update the processed records
        update scope;
    }

    global void finish(Database.BatchableContext BC) {
        // Execute any post-processing logic
    }

    //Schedule method
    global void execute(SchedulableContext sc) {
        // Your scheduled logic goes here
        System.debug('Scheduled job is running...');
    }


    // Method with Parameters and Return Type
    public String exampleMethod(String inputString, Integer inputNumber) {
        // Method logic here
        return 'Result: ' + inputString + ' ' + String.valueOf(inputNumber);
    }

    // Method with No Parameters and Void Return Type
    public void voidMethod() {
        // Method logic here
    }

    // Integration Method - Callout to External System
    @future(callout=true)
    public static void integrationMethod() {
        // Callout logic here (e.g., making an HTTP request)
        ComprehensiveApexClass.makeHttpGetCallout();
    }

    // Method to get a list of SelectOption for a picklist field
    public static List<SelectOption> getPicklistValues(String objectApiName, String fieldApiName) {
        List<SelectOption> options = new List<SelectOption>();

        Schema.SObjectType objectType = Schema.getGlobalDescribe().get(objectApiName);
        if (objectType != null) {
            Schema.DescribeSObjectResult describeResult = objectType.getDescribe();
            if (describeResult.fields.getMap().containsKey(fieldApiName)) {
                Schema.DescribeFieldResult fieldDescribe = describeResult.fields.getMap().get(fieldApiName).getDescribe();

                List<Schema.PicklistEntry> picklistValues = fieldDescribe.getPicklistValues();

                for (Schema.PicklistEntry picklistEntry : picklistValues) {
                    options.add(new SelectOption(picklistEntry.getValue(), picklistEntry.getLabel()));
                }
            }
        }

        return options;
    }

    // Method to make an HTTP GET callout
    public static String makeHttpGetCallout() {
        // Create an HTTP request
        HttpRequest request = new HttpRequest();
        request.setEndpoint(ENDPOINT_URL);
        request.setMethod('GET');

        // Create an HTTP object to send the request
        Http http = new http();
        HttpResponse response = http.send(request);

        // Process the response
        if (response.getStatusCode() == 200) {
            // Successful response
            return response.getBody();
        } else {
            // Handle errors or non-200 status codes
            System.debug('HTTP Request failed with status code: ' + response.getStatusCode());
            return null;
        }
    }

    // Annotations
    @AuraEnabled
    public static String auraEnabledMethod() {
        // Logic for Aura-enabled method
        return 'Aura-enabled method result';
    }

    @TestVisible
    private void testVisibleMethod() {
        // Logic for TestVisible method
    }

    // Inner Class
    public inherited sharing class AnotherApexClass {
        public String name;
        public integer age;

        public AnotherApexClass() {

        }

        public AnotherApexClass(String n, Integer a) {
            name = n;
            age = a;
        }

        public String methodInAnotherClass() {
            // Logic for the method in the inner class
            return 'Wrapper Info - Name: ' + this.name +
               ', Age: ' + this.age;
        }
    }

    // Method that uses the wrapper class
    public AnotherApexClass createWrapper(String name, Integer age) {
        // Creating an instance of the wrapper class
        AnotherApexClass wrapperInstance = new anotherApexClass(name, age);

        // Other logic, if needed

        return wrapperInstance;
    }

    // Another method that uses the wrapper class
    public String getOuterVariableAndWrapperInfo(AnotherApexClass wrapper) {
        return 'Wrapper Info - Name: ' + wrapper.name +
               ', Age: ' + wrapper.age;
    }


    // Method to perform a SOSL search
    public static List<List<SObject>> performSOSLSearch(String searchQuery) {
        // Constructing the SOSL query
        String soslQuery = 'FIND \'' + searchQuery + '\' IN ALL FIELDS RETURNING ';

        // Add the objects you want to search in
        soslQuery += 'Account, Contact, Opportunity';

        // Executing the SOSL search
        List<List<SObject>> searchResults = Search.query(soslQuery);

        return searchResults;
    }


    // Method to perform the roll-up summary
    public static void updateRollupSummary(List<Contact> childRecords) {
        Map<Id, Integer> parentCountMap = new Map<Id, Integer>();

        // Calculate the count of child records per parent
        for (Contact childRecord : childRecords) {
            if (childRecord.AccountId != null) {
                Id parentId = childRecord.AccountId;
                if (!parentCountMap.containsKey(parentId)) {
                    parentCountMap.put(parentId, 1);
                } else {
                    parentCountMap.put(parentId, parentCountMap.get(parentId) + 1);
                }
            }
        }

        // Update the roll-up summary field on the parent records
        List<Account> parentsToUpdate = new List<Account>();
        for (Id parentId : parentCountMap.keySet()) {
            Account parent = new Account([Id = parentId,
                                         RollupSummaryField__c = parentCountMap.get(parentId)
                                         ]);
            parentsToUpdate.add(parent);
        }

        update parentsToUpdate;
    }


    // Method to send a simple email
    public static void sendEmail(String toAddress, String subject, String body) {
        Messaging.SingleEmailMessage email = new Messaging.SingleEmailMessage();

        // Set the target email address
        email.setToAddresses(new String[]{toAddress});

        // Set the email subject and body
        email.setSubject(subject);
        email.setPlainTextBody(body);

        // Send the email
        Messaging.sendEmail(new Messaging.SingleEmailMessage[]{email});
    }


    // Method to send a mass email using an email template
    public static void sendMassEmail(List<Contact> contacts, Id emailTemplateId) {
        List<Messaging.SingleEmailMessage> emailMessages = new List<Messaging.SingleEmailMessage>();

        for (Contact contact : contacts) {
            Messaging.SingleEmailMessage email = new Messaging.SingleEmailMessage();

            // Set the target email address
            email.setToAddresses(new String[]{contact.Email});

            // Set the email template ID
            email.setTemplateId(emailTemplateId);

            // Optionally set additional parameters such as subject, senderDisplayName, etc.
            // email.setSubject('Subject');
            // email.setSenderDisplayName('Sender Name');

            // Add the email message to the list
            emailMessages.add(email);
        }

        // Send the mass email
        List<Messaging.SendEmailResult> sendResults = Messaging.sendEmail(emailMessages);

        // Process the results if needed
        for (Messaging.SendEmailResult result : sendResults) {
            if (result.isSuccess()) {
                System.debug('Mass email sent successfully.');
            } else {
                System.debug('Error sending mass email: ' + result.getErrors()[0].getMessage());
            }
        }
    }


    // Method to parse an XML string
    public static void parseXml(String xmlString) {
        try {
            // Create a new XML document
            Dom.Document doc = new Dom.Document();
            doc.load(xmlString);

            // Access XML elements and attributes
            Dom.XmlNode root = doc.getRootElement();
            List<Dom.XmlNode> childNodes = root.getChildElements();

            for (Dom.XmlNode childNode : childNodes) {
                String nodeName = childNode.getName();
                String nodeValue = childNode.getText();

                System.debug('Node Name: ' + nodeName);
                System.debug('Node Value: ' + nodeValue);
            }
        } catch (Exception e) {
            System.debug('Error parsing XML: ' + e.getMessage());
        }
    }


    // Method to parse a JSON string
    public static void parseJson(String jsonString) {
        try {
            // Deserialize the JSON string
            Map<String, Object> jsonMap = (Map<String, object>)JSON.deserializeUntyped(jsonString);

            // Access JSON elements
            for (String key : jsonMap.keySet()) {
                Object value = jsonMap.get(key);

                System.debug('Key: ' + key);
                System.debug('Value: ' + value);
            }
        } catch (Exception e) {
            System.debug('Error parsing JSON: ' + e.getMessage());
        }
    }


    public class FileUploader {

        public void insertFile(String fileName, Blob fileBody, String parentId) {
            ContentVersion contentVersion = new ContentVersion();
            contentVersion.Title = fileName;
            contentVersion.VersionData = fileBody;
            contentVersion.PathOnClient = '/' + fileName;
            contentVersion.FirstPublishLocationId = parentId;

            insert contentVersion;
        }
    }


    global without sharing class QueryFramework {

        // Method to perform a dynamic SOQL query
        global List<SObject> dynamicQuery(String objectName, List<String> fields, Map<String, String> conditions) {
            // Constructing the query dynamically
            String query = 'SELECT ' + String.join(fields, ',') + ' FROM ' + objectName;

            // Adding conditions to the query
            if (!conditions.isEmpty()) {
                query += ' WHERE ' + getConditionsString(conditions);
            }

            // Executing the query
            try {
                return Database.query(query);
            } catch (Exception e) {
                System.debug('Error executing query: ' + e.getMessage());
                return new List<SObject>();
            }
        }

        // Helper method to construct the WHERE clause of the query
        private String getConditionsString(Map<String, String> conditions) {
            List<String> conditionList = new List<String>();

            for (String field : conditions.keySet()) {
                conditionList.add(field + ' = \'' + conditions.get(field) + '\'');
            }

            return String.join(conditionList, ' AND ');
        }
    }


    public with sharing class CalculationClass {

        // Method for addition
        public Integer add(Integer operand1, Integer operand2) {
            return operand1 + operand2;
        }

        // Method for subtraction
        public Integer subtract(Integer operand1, Integer operand2) {
            return operand1 - operand2;
        }

        // Method for multiplication
        public Integer multiply(Integer operand1, Integer operand2) {
            return operand1 * operand2;
        }

        // Method for division
        public Decimal divide(Integer dividend, Integer divisor) {
            // Handling division by zero to avoid runtime exceptions
            if (divisor != 0) {
                return Decimal.valueOf(dividend) / Decimal.valueOf(divisor);
            } else {
                // You may want to handle this case differently based on your requirements
                System.debug('Division by zero attempted.');
                return null;
            }
        }
    }
}