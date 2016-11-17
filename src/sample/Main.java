package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Vector;

public class Main extends Application {
    private String pathWithoutUser = "C:\\Users\\%username%\\AppData\\LocalLow\\Ludeon Studios\\RimWorld";

    private ListView<String> listOfSavesFromTheRimWorldSaveFolder = new ListView<>();
    private ListView<String> listOfSavesFromTheManagerFolder = new ListView<>();

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("RimWorld Save Manager");

        GridPane folderManagement = folderManagementGrid(primaryStage);
        Pane saveManagement = saveManagementGrid(primaryStage);

        GridPane containerPane = new GridPane();
        containerPane.setAlignment(Pos.TOP_CENTER);
        containerPane.setHgap(10);
        containerPane.setVgap(10);
        containerPane.setPadding(new Insets(25,25,25,25));
        containerPane.setGridLinesVisible(true);

        containerPane.add(folderManagement,0,0);
        containerPane.add(saveManagement,0,1);

        //Managed Saves.
        primaryStage.setScene(new Scene(containerPane));

        primaryStage.show();
    }

    /***
     * Create the save folder where the project/jar is being executed.
     * Makes the application more portable.
     *
     * Returns false if the directory was created, or if the program ran into a security exception.
     * Returns true if the directory already existed (means there might be files there.)
     */
    private File SetupSaveManagerFolder() {
        File saveFolder = new File(GetCurrentExecutionPath() + "rwSaves");

        try {
            if(saveFolder.mkdir()) {
                System.out.println("Created the folder at: " + saveFolder.getPath());
            }
            else {
                System.out.println("Folder already exists at: " + saveFolder.getPath());
            }
        }
        catch(SecurityException e) {
            System.out.println("Try running this jar file as administrator.");
        }
        finally {
            return saveFolder;
        }
    }

    /**
     * I hate doing this, but I hate the clutter of the start method even more.
     * @param primaryStage
     * @return
     */
    private GridPane folderManagementGrid(Stage primaryStage) {

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25,25,25,25));

        Label saveFileLocationLabel = new Label("RimWorld Save Folder: ");
        saveFileLocationLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 12));

        Label managedSaveLabel = new Label("RWSM Folder: ");
        managedSaveLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 12));

        TextField saveFileLocationTxt = new TextField();
        saveFileLocationTxt.setText(PathToWindowsFolder());
        saveFileLocationTxt.setPrefWidth(800);

        TextField rwsmLocationTxt = new TextField();
        rwsmLocationTxt.setText(GetCurrentExecutionPath().toString());

        Button saveLocationDir = new UpdateListButton(listOfSavesFromTheRimWorldSaveFolder, saveFileLocationTxt, primaryStage);
        saveLocationDir.setText("...");
        saveLocationDir.setTooltip(new Tooltip("Open the directory picker to find the save folder for RimWorld"));

        Button managedSaveLocationDirPicker = new UpdateListButton(listOfSavesFromTheManagerFolder, rwsmLocationTxt, primaryStage);
        managedSaveLocationDirPicker.setText("...");
        managedSaveLocationDirPicker.setTooltip(new Tooltip("Open the directory picker to find the location of your managed saves."));

        //Add folder picker logic to these.
