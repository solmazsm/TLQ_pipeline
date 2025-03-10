package lambda;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;
import saaf.Inspector;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
* uwt.lambda_test::handleRequest
*
* @author Wes Lloyd
* @author Robert Cordingly
*/
public class HelloMySQL implements RequestHandler<Request, HashMap<String, Object>> {

/**
 * Lambda Function Handler
 *
 * @param request Request POJO with defined variables from Request.java
 * @param context
 * @return HashMap that Lambda will automatically convert into JSON.
 */
public HashMap<String, Object> handleRequest(Request request, Context context) {

    String bucketname = "test.bucket.sales.dimo";
    String filename = "100SalesRecords_processed.csv";

// Create logger
    LambdaLogger logger = context.getLogger();

//Collect inital data.
    Inspector inspector = new Inspector();
    inspector.inspectAll();

//****************START FUNCTION IMPLEMENTATION*************************
//Add custom key/value attribute to SAAF's output. (OPTIONAL)
//Create and populate a separate response object for function output. (OPTIONAL)
    Response r = new Response();

    try {
        Properties properties = new Properties();
        properties.load(new FileInputStream("db.properties"));
        logger.log("connect");
        String url = properties.getProperty("url");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");
        String driver = properties.getProperty("driver");
        logger.log("connect");
        r.setValue(request.getName());

// r.setMysqlversion(request.getMysqlversion("version"));
// Manually loading the JDBC Driver is commented out
// No longer required since JDBC 4
//Class.forName(driver);
        Connection con = DriverManager.getConnection(url, username, password);
        logger.log("connect");
        /**
         * *
         *
         * s3
         *
         **
         */
        logger.log("s3");
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
//get object file using source bucket and srcKey name
        logger.log("et object file using source bucket and srcKey name");
        S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketname, filename));
//get content of the file
        logger.log("get content of the file");
        InputStream objectData = s3Object.getObjectContent();
//scanning data line by line
        logger.log("scanning data line by line");
        String text = "";
        Scanner scanner = new Scanner(objectData);
        while (scanner.hasNext()) {
            text += scanner.nextLine();
            String[] val = text.split(",");
            PreparedStatement ps = con.prepareStatement("insert into sale11 values('" + val[0] + "','" + val[1] + "','" + val[2] + "','"
                    + val[3] + "','" + val[4] + "','" + val[5] + "'," + val[6]
                    + ",'" + val[7] + "'," + val[8] + "," + val[9]
                    + "," + val[10] + "," + val[11] + "," + val[12]
                    + "," + val[13] + "," + val[14] + "," + val[15] + ")");

            ps.execute();

            //  PreparedStatement ps = con.prepareStatement("insert into mytable values("
            //  'a'");");
            // ps.execute();
            // scanner.close();
            //  ResultSet rs = ps.executeQuery();
            // LinkedList<String> ll = new LinkedList<String>();
            //   while (rs.next()) {
            //       logger.log("name=" + rs.getString("name"));
            //  ll.add(rs.getString("name"));
            //  logger.log("col2=" + rs.getString("col2"));
            //   logger.log("col3=" + rs.getString("col3"));
            //  }
            // rs.close();
            con.close();
            //   r.setNames(ll);

        }
    } catch (Exception e) {
        logger.log("Got an exception working with MySQL! ");
        logger.log(e.getMessage());
    }

//Print log information to the Lambda log as needed
//logger.log("log message...");
    inspector.consumeResponse(r);

//****************END FUNCTION IMPLEMENTATION***************************
//Collect final information such as total runtime and cpu deltas.
    inspector.inspectAllDeltas();
    return inspector.finish();
}

public static void main(String[] args) {
    Context c = new Context() {
        @Override
        public String getAwsRequestId() {
            return "";
        }

        @Override
        public String getLogGroupName() {
            return "";
        }

        @Override
        public String getLogStreamName() {
            return "";
        }

        @Override
        public String getFunctionName() {
            return "";
        }

        @Override
        public String getFunctionVersion() {
            return "";
        }

        @Override
        public String getInvokedFunctionArn() {
            return "";
        }

        @Override
        public CognitoIdentity getIdentity() {
            return null;
        }

        @Override
        public ClientContext getClientContext() {
            return null;
        }

        @Override
        public int getRemainingTimeInMillis() {
            return 0;
        }

        @Override
        public int getMemoryLimitInMB() {
            return 0;
        }

        @Override
        public LambdaLogger getLogger() {
            return new LambdaLogger() {
                @Override
                public void log(String string) {
                    System.out.println("LOG:" + string);
                }
            };
        }
    };

// Create an instance of the class
    HelloMySQL lt = new HelloMySQL();

// Create a request object
    Request req = new Request();

// Grab the name from the cmdline from arg 0
    String name = (args.length > 0 ? args[0] : "");

// Load the name into the request object
    req.setName(name);

// Report name to stdout
    System.out.println("cmd-line param name=" + req.getName());

// Test properties file creation
    Properties properties = new Properties();
    properties.setProperty("driver", "com.mysql.cj.jdbc.Driver");
    properties.setProperty("url", "");
    properties.setProperty("username", "");
    properties.setProperty("password", "");
    try {
        properties.store(new FileOutputStream("test.properties"), "");
    } catch (IOException ioe) {
        System.out.println("error creating properties file.");
    }

// Run the function
//Response resp = lt.handleRequest(req, c);
    System.out.println("The MySQL Serverless can't be called directly without running on the same VPC as the RDS cluster.");
    Response resp = new Response();

// Print out function result
    System.out.println("function result:" + resp.toString());
}

}
