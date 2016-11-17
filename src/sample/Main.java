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
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Vector;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("RimWorld Save Manager");

        String username = System.getProperty("user.name");
        String path = "C:\\Users\\%username%\\AppData\\LocalLow\\Ludeon Studios\\RimWorld";
        String pathToWindowsFolder = path.replace("%username%", username);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25,25,25,25));
        grid.setGridLinesVisible(true);

        Label saveFileLocationLabel = new Label("RimWorld Save Folder: ");
        saveFileLocationLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 12));

        Label managedSaveLabel = new Label("RWSM Folder: ");
        managedSaveLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 12));

        TextField saveFileLocationTxt = new TextField();
        saveFileLocationTxt.setText(pathToWindowsFolder );
        saveFileLocationTxt.setPrefWidth(800);
        TextField rwsmLocationTxt = new TextField();
        rwsmLocationTxt.setText(GetCurrentExecutionPath().toString());

        Button saveLocationDir = new Button("...");
        saveLocationDir.setTooltip(new Tooltip("Open the directory picker to find the save folder for RimWorld"));

        Button managedSaveLocationDirPicker = new Button("...");
        managedSaveLocationDirPicker.setTooltip(new Tooltip("Open the directory picker to find the location of your managed saves."));

        saveLocationDir.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Opening the folder directory.");
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(new File(pathToWindowsFolder));

                try {


                    File selectedDirectory = directoryChooser.showDialog(primaryStage);
                    saveFileLocationTxt.setText(selectedDirectory.getPath());
                }
                catch(IllegalArgumentException e) {
                    System.out.println("Directory does not exist.");
                    directoryChooser.setInitialDirectory(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()));
                    File selectedDirectory = directoryChooser.showDialog(primaryStage);
                    saveFileLocationTxt.setText(selectedDirectory.getPath());

                    //TODO: Set the status in the bottom with appropriate warning.
                    //TODO: Look into creating a StatusBar to reflect what is happening with the program.
                }
            }
        });
        managedSaveLocationDirPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Opening the folder directory.");

                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(new File(GetCurrentExecutionPath().toString()));
                directoryChooser.setTitle("RimWorld Save Manager Save Location");

                try {
                    File selectedDirectory = directoryChooser.showDialog(primaryStage);
                    rwsmLocationTxt.setText(selectedDirectory.getPath());
                }
                catch(IllegalArgumentException e) {
                    System.out.println("Directory does not exist.");
                    directoryChooser.setInitialDirectory(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()));
                    File selectedDirectory = directoryChooser.showDialog(primaryStage);
                    rwsmLocationTxt.setText(selectedDirectory.getPath());

                    //TODO: Set the status in the bottom with appropriate warning.
                    //TODO: Look into creating a StatusBar to reflect what is happening with the program.
                }
            }
        });

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

        //Managed Saves.
        //grid.add(managedSaves, 0, 2, 3, 2);
        primaryStage.setScene(new Scene(grid, 800, 480));

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

    public static void main(String[] args) {
        launch(args);
    }
}
