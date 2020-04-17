import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.docs.v1.model.Document;


import java.util.ArrayList;
import com.google.api.services.docs.v1.model.Request;
import com.google.api.services.docs.v1.model.SubstringMatchCriteria;
import com.google.api.services.docs.v1.model.ReplaceAllTextRequest;
import com.google.api.services.docs.v1.model.BatchUpdateDocumentRequest;
import com.google.api.services.docs.v1.model.BatchUpdateDocumentResponse;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.docs.v1.model.Location;
import com.google.api.services.docs.v1.model.InsertTextRequest;
import com.google.api.services.docs.v1.model.CreateParagraphBulletsRequest;
import com.google.api.services.docs.v1.model.Range;
import com.google.api.services.docs.v1.model.InsertTableRequest;
import com.google.api.services.docs.v1.model.EndOfSegmentLocation;



import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

import java.io.File;

public class DocsQuickstart {
    private static final String APPLICATION_NAME = "Google Docs API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String DOCUMENT_ID = "1-CCA-IqrPpwTMMvfN-rYnCuyP2z107clrtmEEWTteW8";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    //private static final List<String> SCOPES = Collections.singletonList(DocsScopes.DOCUMENTS_READONLY);

    // Para permitir creación de archivos
    private static final List<String> SCOPES = Collections.singletonList(DocsScopes.DOCUMENTS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = DocsQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // Borrar una autorización

      /*  File  fileds =new java.io.File(TOKENS_DIRECTORY_PATH);
        System.out.println("File Store: " + fileds.toPath() );
        if (fileds.exists()) {
            java.nio.file.Files.delete(fileds.toPath());
        }
        */

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private static void createDoc(Docs service) throws IOException {
        Document doc = new Document()
                .setTitle("My Document");
        doc = service.documents().create(doc)
                .execute();
        System.out.println("2.- Created document with title: " + doc.getTitle());
    }

    private static void insertText(Docs service, String [] text) throws IOException {
        List<Request> requests = new ArrayList<>();

        for ( int i = 0 ; i < text.length ; i++)
        requests.add(new Request().setInsertText(new InsertTextRequest()
                .setText(text[i])
                .setLocation(new Location().setIndex(25 * i))));

        BatchUpdateDocumentRequest body = new BatchUpdateDocumentRequest().setRequests(requests);
        BatchUpdateDocumentResponse response = service.documents()
                .batchUpdate(DOCUMENT_ID, body).execute();
    }
    
    private static void merge(Docs service) throws IOException {
        String customerName = "Alice";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String date = formatter.format(LocalDate.now());

        List<Request> requests = new ArrayList<>();
        requests.add(new Request()
                .setReplaceAllText(new ReplaceAllTextRequest()
                        .setContainsText(new SubstringMatchCriteria()
                                .setText("{{customer-name}}")
                                .setMatchCase(true))
                        .setReplaceText(customerName)));
        requests.add(new Request()
                .setReplaceAllText(new ReplaceAllTextRequest()
                        .setContainsText(new SubstringMatchCriteria()
                                .setText("{{date}}")
                                .setMatchCase(true))
                        .setReplaceText(date)));

        BatchUpdateDocumentRequest body = new BatchUpdateDocumentRequest();
        service.documents().batchUpdate(DOCUMENT_ID, body.setRequests(requests)).execute();
        System.out.println("3.- merge: " );
    }

    private static void list ( Docs service)  throws IOException {
        List<Request> requests = new ArrayList<>();
      requests.add(new Request().setInsertText(new InsertTextRequest()
              .setText("Item One\n")
              .setLocation(new Location().setIndex(1))));

      requests.add(new Request().setCreateParagraphBullets(
              new CreateParagraphBulletsRequest()
                      .setRange(new Range()
                              .setStartIndex(1)
                              .setEndIndex(50))
                      .setBulletPreset("BULLET_ARROW_DIAMOND_DISC")));

      BatchUpdateDocumentRequest body = new BatchUpdateDocumentRequest().setRequests(requests);
      BatchUpdateDocumentResponse response = service.documents()
              .batchUpdate(DOCUMENT_ID, body).execute();

    }

    private static void table ( Docs service)  throws IOException {
// Insert a table at the end of the body.
// (An empty or unspecified segmentId field indicates the document's body.)
        List<Request> requests = new ArrayList<>();
        requests.add(
            new Request()
                .setInsertTable(
                    new InsertTableRequest()
                        .setEndOfSegmentLocation(
                            new EndOfSegmentLocation())
                        .setRows(3)
                        .setColumns(3)));
        
        BatchUpdateDocumentRequest body =
            new BatchUpdateDocumentRequest().setRequests(requests);
        BatchUpdateDocumentResponse response =
        service.documents().batchUpdate(DOCUMENT_ID, body).execute();

    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Docs service = new Docs.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Prints the title of the requested doc:
        // https://docs.google.com/document/d/195j9eDD3ccgjQRttHhJPymLJUCOUjs-jmwTrekvdjFE/edit
        Document response = service.documents().get(DOCUMENT_ID).execute();
        String title = response.getTitle();

        System.out.printf("1.- The title of the doc is: %s\n", title);

      createDoc(service);
      merge(service);
      String []texts = {"Mensaje 1", "Mensaje 2", "Mensaje 3"};
     //-- insertText( service, texts);
        list(service);


    }
}