//        saveLocationDir.setOnAction(new LocationSelectorEventHandler(primaryStage, saveFileLocationTxt));
//        managedSaveLocationDirPicker.setOnAction(new LocationSelectorEventHandler(primaryStage, rwsmLocationTxt));

        File managedSaveFolder = SetupSaveManagerFolder();
        List<File> gameSaves = WalkManagedSaveFolder(managedSaveFolder);

        ListView managedSaves = new ListView();
        ObservableList saves = FXCollections.observableArrayList();

        for( File save : gameSaves ) {
            saves.add(save.getName());
        }

        managedSaves.setItems(saves);

        //RimWorld Save Stuff.
        grid.add(saveFileLocationLabel,0,0, 1, 1);
        grid.add(saveFileLocationTxt, 1,0, 4, 1);
        grid.add(saveLocationDir,5,0,1,1);

        //RimWorld Save Manager Stuff.
        grid.add(managedSaveLabel,0,1);
        grid.add(rwsmLocationTxt, 1, 1, 4, 1);
        grid.add(managedSaveLocationDirPicker,5,1,1,1);

        return grid;
    }

    private GridPane saveManagementGrid(Stage primaryStage) {
        GridPane saveManagement = new GridPane();
        saveManagement.setPadding(new Insets(25,25,25,25));
        saveManagement.setAlignment(Pos.CENTER);
        Button moveSaveGameToManagedFolder = new Button("->");
        moveSaveGameToManagedFolder.setAlignment(Pos.CENTER);

        Button moveSavedGameToSaveFolder = new Button("<-");
        moveSavedGameToSaveFolder.setAlignment(Pos.TOP_CENTER);

        double listWidth = 300;

        ColumnConstraints listColumn= new ColumnConstraints();
        listColumn.setPercentWidth(47.5);
        ColumnConstraints arrowColumn = new ColumnConstraints();
        arrowColumn.setPercentWidth(10);

        saveManagement.getColumnConstraints().addAll(listColumn, arrowColumn, listColumn);
        listOfSavesFromTheManagerFolder.setMinWidth(listWidth);
        listOfSavesFromTheRimWorldSaveFolder.setMinWidth(listWidth);

        GridPane buttonLayoutPane = new GridPane();
        buttonLayoutPane.setAlignment(Pos.CENTER);

        buttonLayoutPane.add(moveSaveGameToManagedFolder, 0,0);
        buttonLayoutPane.add(moveSavedGameToSaveFolder, 0, 2);
        saveManagement.add(buttonLayoutPane, 1,0, 1,3);
        saveManagement.add(listOfSavesFromTheRimWorldSaveFolder, 0,0, 1, 3);
        saveManagement.add(listOfSavesFromTheManagerFolder, 2 ,0, 1, 3);

        return saveManagement;
    }

    /**
     * Creates a list of files with full path.
     *
     * @param managedSaveFolder
     * @return Collection of files that are going to be displayed and are currently stored in the manaaged save folder.
     */
    private List<File> WalkManagedSaveFolder(File managedSaveFolder) {
        try {
            List<File> managedSaves = new Vector();


            Files.walk(managedSaveFolder.toPath())
                    .filter(Files::isRegularFile)
                    .forEach(save -> managedSaves.add(save.toFile()));

            //managedSaves.get(0).getName();
            return managedSaves;
        }
        catch (IOException e) {
            System.out.println("ManagedSaveFolder disappeared.");
        }
        return null;
    }

    /***
     * Single point of failure for getting the execution path.
     * @return
     */
    private File GetCurrentExecutionPath() {
        return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    }

    private String PathToWindowsFolder() {
        String username = System.getProperty("user.name");
        return pathWithoutUser.replace("%username%", username);
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Class that represents the logic required to setup and display a directory chooser.
     * It is written this way to abstract the logic the two components require (the two textboxes and button groups).
     */
    public class LocationSelectorEventHandler implements EventHandler<ActionEvent> {

        private TextField elementToBeUpdated;
        private String pathToFolderAccessing;
        private Stage rootParent;
        public LocationSelectorEventHandler(Stage rootParent, TextField fieldToBeUpdated) {
            elementToBeUpdated = fieldToBeUpdated;
            pathToFolderAccessing = fieldToBeUpdated.getText();
            this.rootParent = rootParent;
        }

        @Override
        public void handle(ActionEvent event) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            try {
                //Create directory:
                File pathToRWSaveFolder = new File(pathToFolderAccessing);
                if(pathToRWSaveFolder.mkdirs()) {
                    System.out.println("Folder was not present. It has been created.");
                }

                directoryChooser.setInitialDirectory(pathToRWSaveFolder);
                File selectedDirectory = directoryChooser.showDialog(rootParent);
                elementToBeUpdated.setText(selectedDirectory.getPath());
            }
            catch(SecurityException e) {
                System.out.println("Try running this jar file as administrator.");
            }
            catch(IllegalArgumentException e) {
                System.out.println("Folder does not exist or could not be created.");
            }
        }
    }

}

