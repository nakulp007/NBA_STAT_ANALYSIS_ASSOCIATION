import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import weka.associations.AssociationRule;
import weka.associations.FPGrowth;
import weka.core.Instances;
import weka.core.Option;


public class Main {
    static String OUTPUT_DATA_FILE_NAME = "data.txt";
    static String OUTPUT_ASSOCIATION_RULES_FILE_NAME = "output.txt";

    static String DB_SERVER_ADDRESS = "localhost";
    static int DB_SERVER_PORT = 27017;
    static String DB_NAME = "nbaDb";
    static String COLLECTION_TEAM_RESULTS = "teamResults";

    public static void main(String[] args) {
        //generate data file from DB that can be used to find association rules
        System.out.println("Generating data file.");
        generateDataFile(DB_SERVER_ADDRESS, DB_SERVER_PORT, "", "", DB_NAME, COLLECTION_TEAM_RESULTS, OUTPUT_DATA_FILE_NAME);

        //running FP-Growth for association rules
        System.out.println("Generating association rules using FP-Growth.");
        generateAssociationRules(args, OUTPUT_DATA_FILE_NAME, OUTPUT_ASSOCIATION_RULES_FILE_NAME);

        //delete data file
        deleteFile(OUTPUT_DATA_FILE_NAME);

        System.out.println("Done");
    }

    private static void deleteFile(String dataFile) {
        File file = new File(dataFile);
        if (!file.delete()) {
            System.out.println("Delete operation is failed.");
        }
    }

    private static void generateAssociationRules(String[] args, String dataFile, String outputFile) {
        StringBuilder errText = new StringBuilder();
        FPGrowth fpGrowth = new FPGrowth();

        try (Reader reader = new BufferedReader(new FileReader(dataFile))) {

            // the text is for informative error message... no required
            errText.append("\n\nFP-Growth options:\n\n");
            errText.append("-t <data file>\n");
            errText.append("\tThe name of the data file.\n");
            Enumeration e = fpGrowth.listOptions();
            while (e.hasMoreElements()) {
                Option option = (Option)e.nextElement();
                errText.append(option.synopsis()).append('\n');
                errText.append(option.description()).append('\n');
            }

            //check if user provided -t option to indicate where data file is
            if (dataFile.length() == 0) {
                throw new Exception("No training file given!");
            }
            //set all cmdline options
            fpGrowth.setOptions(args);

            fpGrowth.buildAssociations(new Instances(reader));
            List<AssociationRule> associationRules = fpGrowth.getAssociationRules().getRules();
            //System.out.println(fpGrowth);
            //System.out.println("Total number of association rules: " + associationRules.size());

            //Only want to keep the association rules that end in only win. No other combo on right side.
            //And write to output file
            //This filtered association rule array is not really needed... keeping it there if we need to pass all rules to something later
            ArrayList<AssociationRule> filteredAssociationRules = new ArrayList<AssociationRule>();
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"))) {
                for (AssociationRule associationRule : associationRules) {
                    //System.out.println(associationRules.get(i).toString());
                    if (associationRule.toString().contains("==> [win=1]")) {
                        filteredAssociationRules.add(associationRule);
                        writer.write(associationRule.toString() + "\n");
                        //System.out.println(associationRules.get(i).toString());
                    }
                }
            }

            //System.out.println("Number of filtered association rules: " + filteredAssociationRules.size());
            if(filteredAssociationRules.size() < 1){
                System.out.println("No association rules found.");
            }
        } catch(Exception e) {
            System.out.println("\n"+e.getMessage()+errText);
        }
    }

    private static void generateDataFile(String dbAddress,
            int dbPort, String username, String password, String dbName,
            String collectionName, String dataFileName) {

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataFileName), "utf-8"))){

            //write header
            writer.write("@relation Presence-Absence-Representation\n\n");

            String[] attributeIndividual = {"pts","fg3m","reb","ast","stl","blk","tov"};
            String attributes = generateDataAttributes(attributeIndividual);
            writer.write(attributes);

            writer.write("@attribute win	{0,1}\n\n");
            writer.write("@data\n");

            //get data from database and add to data file
            // connect to the local database server
            MongoClient mongoClient = new MongoClient(dbAddress , dbPort);
            /*
            // Authenticate - optional
            MongoCredential credential = MongoCredential.createMongoCRCredential(userName, database, password);
            MongoClient mongoClient = new MongoClient(new ServerAddress(), Arrays.asList(credential));
            */

            DB db = mongoClient.getDB(dbName);

            // get a collection object to work with
            DBCollection collectionTeamResults = db.getCollection(collectionName);

            // lets get all the documents in the collection and print them out
            ;
            //int totalGames = 0;
            try (DBCursor cursor = collectionTeamResults.find()){
                while (cursor.hasNext()) {
                    DBObject document = cursor.next();
                    //System.out.println(document);
                    String t1data = parseTeamForm((DBObject)((DBObject)document.get("t1")).get("form"));
                    t1data += "," + (((int)document.get("result") == 1) ? 1 : 0);
                    String t2data = parseTeamForm((DBObject)((DBObject)document.get("t2")).get("form"));
                    t2data += "," + (((int)document.get("result") == -1) ? 1 : 0);

                    writer.write(t1data + "\n");
                    writer.write(t2data + "\n");;

                    //totalGames++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // release resources
            mongoClient.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generateDataAttributes(String[] attributeIndividual) {
        String returnString = "";

        for (String anAttributeIndividual : attributeIndividual) {
            returnString += "@attribute " + anAttributeIndividual + "_gte_0.0	{0,1}\n";
            returnString += "@attribute " + anAttributeIndividual + "_gte_0.1	{0,1}\n";
            returnString += "@attribute " + anAttributeIndividual + "_gte_0.2	{0,1}\n";
            returnString += "@attribute " + anAttributeIndividual + "_gte_0.3	{0,1}\n";
            returnString += "@attribute " + anAttributeIndividual + "_gte_0.4	{0,1}\n";
            returnString += "@attribute " + anAttributeIndividual + "_gte_0.5	{0,1}\n";
            returnString += "@attribute " + anAttributeIndividual + "_gte_0.6	{0,1}\n";
            returnString += "@attribute " + anAttributeIndividual + "_gte_0.7	{0,1}\n";
            returnString += "@attribute " + anAttributeIndividual + "_gte_0.8	{0,1}\n";
            returnString += "@attribute " + anAttributeIndividual + "_gte_0.9	{0,1}\n";
        }

        return returnString;
    }

    private static String parseTeamForm(DBObject dbObject) {
        String returnString = "";

        returnString += parseTeamFormStat((Double)dbObject.get("pts"));
        returnString += "," + parseTeamFormStat((Double)dbObject.get("fg3m"));
        returnString += "," + parseTeamFormStat((Double)dbObject.get("reb"));
        returnString += "," + parseTeamFormStat((Double)dbObject.get("ast"));
        returnString += "," + parseTeamFormStat((Double)dbObject.get("stl"));
        returnString += "," + parseTeamFormStat((Double)dbObject.get("blk"));
        returnString += "," + parseTeamFormStat((Double)dbObject.get("tov"));

        return returnString;
    }

    private static String parseTeamFormStat(Double stat) {
        String returnString = "";
        int[] statArray = new int[10];

        //using while loop here to calculate stat relationship instead of nested ifs
        int index = 0;
        double curStat = 0.0d;
        while (stat >= curStat && curStat < 1) {
            statArray[index] = 1;
            index++;
            curStat += .1d;
        }

        returnString = Arrays.toString(statArray).replace(" ", "").replace("[", "").replace("]", "");

        return returnString;
    }
}
