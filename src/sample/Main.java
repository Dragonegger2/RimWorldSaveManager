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
        saveFileLocationLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 15));
        grid.add(saveFileLocationLabel,0,0);

        TextField saveFileLocation = new TextField();
        saveFileLocation.setText(pathToWindowsFolder );
        grid.add(saveFileLocation, 1,0);


        Label managedSaveLabel = new Label("RWSM Folder: ");
        saveFileLocationLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 15));
        grid.add(managedSaveLabel,0,1);

        TextField rwsmLocation = new TextField();
        rwsmLocation.setText(pathToWindowsFolder );
        grid.add(rwsmLocation, 1,1);

        DirectoryChooser saveFileLocationDirectoryChooser = new DirectoryChooser();
        saveFileLocationDirectoryChooser.setInitialDirectory(new File(pathToWindowsFolder));

        Button saveLocationDir = new Button("...");
        saveLocationDir.setTooltip(new Tooltip("Open the directory picker to find the save folder for RimWorld"));

        saveLocationDir.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Opening the folder directory.");
                try {
                    File selectedDirectory = saveFileLocationDirectoryChooser.showDialog(primaryStage);
                    System.out.println(selectedDirectory.getPath());
                    saveFileLocation.setText(selectedDirectory.getPath());
                }
                catch(IllegalArgumentException e) {
                    System.out.println("Directory does not exist.");
                    saveFileLocationDirectoryChooser.setInitialDirectory(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()));
                    File selectedDirectory = saveFileLocationDirectoryChooser.showDialog(primaryStage);
                    System.out.println(selectedDirectory.getPath());
                    saveFileLocation.setText(selectedDirectory.getPath());

                    //TODO: Set the status in the bottom with appropriate warning.
                    //TODO: Look into creating a StatusBar to reflect what is happening with the program.
                }
            }
        });

        grid.add(saveLocationDir, 2, 0);

        primaryStage.setScene(new Scene(grid, 450, 275));

        File managedSaveFolder = SetupSaveManagerFolder();
        List<File> gameSaves = WalkManagedSaveFolder(managedSaveFolder);

        ListView managedSaves = new ListView();
        ObservableList saves = FXCollections.observableArrayList();

        for( File save : gameSaves ) {
            saves.add(save.getName());
        }

        managedSaves.setItems(saves);
        grid.add(managedSaves, 0, 2, 3, 2);
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
        //Get the location the JAR/application was initialized.
        URL locationOfSaveFolder = Main.class.getProtectionDomain().getCodeSource().getLocation();
        File saveFolder = new File(locationOfSaveFolder.getPath() + "rwSaves");

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

    public static void main(String[] args) {
        launch(args);
    }
}
